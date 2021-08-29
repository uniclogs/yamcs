package org.oresat.uniclogs;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.TimeEncoding;

public class Ax25PacketPreprocessor extends AbstractPacketPreprocessor {

    // where from the packet to read the 8 bytes timestamp
    final int timestampOffset = -1;

    // where from the packet to read the 4 bytes sequence count
    final int seqCountOffset = 0;

    // Ax.25 Header callsigns fields
    String destCallsign = "";
    String srcCallsign = "";

    // Constructor used when this preprocessor is used without YAML configuration
    public Ax25PacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    // Constructor used when this preprocessor is used with YAML configuration
    // (packetPreprocessorClassArgs)
    public Ax25PacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
        //timestampOffset = config.getInt("timestampOffset");
        //seqCountOffset = config.getInt("seqCountOffset");
        if (!config.containsKey(CONFIG_KEY_TIME_ENCODING)) {
            this.timeEpoch = TimeEpochs.UNIX;
        }

        this.destCallsign = config.getString("destCallsign", "");
        this.srcCallsign = config.getString("srcCallsign", "");

        // All callsigns must be 6 chars long, use spaces to fill gaps
        // If the callsigns are empty string, they will not be checked
        if (!this.destCallsign.equals("") && this.destCallsign.length() < 6) {
            this.destCallsign +=  " ".repeat(6 - this.destCallsign.length());
        }
        if (!this.destCallsign.equals("") && this.srcCallsign.length() < 6) {
            this.srcCallsign +=  " ".repeat(6 - this.srcCallsign.length());
        }
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        byte[] packet = tmPacket.getPacket();
        boolean corrupted = false;
        long packetCheckword = 0;
        long computedCheckword = 0;

        if (packet.length < 16) { // Expect at least the length of APRS header
            eventProducer.sendWarning("SHORT_PACKET",
                    "Short packet received, length: " + packet.length + "; minimum required length is 16 bytes.");
            return null; // drop packet
        }

        try {
            int n = packet.length;

            // check Ax.25 header
            byte[] header = Arrays.copyOfRange(packet, 0, 16);
            BigInteger bigInt = new BigInteger(header).shiftRight(1);
            header = bigInt.toByteArray();
            header[0] = (byte)((int)header[0] & 0x7f); // fix the leading bit to be 0 after shifted
            String destCallsign = new String(Arrays.copyOfRange(header, 0, 6));
            String srcCallsign = new String(Arrays.copyOfRange(header, 7, 13));

            if ((!this.destCallsign.equals("") && !destCallsign.equals(this.destCallsign))|| 
                (!this.srcCallsign.equals("") && !srcCallsign.equals(this.srcCallsign))) {
                return null; // drop packet
            }

            // computed crc32
            byte[] packetData = Arrays.copyOfRange(packet, 16, n - 4);
            CRC32 crc32 = new CRC32();
            crc32.update(packetData);
            computedCheckword = crc32.getValue();

            // read crc32 from packet
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                packetCheckword = ((((long)packet[n - 1]) & 0xFF) << 24) +
                    ((((long)packet[n - 2]) & 0xFF) << 16) +
                    ((((long)packet[n - 3]) & 0xFF) << 8) +
                    ((((long)packet[n - 4]) & 0xFF));
            } else {
                packetCheckword = ((((long)packet[n - 4]) & 0xFF) << 24) + 
                    ((((long)packet[n - 3]) & 0xFF) << 16) +
                    ((((long)packet[n - 2]) & 0xFF) << 8) +
                    ((((long)packet[n - 1]) & 0xFF));
            }

            if (packetCheckword != computedCheckword) {
                eventProducer.sendWarning("CORRUPTED_PACKET",
                        "Corrupted packet received, computed checkword: " + computedCheckword
                                + "; packet checkword: " + packetCheckword);
                corrupted = true;
            }
        } catch (IllegalArgumentException e) {
            eventProducer.sendWarning("CORRUPTED_PACKET", "Error when computing checkword: " + e.getMessage());
            corrupted = true;
        }

        // get generated time
        long gentime;
        if (timestampOffset < 0) {
            gentime = TimeEncoding.getWallclockTime();
        } else {
            if (packet.length < timestampOffset + 8) {
                //eventProducer.sendWarning(ETYPE_CORRUPTED_PACKET, "Packet too short to extract timestamp");
                gentime = -1;
                corrupted = true;
            } else {
                long t = byteOrder == ByteOrder.BIG_ENDIAN ? ByteArrayUtils.decodeLong(packet, timestampOffset)
                        : ByteArrayUtils.decodeLongLE(packet, timestampOffset);
                gentime = shiftFromEpoch(t);
            }
        }

        tmPacket.setGenerationTime(gentime);
        tmPacket.setInvalid(corrupted);
        return tmPacket;
    }
}
