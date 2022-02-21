package org.oresat.uniclogs;

import org.yamcs.ReadyListener;
import org.yamcs.logging.Log;

import java.io.File;

public class PrepareEnvironment implements ReadyListener {
    private class FailedToPrepareEnvironmentException extends RuntimeException {
        public FailedToPrepareEnvironmentException(String message) {
            super(message);
        }
    }

    private final static String HMAC_SECRET_SYMBOL = "YAMCS_HMAC_SECRET";
    private static String HMAC_SECRET = null;
    private final static File LOG_DIR = new File(System.getProperty("user.home") + "/.cache/yamcs");
    private final static Log LOG = new Log(PrepareEnvironment.class);

    public PrepareEnvironment() {}

    public static String getHmacSecret() {
        return HMAC_SECRET;
    }

    @Override
    public void onReady() {
        LOG_DIR.mkdirs();
        LOG.info("Created log directory at: " + LOG_DIR);

        HMAC_SECRET = System.getenv(HMAC_SECRET_SYMBOL);
        if(HMAC_SECRET == null) {
            throw new FailedToPrepareEnvironmentException("Environment variable: `" + HMAC_SECRET_SYMBOL + "` is required but undefined!\n\tPlease define it in a bashrc or global environment file before re-running Yamcs\n\tEx: `export " + HMAC_SECRET_SYMBOL + "=<Some kind of secret>`");
        }
    }
}
