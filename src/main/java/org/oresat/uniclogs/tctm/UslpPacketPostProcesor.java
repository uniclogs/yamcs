/**
 * USLP Packet Post Processor
 */

package org.oresat.uniclogs.tctm;

import org.oresat.uniclogs.services.UniclogsEnvironment;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.logging.Log;
import org.yamcs.tctm.CcsdsPacket;
import org.yamcs.tctm.CommandPostprocessor;
import org.yamcs.utils.TimeEncoding;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UslpPacketPostProcesor implements CommandPostprocessor {
    protected CommandHistoryPublisher cmdHistory;
    UniclogsEnvironment env;

    static final Log log = new Log(EdlCommandPostprocessor.class);


    public byte[] process(PreparedCommand pc) {
        log.info("USLP Packet Post Processor");
        byte[] binary = pc.getBinary();
        log.info("Binary: " + binary.toString());

        log.info("CCSDS Packet Length: " + CcsdsPacket.getCccsdsPacketLength(binary));

        int newLength = this.getBinaryLength(pc);
        log.info("New Length: " + newLength);

        if (newLength > binary.length) {
            binary = Arrays.copyOf(binary, newLength);
        }


        Integer seqNum = this.env.getSeqNum();
        //byte[] hmacKey = this.env.getHmacKey();
        byte[] hmacKey = "fakekey".getBytes(StandardCharsets.UTF_8);

        CfdpPacket cmd = new CfdpPacket(binary, seqNum, hmacKey);
        byte[] cmdBin = cmd.getBinary();
        this.cmdHistory.publish(pc.getCommandId(), "cfdp-seqnum", seqNum);
        this.cmdHistory.publish(pc.getCommandId(), PreparedCommand.CNAME_BINARY, cmdBin);
        pc.setBinary(cmdBin);

        return cmdBin;
    }
    @Override
    public void setCommandHistoryPublisher(CommandHistoryPublisher commandHistoryListener) {
        this.cmdHistory = commandHistoryListener;
    }

    public UslpPacketPostProcesor(String instanceName, YConfiguration config) {
        String envName = config.getString("envService");
        this.env = YamcsServer.getServer().getInstance(instanceName).getService(UniclogsEnvironment.class, envName);

        if (this.env == null) {
            throw new ConfigurationException("Service " + envName + " does not exist or is not of class UniclogsEnvironment.");
        }

    }

    public UslpPacketPostProcesor(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }

}