package org.oresat.uniclogs.links;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.cfdp.pdu.CfdpHeader;
import org.yamcs.cfdp.pdu.CfdpPacket;
import org.yamcs.tctm.Link;
import org.yamcs.tctm.ccsds.TcTransferFrame;
import org.yamcs.utils.StringConverter;
import org.yamcs.tctm.ccsds.AbstractTcFrameLink;

public class UslpUdpFrameLink extends AbstractTcFrameLink implements Runnable {
    String host;
    int port;
    int spacecraftId;
    DatagramSocket socket;
    InetAddress address;
    Thread thread;
    RateLimiter rateLimiter;

    public UslpUdpFrameLink() {
    }

    public void init(String yamcsInstance, String name, YConfiguration config) {
        super.init(yamcsInstance, name, config);
        this.log.info("Initializing UslpUdpFrameLink");
        this.log.info("Config: " + config);
        this.host = config.getString("host");
        this.port = config.getInt("port");
        this.spacecraftId = config.getInt("spacecraftId");

        try {
            this.address = InetAddress.getByName(this.host);
        } catch (UnknownHostException var5) {
            throw new ConfigurationException("Cannot resolve host '" + this.host + "'", var5);
        }

        if (config.containsKey("frameMaxRate")) {
            this.rateLimiter = RateLimiter.create(config.getDouble("frameMaxRate"), 1L, TimeUnit.SECONDS);
        }

    }

    private void overwriteWithUslpHeader(ByteBuffer buffer, Integer frameLength) {
        // Initialize values
        int tfvn = 12; // '1100' binary is 12 in decimal
        int scid = this.spacecraftId;
        int sourceOrDestinationId = 0;
        int vcid = 1;
        int mapId = 0;
        int endOfFramePrimaryHeaderFlag = 0;
        int bypassSequenceControlFlag = 0;
        int protocolControlCommandFlag = 0;
        int reserveSpares = 0;
        int ocfFlag = 0;
        int vcfCountLength = 1  & 0x7; // keep only 3 bits

        // Reset the buffer's position to the beginning
        buffer.position(0);

        // Set the buffer's order
        //buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Byte 1: TFVN (4 bits) and the most significant 4 bits of SCID
        buffer.put((byte) ((tfvn << 4) | ((scid >> 12) & 0x0F)));

        // Byte 2: The next 8 bits of SCID
        buffer.put((byte) ((scid >> 4) & 0xFF));

        // Byte 3: The last 4 bits of SCID, the 1 bit for source or destination ID, and the most significant 3 bits of VCID
        buffer.put((byte) (((scid & 0x0F) << 4) | (sourceOrDestinationId << 3) | ((vcid >> 3) & 0x07)));

        // Byte 4: The last 3 bits of VCID, 4 bits of MAP ID, and 1 bit for end of frame
        buffer.put((byte) (((vcid & 0x07) << 5) | (mapId << 1) | endOfFramePrimaryHeaderFlag));

        // 5th and 6th bytes: frameLength as a short (16 bits)
        buffer.putShort((short) frameLength.intValue());

        // 7th byte: Various flags and length
        buffer.put((byte) ((bypassSequenceControlFlag << 7)
                | (protocolControlCommandFlag << 6)
                | (reserveSpares << 4)  // reserveSpares is assumed to be 0 for space
                | (ocfFlag << 3)
                | vcfCountLength));
    }

    public void run() {
        while(this.isRunningAndEnabled()) {
            this.log.info("Waiting for frame");
            if (this.rateLimiter != null) {
                this.rateLimiter.acquire();
            }

            TcTransferFrame tf = this.multiplexer.getFrame();
            this.log.info("Got frame");
            if (tf != null) {
                byte[] data = tf.getData();
                // Create a new array that's 2 bytes larger than the original.
                byte[] newData = new byte[data.length + 2];

                newData[0] = 0x00;
                newData[1] = 0x00;

                System.arraycopy(data, 0, newData, 2, data.length);

                ByteBuffer bb = ByteBuffer.wrap(newData);

                this.overwriteWithUslpHeader(bb, newData.length);

                this.log.info("Outgoing frame data: {}", new Object[]{StringConverter.arrayToHexString(data, true)});

                DatagramPacket dtg = new DatagramPacket(newData, newData.length, this.address, this.port);

                try {
                    this.socket.send(dtg);
                } catch (IOException var5) {
                    this.log.warn("Error sending datagram", var5);
                    this.notifyFailed(var5);
                    return;
                }

                if (tf.isBypass()) {
                    this.ackBypassFrame(tf);
                }

                ++this.frameCount;
            }
        }

    }

    protected void doDisable() throws Exception {
        if (this.thread != null) {
            this.thread.interrupt();
        }

        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }

    }

    protected void doEnable() throws Exception {
        this.socket = new DatagramSocket();
        this.thread = new Thread(this);
        this.thread.start();
    }

    protected void doStart() {
        try {
            this.doEnable();
            this.notifyStarted();
        } catch (Exception var2) {
            this.log.warn("Exception starting link", var2);
            this.notifyFailed(var2);
        }

    }

    protected void doStop() {
        try {
            this.doDisable();
            this.multiplexer.quit();
            this.notifyStopped();
        } catch (Exception var2) {
            this.log.warn("Exception stopping link", var2);
            this.notifyFailed(var2);
        }

    }

    protected Link.Status connectionStatus() {
        return this.socket == null ? Status.DISABLED : Status.OK;
    }
}
