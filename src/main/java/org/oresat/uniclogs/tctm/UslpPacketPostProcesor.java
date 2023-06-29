/**
 * USLP Packet Post Processor
 */

package org.oresat.uniclogs.tctm;

import org.oresat.uniclogs.services.UniclogsEnvironment;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.logging.Log;
import org.yamcs.tctm.CcsdsPacket;
import org.yamcs.tctm.CcsdsSeqCountFiller;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.GpsCcsdsTime;
import org.yamcs.utils.TimeEncoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class UslpPacketPostProcesor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;
    protected CcsdsSeqCountFiller seqFiller = new CcsdsSeqCountFiller();
    UniclogsEnvironment env;

    static final Log log = new Log(EdlCommandPostprocessor.class);

    private void overwriteWithUslpHeader(ByteBuffer buffer, Integer frameLength) {
        // Initialize values
        int tfvn = 12; // '1100' binary is 12 in decimal
        // Convert ASCII 'O' and 'S' to their integer equivalents and concatenate them.
        int scid = ('O' << 8) | 'S';  // This operation left-shifts the ASCII value of 'O' by 8 bits and then performs
                                      // a bitwise OR operation with the ASCII value of 'S'.

        int sourceOrDestinationId = 0;
        int vcid = 0;
        int mapId = 0;
        int endOfFramePrimaryHeaderFlag = 0;
        int bypassSequenceControlFlag = 0;
        int protocolControlCommandFlag = 0;
        int reserveSpares = 0;
        int ocfFlag = 0;
        int vcfCountLength = 1  & 0x7; // keep only 3 bits

        // Reset the buffer's position to the beginning
        buffer.position(0);

        // Set the buffer's order
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Byte 1: TFVN (4 bits) and the most significant 4 bits of SCID
        buffer.put((byte) ((tfvn << 4) | ((scid >> 12) & 0x0F)));

        // Byte 2: The next 8 bits of SCID
        buffer.put((byte) ((scid >> 4) & 0xFF));

        // Byte 3: The last 4 bits of SCID, the 1 bit for source or destination ID, and the most significant 3 bits of VCID
        buffer.put((byte) (((scid & 0x0F) << 4) | (sourceOrDestinationId << 3) | ((vcid >> 3) & 0x07)));

        // Byte 4: The last 3 bits of VCID, 4 bits of MAP ID, and 1 bit for end of frame
        buffer.put((byte) (((vcid & 0x07) << 5) | (mapId << 1) | endOfFramePrimaryHeaderFlag));

        // 5th and 6th bytes: frameLength as a short (16 bits)
        buffer.putShort((short) frameLength.intValue());

        // 7th byte: Various flags and length
        buffer.put((byte) ((bypassSequenceControlFlag << 7)
                | (protocolControlCommandFlag << 6)
                | (reserveSpares << 4)  // reserveSpares is assumed to be 0 for space
                | (ocfFlag << 3)
                | vcfCountLength));
    }

    public byte[] process(PreparedCommand pc) {
        byte[] binary = pc.getBinary();
        log.info("Processing prepared command with binary length: " + binary.length);
        log.info("Processing prepared command with binary: " + Arrays.toString(binary));
//        boolean secHeaderFlag = CcsdsPacket.getSecondaryHeaderFlag(binary);
//        boolean checksumIndicator = false;
//        if (secHeaderFlag) {
//            log.info("Secondary header flag is set");
//            checksumIndicator = CcsdsPacket.getChecksumIndicator(binary);
//        }

        int newLength = this.getBinaryLength(pc);

        if (newLength > binary.length) {
            binary = Arrays.copyOf(binary, newLength);
        }

        ByteBuffer bb = ByteBuffer.wrap(binary);
        //int seqCount = this.seqFiller.getSeqCount(bb);

        this.overwriteWithUslpHeader(bb, newLength);
//        bb.putShort(4, (short)(binary.length - 7));
//        int seqCount = this.seqFiller.fill(binary);
//        if (secHeaderFlag) {
//            GpsCcsdsTime gpsTime = TimeEncoding.toGpsTime(pc.getCommandId().getGenerationTime());
//            bb.putInt(6, gpsTime.coarseTime);
//            bb.put(10, gpsTime.fineTime);
//        }

        //this.cmdHistory.publish(pc.getCommandId(), "ccsds-seqcount", seqCount);
        this.cmdHistory.publish(pc.getCommandId(), "binary", binary);
        return binary;
    }

    @Override
    public void setCommandHistoryPublisher(CommandHistoryPublisher commandHistoryListener) {
        this.cmdHistory = commandHistoryListener;
    }

    public UslpPacketPostProcesor(String instanceName, YConfiguration config) {
        String envName = config.getString("envService");
        this.env = YamcsServer.getServer().getInstance(instanceName).getService(UniclogsEnvironment.class, envName);

        if (this.env == null) {
            throw new ConfigurationException("Service " + envName + " does not exist or is not of class UniclogsEnvironment.");
        }

    }

    public UslpPacketPostProcesor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }

}