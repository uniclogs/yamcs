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
    UniclogsService env;

    @Override
    public byte[] process(PreparedCommand pc) {
        Integer seqNum = this.env.getSeqNum();
        byte[] hmacKey = this.env.getHmacKey();

        EDLPacket cmd = new EDLPacket(pc.getBinary(), seqNum, hmacKey);
        byte[] cmdBin = cmd.getBinary();
        
        pc.setBinary(cmdBin);
        this.cmdHistory.publish(pc.getCommandId(), "edl-seqnum", seqNum);
        this.cmdHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, cmdBin);
        return cmdBin;
    }

    @Override
    public void setCommandHistoryPublisher(CommandHistoryPublisher commandHistoryListener) {
        this.cmdHistory = commandHistoryListener;
    }

    public EdlCommandPostprocessor(String instanceName, YConfiguration config) {
        this.instanceName = instanceName;
        this.config = config;
        this.env = YamcsServer.getServer().getInstance(instanceName).getServices(UniclogsService.class).get(0);
    }

    public EdlCommandPostprocessor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }
    
}
