package disk;

import util.BitUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static disk.Writer.DAY;
import static disk.Writer.DAY_ENCODING;
import static disk.Writer.HOUR;
import static disk.Writer.HOUR_ENCODING;
import static disk.Writer.SECOND;
import static disk.Writer.SECOND_ENCODING;

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

    public int readShort() throws IOException {
        final byte b1 = readByte();
        final byte b2 = readByte();
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
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

    /**
     * Reads a float in a variable-length format. Reads between one and five bytes. Small integral
     * values typically take fewer bytes.
     */
    public float readZFloat() throws IOException {
        int b = readByte() & 0xFF;
        if (b == 0xFF) {
            // negative value
            return Float.intBitsToFloat(readInt());
        } else if ((b & 0x80) != 0) {
            // small integer [-1..125]
            return (b & 0x7f) - 1;
        } else {
            // positive float
            int bits = b << 24 | ((readShort() & 0xFFFF) << 8) | (readByte() & 0xFF);
            return Float.intBitsToFloat(bits);
        }
    }

    /**
     * Reads a double in a variable-length format. Reads between one and nine bytes. Small integral
     * values typically take fewer bytes.
     */
    public double readZDouble() throws IOException {
        int b = readByte() & 0xFF;
        if (b == 0xFF) {
            // negative value
            return Double.longBitsToDouble(readLong());
        } else if (b == 0xFE) {
            // float
            return Float.intBitsToFloat(readInt());
        } else if ((b & 0x80) != 0) {
            // small integer [-1..124]
            return (b & 0x7f) - 1;
        } else {
            // positive double
            long bits =
                    ((long) b) << 56
                            | ((readInt() & 0xFFFFFFFFL) << 24)
                            | ((readShort() & 0xFFFFL) << 8)
                            | (readByte() & 0xFFL);
            return Double.longBitsToDouble(bits);
        }
    }

    /**
     * Reads a long in a variable-length format. Reads between one andCorePropLo nine bytes. Small
     * values typically take fewer bytes.
     */
    public long readTLong() throws IOException {
        int header = readByte() & 0xFF;

        long bits = header & 0x1F;
        if ((header & 0x20) != 0) {
            // continuation bit
            bits |= readVLong() << 5;
        }

        long l = BitUtils.zigZagDecode(bits);

        switch (header & DAY_ENCODING) {
            case SECOND_ENCODING:
                l *= SECOND;
                break;
            case HOUR_ENCODING:
                l *= HOUR;
                break;
            case DAY_ENCODING:
                l *= DAY;
                break;
            case 0:
                // uncompressed
                break;
            default:
                throw new AssertionError();
        }

        return l;
    }


}
