package org.oresat.uniclogs;


import org.yamcs.InitException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.http.HttpServer;
import org.yamcs.logging.Log;

import java.nio.file.Path;

public class UniclogsServer extends HttpServer {
    private final static Log LOG = new Log(UniclogsServer.class);
    private final static YamcsServer server = YamcsServer.getServer();

    @Override
    public void init(String instanceName, String serviceName, YConfiguration config) throws InitException {
        // Call the parent server init
        super.init(instanceName, serviceName, config);
        LOG.info("Started " + serviceName + ":" + instanceName + " with config: " + config + " from: " + server.getConfigDirectory());

        // Add listeners
        server.addReadyListener(new UniclogsEnvironment());
    }

    public static Path getDataDirectory() {
        return server.getDataDirectory().toAbsolutePath();
    }

    public static final YamcsServer getServerInstance() {
        return server;
    }
}