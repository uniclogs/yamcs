package org.oresat.uniclogs;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.protobuf.Commanding;
import org.yamcs.protobuf.Commanding.VerifierConfig;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.xtce.XtceDb;

public class EdlCommandPostprocessor implements CommandPostprocessor {

    protected CommandHistoryPublisher cmdHistory;
    private String instanceName;
    private YConfiguration config;
    private Integer seqNum;
    byte[] hmacSecret;

    @Override
    public byte[] process(PreparedCommand pc) {
        EDLCommand cmd = new EDLCommand(pc.getBinary(), this.seqNum, this.hmacSecret);
        byte[] cmdBin = cmd.getBinary();
        
        pc.setBinary(cmdBin);
        this.cmdHistory.publish(pc.getCommandId(), "edl-seqnum", 128);
        this.cmdHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, cmdBin);
        this.seqNum++;
        return cmdBin;
    }

    @Override
    public void setCommandHistoryPublisher(CommandHistoryPublisher commandHistoryListener) {
        this.cmdHistory = commandHistoryListener;
    }

    public EdlCommandPostprocessor(String instanceName, YConfiguration config) {
        this.instanceName = instanceName;
        this.config = config;
        this.seqNum = UniclogsEnvironment.getSequenceNumber();
        this.hmacSecret = UniclogsEnvironment.getHmacSecret();
    }

    public EdlCommandPostprocessor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }
    
}
