package org.oresat.uniclogs;

import org.yamcs.TmPacket;


public class EDLPacket extends Packet {
    private static final Integer SEQ_NUM_OFFSET = 7;

    public EDLPacket(byte[] packet, Integer seqNum, byte[] hmacSecret) {
        //sequence number offset of 7
        super(packet, seqNum, SEQ_NUM_OFFSET);

        // set sequence number in packet
        this.encodeSeqNum();
        

        // set frame length in packet: C = (Total Number of Octets in the Transfer Frame) − 1
        // CRC adds 2, HMAC adds 32 -> (size + (34 - 1))
        this.encodeFrameLength(33, 4);
        this.addHmac(hmacSecret);
        
        // Add CRC data to packet
        this.encodeCrc();

    }

    public EDLPacket(TmPacket tmPacket) {
        super(tmPacket.getPacket(), getSequenceNumber(tmPacket.getPacket(), SEQ_NUM_OFFSET), SEQ_NUM_OFFSET);
    }
}
