package org.oresat.uniclogs.tctm;

import org.yamcs.YConfiguration;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.tctm.CommandPostprocessor;

public class EdlCommandPostprocessor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;

    @Override
    public byte[] process(PreparedCommand pc) {

        EDLPacket cmd = new EDLPacket(pc.getBinary(), 0, null);
        byte[] cmdBin = cmd.getBinary();

        pc.setBinary(cmdBin);
        this.cmdHistory.publish(pc.getCommandId(), "edl-seqnum", 0);
        this.cmdHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, cmdBin);
        return cmdBin;
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
