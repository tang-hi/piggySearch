package disk;

import util.BitUtils;
import util.BytesRef;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public abstract class Writer {

    // for compression of timestamps
    static final long SECOND = 1000L;
    static final long HOUR = 60 * 60 * SECOND;
    static final long DAY = 24 * HOUR;
    static final int SECOND_ENCODING = 0x40;
    static final int HOUR_ENCODING = 0x80;
    static final int DAY_ENCODING = 0xC0;

    public abstract void close();

    public abstract long size();

    public abstract void writeByte(byte b) throws IOException;

    public abstract void writeBytes(byte[] b, int offset, int length) throws  IOException;

    public List<ByteBuffer> toByteBuffer() {
        return null;
    }

    public void writeBytes(byte[] b, int length) throws IOException {
        writeBytes(b, 0, length);
    }

    public void writeShort(short i) throws IOException {
        writeByte((byte) i);
        writeByte((byte) (i>>8));
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

    public void writeTlong(long l) throws IOException {
        int header;
        if (l % SECOND != 0) {
            header = 0;
        } else if (l % DAY == 0) {
            // timestamp with day precision
            header = DAY_ENCODING;
            l /= DAY;
        } else if (l % HOUR == 0) {
            // timestamp with hour precision, or day precision with a timezone
            header = HOUR_ENCODING;
            l /= HOUR;
        } else {
            // timestamp with second precision
            header = SECOND_ENCODING;
            l /= SECOND;
        }

        final long zigZagL = BitUtils.zigZagEncode(l);
        header |= (zigZagL & 0x1F); // last 5 bits
        final long upperBits = zigZagL >>> 5;
        if (upperBits != 0) {
            header |= 0x20;
        }
        writeByte((byte) header);
        if (upperBits != 0) {
            writeVLong(upperBits);
        }
    }

    public void writeString(String s) throws IOException {
        final BytesRef bytesRef = new BytesRef(s);
        writeVInt(bytesRef.length);
        writeBytes(bytesRef.bytes, 0, bytesRef.length);
    }

    public void writeZFloat(float f) throws IOException {
        int intVal = (int) f;
        int floatBits = Float.floatToIntBits(f);

        if(f == intVal && intVal >= -1 && intVal <= 0x7D && floatBits != Float.floatToIntBits(-0f)){
            writeByte((byte) (0x80 | (1 + intVal)));
        } else if ((floatBits >>> 31) == 0) {
            writeByte((byte) (floatBits >> 24));
            writeShort((short) (floatBits >> 8));
            writeByte((byte) floatBits);
        } else {
            writeByte((byte) 0xFF);
            writeInt(floatBits);
        }
    }

    public void writeZDouble(double d) throws IOException {
        int intVal = (int) d;
        final long doubleBits = Double.doubleToLongBits(d);

        if (d == intVal && intVal >= -1 && intVal <= 0x7C && doubleBits != Double.doubleToLongBits(-0d)) {
            // small integer value [-1..124]: single byte
            writeByte((byte) (0x80 | (intVal + 1)));
        } else if (d == (float) d) {
            // d has an accurate float representation: 5 bytes
            writeByte((byte) 0xFE);
            writeInt(Float.floatToIntBits((float) d));
        } else if ((doubleBits >>> 63) == 0) {
            // other positive doubles: 8 bytes
            writeByte((byte) (doubleBits >> 56));
            writeInt((int) (doubleBits >>> 24));
            writeShort((short) (doubleBits >>> 8));
            writeByte((byte) (doubleBits));
        } else {
            // other negative doubles: 9 bytes
            writeByte((byte) 0xFF);
            writeLong(doubleBits);
        }
    }
}
