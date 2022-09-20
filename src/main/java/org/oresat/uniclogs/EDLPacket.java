package org.oresat.uniclogs;

import java.util.Arrays;
import java.util.zip.CRC32;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.TmPacket;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;

public class EDLPacket {
    ByteArray packet;

    public EDLPacket(byte[] packet, Integer seqNum, byte[] hmacSecret) {
        // set sequence number in packet
        ByteArrayUtils.encodeInt(seqNum, packet, 7);

        this.packet = ByteArray.wrap(packet);
        

        // set frame length in packet: C = (Total Number of Octets in the Transfer Frame) − 1
        // CRC adds 2, HMAC adds 32 -> (size + (34 - 1))
        ByteArrayUtils.encodeUnsignedShort(this.packet.size()+33, this.packet.array(), 4);
        this.addHmac(hmacSecret);
        
        // Add CRC data to packet
        this.packet.addInt(this.createCrc(this.packet.array()).intValue());

    }

    public EDLPacket(TmPacket tmPacket) {
        this.packet = ByteArray.wrap(tmPacket.getPacket());
    }

    private void addHmac(byte[] hmacSecret) {
        this.packet.add(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, hmacSecret).hmac(this.packet.array()));
    }

    public Integer getSeqNum() {
        Short num = ByteArrayUtils.decodeShort(this.packet.array(), 6);
        return num.intValue();
    }

    private Long createCrc(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    public byte[] getBinary() {
        return this.packet.toArray();
    }

    public boolean containsValidCrc() {
        // get packet data without the CRC data, calculate CRC value from packet data       
        byte[] pkt = Arrays.copyOfRange(this.packet.array(), 0, this.packet.size()-8);
        Long calcCrc = this.createCrc(pkt);

        // get CRC data from packet
        Long packetCrc = ByteArrayUtils.decodeLong(this.packet.array(), this.packet.size()-8);
        
        // return the result of if the calculated CRC matches the returning CRC data
        return packetCrc.equals(calcCrc);
    }
}
