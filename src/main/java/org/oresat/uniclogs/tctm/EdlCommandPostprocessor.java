package org.oresat.uniclogs.tctm;
import org.oresat.uniclogs.services.UniclogsEnvironment;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.xtce.Argument;
import org.yamcs.commanding.ArgumentValue;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.tctm.CommandPostprocessor;

// For fake HMAC key
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.Map;

import org.yamcs.logging.Log;
public class EdlCommandPostprocessor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;
    UniclogsEnvironment env;

    static final Log log = new Log(EdlCommandPostprocessor.class);

    @Override
    public byte[] process(PreparedCommand pc) {
        Integer seqNum = this.env.getSeqNum();
        //byte[] hmacKey = this.env.getHmacKey();
        byte[] hmacKey = "fakekey".getBytes(StandardCharsets.UTF_8);

        EDLPacket cmd = new EDLPacket(pc.getBinary(), seqNum, hmacKey);
        byte[] cmdBin = cmd.getBinary();
        Map<Argument, ArgumentValue> args = pc.getArgAssignment();

        log.info(args.toString());
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
