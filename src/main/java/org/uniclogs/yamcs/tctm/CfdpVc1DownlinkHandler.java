package org.uniclogs.yamcs.tctm;

import org.yamcs.YConfiguration;
import org.yamcs.logging.Log;
import org.yamcs.tctm.AbstractLink;
import org.yamcs.tctm.ccsds.DownlinkTransferFrame;
import org.yamcs.tctm.ccsds.VcDownlinkHandler;
import org.yamcs.utils.StringConverter;
import org.yamcs.utils.TimeEncoding;
import org.yamcs.yarch.DataType;
import org.yamcs.yarch.Stream;
import org.yamcs.yarch.Tuple;
import org.yamcs.yarch.TupleDefinition;
import org.yamcs.yarch.YarchDatabase;
import org.yamcs.yarch.YarchDatabaseInstance;

/**
 * VCA handler for USLP VC=1 TM: strips HMAC-SHA3-256 off of the TFDZ and emits
 * the raw CFDP PDU as a tuple (gentime, entityId, seqNum, pdu) on the cfdp_in
 * stream, matching the input required for CfdpService.inStream.
 */
public class CfdpVc1DownlinkHandler extends AbstractLink implements VcDownlinkHandler {
    private static final Log log = new Log(CfdpVc1DownlinkHandler.class);

    private byte[] hmacKey;
    private boolean verifyHmac;
    private boolean stripFecf;
    private String streamName;
    private Stream cfdpInStream;

    private static final TupleDefinition TDEF = new TupleDefinition();
    static {
        TDEF.addColumn("gentime", DataType.TIMESTAMP);
        TDEF.addColumn("entityId", DataType.LONG);
        TDEF.addColumn("seqNum", DataType.INT);
        TDEF.addColumn("pdu", DataType.BINARY);
    }

    @Override
    public void init(String instance, String name, YConfiguration config) {
        super.init(instance, name, config);
        YConfiguration args = config.containsKey("vcaHandlerArgs")
                ? config.getConfig("vcaHandlerArgs")
                : config;
        this.hmacKey = StringConverter.hexStringToArray(args.getString("hmacKeyHex"));
        this.verifyHmac = args.getBoolean("verifyHmac", false);
        this.stripFecf = args.getBoolean("stripFecf", true);
        this.streamName = args.getString("cfdpStream", "cfdp_in");
    }

    private Stream stream() {
        if (cfdpInStream == null) {
            YarchDatabaseInstance ydb = YarchDatabase.getInstance(yamcsInstance);
            cfdpInStream = ydb.getStream(streamName);
            if (cfdpInStream == null) {
                log.warn("CFDP stream '{}' not found in instance '{}'", streamName, yamcsInstance);
            }
        }
        return cfdpInStream;
    }

    @Override
    public void handle(DownlinkTransferFrame frame) {
        if (isDisabled()) {
            return;
        }
        // Skip 1-byte TFDF header (VpNoSegmentation construction rules byte).
        int tfdzStart = frame.getDataStart() + 1;
        int tfdzEnd = frame.getDataEnd() - (stripFecf ? CfdpVc1FrameCodec.FECF_LEN : 0);
        int tfdzLen = tfdzEnd - tfdzStart;
        if (tfdzLen < CfdpVc1FrameCodec.HMAC_LEN) {
            log.warn("VC1 TFDZ too short ({}), dropping", tfdzLen);
            return;
        }
        byte[] tfdz = new byte[tfdzLen];
        System.arraycopy(frame.getData(), tfdzStart, tfdz, 0, tfdzLen);

        byte[] pdu;
        try {
            pdu = CfdpVc1FrameCodec.unwrap(tfdz, hmacKey, verifyHmac);
        } catch (SecurityException e) {
            log.warn("VC1 HMAC verify failed, dropping PDU: {}", e.getMessage());
            return;
        }
        log.debug("VC1 PDU in ({} bytes): {}",
                pdu.length, StringConverter.arrayToHexString(pdu, true));

        Stream s = stream();
        if (s == null) return;
        long now = TimeEncoding.getWallclockTime();
        int seqNum = (int) frame.getVcFrameSeq();
        Tuple t = new Tuple(TDEF, new Object[] { now, 0L, seqNum, pdu });
        s.emitTuple(t);
    }

    @Override
    protected Status connectionStatus() {
        return Status.OK;
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }
}
