package store;


import util.BitUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static store.DataOutput.DAY;
import static store.DataOutput.DAY_ENCODING;
import static store.DataOutput.HOUR;
import static store.DataOutput.HOUR_ENCODING;
import static store.DataOutput.SECOND;
import static store.DataOutput.SECOND_ENCODING;


public abstract class DataInput implements Cloneable {
    /**
     * Reads and returns a single byte.
     *
     * @see DataOutput#writeByte(byte)
     */
    public abstract byte readByte() throws IOException;

    /**
     * Reads a specified number of bytes into an array at the specified offset.
     *
     * @param b the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param len the number of bytes to read
     * @see DataOutput#writeBytes(byte[],int)
     */
    public abstract void readBytes(byte[] b, int offset, int len) throws IOException;

    /**
     * Reads a specified number of bytes into an array at the specified offset with control over
     * whether the read should be buffered (callers who have their own buffer should pass in "false"
     * for useBuffer). Currently only {@link BufferedIndexInput} respects this parameter.
     *
     * @param b the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param len the number of bytes to read
     * @param useBuffer set to false if the caller will handle buffering.
     * @see DataOutput#writeBytes(byte[],int)
     */
    public void readBytes(byte[] b, int offset, int len, boolean useBuffer) throws IOException {
        // Default to ignoring useBuffer entirely
        readBytes(b, offset, len);
    }

    /**
     * Reads two bytes and returns a short (LE byte order).
     *
     * @see DataOutput#writeShort(short)
     */
    public short readShort() throws IOException {
        final byte b1 = readByte();
        final byte b2 = readByte();
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    /**
     * Reads four bytes and returns an int (LE byte order).
     *
     * @see DataOutput#writeInt(int)
     */
    public int readInt() throws IOException {
        final byte b1 = readByte();
        final byte b2 = readByte();
        final byte b3 = readByte();
        final byte b4 = readByte();
        return ((b4 & 0xFF) << 24) | ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF);
    }

