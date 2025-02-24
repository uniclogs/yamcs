package org.uniclogs.yamcs.tctm;

import org.yamcs.TmPacket;

public class BeaconPacket extends Packet {
    private static final Integer SEQ_NUM_OFFSET = 26;

    public BeaconPacket(TmPacket tmPacket) {
        super(tmPacket.getPacket(), tmPacket.getPacket().length,
                getSequenceNumber(tmPacket.getPacket(), SEQ_NUM_OFFSET), SEQ_NUM_OFFSET);

        // byte[] pkt = tmPacket.getPacket();
        // log.info(HexUtils.hex(Arrays.copyOfRange(pkt, 0, pkt.length - 4)));
        // log.info(HexUtils.hex(Arrays.copyOfRange(pkt, pkt.length - 4, pkt.length)));
        // log.info(HexUtils.hex(Arrays.copyOfRange(pkt, 0, pkt.length)));
    }

    @Override
    public boolean validCrc() {
        return this.crc32(16, this.data.array().length - 20);
    }
}