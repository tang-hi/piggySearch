package disk;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DiskReader extends Reader {

    private String file;
    private RamReader reader;

    private MappedByteBuffer mbb;

    FileChannel fc;

    public DiskReader(String file) throws IOException {
        this.file = file;
        fc = new FileInputStream(file).getChannel();
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        this.reader = new RamReader(mbb.order(ByteOrder.LITTLE_ENDIAN));
    }
    @Override
    public byte readByte() throws IOException {
        return reader.readByte();
    }

    @Override
    public void readBytes(byte[] buffer, int offset, int len) throws IOException {
        reader.readBytes(buffer, offset, len);
    }

    public void setPos(long pos) {
        reader.setPos(pos);
    }

    public boolean hasRemaining() {
        return reader.hasRemaining();
    }

    public void close() throws IOException {
        fc.close();
        mbb = null;
        reader = null;
    }
}
