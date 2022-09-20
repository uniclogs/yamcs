package org.oresat.uniclogs;
import org.yamcs.logging.Log;
import org.yamcs.tctm.ccsds.error.CrcCciitCalculator;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.xtce.util.HexUtils;
public abstract class Packet {
    static final Log log = new Log(Packet.class);
    final CrcCciitCalculator crcCalc = new CrcCciitCalculator();
    Integer sequenceNumber;
    Integer sequenceNumberOffset;
    ByteArray data;

    protected Packet(byte[] data, Integer sequenceNumber, Integer sequenceNumberOffset) {
        this.data = ByteArray.wrap(data);
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

    protected void encodeCrc() {
        log.info("Enc Crc Packet Data: " + HexUtils.hex(this.data.array()));
        int crc = this.crcCalc.compute(this.data.array(), 0, this.data.array().length);
        log.info(String.format("CRC_16 (%d) added to packet (seqNum: %d).", crc, this.sequenceNumber));
        this.data.addShort((short) crc);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    protected void encodeSeqNum() {
        log.info("Enc SeqNum Packet Data: " + HexUtils.hex(this.data.array()));
        ByteArrayUtils.encodeInt(this.sequenceNumber, this.data.array(), this.sequenceNumberOffset);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    protected void encodeFrameLength(Integer intToAdd, Integer frameLengthOffset) {
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
        ByteArrayUtils.encodeUnsignedShort(this.data.size()+intToAdd, this.data.array(), frameLengthOffset);
        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
    }

    protected boolean validCrc() {
        Integer calculatedCrc = this.crcCalc.compute(this.data.array(), 0, this.data.size()-2);
        Integer collectedCrc = ByteArrayUtils.decodeUnsignedShort(this.data.array(), this.data.size() -2);
        log.info(String.format("CRC_16: Calculated Value: %d, Expected Value: %d", calculatedCrc, collectedCrc));
        return calculatedCrc.equals(collectedCrc);
    }

    protected Integer getSeqNum() {
        return this.sequenceNumber;
    }

}
