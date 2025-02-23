package org.uniclogs.yamcs.tctm;
import org.uniclogs.services.UniclogsEnvironment;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.tctm.CommandPostprocessor;


public class EdlCommandPostprocessor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;
    UniclogsEnvironment env;

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
        String envName = config.getString("envService");
        this.env = YamcsServer.getServer().getInstance(instanceName).getService(UniclogsEnvironment.class, envName);
        
        if (this.env == null) {
            throw new ConfigurationException("Service " + envName + " does not exist or is not of class UniclogsEnvironment.");
        }
    }

    public EdlCommandPostprocessor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }
    
}
