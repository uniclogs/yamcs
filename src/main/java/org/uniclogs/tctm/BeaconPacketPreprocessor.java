package org.uniclogs.tctm;

import java.util.Date;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.logging.Log;
import org.yamcs.tctm.AbstractPacketPreprocessor;

public class BeaconPacketPreprocessor extends AbstractPacketPreprocessor {
    static final Log log = new Log(BeaconPacketPreprocessor.class);
    private int expectedPacketSize = 0;

    // Constructor used when this preprocessor is used without YAML configuration
    public BeaconPacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    // Constructor used when this preprocessor is used with YAML configuration
    // (packetPreprocessorClassArgs)
    public BeaconPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
        this.expectedPacketSize = config.getInt("packetSize");
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        log.info("Got packet of " + tmPacket.length() +  "b!");
        // Reject the packet if it's not exactly the expected size
        if(tmPacket.getPacket().length != expectedPacketSize) {
            String msg = String.format("Rejecting packet with only %d bytes! (Expected %d bytes)", tmPacket.getPacket().length, expectedPacketSize);
            log.error(msg);
            this.eventProducer.sendWarning("PACKET_INVALID_SIZE", msg);
            tmPacket.setDoNotArchive();
            tmPacket.setInvalid();
            return tmPacket;
        }

        tmPacket.setGenerationTime(new Date().getTime());
        return tmPacket;
    }
}
