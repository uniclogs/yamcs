package org.oresat.uniclogs.tctm;

import java.util.Date;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;

public class EdlPacketPreprocessor extends AbstractPacketPreprocessor {

    public EdlPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
    }

    public EdlPacketPreprocessor(String yamcsInstance) {
        super(yamcsInstance, YConfiguration.emptyConfig());
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        EDLPacket packet = new EDLPacket(tmPacket);
        tmPacket.setSequenceCount(packet.getSeqNum());
        if (!packet.validCrc()) {
            tmPacket.setInvalid();
            this.eventProducer.sendWarning("PACKET_CORRUPT", "EDL Response Packet Corrupted");
        }
        tmPacket.setGenerationTime(new Date().getTime());
        tmPacket.setLocalGenTimeFlag();
        return tmPacket;
    }
}