package org.oresat.uniclogs;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.yamcs.YConfiguration;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.logging.Log;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.utils.ByteArray;
import org.yamcs.utils.ByteArrayUtils;

public class EDLCommandPostprocessor implements CommandPostprocessor {

    final static Log LOG = new Log(EDLCommandPostprocessor.class);

    private String instanceName;
    private YConfiguration config;
    private CommandHistoryPublisher commandHistory;

    public EDLCommandPostprocessor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }

    public EDLCommandPostprocessor(String instanceName, YConfiguration config) {
        this.instanceName = instanceName;
        this.config = config;
    }

    private final String byteArrayToHexString(ByteArray data) {
        return byteArrayToHexString(data.toArray());
    }

    private final String byteArrayToHexString(byte[] data) {
        StringBuilder hex = new StringBuilder();
        for (byte d: data) {
            hex.append(String.format("%2X", Byte.toUnsignedInt(d)).replace(' ', '0'));
        }
        return hex.toString();
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
        // Get the header
        byte[] header = new byte[] {(byte)0xC4, (byte)0xF5, (byte)0x38, 0x00, 0x00, 0x00, 0x00, (byte)0xE5};
        LOG.debug("Header: " + byteArrayToHexString(header));

        // Get the payload
        byte[] payload = pc.getBinary();
        LOG.debug("Command Payload: " + byteArrayToHexString(payload));

        // Get the salt
        Integer serialNumber = PrepareEnvironment.getSerialNumber(); // TODO: Load this from somewhere externally
        byte[] salt = ByteArrayUtils.encodeInt(serialNumber);
        LOG.debug("Salt as a Serial Number: " + ByteArrayUtils.decodeInt(salt, 0) + ": " + byteArrayToHexString(salt));

        // Generate the HMAC Key
        String secret = PrepareEnvironment.getHmacSecret();
        HmacUtils hmacGenerator = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
        ByteArray payloadSalt = new ByteArray();
        payloadSalt.add(salt);
        payloadSalt.add(payload);
        byte[] hmacKey = hmacGenerator.hmac(payloadSalt.toArray());
        String hmacHex = hmacGenerator.hmacHex(payloadSalt.toArray());
        LOG.debug("Generated HMAC Key: " + hmacHex.toLowerCase());

        // Begin assembling the message
        ByteArray message = new ByteArray();
        message.add(header);
        message.add(hmacKey);
        message.add(salt);
        message.add(payload);

        // Since we modified the binary, update the binary in Command History too.
        commandHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, message.toArray());

        LOG.debug("Here's the CMD message byte structure: {header: " + header.length + "b, payload: " + payload.length + "b, salt: " + salt.length + "b, hmac-key: " + hmacKey.length + "b}");
        LOG.debug("Sending a message with a total size of " + message.size() +" bytes: " + byteArrayToHexString(message));
        return message.toArray();
    }
}

