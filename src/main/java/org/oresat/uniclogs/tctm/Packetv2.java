//package org.oresat.uniclogs.tctm;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
//import org.yamcs.logging.Log;
//import org.yamcs.tctm.ccsds.error.Crc16Calculator;
//import org.yamcs.tctm.ccsds.error.Crc32Calculator;
//import org.yamcs.utils.ByteArrayUtils;
//import org.yamcs.xtce.util.HexUtils;
//
//public abstract class Packetv2 {
//    static final Log log = new Log(Packet.class);
//    final Crc32Calculator crcCalc = new Crc32Calculator(0x91267E8A);
//    Integer sequenceNumber;
//    Integer sequenceNumberOffset;
//    ByteBuffer payload;
//    ByteBuffer packet;
//
//    protected Packetv2(byte[] data, Integer size, Integer sequenceNumber, Integer sequenceNumberOffset) {
//        this.data = ByteBuffer.allocate(size);
//        this.data.order(ByteOrder.LITTLE_ENDIAN);
//        this.data.put(data);
//        this.sequenceNumber = sequenceNumber;
//        this.sequenceNumberOffset = sequenceNumberOffset;
//    }
//
//    protected void generateTransferFramePrimaryHeader(){
//        /**
//         *  Implemented based on CCSDS 732.1-B-2, Section 4.1.2
//         *  Transfer Frame Primary Header construction:
//         *  a. TFVN - Transfer Frame Version Number - 4 bits
//         *  b. SCID - Spacecraft ID - 16 bits
//         *  c. Source or Destination ID - 1 bit
//         *  d. VCID - Virtual Channel ID - 6 bits
//         *  e. MAP ID - Multiplexer Access Point ID - 4 bits
//         *  f. End of Frame Primary Header Flag - 1 bit
//         *  g. Frame Length - 16 bits
//         *  h. Bypass/Sequence Control Flag - 1 bit
//         *  i. Protocol Control Command Flag - 1 bit
//         *  j. Reserve Spares - 2 bits
//         *  k. OCF Flag - 1 bit
//         *  l. VCF Count Length - 3 bit
//         *  m. VCF Count - (0 - 56 bits)
//         */
//    }
//
//    protected Integer calcFrameLength() {
//        /**
//         * Implemented based on CCSDS 732.1-B-2, Section 4.1.2.7
//         * Frame Length = (Total Number of Octets in the Transfer Frame) − 1
//         */
//        return (this.packet.array().length - 1);
//    }
//
//    protected Integer getSequenceNumber() {
//        return getSequenceNumber(this.data.array(), this.sequenceNumberOffset);
//    }
//
//    protected static Integer getSequenceNumber(byte[] data, Integer offset) {
//        return ByteArrayUtils.decodeInt(data, offset);
//    }
//
//    protected byte[] getBinary() {
//        return this.data.array();
//    }
//
//    protected int calcCrc(byte[] data) {
//        return this.crcCalc.compute(data, 0, data.length, 0);
//    }
//
//    protected void encodeSeqNum() {
//        log.info("Enc SeqNum Packet Data: " + HexUtils.hex(this.data.array()));
//        this.data.putInt(this.sequenceNumberOffset, this.sequenceNumber);
//        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
//    }
//
//    protected void encodeFrameLength(Integer intToAdd, Integer frameLengthOffset) {
//        Integer length = intToAdd + this.data.array().length;
//        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
//        this.data.putShort(frameLengthOffset,  length.shortValue());
//        log.info("Packet Data: " + HexUtils.hex(this.data.array()));
//    }
//
//    abstract boolean validCrc();
//
//    protected boolean crc16() {
//        Crc16Calculator crc = new Crc16Calculator(0x1021);
//        Integer calculatedCrc = crc.compute(this.data.array(), 0, this.data.array().length-2, 0xFFFFFFFF);
//        Integer collectedCrc = ByteArrayUtils.decodeInt(this.data.array(), this.data.array().length -2);
//        log.info(String.format("CRC_16: Calculated Value: %d, Expected Value: %d", calculatedCrc, collectedCrc));
//        return calculatedCrc.equals(collectedCrc);
//    }
//
//    protected boolean crc32(int offset, int length) {
//        Crc32Calculator crc = new Crc32Calculator(0x04C11DB7);
//        Integer calculatedCrc = crc.compute(this.data.array(), offset, length, 0);
//        Integer collectedCrc = ByteArrayUtils.decodeIntLE(this.data.array(), this.data.array().length - 4);
//        log.info(String.format("CRC_32: Calculated Value: %d, Expected Value: %d", calculatedCrc, collectedCrc));
//        return calculatedCrc.equals(collectedCrc);
//    }
//
//    protected Integer getSeqNum() {
//        return this.sequenceNumber;
//    }
//
//}
