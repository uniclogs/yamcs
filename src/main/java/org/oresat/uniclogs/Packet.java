package org.oresat.uniclogs;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.logging.Log;
import org.yamcs.tctm.ccsds.error.CrcCciitCalculator;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;

public abstract class Packet {
    static final Log log = new Log(Packet.class);
    final CrcCciitCalculator crcCalc = new CrcCciitCalculator();
    Integer sequenceNumber;
    Integer sequenceNumberOffset;
    ByteArray data;

    protected Packet(byte[] data, Integer sequenceNumber, Integer sequenceNumberOffset) {
        this.data = ByteArray.wrap(data);
        this.sequenceNumber = sequenceNumber;
        this.sequenceNumberOffset = sequenceNumberOffset;
    }


    protected void addHmac(byte[] hmacSecret) {
        byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, hmacSecret).hmac(this.data.array());
        this.data.add(hmac);
        log.info(String.format("HMAC_SHA_256 (%s bit) added to packet (seqNum: %d).", hmac.length, this.sequenceNumber));
    } 

    protected Integer getSequenceNumber() {
        return getSequenceNumber(this.data.array(), this.sequenceNumberOffset);
    }

    protected static Integer getSequenceNumber(byte[] data, Integer offset) {
        return ByteArrayUtils.decodeInt(data, offset);
    }

    protected byte[] getBinary() {
        return this.data.array();
    }

    protected void encodeCrc() {
        Short crc = (short) this.crcCalc.compute(this.data.array(), 0, this.data.array().length);
        log.info(String.format("CRC_16 (%d) added to packet (seqNum: %d).", crc, this.sequenceNumber));
        this.data.addShort(crc);
    }

    protected void encodeSeqNum() {
        ByteArrayUtils.encodeInt(this.sequenceNumber, this.data.array(), this.sequenceNumberOffset);
    }

    protected void encodeFrameLength(Integer intToAdd, Integer frameLengthOffset) {
        ByteArrayUtils.encodeUnsignedShort(this.data.size()+intToAdd, this.data.array(), frameLengthOffset);
    }

    protected boolean validCrc() {
        Integer calculatedCrc = this.crcCalc.compute(this.data.array(), 0, this.data.size()-2);
        Integer collectedCrc = ByteArrayUtils.decodeUnsignedShort(this.data.array(), this.data.size() -2);
        return calculatedCrc.equals(collectedCrc);
    }

    protected Integer getSeqNum() {
        return this.sequenceNumber;
    }

}
