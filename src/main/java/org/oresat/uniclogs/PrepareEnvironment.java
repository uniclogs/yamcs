package org.oresat.uniclogs;

import org.yamcs.ReadyListener;
import org.yamcs.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class PrepareEnvironment implements ReadyListener {
    private final static Log LOG = new Log(PrepareEnvironment.class);

    private static String HMAC_SECRET = null;
    private static Integer SERIAL_NUMBER = null;
    private static String secretPath = UniclogsServer.getCacheDir() + "/secret";
    private static String snPath = UniclogsServer.getCacheDir() + "/serial-number";

    public PrepareEnvironment() {}

    public static String getHmacSecret() {
        return HMAC_SECRET;
    }

    public static Integer getSerialNumber() {
        return SERIAL_NUMBER;
    }

    private static String loadOrDefault(String filePath, String defaultValue) {
        File file = new File(filePath);
        if(!file.exists()) {
            FileWriter writer;
            try {
                writer = new FileWriter(filePath);
                writer.write(defaultValue);
                writer.flush();
                return defaultValue;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        try{
            Scanner in = new Scanner(new File(filePath));
            return in.next();
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    private static String loadHmacSecret() {
        return loadOrDefault(secretPath, "<Change This To A Super Special Secret>");
    }

    private static Integer loadSerialNumber() {
        String serialNumber = loadOrDefault(snPath, "-1");
        if(serialNumber == null) {
            return null;
        }
        return Integer.parseInt(serialNumber);
    }

    @Override
    public void onReady() {
        boolean quit = false;
        HMAC_SECRET = loadHmacSecret();
        LOG.info("SECRET: " + getHmacSecret());
        if(HMAC_SECRET.equals("<Change This To A Super Special Secret>")) {
            LOG.error("DEFAULT SECRET DETECTED! Please update the file: " + secretPath + " with a secret before re-running Yamcs!");
            quit = true;
        }

        SERIAL_NUMBER = loadSerialNumber();
        LOG.info("SERIAL NUMBER: " + SERIAL_NUMBER.toString());
        if(SERIAL_NUMBER == null) {
            LOG.error("DEFAULT SERIAL NUMBER! Please update the file: " + secretPath + " with a serial-number re-running Yamcs!");
            quit = true;
        }

        if(quit) {
            System.exit(-1);
        }
    }
}
