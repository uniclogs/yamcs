package org.uniclogs.yamcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.uniclogs.yamcs.tctm.BeaconPacket;
import org.yamcs.TmPacket;
import org.yamcs.xtce.util.HexUtils;

public class TestBeaconPacket {
    private BeaconPacket packet = null;
    private String raw_packet;

    @BeforeEach
    void setup() {
        this.raw_packet = "DEADBEEF7C9CA35A";
    }

    @AfterEach
    void teardown() {
        this.packet = null;
    }

    @Test
    void beacon_packet_raises_exception_on_null_input() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> {
                    this.packet = new BeaconPacket(null);
                });
    }

    @Disabled
    @Test
    void beacon_packet_validates_correct_crc() {
        
        TmPacket ingress_packet = new TmPacket(0, HexUtils.unhex(this.raw_packet));
        this.packet = new BeaconPacket(ingress_packet);
        Assertions.assertTrue(this.packet.validCrc());
    }
}
