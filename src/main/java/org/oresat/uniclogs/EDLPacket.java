package org.oresat.uniclogs;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.TmPacket;
import org.yamcs.tctm.ccsds.error.CrcCciitCalculator;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;

public class EDLPacket {
    final CrcCciitCalculator crcCalc = new CrcCciitCalculator();
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
        this.packet.addShort((short) this.createCrc(this.packet.array()));

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

    private int createCrc(byte[] data) {
        return this.crcCalc.compute(data, 0, data.length);
    }

    public byte[] getBinary() {
        return this.packet.toArray();
    }

    public boolean containsValidCrc() {
        // get packet data without the CRC data, calculate CRC value from packet data       
        int calcCrc = this.crcCalc.compute(this.packet.array(), 0, this.packet.size()-2);

        // get CRC data from packet
        Integer packetCrc = ByteArrayUtils.decodeUnsignedShort(this.packet.array(), this.packet.size()-2);
        
        // return the result of if the calculated CRC matches the returning CRC data
        return packetCrc.equals(calcCrc);
    }
}
