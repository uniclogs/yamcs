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
        byte[] frameStart = new byte[] {(byte)0xE5};

        // Get the payload
        byte[] payload = pc.getBinary();
        LOG.debug("Command Payload: " + byteArrayToHexString(payload));

        // Get the salt
        Integer sequenceNumber = UniclogsEnvironment.getSequenceNumber();
        byte[] salt = ByteArrayUtils.encodeInt(sequenceNumber);
        LOG.debug("Salt as a Sequence Number: " + sequenceNumber + ": " + byteArrayToHexString(salt));

        // Generate the SPI (Statically set to zero for OreSat0)
        byte[] spi = new byte[] {0x00, (byte)0x01};
        LOG.debug("Generated SPI: " + byteArrayToHexString(spi));

        // Generate the HMAC Key
        String secret = UniclogsEnvironment.getHmacSecret();
        HmacUtils hmacGenerator = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
        ByteArray payloadSalt = new ByteArray();
        payloadSalt.add(salt);
        payloadSalt.add(payload);
        byte[] hmacKey = hmacGenerator.hmac(payloadSalt.toArray());
        LOG.debug("Generated HMAC Key: " + byteArrayToHexString(hmacKey));

        // Get the header
        byte[] header = new byte[] {(byte)0xC4, (byte)0xF5, (byte)0x38, 0x00, 0x00, 0x00, 0x00};
        int size = header.length + spi.length + salt.length + frameStart.length + payload.length + hmacKey.length + 2;  // Adding 2 to account for the FECF
        ByteArrayUtils.encodeUnsignedShort((short) size, header, 4);
        LOG.debug("Header: " + byteArrayToHexString(header));

        // Begin assembling the message
        ByteArray message = new ByteArray();
        message.add(header);
        message.add(spi);
        message.add(salt);
        message.add(frameStart);
        message.add(payload);
        message.add(hmacKey);

        // Since we modified the binary, update the binary in Command History too.
        commandHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, message.toArray());
        UniclogsEnvironment.incrementSequenceNumber(); // Automatically update the salt value

        LOG.debug("Here's the CMD message byte structure: {header: " + header.length + "b, frame-start: " + frameStart.length + "b, payload: " + payload.length + "b, salt: " + salt.length + "b, hmac-key: " + hmacKey.length + "b}");
        LOG.debug("Sending a message with a total size of " + message.size() +" bytes: " + byteArrayToHexString(message));
        return message.toArray();
    }
}
