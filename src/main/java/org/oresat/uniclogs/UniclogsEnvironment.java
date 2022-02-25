package org.oresat.uniclogs;

import org.yamcs.ReadyListener;
import org.yamcs.logging.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class UniclogsEnvironment implements ReadyListener {
    private final static Log LOG = new Log(UniclogsEnvironment.class);

    private static String HMAC_SECRET = null;
    private static Integer SEQUENCE_NUMBER = null;
    private static String secretPath = UniclogsServer.getCacheDir() + "/secret";
    private static String snPath = UniclogsServer.getCacheDir() + "/sequence-number";

    public UniclogsEnvironment() {}

    public static String getHmacSecret() {
        return HMAC_SECRET;
    }

    public static Integer getSequenceNumber() {
        return SEQUENCE_NUMBER;
    }

    public static void setSequenceNumber(Integer sequenceNumber) {
        SEQUENCE_NUMBER = sequenceNumber;
        dumpSequenceNumber(SEQUENCE_NUMBER);
    }

    public static void incrementSequenceNumber() {
        setSequenceNumber(SEQUENCE_NUMBER + 1);
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

    private static Integer loadSequenceNumber() {
        String serialNumber = loadOrDefault(snPath, "0");
        if(serialNumber == null) {
            return null;
        }
        return Integer.parseInt(serialNumber);
    }

    private static void dumpSequenceNumber(Integer sequenceNumber) {
        FileWriter writer;
        try{
            writer = new FileWriter(snPath);
            writer.write(sequenceNumber.toString());
            writer.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void onReady() {
        boolean quit = false;
        HMAC_SECRET = loadHmacSecret();
        if(HMAC_SECRET.equals("<Change This To A Super Special Secret>")) {
            LOG.error("DEFAULT SECRET DETECTED! Please update the file: " + secretPath + " with a secret before re-running Yamcs!");
            quit = true;
        }

        SEQUENCE_NUMBER = loadSequenceNumber();

        if(quit) {
            System.exit(-1);
        }
        LOG.info("Loaded last-known sequence number: " + SEQUENCE_NUMBER);
    }
}
