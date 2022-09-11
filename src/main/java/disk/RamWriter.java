package disk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class RamWriter extends Writer{

    private static final int BLOCK_SIZE = 4 * 1024;

    private ArrayDeque<ByteBuffer> blocks;

    private ByteBuffer curBlock;

    private long size;

    public RamWriter() {
       blocks = new ArrayDeque<>();
       curBlock = ByteBuffer.allocate(BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
       blocks.add(curBlock);
       size = 0;
    }

    @Override
    public void writeByte(byte b) throws IOException {
        if(hasRemaining() == false) {
            growBlocks();
        }
        curBlock.put(b);
        size++;
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        // TODO: it should be optimized
        for(int i = 0; i < length; i++) {
            writeByte(b[offset + i]);
        }
    }

    public List<ByteBuffer> toByteBuffers() {
        List<ByteBuffer> result = new ArrayList<>(Math.max(blocks.size(), 1));

        for(ByteBuffer bb : blocks) {
            bb = bb.asReadOnlyBuffer().flip().order(ByteOrder.LITTLE_ENDIAN);
            result.add(bb);
        }
        return result;
    }

    @Override
    public void close() {
        blocks = null;
    }

    @Override
    public long size() {
        return size;
    }

    private boolean hasRemaining() {
        return curBlock.hasRemaining();
    }

    private void growBlocks() {
        curBlock = ByteBuffer.allocate(BLOCK_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        blocks.add(curBlock);
    }

}
