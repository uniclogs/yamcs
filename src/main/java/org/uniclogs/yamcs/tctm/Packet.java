package org.uniclogs.yamcs.tctm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.yamcs.logging.Log;
import org.yamcs.tctm.ccsds.error.Crc16Calculator;
import org.yamcs.tctm.ccsds.error.Crc32Calculator;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.xtce.util.HexUtils;

public abstract class Packet {
    static final Log log = new Log(Packet.class);
    final Crc32Calculator crcCalc = new Crc32Calculator(0x91267E8A);
    Integer sequenceNumber;
    Integer sequenceNumberOffset;
    ByteBuffer data;

    protected Packet(byte[] data, Integer size, Integer sequenceNumber, Integer sequenceNumberOffset) {
        this.data = ByteBuffer.allocate(size);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        this.data.put(data);
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
        return this.data.array();
    }

    protected int calcCrc(byte[] data) {
        return this.crcCalc.compute(data, 0, data.length, 0);
    }

    protected void encodeSeqNum() {
        log.info("Enc SeqNum Packet Data: " + HexUtils.hex(this.data.array()));
        this.data.putInt(this.sequenceNumberOffset, this.sequenceNumber);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    protected void encodeFrameLength(Integer intToAdd, Integer frameLengthOffset) {
        Integer length = intToAdd + this.data.array().length;
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
        this.data.putShort(frameLengthOffset, length.shortValue());
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    abstract boolean validCrc();

    protected boolean crc16() {
        Crc16Calculator crc = new Crc16Calculator(0x1021);
        Integer calculatedCrc = crc.compute(this.data.array(), 0, this.data.array().length - 2, 0xFFFFFFFF);
        Integer collectedCrc = ByteArrayUtils.decodeInt(this.data.array(), this.data.array().length - 2);
        log.info(String.format("CRC_16: Calculated Value: %d, Expected Value: %d", calculatedCrc, collectedCrc));
        return calculatedCrc.equals(collectedCrc);
    }

    protected boolean crc32(int offset, int length) {
        Crc32Calculator crc = new Crc32Calculator(0x04C11DB7);
        Integer calculatedCrc = crc.compute(this.data.array(), offset, length, 0);
        Integer collectedCrc = ByteArrayUtils.decodeIntLE(this.data.array(), this.data.array().length - 4);
        log.info(String.format("CRC_32: Calculated Value: %d, Expected Value: %d", calculatedCrc, collectedCrc));
        return calculatedCrc.equals(collectedCrc);
    }

    protected Integer getSeqNum() {
        return this.sequenceNumber;
    }

}
