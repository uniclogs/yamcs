package org.oresat.uniclogs;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.CRC32;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.TimeEncoding;

public class BeaconPacketPreprocessor extends AbstractPacketPreprocessor {

    // where from the packet to read the 8 bytes timestamp
    int timestampOffset = -1;

    // where from the packet to read the 4 bytes sequence count
    final int seqCountOffset = 0;

    // Constructor used when this preprocessor is used without YAML configuration
    public BeaconPacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    // Constructor used when this preprocessor is used with YAML configuration
    // (packetPreprocessorClassArgs)
    public BeaconPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
        timestampOffset = config.getInt("timestampOffset");
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
            return null; // drop packet
        }

        int n = packet.length;
        byte[] packetData = Arrays.copyOfRange(packet, 16, n - 4);

        try {
            // computed crc32
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
            if (packet.length < timestampOffset + 4) {
                eventProducer.sendWarning("CORRUPTED_PACKET", "Packet too short to extract timestamp");
                gentime = -1;
                corrupted = true;
            } else {
                long rawtime;

                if (byteOrder == ByteOrder.BIG_ENDIAN)
                    rawtime = (long)ByteArrayUtils.decodeInt(packet, timestampOffset);
                else
                    rawtime = (long)ByteArrayUtils.decodeIntLE(packet, timestampOffset);

                // rawtime is in seconds, shiftFromEpoch wants milliseconds
                gentime = shiftFromEpoch(rawtime*1000);
            }
        }

        // drop Ax25 header
        TmPacket tmPacket2 = new TmPacket(tmPacket.getReceptionTime(), Arrays.copyOfRange(packet, 16, n));

        tmPacket2.setGenerationTime(gentime);
        tmPacket2.setInvalid(corrupted);
        return tmPacket2;
    }
}
