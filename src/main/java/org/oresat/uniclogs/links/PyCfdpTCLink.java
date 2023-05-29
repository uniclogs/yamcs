package org.oresat.uniclogs.links;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamcs.YConfiguration;
import org.yamcs.commanding.ArgumentValue;
import org.yamcs.commanding.PreparedCommand;
import org.yamcs.tctm.AbstractTcDataLink;
import org.yamcs.xtce.Argument;

public class PyCfdpTCLink extends AbstractTcDataLink {
    private String cmdString = null;
    private Status status = Status.UNAVAIL;

    static Logger log = LoggerFactory.getLogger(PyCfdpTCLink.class);

    public void init(String yamcsInstance, String linkName, YConfiguration config) {
        // Do any init at yamcs bootup time here
        super.init(yamcsInstance, linkName, config);
        log.info(config.toString());
        if (config.containsKey("args")) {
            // get dictionary args
            Map<String, Object> args = config.getMap("args");
            if (args.containsKey("pythonCmd")) {
                this.cmdString = (String) args.get("pythonCmd");
            } else {
                throw new IllegalArgumentException("pythonCmd not found in args");
            }
        } else {
            throw new IllegalArgumentException("args not provided in configuration");
        }

        log.info("Attempting to initialize post processor");
        initPostprocessor(yamcsInstance, config);

        // Put the link in an OK state
        this.status = Status.OK;

    }

    @Override
    protected Status connectionStatus() {
        return this.status;
    }

    @Override
    protected void doStart() {
        // Put the link in an OK state
        this.status = Status.OK;
        this.notifyStarted();
    }

    @Override
    protected void doStop() {
        // Do any other cleanup here
        this.status = Status.DISABLED;
        this.notifyStopped();
    }

    @Override
    public boolean sendCommand(PreparedCommand cmd) {
        Map<Argument, ArgumentValue> args = cmd.getArgAssignment();
        log.info(args.toString());
        return true;
//        try {
//            // Process TC command args
//            Map<Argument, ArgumentValue> args = cmd.getArgAssignment();
//            // Do stuff
//
//            // log the args to info output
//            this.log.info(args.toString());
//
//            // This structure may change depending on what type of proc the shell function
//            // is (e.g. forking, simple, notif, etc.)
//            // but a (Tc|Tm)DataLink is like a constantly running service, so you'll want to
//            // either start a thread to deal
//            // with the shell command here, or just do it in here if it's small enough.
//
//            // Attempt to run the script
//            //Process p = Runtime.getRuntime().exec(this.cmdString).onExit().get();
//            //BufferedReader stdout = p.inputReader();
//            //String output = stdout.readLine();
//            //this.log.info(output); // Do something with stdout
//        } catch (InterruptedException | IOException | ExecutionException e) {
//            // Something somewhere failed, so notify yamcs
//            // that this link stopped if the shell command stopped
//            // and put the link in a failed state
//            this.status = Status.FAILED;
//            e.printStackTrace();
//            this.notifyFailed(e);
//            return false;
//        }
//        return true;
    }

}