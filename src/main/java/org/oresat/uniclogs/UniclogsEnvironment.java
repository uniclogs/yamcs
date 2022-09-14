package org.oresat.uniclogs;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.yamcs.ReadyListener;
import org.yamcs.YamcsServer;
import org.yamcs.logging.Log;
import org.yamcs.yarch.YarchDatabaseInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UniclogsEnvironment implements ReadyListener {
    private final static Log LOG = new Log(UniclogsEnvironment.class);
    YarchDatabaseInstance db;

    private static byte[] HMAC_SECRET = {0x00, 0x01};
    private static Integer SEQUENCE_NUMBER = 1;
    private static Path secretPath = Paths.get(UniclogsServer.getDataDirectory() + "/secret");
    private static Path sequenceNumberPath = Paths.get(UniclogsServer.getDataDirectory() + "/sequence-number");

    public UniclogsEnvironment() {
    }

    public static byte[] getHmacSecret() {
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

    private static void makeParent(Path path) {
        path.getParent().toAbsolutePath().toFile().mkdirs();
    }

    private static String loadOrDefault(Path path, String defaultValue) {
        File file = path.toFile();
        LOG.debug("File `" + path + "` exists: " + file.exists());
        if(!file.exists()) {
            FileWriter writer;
            try {
                writer = new FileWriter(file);
                writer.write(defaultValue);
                writer.flush();
                return defaultValue;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        try{
            Scanner in = new Scanner(path.toFile());
            return in.next();
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    private static byte[] loadHmacSecret() {
        String hexString = loadOrDefault(secretPath, "0A0B0C0D");
        try {
            return Hex.decodeHex(hexString);
        } catch (DecoderException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    private static Integer loadSequenceNumber() {
        String serialNumber = loadOrDefault(sequenceNumberPath, "0");
        if(serialNumber == null) {
            return null;
        }
        return Integer.parseInt(serialNumber);
    }

    private static void dumpSequenceNumber(Integer sequenceNumber) {
        FileWriter writer;
        try{
            writer = new FileWriter(sequenceNumberPath.toFile());
            writer.write(sequenceNumber.toString());
            writer.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void onReady() {
        boolean quit = false;

        LOG.info("Using Data Dir: " + UniclogsServer.getDataDirectory());
        makeParent(secretPath);
        HMAC_SECRET = loadHmacSecret();
        if(HMAC_SECRET.equals("<Change This To A Super Special Secret>")) {
            LOG.error("DEFAULT SECRET DETECTED! Please update the file: " + secretPath + " with a secret before re-running Yamcs!");
            quit = true;
        }

        makeParent(sequenceNumberPath);
        SEQUENCE_NUMBER = loadSequenceNumber();
        if(quit) {
            System.exit(-1);
        }
        LOG.info("Loaded last-known sequence number: " + SEQUENCE_NUMBER);
    }
}
