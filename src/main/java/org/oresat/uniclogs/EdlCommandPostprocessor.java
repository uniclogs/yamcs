package org.oresat.uniclogs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.YConfiguration;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.logging.Log;
import org.yamcs.tctm.CcsdsSeqCountFiller;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.utils.ByteArrayUtils;

public class EdlCommandPostprocessor implements CommandPostprocessor {

    final static Log LOG = new Log(EdlCommandPostprocessor.class);

    private CommandHistoryPublisher commandHistory;

    public EdlCommandPostprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    public EdlCommandPostprocessor(String yamcsInstance, YConfiguration config) {
    }

    // Called by Yamcs during initialization
    @Override
    public void setCommandHistoryPublisher(CommandHistoryPublisher commandHistory) {
        this.commandHistory = commandHistory;
    }

    // Called by Yamcs *after* a command was submitted, but *before* the link handles it.
    // This method must return the (possibly modified) packet binary.
    @Override
    public byte[] process(PreparedCommand pc) {
        byte[] payload = pc.getBinary();
        byte[] header = new byte[] {(byte)0xC4, (byte)0xF5, (byte)0x38, 0x00, 0x00, 0x00, 0x00, (byte)0xE5};

        // Load Secret
        String secret = "asdf";

        // Generate HMAC
        byte[] hmacBin = null;
        String hmacHex = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret).hmacHex(payload);
        LOG.debug("Generated HMAC Key: " + hmacHex);

        try {
            hmacBin = Hex.decodeHex(hmacHex);
        } catch (DecoderException e) {
            LOG.error("Failed to decode hex string: " + e.getMessage());
            e.printStackTrace();
            return null;
        }


        byte[] binary = new byte[header.length + payload.length];

        // Set packet length bytes
        ByteArrayUtils.encodeUnsignedShort(payload.length + 12, header, 4);

        System.arraycopy(header, 0, binary, 0, header.length);
        System.arraycopy(payload, 0, binary, header.length, payload.length);

        // Append HMAC to binary
        try {
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bStream.write(binary);
            bStream.write(hmacBin);
            binary = bStream.toByteArray();
        } catch (IOException e) {
            LOG.error("Failed to append to payload: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        // Since we modified the binary, update the binary in Command History too.
        commandHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, binary);

        LOG.debug("");
        return binary;
    }
}

