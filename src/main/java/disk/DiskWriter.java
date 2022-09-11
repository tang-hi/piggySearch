package disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DiskWriter extends Writer{

    private String file;

    private FileChannel out;

    private RandomAccessFile raf;

    private byte[] singleByte;
    public DiskWriter(String file) throws IOException {
        //file check
        this.file = file;
        raf = new RandomAccessFile(file, "rw");

       out = raf.getChannel();
       out.truncate(0);
       singleByte = new byte[1];
    }

    @Override
    public void close() {
        try {
            flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public long size() {
        try {
            return out.position() ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeByte(byte b) throws IOException {
        singleByte[0] = b;
        out.write(ByteBuffer.wrap(singleByte));
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        for(int i = 0; i < length; i++) {
            writeByte(b[offset+i]);
        }
    }

    public void flush() throws IOException {
        out.close();
        raf.close();
    }
}
