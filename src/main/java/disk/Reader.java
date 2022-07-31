package disk;

import util.BitUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class Reader {

    public abstract byte readByte() throws IOException;

    // put bytes in the buffer.
    public abstract void readBytes(byte[] buffer, int offset, int len) throws IOException;

    public int readInt() throws IOException {
        final byte b1 = readByte();
        final byte b2 = readByte();
        final byte b3 = readByte();
        final byte b4 = readByte();
        return ((b4 & 0xFF) << 24) | ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF);
    }

    public int readVInt() throws IOException {
        byte b;
        int i = 0;
        int shift = 0;
        do {
            b = readByte();
            i |= ((b & 0x7f) << shift);
            shift += 7;
        } while ((b & 0x80) != 0);
        return i;
    }

    public int readZInt() throws IOException {
        return BitUtils.zigZagDecode(readVInt());
    }

    public long readLong() throws IOException {
       int low =  readInt();
       int high = readInt();
       return ((long)high << 32) | (low & 0xffffffffL);
    }

    public long readVLong() throws IOException {
        byte b;
        long i = 0;
        int shift = 0;
        do {
            b = readByte();
            i |= ((long) (b & 0x7f) << shift);
            shift += 7;
        } while ((b & 0x80) != 0);
        return i;
    }

    public long readZLong() throws IOException {
        return BitUtils.zigZagDecode(readVLong());
    }

    public String readString() throws IOException {
        int length = readVInt();
        byte[] buffer = new byte[length];
        readBytes(buffer, 0, length);
        return new String(buffer, 0, length, StandardCharsets.UTF_8);
    }

}
