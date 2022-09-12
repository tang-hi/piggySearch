package store;

import java.io.IOException;

/**
 * Random Access Index API. Unlike {@link IndexInput}, this has no concept of file position, all
 * reads are absolute. However, like IndexInput, it is only intended for use by a single thread.
 */
public interface RandomAccessInput {

    /**
     * Reads a byte at the given position in the file
     *
     * @see DataInput#readByte
     */
    byte readByte(long pos) throws IOException;
    /**
     * Reads a short (LE byte order) at the given position in the file
     *
     * @see DataInput#readShort
     */
    short readShort(long pos) throws IOException;
    /**
     * Reads an integer (LE byte order) at the given position in the file
     *
     * @see DataInput#readInt
     */
    int readInt(long pos) throws IOException;
    /**
     * Reads a long (LE byte order) at the given position in the file
     *
     * @see DataInput#readLong
     */
    long readLong(long pos) throws IOException;
}