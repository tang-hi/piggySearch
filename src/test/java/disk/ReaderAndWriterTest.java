package disk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ReaderAndWriterTest {

    Writer mockWriter;

    Reader mockReader;

    @BeforeEach
    public void reset() {
        // 4k bytes
        FakeDisk disk = new FakeDisk();
        mockReader = new Reader() {
            @Override
            public byte readByte() throws IOException {
                return disk.read();
            }

            @Override
            public void readBytes(byte[] buffer, int offset, int len) throws IOException {
                for(int i = 0; i < len; i++) {
                    buffer[offset + i] = disk.read();
                }
            }
        };

        mockWriter = new Writer() {
            @Override
            public void writeByte(byte b) throws IOException {
                disk.write(b);
            }

            @Override
            public void writeBytes(byte[] b, int offset, int length) throws IOException {
                for(int i = 0; i < length; i++) {
                    disk.write(b[offset + i]);
                }
            }
        };
    }

    // TODO: Does java has stream which could be read and write simultaneously
    private static final class FakeDisk {
        byte[] disk = new byte[4 * 1024];

        int writeOffset = 0;
        int readOffset = 0;

        public void write(byte b) {
            assert writeOffset >= readOffset;
            disk[writeOffset++] = b;
        }

        public byte read() {
            if(readOffset >= writeOffset) {
                throw new IndexOutOfBoundsException("read more than write");
            }
            return disk[readOffset++];
        }
    }


    @Test
    public void writeAndRead() throws IOException {
        // simple
        mockWriter.writeByte((byte) 0x1234);
        assertEquals((byte)0x1234, mockReader.readByte());

        mockWriter.writeInt(100);
        assertEquals(100, mockReader.readInt());

        mockWriter.writeVInt(9999);
        assertEquals(9999, mockReader.readVInt());

        mockWriter.writeZInt(-8888);
        assertEquals(-8888, mockReader.readZInt());

        mockWriter.writeLong(0x00FFFFFFL);
        assertEquals(0x00FFFFFFL, mockReader.readLong());

        mockWriter.writeVLong(0x00FFFFFEL);
        assertEquals(0x00FFFFFEL, mockReader.readVLong());

        mockWriter.writeZLong(0x00F8FFFEL);
        assertEquals(0x00F8FFFEL, mockReader.readZLong());

        mockWriter.writeString("hello,你好");
        assertEquals("hello,你好", mockReader.readString());
    }
}