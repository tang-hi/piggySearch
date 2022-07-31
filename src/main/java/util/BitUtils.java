package util;

public class BitUtils {

    /** Same as {@link #zigZagEncode(long)} but on integers. */
    public static int zigZagEncode(int i) {
        return (i >> 31) ^ (i << 1);
    }

    /**
     * <a href="https://developers.google.com/protocol-buffers/docs/encoding#types">Zig-zag</a> encode
     * the provided long. Assuming the input is a signed long whose absolute value can be stored on
     * <code>n</code> bits, the returned value will be an unsigned long that can be stored on <code>
     * n+1</code> bits.
     */
    public static long zigZagEncode(long l) {
        return (l >> 63) ^ (l << 1);
    }

    /** Decode an int previously encoded with {@link #zigZagEncode(int)}. */
    public static int zigZagDecode(int i) {
        return ((i >>> 1) ^ -(i & 1));
    }

    /** Decode a long previously encoded with {@link #zigZagEncode(long)}. */
    public static long zigZagDecode(long l) {
        return ((l >>> 1) ^ -(l & 1));
    }
}
