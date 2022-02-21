package org.oresat.uniclogs;


import org.yamcs.InitException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.http.HttpServer;
import org.yamcs.logging.Log;

public class UniclogsServer extends HttpServer {
    private final static Log LOG = new Log(UniclogsServer.class);
    private final static YamcsServer server = YamcsServer.getServer();

    private static String HMAC_SECRET = null;

    @Override
    public void init(String instanceName, String serviceName, YConfiguration config) throws InitException {
        // Call the parent server init
        super.init(instanceName, serviceName, config);
        LOG.info("Started " + serviceName + ":" + instanceName + " with config: " + config + " from: " + config.configDirectory);

        // Add listeners
        server.addReadyListener(new PrepareEnvironment());

        // Fetch the necessary Environment Variables
        HMAC_SECRET = PrepareEnvironment.getHmacSecret();
    }

    public static String getHmacSecret() {
        return HMAC_SECRET;
    }
}