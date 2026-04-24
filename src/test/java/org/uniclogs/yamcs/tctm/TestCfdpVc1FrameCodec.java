package org.uniclogs.yamcs.tctm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.yamcs.utils.StringConverter;

public class TestCfdpVc1FrameCodec {

    // Pinned against Python: hmac.digest(key, msg, hashlib.sha3_256)
    // where key = 00..1f (32 bytes), msg = deadbeefcafef00d
    private static final byte[] PINNED_KEY = StringConverter.hexStringToArray(
            "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
    private static final byte[] PINNED_MSG = StringConverter.hexStringToArray("deadbeefcafef00d");
    private static final byte[] PINNED_MAC = StringConverter.hexStringToArray(
            "a484bfffca5dc8a3b174408a1435367e3b670b0c2f50a3affe6d44dc98789c8a");

    @Test
    void hmacMatchesPythonReference() {
        byte[] mac = CfdpVc1FrameCodec.hmac(PINNED_KEY, PINNED_MSG);
        assertEquals(32, mac.length);
        assertArrayEquals(PINNED_MAC, mac);
    }

    @Test
    void wrapAppendsMacAfterPdu() {
        byte[] tfdz = CfdpVc1FrameCodec.wrap(PINNED_MSG, PINNED_KEY);
        assertEquals(PINNED_MSG.length + CfdpVc1FrameCodec.HMAC_LEN, tfdz.length);
        byte[] macSuffix = new byte[CfdpVc1FrameCodec.HMAC_LEN];
        System.arraycopy(tfdz, PINNED_MSG.length, macSuffix, 0, CfdpVc1FrameCodec.HMAC_LEN);
        assertArrayEquals(PINNED_MAC, macSuffix);
    }

    @Test
    void roundTripWithVerify() {
        byte[] pdu = StringConverter.hexStringToArray("0102030405");
        byte[] key = new byte[32];
        byte[] tfdz = CfdpVc1FrameCodec.wrap(pdu, key);
        byte[] recovered = CfdpVc1FrameCodec.unwrap(tfdz, key, true);
        assertArrayEquals(pdu, recovered);
    }

    @Test
    void unwrapRejectsTamperedPdu() {
        byte[] pdu = StringConverter.hexStringToArray("0102030405");
        byte[] key = new byte[32];
        byte[] tfdz = CfdpVc1FrameCodec.wrap(pdu, key);
        tfdz[0] ^= 0x01;
        assertThrows(SecurityException.class,
                () -> CfdpVc1FrameCodec.unwrap(tfdz, key, true));
    }

    @Test
    void unwrapSkipsVerifyWhenDisabled() {
        byte[] pdu = StringConverter.hexStringToArray("0102030405");
        byte[] key = new byte[32];
        byte[] tfdz = CfdpVc1FrameCodec.wrap(pdu, key);
        tfdz[0] ^= 0x01;
        byte[] recovered = CfdpVc1FrameCodec.unwrap(tfdz, key, false);
        byte[] expected = new byte[pdu.length];
        System.arraycopy(tfdz, 0, expected, 0, pdu.length);
        assertArrayEquals(expected, recovered);
    }

    @Test
    void unwrapRejectsTooShort() {
        byte[] tooShort = new byte[10];
        assertThrows(IllegalArgumentException.class,
                () -> CfdpVc1FrameCodec.unwrap(tooShort, new byte[32], false));
    }

    // Pinned against spacepackets + binascii.crc_hqx (C3's edl_packet.py format)
    // pdu = 0102030405, key as below, seqNum = 0x12345678, SCID 0x4F53, VCID 1
    @Test
    void packUslpFrameMatchesSpacepacketsOutput() {
        byte[] pdu = StringConverter.hexStringToArray("0102030405");
        byte[] expected = StringConverter.hexStringToArray(
                "c4f5302000320078563412e5"
                + "0102030405"
                + "e5ab77bfdb5f437989e61ee9523f0fe4b0b6805f3542f486846a725ae9c679cf"
                + "b5ef");
        byte[] actual = CfdpVc1FrameCodec.packUslpFrame(
                pdu, PINNED_KEY, 0x12345678, 0x4F53, 1);
        assertArrayEquals(expected, actual);
    }

    @Test
    void crc16CcittKnownVector() {
        // crc_hqx(b"123456789", 0) = 0x31C3
        byte[] msg = "123456789".getBytes();
        assertEquals(0x31C3, CfdpVc1FrameCodec.crc16Ccitt(msg, 0, msg.length, 0));
    }
}
