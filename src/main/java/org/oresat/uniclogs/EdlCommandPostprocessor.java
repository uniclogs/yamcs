package org.oresat.uniclogs;

import java.math.BigInteger;

import org.yamcs.YConfiguration;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.tctm.CcsdsSeqCountFiller;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.utils.ByteArrayUtils;

public class EdlCommandPostprocessor implements CommandPostprocessor {

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
        byte[] binary = new byte[header.length + payload.length];

        // Set packet length bytes
        ByteArrayUtils.encodeShort(payload.length + 12, header, 4);

        System.arraycopy(header, 0, binary, 0, header.length);
        System.arraycopy(payload, 0, binary, header.length, payload.length);

        // Since we modified the binary, update the binary in Command History too.
        commandHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, binary);

        return binary;
    }
}

