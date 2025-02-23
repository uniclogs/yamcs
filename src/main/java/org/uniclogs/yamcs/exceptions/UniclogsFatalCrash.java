package org.uniclogs.yamcs.exceptions;

import org.uniclogs.services.UniclogsServer;
import org.yamcs.CrashHandler;
import org.yamcs.logging.Log;

public class UniclogsFatalCrash implements CrashHandler {
    private final static Log LOG = new Log(UniclogsFatalCrash.class);

    @Override
    public void handleCrash(String type, String message) {
        LOG.error(type + ": " + message);
        UniclogsServer.getServerInstance().shutDown();
        System.exit(-1);
    }
}
