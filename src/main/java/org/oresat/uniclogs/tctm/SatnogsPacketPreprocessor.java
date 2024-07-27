package org.oresat.uniclogs.tctm;

import java.util.Date;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;

public class SatnogsPacketPreprocessor extends AbstractPacketPreprocessor {

    public SatnogsPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
    }

    public SatnogsPacketPreprocessor(String yamcsInstance) {
        super(yamcsInstance, YConfiguration.emptyConfig());
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        SatnogsPacket packet = new SatnogsPacket(tmPacket);
        tmPacket.setSequenceCount(packet.getSeqNum());
        if (!packet.validCrc()) {
            tmPacket.setInvalid();
            this.eventProducer.sendWarning("PACKET_CORRUPT", "Satnogs Response Packet Corrupted");
        }
        tmPacket.setGenerationTime(new Date().getTime());
        tmPacket.setLocalGenTimeFlag();
        return tmPacket;
    }
}