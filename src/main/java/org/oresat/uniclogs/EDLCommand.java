package org.oresat.uniclogs;

import java.util.zip.CRC32;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;

public class EDLCommand {
    ByteArray packet;

    public EDLCommand(byte[] packet, Integer seqNum, byte[] hmacSecret) {
        this.packet = new ByteArray();
        this.packet.add(packet);
        this.packet.add(ByteArrayUtils.encodeInt(seqNum));
        this.addHmac(hmacSecret);
        this.addCrc();
    }


    private void addHmac(byte[] hmacSecret) {
        ByteArray message = new ByteArray();
        message.add(this.packet.toArray());
        this.packet.add(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, hmacSecret).hmac(message.array()));
    }

    private void addCrc() {
        CRC32 crc = new CRC32();
        crc.update(this.packet.array());
        packet.addLong(crc.getValue());
    }

    public byte[] getBinary() {
        return this.packet.toArray();
    }
}
