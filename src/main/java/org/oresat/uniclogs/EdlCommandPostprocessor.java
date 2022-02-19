package org.oresat.uniclogs;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.YConfiguration;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.logging.Log;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.utils.ByteArrayUtils;

public class EdlCommandPostprocessor implements CommandPostprocessor {

    final static Log LOG = new Log(EdlCommandPostprocessor.class);

    private CommandHistoryPublisher commandHistory;

    public EdlCommandPostprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    public EdlCommandPostprocessor(String yamcsInstance, YConfiguration config) {
        LOG.debug("Yamcs hot patched EDL Post-Processor with Yamcs-client-instance: " + yamcsInstance + ", and config: " + config);
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
        LOG.debug("Header: " + header.length + " bytes, Payload: " + payload.length + " bytes");

        // Load Secret
        String secret = "asdf12345"; // TODO: Make this come from an environment variable or something

        // Generate HMAC
        HmacUtils hmacGenerator = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secret);
        byte[] hmacKey = hmacGenerator.hmac(payload);
        String hmacHex = hmacGenerator.hmacHex(payload);
        LOG.debug("Generated HMAC Key: (" + hmacKey + " : " + hmacKey.length + " bytes)" + hmacHex);

        // Initialize the returnable byte string
        final int preKeyLength = header.length + payload.length;
        final int postKeyLength = preKeyLength + (hmacKey.length);
        byte[] binary = new byte[postKeyLength];

        // Set packet length bytes
        ByteArrayUtils.encodeUnsignedShort(payload.length + 12, header, 4);

        // Load the returnable byte string
        System.arraycopy(header, 0, binary, 0, header.length); // Load the header
        System.arraycopy(payload, 0, binary, header.length, payload.length); // Load the payload
        System.arraycopy(hmacKey, 0, binary, preKeyLength, hmacKey.length); // Load the HMAC key

        // Since we modified the binary, update the binary in Command History too.
        commandHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, binary);

        LOG.debug("Sending payload with " + binary.length +  " bytes!");
        return binary;
    }
}

