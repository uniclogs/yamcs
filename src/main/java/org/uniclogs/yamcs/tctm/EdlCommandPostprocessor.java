package org.uniclogs.yamcs.tctm;

import org.yamcs.YConfiguration;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import java.util.Arrays;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.logging.Log;

public class EdlCommandPostprocessor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;

    @Override
    public byte[] process(PreparedCommand pc) {
		int old_packet_length = pc.getBinary().length;
		byte[] bytes = Arrays.copyOf(pc.getBinary(), old_packet_length + 32);

		// TODO: add HMAC configuring in the 32 bytes after the PDU.

		return bytes;
    }

	@Override
    public int getBinaryLength(PreparedCommand pc) {
        return pc.getBinary().length + 32;
    }

    @Override
    public void setCommandHistoryPublisher(CommandHistoryPublisher commandHistoryListener) {
        this.cmdHistory = commandHistoryListener;
    }

    public EdlCommandPostprocessor(String instanceName, YConfiguration config) {
    }

    public EdlCommandPostprocessor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }

}
