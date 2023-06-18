package org.oresat.uniclogs.tctm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.TmPacket;
import org.yamcs.logging.Log;
import org.yamcs.xtce.util.HexUtils;

public class CfdpPacket {
    private static final Integer SEQ_NUM_OFFSET = 7;
    static final Log log = new Log(CfdpPacket.class);
    private final byte[] data;
    private final byte[] tf_data;

    public CfdpPacket(byte[] packet, Integer seqNum, byte[] hmacSecret) {
        this.tf_data = packet;
        this.data = addHeaderToPacket();
        log.info(String.format("Creating CfdpPacket with seqNum: %d", seqNum));
        log.info(String.format("Packet length: %d", this.data.length));
        log.info(String.format("Packet: %s", HexUtils.hex(addHeaderToPacket())));
    }

    private byte[] addHeaderToPacket() {
        // Generate the header
        ByteBuffer header = generateTransferFramePrimaryHeader();

        log.info(String.format("Header size: %d", header.capacity()));

        // Create a new byte array to hold the header and the packet data
        byte[] dataWithHeader = new byte[header.capacity() + tf_data.length];

        log.info(String.format("New dataWithHeader size: %d", dataWithHeader.length));

        // Add the header and packet data to the new array
        System.arraycopy(header.array(), 0, dataWithHeader, 0, header.capacity());
        System.arraycopy(tf_data, 0, dataWithHeader, header.capacity(), tf_data.length);

        log.info(String.format("Final dataWithHeader size: %d", dataWithHeader.length));

        return dataWithHeader;
    }

    private ByteBuffer generateTransferFramePrimaryHeader() {
        // Initialize values
        int tfvn = 12; // '1100' binary is 12 in decimal
        int scid = 42069;
        int sourceOrDestinationId = 0;
        int vcid = 0;
        int mapId = 0;
        int endOfFramePrimaryHeaderFlag = 0;
        int frameLength = (7 + this.tf_data.length) - 1;
        int bypassSequenceControlFlag = 0;
        int protocolControlCommandFlag = 0;
        int reserveSpares = 0;
        int ocfFlag = 0;
        int vcfCountLength = 1;

        // Calculate new length
        int newLength = 7 + this.tf_data.length;

        // Create ByteBuffer with enough capacity
        ByteBuffer buffer = ByteBuffer.allocate(newLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);


        // Pack the values into the ByteBuffer
        // a. TFVN - Transfer Frame Version Number - 4 bits
        // b. SCID - Spacecraft ID - 16 bits
        buffer.putShort((short) ((tfvn << 12) | scid));
        log.info("Packed TFVN and SCID");

        // c. Source or Destination ID - 1 bit
        // d. VCID - Virtual Channel ID - 6 bits
        // e. MAP ID - Multiplexer Access Point ID - 4 bits
        buffer.put((byte) ((sourceOrDestinationId << 7) | (vcid << 1) | mapId));
        log.info("Packed Source/Destination ID, VCID, MAP ID");

        // f. End of Frame Primary Header Flag - 1 bit
        // g. Frame Length - 16 bits
        buffer.putShort((short) ((endOfFramePrimaryHeaderFlag << 15) | frameLength));
        log.info("Packed End of Frame Primary Header Flag and Frame Length");

        // h. Bypass/Sequence Control Flag - 1 bit
        // i. Protocol Control Command Flag - 1 bit
        // j. Reserve Spares - 2 bits
        // k. OCF Flag - 1 bit
        // l. VCF Count Length - 3 bits
        buffer.put((byte) ((bypassSequenceControlFlag << 7) | (protocolControlCommandFlag << 6) | (reserveSpares << 4) | (ocfFlag << 3) | vcfCountLength));
        log.info("Packed Bypass/Sequence Control Flag, Protocol Control Command Flag, Reserve Spares, OCF Flag, VCF Count Length");

        // Add tf_data into buffer
        buffer.put(this.tf_data);
        log.info("Added tf_data into buffer");

        return buffer;
    }

    protected byte[] getBinary() {
        return this.data;

    }
}