    /**
     * Reads an int stored in variable-length format. Reads between one and five bytes. Smaller values
     * take fewer bytes. Negative numbers are supported, but should be avoided.
     *
     * <p>The format is described further in {@link DataOutput#writeVInt(int)}.
     *
     * @see DataOutput#writeVInt(int)
     */
    public int readVInt() throws IOException {
        byte b = readByte();
        if (b >= 0) return b;
        int i = b & 0x7F;
        b = readByte();
        i |= (b & 0x7F) << 7;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7F) << 14;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7F) << 21;
        if (b >= 0) return i;
        b = readByte();
        // Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
        i |= (b & 0x0F) << 28;
        if ((b & 0xF0) == 0) return i;
        throw new IOException("Invalid vInt detected (too many bits)");
    }

    /**
     * Read a {@link BitUtils#zigZagDecode(int) zig-zag}-encoded {@link #readVInt() variable-length}
     * integer.
     *
     * @see DataOutput#writeZInt(int)
     */
    public int readZInt() throws IOException {
        return BitUtils.zigZagDecode(readVInt());
    }

    /**
     * Reads eight bytes and returns a long (LE byte order).
     *
     * @see DataOutput#writeLong(long)
     */
    public long readLong() throws IOException {
        return (readInt() & 0xFFFFFFFFL) | (((long) readInt()) << 32);
    }

    /**
     * Read a specified number of longs.
     *
     * @lucene.experimental
     */
    public void readLongs(long[] dst, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, dst.length);
        for (int i = 0; i < length; ++i) {
            dst[offset + i] = readLong();
        }
    }

    /**
     * Reads a specified number of ints into an array at the specified offset.
     *
     * @param dst the array to read bytes into
     * @param offset the offset in the array to start storing ints
     * @param length the number of ints to read
     */
    public void readInts(int[] dst, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, dst.length);
        for (int i = 0; i < length; ++i) {
            dst[offset + i] = readInt();
        }
    }

    /**
     * Reads a specified number of floats into an array at the specified offset.
     *
     * @param floats the array to read bytes into
     * @param offset the offset in the array to start storing floats
     * @param len the number of floats to read
     */
    public void readFloats(float[] floats, int offset, int len) throws IOException {
        Objects.checkFromIndexSize(offset, len, floats.length);
        for (int i = 0; i < len; i++) {
            floats[offset + i] = Float.intBitsToFloat(readInt());
        }
    }

    /**
     * Reads a long stored in variable-length format. Reads between one and nine bytes. Smaller values
     * take fewer bytes. Negative numbers are not supported.
     *
     * <p>The format is described further in {@link DataOutput#writeVInt(int)}.
     *
     * @see DataOutput#writeVLong(long)
     */
    public long readVLong() throws IOException {
        return readVLong(false);
    }

    private long readVLong(boolean allowNegative) throws IOException {
        byte b = readByte();
        if (b >= 0) return b;
        long i = b & 0x7FL;
        b = readByte();
        i |= (b & 0x7FL) << 7;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 14;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 21;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 28;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 35;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 42;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 49;
        if (b >= 0) return i;
        b = readByte();
        i |= (b & 0x7FL) << 56;
        if (b >= 0) return i;
        if (allowNegative) {
            b = readByte();
            i |= (b & 0x7FL) << 63;
            if (b == 0 || b == 1) return i;
            throw new IOException("Invalid vLong detected (more than 64 bits)");
        } else {
            throw new IOException("Invalid vLong detected (negative values disallowed)");
        }
    }

    /**
     * Read a {@link BitUtils#zigZagDecode(long) zig-zag}-encoded {@link #readVLong() variable-length}
     * integer. Reads between one and ten bytes.
     *
     * @see DataOutput#writeZLong(long)
     */
    public long readZLong() throws IOException {
        return BitUtils.zigZagDecode(readVLong(true));
    }

    /**
     * Reads a string.
     *
     * @see DataOutput#writeString(String)
     */
    public String readString() throws IOException {
        int length = readVInt();
        final byte[] bytes = new byte[length];
        readBytes(bytes, 0, length);
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

    /**
     * Returns a clone of this stream.
     *
     * <p>Clones of a stream access the same data, and are positioned at the same point as the stream
     * they were cloned from.
     *
     * <p>Expert: Subclasses must ensure that clones may be positioned at different points in the
     * input from each other and from the stream they were cloned from.
     */
    @Override
    public DataInput clone() {
        try {
            return (DataInput) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("This cannot happen: Failing to clone DataInput", e);
        }
    }

    /**
     * Reads a Map&lt;String,String&gt; previously written with {@link
     * DataOutput#writeMapOfStrings(Map)}.
     *
     * @return An immutable map containing the written contents.
     */
    public Map<String, String> readMapOfStrings() throws IOException {
        int count = readVInt();
        if (count == 0) {
            return Collections.emptyMap();
        } else if (count == 1) {
            return Collections.singletonMap(readString(), readString());
        } else {
            Map<String, String> map = count > 10 ? new HashMap<>() : new TreeMap<>();
            for (int i = 0; i < count; i++) {
                final String key = readString();
                final String val = readString();
                map.put(key, val);
            }
            return Collections.unmodifiableMap(map);
        }
    }

    /**
     * Reads a Set&lt;String&gt; previously written with {@link DataOutput#writeSetOfStrings(Set)}.
     *
     * @return An immutable set containing the written contents.
     */
    public Set<String> readSetOfStrings() throws IOException {
        int count = readVInt();
        if (count == 0) {
            return Collections.emptySet();
        } else if (count == 1) {
            return Collections.singleton(readString());
        } else {
            Set<String> set = count > 10 ? new HashSet<>() : new TreeSet<>();
            for (int i = 0; i < count; i++) {
                set.add(readString());
            }
            return Collections.unmodifiableSet(set);
        }
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

    /**
     * Skip over <code>numBytes</code> bytes. This method may skip bytes in whatever way is most
     * optimal, and may not have the same behavior as reading the skipped bytes. In general, negative
     * <code>numBytes</code> are not supported.
     */
    public abstract void skipBytes(final long numBytes) throws IOException;
}
