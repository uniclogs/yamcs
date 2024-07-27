package org.oresat.uniclogs.tctm;
import org.oresat.uniclogs.services.SatnogsTransferService;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.tctm.CommandPostprocessor;


public class SatnogsCommandPostprocessor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;
    SatnogsTransferService env;

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

    public SatnogsCommandPostprocessor(String instanceName, YConfiguration config) {
        String envName = config.getString("envService");
        this.env = YamcsServer.getServer().getInstance(instanceName).getService(SatnogsTransferService.class, envName);

        if (this.env == null) {
            throw new ConfigurationException("Service " + envName + " does not exist or is not of class SatnogsTransferService.");
        }
    }

    public SatnogsCommandPostprocessor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }

}
