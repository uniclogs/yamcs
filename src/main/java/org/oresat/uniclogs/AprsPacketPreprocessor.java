package org.oresat.uniclogs;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;
import org.yamcs.tctm.AbstractPacketPreprocessor.TimeEpochs;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.TimeEncoding;

public class AprsPacketPreprocessor extends AbstractPacketPreprocessor {

    // where from the packet to read the 8 bytes timestamp
    final int timestampOffset = -1;

    // where from the packet to read the 4 bytes sequence count
    final int seqCountOffset = 0;

    // Constructor used when this preprocessor is used without YAML configuration
    public AprsPacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    // Constructor used when this preprocessor is used with YAML configuration
    // (packetPreprocessorClassArgs)
    public AprsPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
        //timestampOffset = config.getInt("timestampOffset");
        //seqCountOffset = config.getInt("seqCountOffset");
        if (!config.containsKey(CONFIG_KEY_TIME_ENCODING)) {
            this.timeEpoch = TimeEpochs.UNIX;
        }
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        byte[] packet = tmPacket.getPacket();
        boolean corrupted = false;
        long packetCheckword = 0;
        long computedCheckword = 0;

        if (packet.length < 16) { // Expect at least the length of APRS header
            eventProducer.sendWarning("SHORT_PACKET",
                    "Short packet received, length: " + packet.length + "; minimum required length is 16 bytes.");
            // If we return null, the packet is dropped.
            return null;
        }

        try {
            int n = packet.length;

            // computed crc32
            byte[] packetData = Arrays.copyOfRange(packet, 16, n - 4);
            CRC32 crc32 = new CRC32();
            crc32.update(packetData);
            computedCheckword = crc32.getValue();

            // read crc32 from packet
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                packetCheckword = ((((long)packet[n - 1]) & 0xFF) << 24) +
                    ((((long)packet[n - 2]) & 0xFF) << 16) +
                    ((((long)packet[n - 3]) & 0xFF) << 8) +
                    ((((long)packet[n - 4]) & 0xFF));
            } else {
                packetCheckword = ((((long)packet[n - 4]) & 0xFF) << 24) + 
                    ((((long)packet[n - 3]) & 0xFF) << 16) +
                    ((((long)packet[n - 2]) & 0xFF) << 8) +
                    ((((long)packet[n - 1]) & 0xFF));
            }

            if (packetCheckword != computedCheckword) {
                eventProducer.sendWarning("CORRUPTED_PACKET",
                        "Corrupted packet received, computed checkword: " + computedCheckword
                                + "; packet checkword: " + packetCheckword);
                corrupted = true;
            }
        } catch (IllegalArgumentException e) {
            eventProducer.sendWarning("CORRUPTED_PACKET", "Error when computing checkword: " + e.getMessage());
            corrupted = true;
        }

        // get generated time
        long gentime;
        if (timestampOffset < 0) {
            gentime = TimeEncoding.getWallclockTime();
        } else {
            if (packet.length < timestampOffset + 8) {
                //eventProducer.sendWarning(ETYPE_CORRUPTED_PACKET, "Packet too short to extract timestamp");
                gentime = -1;
                corrupted = true;
            } else {
                long t = byteOrder == ByteOrder.BIG_ENDIAN ? ByteArrayUtils.decodeLong(packet, timestampOffset)
                        : ByteArrayUtils.decodeLongLE(packet, timestampOffset);
                gentime = shiftFromEpoch(t);
            }
        }

        tmPacket.setGenerationTime(gentime);
        tmPacket.setInvalid(corrupted);
        return tmPacket;
    }
}
