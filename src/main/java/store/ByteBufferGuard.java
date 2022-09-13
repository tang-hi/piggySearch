package store;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.atomic.AtomicInteger;

final class ByteBufferGuard {

    /**
     * Pass in an implementation of this interface to cleanup ByteBuffers. MMapDirectory implements
     * this to allow unmapping of bytebuffers with private Java APIs.
     */
    @FunctionalInterface
    static interface BufferCleaner {
        void freeBuffer(String resourceDescription, ByteBuffer b) throws IOException;
    }

    private final String resourceDescription;
    private final BufferCleaner cleaner;

    /** Not volatile; see comments on visibility below! */
    private boolean invalidated = false;

    /** Used as a store-store barrier; see comments below! */
    private final AtomicInteger barrier = new AtomicInteger();

    /**
     * Creates an instance to be used for a single {@link ByteBufferIndexInput} which must be shared
     * by all of its clones.
     */
    public ByteBufferGuard(String resourceDescription, BufferCleaner cleaner) {
        this.resourceDescription = resourceDescription;
        this.cleaner = cleaner;
    }

    /** Invalidates this guard and unmaps (if supported). */
    public void invalidateAndUnmap(ByteBuffer... bufs) throws IOException {
        if (cleaner != null) {
            invalidated = true;
            // This call should hopefully flush any CPU caches and as a result make
            // the "invalidated" field update visible to other threads. We specifically
            // don't make "invalidated" field volatile for performance reasons, hoping the
            // JVM won't optimize away reads of that field and hardware should ensure
            // caches are in sync after this call. This isn't entirely "fool-proof"
            // (see LUCENE-7409 discussion), but it has been shown to work in practice
            // and we count on this behavior.
            barrier.lazySet(0);
            // we give other threads a bit of time to finish reads on their ByteBuffer...:
            Thread.yield();
            // finally unmap the ByteBuffers:
            for (ByteBuffer b : bufs) {
                cleaner.freeBuffer(resourceDescription, b);
            }
        }
    }

    private void ensureValid() {
        if (invalidated) {
            // this triggers an AlreadyClosedException in ByteBufferIndexInput:
            throw new NullPointerException();
        }
    }

    public void getBytes(ByteBuffer receiver, byte[] dst, int offset, int length) {
        ensureValid();
        receiver.get(dst, offset, length);
    }

    public byte getByte(ByteBuffer receiver) {
        ensureValid();
        return receiver.get();
    }

    public short getShort(ByteBuffer receiver) {
        ensureValid();
        return receiver.getShort();
    }

    public int getInt(ByteBuffer receiver) {
        ensureValid();
        return receiver.getInt();
    }

    public long getLong(ByteBuffer receiver) {
        ensureValid();
        return receiver.getLong();
    }

    public byte getByte(ByteBuffer receiver, int pos) {
        ensureValid();
        return receiver.get(pos);
    }

    public short getShort(ByteBuffer receiver, int pos) {
        ensureValid();
        return receiver.getShort(pos);
    }

    public int getInt(ByteBuffer receiver, int pos) {
        ensureValid();
        return receiver.getInt(pos);
    }

    public long getLong(ByteBuffer receiver, int pos) {
        ensureValid();
        return receiver.getLong(pos);
    }

    public void getLongs(LongBuffer receiver, long[] dst, int offset, int length) {
        ensureValid();
        receiver.get(dst, offset, length);
    }

    public void getInts(IntBuffer receiver, int[] dst, int offset, int length) {
        ensureValid();
        receiver.get(dst, offset, length);
    }

    public void getFloats(FloatBuffer receiver, float[] dst, int offset, int length) {
        ensureValid();
        receiver.get(dst, offset, length);
    }
}