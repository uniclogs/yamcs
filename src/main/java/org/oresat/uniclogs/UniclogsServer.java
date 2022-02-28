package org.oresat.uniclogs;


import org.yamcs.InitException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.http.HttpServer;
import org.yamcs.logging.Log;

import java.io.File;
import java.nio.file.Path;

public class UniclogsServer extends HttpServer {
    private final static Log LOG = new Log(UniclogsServer.class);
    private final static YamcsServer server = YamcsServer.getServer();

    @Override
    public void init(String instanceName, String serviceName, YConfiguration config) throws InitException {
        // Call the parent server init
        super.init(instanceName, serviceName, config);
        LOG.info("Started " + serviceName + ":" + instanceName + " with config: " + config + " from: " + config.configDirectory);

        // Add listeners
        server.addReadyListener(new UniclogsEnvironment());
    }

    public static final YamcsServer getServerInstance() {
        return server;
    }

    public static Path getCacheDir() {
        return new File(System.getProperty("user.home") + "/.cache/yamcs").toPath().toAbsolutePath();
    }
}