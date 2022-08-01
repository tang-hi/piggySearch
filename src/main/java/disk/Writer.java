package disk;

import util.BitUtils;
import util.BytesRef;

import java.io.IOException;

public abstract class Writer {

    public abstract void writeByte(byte b) throws IOException;

    public abstract void writeBytes(byte[] b, int offset, int length) throws  IOException;

    public void writeBytes(byte[] b, int length) throws IOException {
        writeBytes(b, 0, length);
    }

    // little end style
    public void writeInt(int i) throws IOException {
        writeByte((byte) i);
        writeByte((byte) (i >> 8));
        writeByte((byte) (i >> 16));
        writeByte((byte) (i >> 24));
    }

    public void writeVInt(int i) throws IOException {
        while((i & ~0x7f) != 0) {
            writeByte( (byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }

    public void writeZInt(int i) throws IOException {
        writeVInt(BitUtils.zigZagEncode(i));
    }

    public void writeLong(long i) throws IOException {
        writeInt((int) i);
        writeInt((int) (i >> 32));
    }

    public void writeVLong(long i) throws IOException {
        while((i & ~0x7f) != 0) {
            writeByte( (byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }

    public void writeZLong(long i) throws IOException {
        writeVLong(BitUtils.zigZagEncode(i));
    }

    public void writeString(String s) throws IOException {
        final BytesRef bytesRef = new BytesRef(s);
        writeVInt(bytesRef.length);
        writeBytes(bytesRef.bytes, 0, bytesRef.length);
    }
}
