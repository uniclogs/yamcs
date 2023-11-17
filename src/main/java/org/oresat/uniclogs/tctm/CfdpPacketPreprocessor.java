package org.oresat.uniclogs.tctm;

import java.nio.ByteBuffer;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.cfdp.pdu.CfdpPacket;
import org.yamcs.logging.Log;
import org.yamcs.tctm.AbstractPacketPreprocessor;

public class CfdpPacketPreprocessor extends AbstractPacketPreprocessor {
    public static Log log = new Log(CfdpPacketPreprocessor.class);

    public CfdpPacketPreprocessor(String yamcsInstance) {
        super(yamcsInstance, YConfiguration.emptyConfig());
    }

    public CfdpPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        ByteBuffer packetBb = ByteBuffer.wrap(tmPacket.getPacket());
        CfdpPacket packet = CfdpPacket.getCFDPPacket(packetBb);

        log.info("CFDP Packet:\n\tHeader: " + packet.getHeader() + "\n\tTransaction ID: " + packet.getTransactionId());

        return super.process(tmPacket);
    }
}
