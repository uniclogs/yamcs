package org.oresat.uniclogs;
import java.nio.ByteBuffer;

import org.yamcs.logging.Log;
import org.yamcs.tctm.Iso16CrcCalculator;
import org.yamcs.tctm.ccsds.error.Crc16Calculator;
import org.yamcs.tctm.ccsds.error.CrcCciitCalculator;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.xtce.util.HexUtils;
public abstract class Packet {
    static final Log log = new Log(Packet.class);
    final Iso16CrcCalculator crcCalc = new Iso16CrcCalculator();
    Integer sequenceNumber;
    Integer sequenceNumberOffset;
    ByteBuffer data;

    protected Packet(byte[] data, Integer size, Integer sequenceNumber, Integer sequenceNumberOffset) {
        this.data = ByteBuffer.allocate(size);
        this.data.put(data);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
        this.sequenceNumber = sequenceNumber;
        this.sequenceNumberOffset = sequenceNumberOffset;
    }


    protected Integer getSequenceNumber() {
        return getSequenceNumber(this.data.array(), this.sequenceNumberOffset);
    }

    protected static Integer getSequenceNumber(byte[] data, Integer offset) {
        return ByteArrayUtils.decodeInt(data, offset);
    }

    protected byte[] getBinary() {
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
        return this.data.array();
    }

    protected int calcCrc(byte[] data) {
        return this.crcCalc.compute(data, 0, data.length);
    }

    protected void encodeSeqNum() {
        log.info("Enc SeqNum Packet Data: " + HexUtils.hex(this.data.array()));
        this.data.putInt(this.sequenceNumberOffset, this.sequenceNumber);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    protected void encodeFrameLength(Integer intToAdd, Integer frameLengthOffset) {
        Integer length = intToAdd + this.data.array().length;
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
        this.data.putShort(frameLengthOffset,  length.shortValue());
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    protected boolean validCrc() {
        Integer calculatedCrc = this.crcCalc.compute(this.data.array(), 0, this.data.array().length-2);
        Integer collectedCrc = ByteArrayUtils.decodeUnsignedShort(this.data.array(), this.data.array().length -2);
        log.info(String.format("CRC_16: Calculated Value: %d, Expected Value: %d", calculatedCrc, collectedCrc));
        return calculatedCrc.equals(collectedCrc);
    }

    protected Integer getSeqNum() {
        return this.sequenceNumber;
    }

}
