package org.uniclogs.yamcs.security.sdls;

// Everything here is imported by the example implementation.
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamcs.memento.MementoDb;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.security.sdls.IvSeqNum;
import org.yamcs.security.sdls.SdlsMemento;
import org.yamcs.security.sdls.SdlsSecurityAssociation;



/**
 * Implementation of how oresat handles SDLS.
 * 
 * Fun issue with the current implementation:
 * Yamcs expects each vcid to have its own sequence number.
 * Oresat expects each vcid to share the same global sequence number.
 * 
 * I've currently fixed this by just readig and writing the sequence number before and after each use.
 * A better implementation should be created later, potentially removing the sequence number entirely
 * and just using properties, if that is a thing in java.
 */
public class OresatSdlsSecurityAssociation implements SdlsSecurityAssociation {
    private static final String MAC_ALG = "HmacSHA3-256";
    private static final int MAC_LEN_BITS = 256;
    private static final int SEQ_NUM_LEN_BYTES = 4;
    
    private final short spi;
    private final int seqNumWindow;
    private final boolean verifySeqNum;
    private IvSeqNum seqNum;
    private SecretKey secretKey;
    private boolean skipVerifyingNextSeqNum = false;
	

	private final String instanceName;
    private final String linkName;
    private static final Logger log = LoggerFactory.getLogger(OresatSdlsSecurityAssociation.class);

    /**
     * @param key                the 256-bit key used for encryption/decryption
     * @param spi                the security parameter index, shared between sender and receiver.
     * @param seqNumWindow       a positive integer; only frames whose sequence number differs by this integer at
     *                           maximum will be accepted.
     * @param verifySeqNum       whether to verify the received anti-replay sequence number based on the seqNumWindow.
     * @param initialSeqNumBytes if no value is found in the Mememto DB for the initial sequence number, then use this
     *                           one. Can be null.
     */
	public OresatSdlsSecurityAssociation(String instanceName, String linkName, byte[] key, short spi,
                                           byte[] initialSeqNumBytes, int seqNumWindow, boolean verifySeqNum) {
        this.instanceName = instanceName;
        this.linkName = linkName;
        this.spi = spi;
        this.secretKey = new SecretKeySpec(key, MAC_ALG);

        // If we have information to retrieve a persisted sequence number, do so
        if (instanceName != null) { // oresat doesn't use a seperate sequence number per VCID, so we ignore link name.
            this.seqNum = loadSeqNum();
            log.warn("Trying to load sequence number for instance name {}, got {}.", instanceName, this.seqNum);
        }
        // if we did not find a sequence number used the initial one if set
        if (this.seqNum == null) {
            log.warn("could not find seqnum: instance name {}.", instanceName);
            if (initialSeqNumBytes != null) {
                this.seqNum = IvSeqNum.fromBytes(initialSeqNumBytes, SEQ_NUM_LEN_BYTES);
            } else {// if not set, just start from 0 but on the downlink do not verify the first frame
                this.seqNum = new IvSeqNum(SEQ_NUM_LEN_BYTES);
                skipVerifyingNextSeqNum = true;
            }
        }
        this.seqNumWindow = Math.abs(seqNumWindow); // just to ensure it's not negative
        this.verifySeqNum = verifySeqNum;
    }

    /**
     * @return Size of security header in bytes
     */
    @Override
    public int getHeaderSize() {
        // 16-bit SPI + size of Seqnum
        return 6;
    }

    /**
     * @return Size of security trailer in bytes
     */
    @Override
    public int getTrailerSize() {
        // Length of the MAC in bytes.
        return (MAC_LEN_BITS / 8);
    }

    @Override
    public int getKeyLenBits() {
        return 256;
    }

    @Override
    public String getAlgorithm() {
        return "HmacSHA3-256";
    }

    @Override
    public byte[] securityHdrAuthMask() {
        byte[] authMask = new byte[6];
        // Authenticate SPI and sequence number.
		for (int i = 0; i < 6; i++) {
			authMask[i] = (byte) 0xff;
		}
        return authMask;
    }

