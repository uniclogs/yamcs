package org.uniclogs.yamcs.tctm;

public final class CfdpVc1FrameCodec {

    private CfdpVc1FrameCodec() {}

    public static byte[] wrap(byte[] pdu) {
        byte[] out = new byte[pdu.length];
        System.arraycopy(pdu, 0, out, 0, pdu.length);
        return out;
    }

    public static byte[] unwrap(byte[] tfdz) {
        int pduLen = tfdz.length;
        byte[] pdu = new byte[pduLen];
        System.arraycopy(tfdz, 0, pdu, 0, pduLen);
        return pdu;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
