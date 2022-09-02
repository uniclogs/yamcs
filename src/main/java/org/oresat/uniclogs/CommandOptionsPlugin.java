package org.oresat.uniclogs;

import org.yamcs.CommandOption;
import org.yamcs.Plugin;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.CommandOption.CommandOptionType;
import org.yamcs.xtce.CheckWindow;
import org.yamcs.xtce.CommandVerifier;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.SequenceContainer;
import org.yamcs.xtce.XtceDb;
import org.yamcs.xtce.CommandVerifier.TerminationAction;

public class CommandOptionsPlugin implements Plugin {

    static final String INSTANCE_NAME = "oresat0";
    
    @Override
    public void onLoad(YConfiguration config) {
        YamcsServer yamcs = YamcsServer.getServer();
        yamcs.addCommandOption(new CommandOption("requiresResponse", "Command Requires a Response", CommandOptionType.BOOLEAN));
        XtceDb db = yamcs.getInstance(INSTANCE_NAME).getXtceDb();
        db.getMetaCommands().forEach(cmd -> {
            addVerifier(cmd, db.getRootSequenceContainer());
        });
    }

    private MetaCommand addVerifier(MetaCommand cmd, SequenceContainer cont) {
        CommandVerifier cv = new CommandVerifier(CommandVerifier.Type.CONTAINER, "Response");
        cv.setContainerRef(cont);
        CheckWindow cw = new CheckWindow(0, 10000, CheckWindow.TimeWindowIsRelativeToType.LAST_VERIFIER);
        cv.setCheckWindow(cw);
        cv.setOnTimeout(TerminationAction.FAIL);
        cmd.addVerifier(cv);
        cmd.getTransmissionConstraintList().forEach(System.err::println);
        return cmd;
    }
}
