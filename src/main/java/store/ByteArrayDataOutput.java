package store;

import util.BitUtils;
import util.BytesRef;

public class ByteArrayDataOutput extends DataOutput{
    private byte[] bytes;

    private int pos;
    private int limit;

    public ByteArrayDataOutput(byte[] bytes) {
        reset(bytes);
    }

    public ByteArrayDataOutput(byte[] bytes, int offset, int len) {
        reset(bytes, offset, len);
    }

    public ByteArrayDataOutput() {
        reset(BytesRef.EMPTY_BYTES);
    }

    public void reset(byte[] bytes) {
        reset(bytes, 0, bytes.length);
    }

    public void reset(byte[] bytes, int offset, int len) {
        this.bytes = bytes;
        pos = offset;
        limit = offset + len;
    }

    public int getPosition() {
        return pos;
    }

    @Override
    public void writeByte(byte b) {
        assert pos < limit;
        bytes[pos++] = b;
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) {
        assert pos + length <= limit;
        System.arraycopy(b, offset, bytes, pos, length);
        pos += length;
    }

    @Override
    public void writeShort(short i) {
        assert pos + Short.BYTES <= limit;
        BitUtils.VH_LE_SHORT.set(bytes, pos, i);
        pos += Short.BYTES;
    }

    @Override
    public void writeInt(int i) {
        assert pos + Integer.BYTES <= limit;
        BitUtils.VH_LE_INT.set(bytes, pos, i);
        pos += Integer.BYTES;
    }

    @Override
    public void writeLong(long i) {
        assert pos + Long.BYTES <= limit;
        BitUtils.VH_LE_LONG.set(bytes, pos, i);
        pos += Long.BYTES;
    }

}
