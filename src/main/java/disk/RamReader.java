package disk;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RamReader extends Reader{

    private static final int BLOCK_SIZE = 4 * 1024;

    private ByteBuffer[] blocks;

    private long pos;

    private long size;

    public RamReader(List<ByteBuffer> buffers) {
        init(buffers);
    }

    public RamReader(ByteBuffer buffer) {
        List<ByteBuffer> bbs = new ArrayList<>();
        int idx = 0;
        while(buffer.remaining() >= BLOCK_SIZE) {
            ByteBuffer bb = buffer.slice(idx, BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            bbs.add(bb);
            idx += BLOCK_SIZE;
            buffer.position(idx);
        }
        if(buffer.remaining() > 0) {
            bbs.add(buffer.slice());
        }

        init(bbs);
    }

    public void init(List<ByteBuffer> bbs) {
        blocks = bbs.stream().map(bb -> bb.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN))
                .toArray(ByteBuffer[]::new);
        size = Arrays.stream(blocks)
                .mapToLong(Buffer::remaining)
                .sum();
        pos = blocks[0].position();
    }
    @Override
    public byte readByte() throws IOException {
        if(!hasRemaining()) {
            throw new IllegalStateException("no more data");
        }
        byte b = blocks[blockIndex()].get(blockOffset());
        pos++;
        return b;
    }

    @Override
    public void readBytes(byte[] buffer, int offset, int len) throws IOException {
        for(int  i = 0; i< len; i++) {
            buffer[offset +i] =  readByte();
        }
    }

    public void setPos(long pos) {
        if(pos < 0 || pos >= size) {
            throw new IllegalArgumentException("pos is invalid");
        }
        this.pos = pos;
    }

    public boolean hasRemaining() {
        return pos < size;
    }

    private int blockIndex() {
        return (int) (pos / BLOCK_SIZE);
    }

    private int blockOffset() {
        return (int) (pos % BLOCK_SIZE);
    }

}