	/**
	 * Returns the data that should be checked against the HMAC.
	 */
	private static byte[] computeAD(byte[] buffer, int frameStart, int dataEnd, byte[] authMask) {
        // Create AD buffer for auth mask
        byte[] ad = new byte[dataEnd - frameStart];

        // Apply auth mask to frame
		int i;
        for (i = 0; i < authMask.length; ++i) {
            ad[i] = (byte) (buffer[frameStart + i] & authMask[i]);
        }
		for (;i < dataEnd - frameStart; i++) {
			ad[i] = buffer[frameStart + i];
		}

        return ad;
    }

    /**
     * @return Get the sequence number (IV) as bytes
     */
    byte[] getSeqNumBytes() {
        return seqNum.toBytes(SEQ_NUM_LEN_BYTES);
    }

	// TODO: make sure these work out well.
    /**
     * Load a persisted sequence number, defaulting to zero.
     *
     * @return the sequence number for the next frame to send, or null if not found
     */
    IvSeqNum loadSeqNum() {
        MementoDb mementoDb = MementoDb.getInstance(instanceName);
        return mementoDb.getObject(SdlsMemento.MEMENTO_KEY, SdlsMemento.class) // due to how oresat handles things, I'm changing linkname to instanceName.
                .map(memento -> memento.getSeqNum(instanceName, spi)).orElse(null); // eventually, OresatMemento should be created to clean this.
    }

    /**
     * Save the current sequence number to the database
     */
    void persistSeqNum() {
        MementoDb mementoDb = MementoDb.getInstance(instanceName);
        SdlsMemento memento = mementoDb.getObject(SdlsMemento.MEMENTO_KEY, SdlsMemento.class)
                .orElse(new SdlsMemento());
        memento.saveSeqNum(instanceName, spi, seqNum); // due to how oresat handles things, I'm changing linkname to instanceName.
        mementoDb.putObject(SdlsMemento.MEMENTO_KEY, memento); // eventually, OresatMemento should be created to clean this.
    }

    /**
     * Encrypt the provided trasferFrame and authenticate data.
     * <p>
     * The partialAuthMask has to cover from the beginning of the frame until the start of the security header. If it is
     * larger, the last bytes will not be used.
     *
     * @param buffer         The full transfer frame, including empty security header and trailer
     * @param frameStart     The first byte of the frame in the buffer
     * @param secHeaderStart The offset of the security header
     * @param secTrailerEnd  First byte following the security trailer
     * @param authMask 		 Auth Mask used to authenticate everything up to the data zone. Doesn't look like it contains 
	 * 						 	the data zone, so that should be forced to be included as well. It looks like this does
	 * 							not comply with CCSDS SDLS 4.2.2.6.2 i
     * @throws GeneralSecurityException if encryption fails
     */
    @Override
    public void applySecurity(byte[] buffer, int frameStart, int secHeaderStart, int secTrailerEnd,
                              byte[] authMask) throws GeneralSecurityException {
        if (instanceName != null) {
            loadSeqNum();
        }
        byte[] seqNumBytes = getSeqNumBytes();
		
		// Fill security header
        // first two bytes are SPI
        ByteArrayUtils.encodeUnsignedShort(spi, buffer, secHeaderStart);
        // the next are IV
        System.arraycopy(seqNumBytes, 0, buffer, secHeaderStart + 2, seqNumBytes.length);
        // and increment the sequence number
        seqNum.increment();
        if (instanceName != null) {
            persistSeqNum();
        }

        byte[] ad = computeAD(buffer, frameStart, secTrailerEnd - 32, authMask);
		byte[] hmac = genHmac(ad);

		System.arraycopy(hmac, 0, buffer, secTrailerEnd - 32, MAC_LEN_BITS / 8);
    }


