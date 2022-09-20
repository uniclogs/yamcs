package org.oresat.uniclogs;

import org.yamcs.TmPacket;

public class BeaconPacket extends Packet {
    private static final Integer SEQ_NUM_OFFSET = 26;

    public BeaconPacket(TmPacket tmPacket) {
        super(tmPacket.getPacket(), getSequenceNumber(tmPacket.getPacket(), SEQ_NUM_OFFSET), SEQ_NUM_OFFSET);
    }
}
