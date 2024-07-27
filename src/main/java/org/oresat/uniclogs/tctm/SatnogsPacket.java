package org.oresat.uniclogs.tctm;

import java.util.Arrays;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.TmPacket;
import org.yamcs.xtce.util.HexUtils;


public class SatnogsPacket extends Packet {
    private static final Integer SEQ_NUM_OFFSET = 7;

    public SatnogsPacket(byte[] packet, Integer seqNum, byte[] hmacSecret) {
        //sequence number offset of 7
        super(packet, packet.length+36, seqNum, SEQ_NUM_OFFSET);

        // set sequence number in packet
        this.encodeSeqNum();


        // set frame length in packet: C = (Total Number of Octets in the Transfer Frame) − 1
        // CRC adds 4, HMAC adds 32 -> (size + (36 - 1))
        this.encodeFrameLength(35, 4);
        this.addHmac(hmacSecret);

        // Add CRC data to packet
        this.encodeCrc();

    }

    private void addHmac(byte[] hmacSecret) {
        byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, hmacSecret).hmac(this.data.array());
        log.info(String.format("Hmac bytes: %s", Arrays.toString(hmac)));
        this.data.put(hmac);
        log.info(String.format("HMAC_SHA_256 (%s) added to packet (seqNum: %d).", HexUtils.hex(hmac), this.sequenceNumber));
    }

    protected void encodeCrc() {
        int crc = this.calcCrc(this.data.array());
        log.info(String.format("CRC_32 (%d) added to packet (seqNum: %d).", crc, this.sequenceNumber));
        this.data.putInt(crc);
    }

    public SatnogsPacket(TmPacket tmPacket) {
        super(tmPacket.getPacket(), tmPacket.getPacket().length, getSequenceNumber(tmPacket.getPacket(), SEQ_NUM_OFFSET), SEQ_NUM_OFFSET);
    }

    @Override
    boolean validCrc() {
        return crc32(0, this.data.array().length);
    }
}
