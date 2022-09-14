package org.oresat.uniclogs;

import java.nio.ByteOrder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsException;
import org.yamcs.tctm.AbstractPacketPreprocessor;

public class EdlPacketPreprocessor extends AbstractPacketPreprocessor {

    public EdlPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
    }

    public EdlPacketPreprocessor(String yamcsInstance) {
        super(yamcsInstance, YConfiguration.emptyConfig());
    }

    @Override
    public TmPacket process(TmPacket tmPacket) {
        EDLPacket packet = new EDLPacket(tmPacket);
        /**try {
            computeCrc(Arrays.copyOfRange(packet, 16, packet.length - 4));
        } catch (CorruptedPacketException e) {
            eventProducer.sendWarning(e.getMessage());
            tmPacket.setInvalid();
        }**/
        tmPacket.setSequenceCount(packet.getSeqNum());
        tmPacket.setGenerationTime(new Date().getTime());
        tmPacket.setLocalGenTimeFlag();
        return tmPacket;
    }


    private void computeCrc(byte[] packet) throws CorruptedPacketException {
        CRC32 crc = new CRC32();
        crc.update(packet);
        Long expected = getCheckword(packet);
        Long checkword = crc.getValue();
        if (!expected.equals(checkword)) {
            throw new CorruptedPacketException(checkword, expected);
        }
    }

    private Long getCheckword(byte[] packet) {
        Integer[] order = {1,2,3,4};
        Integer[] shift = {24, 16, 8, 0};

        ArrayList<Integer> ordering = new ArrayList<>(List.of(order));
        if (this.byteOrder.equals(ByteOrder.BIG_ENDIAN)) {
            Collections.reverse(ordering);
        }

        long checkword = 0;
        for (int i=0; i<4; i++) {
            checkword += (long) packet[packet.length - ordering.get(i)] & 0xFF << shift[i];
        }

        return checkword;
    }
    
}

class CorruptedPacketException extends Exception {
    public CorruptedPacketException(Long packetCheckword, Long expectedCheckword) {
        super(String.format("Corrupted Packet: Expected %d but got %d", expectedCheckword, packetCheckword));
    }
}
