package org.uniclogs.yamcs.tctm;

import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class CfdpVc1FrameCodec {
    public static final int HMAC_LEN = 32;
    public static final int FECF_LEN = 2;
    public static final int PRIMARY_HEADER_LEN = 7;
    public static final int INSERT_ZONE_LEN = 4;
    public static final int TFDF_HEADER_LEN = 1;
    public static final int TC_MIN_LEN =
            PRIMARY_HEADER_LEN + INSERT_ZONE_LEN + TFDF_HEADER_LEN + HMAC_LEN + FECF_LEN; // 46
    private static final String MAC_ALG = "HmacSHA3-256";
    public static final int DEFAULT_SCID = 0x4F53;
    public static final int DEFAULT_VCID = 1;
    // TFDF header: VpNoSegmentation (0b111 << 5) | MISSION_SPECIFIC_INFO_1_MAPA_SDU (0b00101) = 0xE5
    public static final byte TFDF_HEADER_BYTE = (byte) 0xE5;

    private CfdpVc1FrameCodec() {}

    public static byte[] wrap(byte[] pdu, byte[] hmacKey) {
        byte[] mac = hmac(hmacKey, pdu);
        byte[] out = new byte[pdu.length + HMAC_LEN];
        System.arraycopy(pdu, 0, out, 0, pdu.length);
        System.arraycopy(mac, 0, out, pdu.length, HMAC_LEN);
        return out;
    }

    public static byte[] unwrap(byte[] tfdz, byte[] hmacKey, boolean verify) {
        if (tfdz.length < HMAC_LEN) {
            throw new IllegalArgumentException("TFDZ shorter than HMAC: " + tfdz.length);
        }
        int pduLen = tfdz.length - HMAC_LEN;
        byte[] pdu = new byte[pduLen];
        System.arraycopy(tfdz, 0, pdu, 0, pduLen);
        if (verify) {
            byte[] received = new byte[HMAC_LEN];
            System.arraycopy(tfdz, pduLen, received, 0, HMAC_LEN);
            byte[] expected = hmac(hmacKey, pdu);
            if (!constantTimeEquals(received, expected)) {
                throw new SecurityException("HMAC mismatch");
            }
        }
        return pdu;
    }

    public static byte[] hmac(byte[] key, byte[] msg) {
        try {
            Mac mac = Mac.getInstance(MAC_ALG);
            mac.init(new SecretKeySpec(key, MAC_ALG));
            return mac.doFinal(msg);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(MAC_ALG + " unavailable", e);
        }
    }

    /**
     * Pack a full USLP VC=1 TC frame: primary header + insert-zone seq-num + TFDF header +
     * pdu + HMAC-SHA3-256 + CRC16 FECF. Matches oresat_c3/protocols/edl_packet.py format.
     */
    public static byte[] packUslpFrame(byte[] pdu, byte[] hmacKey, int seqNum, int scid, int vcid) {
        int totalLen = TC_MIN_LEN + pdu.length;
        byte[] frame = new byte[totalLen];

        // Primary header (7 bytes):
        //   byte 0: TFVN(4)=0b1100 | SCID[15:12]
        //   byte 1: SCID[11:4]
        //   byte 2: SCID[3:0] | src_dest(1)=0 SOURCE | VCID[5:3]
        //   byte 3: VCID[2:0] | MAP_ID(4)=0 | eof_primary_hdr(1)=0
        //   bytes 4-5: frame_length BE  (= totalLen - 1)
        //   byte 6: bypass/protocol flags | vcf_count_len=0
        frame[0] = (byte) (0xC0 | ((scid >> 12) & 0x0F));
        frame[1] = (byte) ((scid >> 4) & 0xFF);
        frame[2] = (byte) (((scid & 0x0F) << 4) /* src_dest=0 */ | ((vcid >> 3) & 0x07));
        frame[3] = (byte) (((vcid & 0x07) << 5) /* map=0, eof=0 */);
        int frameLenField = totalLen - 1;
        frame[4] = (byte) ((frameLenField >> 8) & 0xFF);
        frame[5] = (byte) (frameLenField & 0xFF);
        frame[6] = 0x00;

        // Insert zone: 4-byte seq num little-endian
        frame[7] = (byte) (seqNum & 0xFF);
        frame[8] = (byte) ((seqNum >> 8) & 0xFF);
        frame[9] = (byte) ((seqNum >> 16) & 0xFF);
        frame[10] = (byte) ((seqNum >> 24) & 0xFF);

        // TFDF header
        frame[11] = TFDF_HEADER_BYTE;

        // TFDZ: pdu || HMAC
        System.arraycopy(pdu, 0, frame, 12, pdu.length);
        byte[] mac = hmac(hmacKey, pdu);
        System.arraycopy(mac, 0, frame, 12 + pdu.length, HMAC_LEN);

        // FECF: CRC16-CCITT (poly 0x1021, init 0) over frame[0 .. totalLen-3], little-endian
        int crc = crc16Ccitt(frame, 0, totalLen - FECF_LEN, 0);
        frame[totalLen - 2] = (byte) (crc & 0xFF);
        frame[totalLen - 1] = (byte) ((crc >> 8) & 0xFF);
        return frame;
    }

    public static int crc16Ccitt(byte[] data, int offset, int length, int init) {
        int crc = init & 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= (data[offset + i] & 0xFF) << 8;
            for (int b = 0; b < 8; b++) {
                crc = ((crc & 0x8000) != 0) ? ((crc << 1) ^ 0x1021) : (crc << 1);
                crc &= 0xFFFF;
            }
        }
        return crc;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
