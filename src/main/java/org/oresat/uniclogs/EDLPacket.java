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
        ByteArrayUtils.encodeUnsignedShort(seqNum, packet, 6);

        this.packet = ByteArray.wrap(packet);
        this.addHmac(hmacSecret);
        this.addCrc();

        // set frame length in packet: C = (Total Number of Octets in the Transfer Frame) − 1
        ByteArrayUtils.encodeUnsignedShort(this.packet.size()-1, this.packet.array(), 4);
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

    private void addCrc() {
        CRC32 crc = new CRC32();
        crc.update(this.packet.array());
        packet.addLong(crc.getValue());
    }

    public byte[] getBinary() {
        return this.packet.toArray();
    }


    public boolean containsValidCrc() {
        CRC32 crc = new CRC32();
        crc.update(this.packet.array());
        Long expected = ByteArrayUtils.decodeLong(this.packet.array(), packet.size()-2);
        return expected.equals(crc.getValue());
    }
}
