package org.uniclogs.yamcs.tctm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import org.yamcs.YConfiguration;
import org.yamcs.logging.Log;
import org.yamcs.tctm.AbstractLink;
import org.yamcs.utils.StringConverter;
import org.yamcs.yarch.Stream;
import org.yamcs.yarch.StreamSubscriber;
import org.yamcs.yarch.Tuple;
import org.yamcs.yarch.YarchDatabase;
import org.yamcs.yarch.YarchDatabaseInstance;

/**
 * Subscribes to the cfdp_out stream, wraps each PDU in a USLP VC=1 TC frame
 * (HMAC-SHA3-256 + CRC16 FECF) and sends it via UDP to the C3.
 *
 * Bypasses Yamcs's stock TC command plumbing because cfdp_out tuples are not
 * PreparedCommand-shaped (see CfdpService writing (gentime, entityId, seqNum, pdu)).
 */
public class CfdpVc1UdpTcLink extends AbstractLink {
    private static final Log log = new Log(CfdpVc1UdpTcLink.class);

    private byte[] hmacKey;
    private String host;
    private int port;
    private int scid;
    private int vcid;
    private String streamName;
    private final AtomicInteger seqNum = new AtomicInteger(1);

    private DatagramSocket socket;
    private InetAddress dest;
    private Stream cfdpOutStream;
    private StreamSubscriber subscriber;

    @Override
    public void init(String instance, String name, YConfiguration config) {
        super.init(instance, name, config);
        this.hmacKey = StringConverter.hexStringToArray(config.getString("hmacKeyHex"));
        this.host = config.getString("host", "localhost");
        this.port = config.getInt("port", 10025);
        this.scid = config.getInt("scid", CfdpVc1FrameCodec.DEFAULT_SCID);
        this.vcid = config.getInt("vcid", CfdpVc1FrameCodec.DEFAULT_VCID);
        this.streamName = config.getString("stream", "cfdp_out");
        this.seqNum.set(config.getInt("seqNumStart", 1));
    }

    @Override
    protected void doStart() {
        try {
            socket = new DatagramSocket();
            dest = InetAddress.getByName(host);
        } catch (SocketException | UnknownHostException e) {
            notifyFailed(e);
            return;
        }

        YarchDatabaseInstance ydb = YarchDatabase.getInstance(yamcsInstance);
        cfdpOutStream = ydb.getStream(streamName);
        if (cfdpOutStream == null) {
            notifyFailed(new IllegalStateException("stream '" + streamName + "' not found"));
            return;
        }
        subscriber = new StreamSubscriber() {
            @Override public void onTuple(Stream s, Tuple tuple) { sendPdu(tuple); }
            @Override public void streamClosed(Stream s) {}
        };
        cfdpOutStream.addSubscriber(subscriber);
        log.info("CfdpVc1UdpTcLink subscribed to '{}' -> {}:{}", streamName, host, port);
        notifyStarted();
    }

    private void sendPdu(Tuple tuple) {
        if (isDisabled()) {
            return;
        }
        Object col = tuple.getColumn("pdu");
        if (!(col instanceof byte[])) {
            log.warn("cfdp_out tuple missing pdu bytes");
            return;
        }
        byte[] pdu = (byte[]) col;
        int sn = seqNum.getAndIncrement();
        byte[] frame = CfdpVc1FrameCodec.packUslpFrame(pdu, hmacKey, sn, scid, vcid);
        try {
            socket.send(new DatagramPacket(frame, frame.length, dest, port));
            dataOut(1, frame.length);
            log.debug("VC1 PDU out ({} B): {}",
                    pdu.length, StringConverter.arrayToHexString(pdu, true));
        } catch (IOException e) {
            log.warn("Failed to send CFDP PDU: {}", e.getMessage());
        }
    }

    @Override
    protected void doStop() {
        if (cfdpOutStream != null && subscriber != null) {
            cfdpOutStream.removeSubscriber(subscriber);
        }
        if (socket != null) {
            socket.close();
        }
        notifyStopped();
    }

    @Override
    protected Status connectionStatus() {
        return socket != null && !socket.isClosed() ? Status.OK : Status.UNAVAIL;
    }
}
