package org.oresat.uniclogs;

import java.util.Arrays;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.TmPacket;
import org.yamcs.xtce.util.HexUtils;


public class EDLPacket extends Packet {
    private static final Integer SEQ_NUM_OFFSET = 7;

    public EDLPacket(byte[] packet, Integer seqNum, byte[] hmacSecret) {
        //sequence number offset of 7
        super(packet, packet.length+34, seqNum, SEQ_NUM_OFFSET);

        // set sequence number in packet
        this.encodeSeqNum();
        

        // set frame length in packet: C = (Total Number of Octets in the Transfer Frame) − 1
        // CRC adds 2, HMAC adds 32 -> (size + (34 - 1))
        this.encodeFrameLength(33, 4);
        this.addHmac(hmacSecret);
        
        // Add CRC data to packet
        this.encodeCrc();

    }

    private void addHmac(byte[] hmacSecret) {
        byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, hmacSecret).hmac(this.data.array());
        log.info(String.format("Hmac bytes: %s", Arrays.toString(hmac)));
        this.data.allocateDirect(32);
        this.data.put(hmac);
        log.info(String.format("HMAC_SHA_256 (%s) added to packet (seqNum: %d).", HexUtils.hex(hmac), this.sequenceNumber));
    }

    protected void encodeCrc() {
        log.info("Enc Crc Packet Data: " + HexUtils.hex(this.data.array()));
        int crc = this.calcCrc(this.data.array());
        log.info(String.format("CRC_16 (%d) added to packet (seqNum: %d).", crc, this.sequenceNumber));
        this.data.putShort((short) crc);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    public EDLPacket(TmPacket tmPacket) {
        super(tmPacket.getPacket(), tmPacket.getPacket().length, getSequenceNumber(tmPacket.getPacket(), SEQ_NUM_OFFSET), SEQ_NUM_OFFSET);
    }
}
