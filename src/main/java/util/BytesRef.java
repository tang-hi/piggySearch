package util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class BytesRef implements Comparable<BytesRef>{
    public static final byte[] EMPTY_BYTES = new byte[0];

    public byte[] bytes;

    public int offset;

    public int length;


    public BytesRef(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    public BytesRef() {
        this(EMPTY_BYTES);
    }

    public BytesRef(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public BytesRef(String s) {
        bytes = s.getBytes(StandardCharsets.UTF_8);
        length = bytes.length;
        offset = 0;
    }

    /** Unsigned byte order comparison */
    @Override
    public int compareTo(BytesRef other) {
        return Arrays.compareUnsigned(
                this.bytes,
                this.offset,
                this.offset + this.length,
                other.bytes,
                other.offset,
                other.offset + other.length);
    }

    /**
     * Creates a new BytesRef that points to a copy of the bytes from <code>other</code>
     *
     * <p>The returned BytesRef will have a length of other.length and an offset of zero.
     */
    public static BytesRef deepCopyOf(BytesRef other) {
        return new BytesRef(
                ArrayUtil.copyOfSubArray(other.bytes, other.offset, other.offset + other.length),
                0,
                other.length);
    }
}