    /**
     * Verify and decrypt a transferFrame.
     *
     * <p>
     * This function knows the size of the security header and trailer and those sizes are used to find the data start
     * and data end.
     * <p>
     * For MAC, this function authenticates data in `buffer` from `frameStart`, after applying `authMask` in a
     * bitwise-AND. The length of the authenticated data is equal to the length of the `authMask`.
     *
     * @param buffer         the buffer containing the transfer framez
     * @param frameStart     the index of the first byte of the transfer frame in the buffer
     * @param secHeaderStart index of the first byte of security header
     * @param secTrailerEnd  index of the first byte after the security trailer
     * @param authMask       Auth Mask used to authenticate everything up to the data zone. Doesn't look like it contains 
	 * 						 	the data zone, so that should be forced to be included as well. It looks like this does
	 * 							not comply with CCSDS SDLS 4.2.2.6.2 i
     * @return a code indicating the verification/decryption status
     *
     */
    public VerificationStatusCode processSecurity(byte[] buffer, int frameStart, int secHeaderStart,
                                                                                  int secTrailerEnd, byte[] authMask) {
        // Read security header
        // first two bytes are SPI
        short receivedSpi = (short) ByteArrayUtils.decodeUnsignedShort(buffer, secHeaderStart);

        // Check that the received SPI is the SPI for this SA
        if (receivedSpi != spi) {
            log.warn("Expected SPI {}, received SPI {}", spi, receivedSpi);
            return VerificationStatusCode.InvalidSPI;
        }

        // Next bytes of the header are SeqNum
        byte[] receivedSeqNumBytes = new byte[SEQ_NUM_LEN_BYTES];
        System.arraycopy(buffer, secHeaderStart + 2, receivedSeqNumBytes, 0, SEQ_NUM_LEN_BYTES);

        byte[] ad = computeAD(buffer, frameStart, secTrailerEnd - 32, authMask);

		byte[] expectedHmac = genHmac(ad);
		byte[] actHmac = new byte[MAC_LEN_BITS / 8];
        System.arraycopy(buffer, secTrailerEnd - 32, actHmac, 0, MAC_LEN_BITS / 8);
		if (!compByteArray(expectedHmac, actHmac)) {
			return VerificationStatusCode.MacVerificationFailure;
		}

        // Check the sequence number
		
        IvSeqNum receivedSeqNum = IvSeqNum.fromBytes(receivedSeqNumBytes, SEQ_NUM_LEN_BYTES);

        if (instanceName != null) {
            loadSeqNum();
        }
        if (skipVerifyingNextSeqNum) {
            skipVerifyingNextSeqNum = false;
        } else if (verifySeqNum && !seqNum.verifyInWindow(receivedSeqNum, seqNumWindow)) {
            return VerificationStatusCode.AntiReplaySequenceNumberFailure;
        }
        seqNum = receivedSeqNum;
        // Save the last received seq num
        if (instanceName != null) {
            persistSeqNum();
        }

        // Zero the sec header and sec trailer
		int dataStart = secHeaderStart + getHeaderSize();
        Arrays.fill(buffer, dataStart - getHeaderSize(), dataStart, (byte) 0);
        Arrays.fill(buffer, secTrailerEnd - getTrailerSize(), secTrailerEnd, (byte) 0);

        log.debug("Processed security SPI {}, seq num {}", receivedSpi, seqNum);
        return VerificationStatusCode.NoFailure;
    }


    /**
     * Do not verify the sequence number for the next received frame
     */
    @Override
    public void skipVerifyingNextSeqNum() {
        skipVerifyingNextSeqNum = true;
    }

    /**
	 * Modified from CfdpVc1FrameCodec, written by Tim Pupkiewicz.
	 */
	private byte[] genHmac(byte[] msg) {
        try {
            Mac mac = Mac.getInstance(MAC_ALG);
            mac.init(this.secretKey);
            return mac.doFinal(msg);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(MAC_ALG + " unavailable", e);
        }
    }

	/**
	 * Taken from CfdpVc1FrameCodec, written by Tim Pupkiewicz.
	 */
	private static boolean compByteArray(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
	}


    /* Methods used by HTTP API */

    /**
     * Update the secret key
     *
     * @param secretKey a 256-bit key to be used by AES-GCM
     */
    @Override
    public void setSecretKey(byte[] secretKey) {
        this.secretKey = new SecretKeySpec(secretKey, MAC_ALG);
    }

    /**
     * Get the current sequence number
     *
     * @return the current sequence number in big-endian order
     */
    @Override
    public byte[] getSeqNum() {
        return getSeqNumBytes();
    }

    /**
     * Reset the anti-replay sequence number
     *
     * @param newSeqNum the bytes of the new sequence number, in big-endian order
     */
    @Override
    public void setSeqNum(byte[] newSeqNum) {
        this.seqNum = IvSeqNum.fromBytes(newSeqNum, SEQ_NUM_LEN_BYTES);
    }
}