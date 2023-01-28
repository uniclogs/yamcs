package org.oresat.uniclogs.tctm;

import java.util.Date;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;

public class BeaconPacketPreprocessor extends AbstractPacketPreprocessor {

    // Constructor used when this preprocessor is used without YAML configuration
    public BeaconPacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    // Constructor used when this preprocessor is used with YAML configuration
    // (packetPreprocessorClassArgs)
    public BeaconPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        BeaconPacket packet = new BeaconPacket(tmPacket);
        if (!packet.validCrc()) {
            //tmPacket.setInvalid();
            this.eventProducer.sendWarning("PACKET_CORRUPT", "Beacon Packet Corrupted");
        }
        tmPacket.setSequenceCount(packet.getSeqNum());
        tmPacket.setGenerationTime(new Date().getTime());
        return tmPacket;
    }
}
