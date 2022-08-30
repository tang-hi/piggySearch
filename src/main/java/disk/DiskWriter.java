package disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DiskWriter extends Writer{

    private RamWriter writer;

    private boolean flushed;

    private String file;

    private FileChannel out;

    private RandomAccessFile raf;
    public DiskWriter(String file) throws IOException {
        //file check
        this.file = file;
        raf = new RandomAccessFile(file, "rw");

       out = raf.getChannel();
       out.truncate(0);
       writer = new RamWriter();
       flushed = false;
    }

    @Override
    public void writeByte(byte b) throws IOException {
        assert flushed == false;
        writer.writeByte(b);
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        writer.writeBytes(b, offset, length);
    }

    public void flush() throws IOException {
        assert flushed == false;
        for(ByteBuffer bb : writer.toByteBuffers()) {
            out.write(bb);
        }
        out.close();
        raf.close();
    }
}
