package org.oresat.uniclogs.services;

import org.yamcs.AbstractYamcsService;
import org.yamcs.InitException;
import org.yamcs.YConfiguration;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.yarch.Bucket;
import org.yamcs.yarch.YarchDatabase;
import org.yamcs.yarch.YarchDatabaseInstance;
import java.util.HashSet;
import java.util.Set;

import java.io.IOException;

public class SatnogsTransferService extends AbstractYamcsService {
    static final String sequenceNumberId = "seqNum";
    static final String hmacKeyId = "hmacKey";
    String hmacEnvVar;
    byte[] hmacKey;
    Integer seqNum;
    Bucket db;

    private byte[] loadHmacFromEnv(String varName) {
        return System.getenv(varName).getBytes();
    }

    private Set<String> processedData;
    // processedData set keeps track of unique data

    @Override
    protected void doStart() {
        try {
            this.hmacKey = this.loadHmacKey();
            this.seqNum = this.loadSeqNum();
            this.log.info("Loaded Sequence Number: " + this.seqNum);
            processedData = new HashSet<>();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        this.notifyStarted();
    }

    @Override
    protected void doStop() {
        try {
            this.saveSeqNum(this.seqNum);
            this.saveHmacKey(this.hmacKey);
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        this.notifyStopped();
    }

    public Integer getSeqNum() {
        Integer num = this.seqNum;
        this.seqNum++;
        return num;
    }

    public Boolean getDeduplication(String contentArtifactsSet) {
        // Check if the data is unique (not already processed)
        if (!processedData.contains(contentArtifactsSet)){
            System.out.println("Processing unique data: " + contentArtifactsSet);
            // Add the processed data to the set
            this.log.info("DB Satnogs Artifacts: " + processedData.add(contentArtifactsSet));
            return true;
        } else {
            System.out.println("Duplicate data received: " + contentArtifactsSet);
            // Handle duplicate data (If the text is present, it won't be added and the method returns false)
            return false;
        }
    }

    public byte[] getHmacKey() {
        return this.hmacKey;
    }

    @Override
    public void init(String yamcsInstance, String serviceName, YConfiguration config) throws InitException {
        super.init(yamcsInstance, serviceName, config);
        this.hmacEnvVar = config.getString("hmacEnvVar");
        log.info(this.hmacEnvVar);
        YarchDatabaseInstance instanceDb = YarchDatabase.getInstance(yamcsInstance);
        try {
            this.db = instanceDb.getBucket("env");
            if (this.db == null) {
                this.log.info(String.format("Env not found for %s, creating new env...", yamcsInstance));
                this.db = instanceDb.createBucket("env");
            }
        } catch (IOException e) {
            throw new InitException(e.getMessage());
        }
    }

    private Integer loadSeqNum() throws IOException {
        byte[] numBytes = db.getObject(sequenceNumberId);
        if (numBytes == null) {
            this.log.info(String.format("Sequence Number not found for %s, Sequence Number set to 1", yamcsInstance));
            this.saveSeqNum(1);
            return 1;
        }
        return ByteArrayUtils.decodeInt(db.getObject(sequenceNumberId), 0);
    }

    private byte[] loadHmacKey() throws IOException {
        byte[] hmac = this.db.getObject(hmacKeyId);
        if (hmac == null) {
            this.log.info(String.format("Hmac Key not in %s. Loading from env var: %s", yamcsInstance, this.hmacEnvVar));
            hmac = this.loadHmacFromEnv(this.hmacEnvVar);
            this.saveHmacKey(hmac);
        }
        return hmac;
    }

    private void saveSeqNum(Integer seq) throws IOException {
        db.putObject(sequenceNumberId, "Integer", null, ByteArrayUtils.encodeInt(seq));
    }

    private void saveHmacKey(byte[] hmacKey) throws IOException {
        db.putObject(hmacKeyId, "bytes", null, hmacKey);
    }
}