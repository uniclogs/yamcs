package org.oresat.uniclogs;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.TimeEncoding;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.CRC32;

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
            tmPacket.setInvalid();
            this.eventProducer.sendWarning("PACKET_CORRUPT", "Beacon Packet Corrupted");
        }
        tmPacket.setSequenceCount(packet.getSeqNum());
        tmPacket.setGenerationTime(new Date().getTime());
        return tmPacket;
    }
}
