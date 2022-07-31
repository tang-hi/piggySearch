package util;

import java.nio.charset.StandardCharsets;

public final class BytesRef {
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

}
