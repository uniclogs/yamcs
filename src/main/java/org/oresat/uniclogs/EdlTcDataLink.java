package org.oresat.uniclogs;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.cmdhistory.CommandHistoryPublisher;
import org.yamcs.cmdhistory.StreamCommandHistoryPublisher;
import org.yamcs.cmdhistory.CommandHistoryPublisher.AckStatus;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.protobuf.Commanding;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.VerifierConfig;
import org.yamcs.tctm.AbstractTcDataLink;
import org.yamcs.tctm.TcDataLink;
import org.yamcs.tctm.UdpTcDataLink;
import org.yamcs.xtce.CheckWindow;
import org.yamcs.xtce.CommandVerifier;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.SequenceContainer;
import org.yamcs.xtce.CommandVerifier.TerminationAction;

import com.google.protobuf.Descriptors.FieldDescriptor;

public class EdlTcDataLink extends UdpTcDataLink {

    static final String INSTANCE_NAME = "oresat0";
    static final String REQ_RESP = "requiresResponse";

    public EdlTcDataLink(String yamcsInstance, String linkname, YConfiguration config) {
        init(yamcsInstance, linkname, config);
    }

    protected boolean requiresResponse(PreparedCommand pc) {
        CommandHistoryAttribute cha = pc.getAttribute(REQ_RESP);
        if (cha == null) {
            return false;
        } else {
            System.err.println(cha.getValue().getBooleanValue());
            return cha.getValue().getBooleanValue();
        }
    }

    private PreparedCommand addVerifierFromOption(PreparedCommand pc) {
        if (!requiresResponse(pc)) {
            
        }
        return pc;
    }

    @Override
    public void uplinkCommand(PreparedCommand pc) throws IOException {
        this.addVerifierFromOption(pc);
        System.err.println(pc.getMetaCommand().hasCommandVerifiers());
        super.uplinkCommand(pc);
    }

}
