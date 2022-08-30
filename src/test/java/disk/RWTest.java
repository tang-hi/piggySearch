package disk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RWTest {

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
        mockWriter.writeInt(100);
        mockWriter.writeVInt(9999);
        mockWriter.writeZInt(-8888);
        mockWriter.writeLong(0x00FFFFFFL);
        mockWriter.writeVLong(0x00FFFFFEL);
        mockWriter.writeZLong(0x00F8FFFEL);
        mockWriter.writeString("hello,你好");
        mockWriter.writeTlong(Writer.SECOND * 5);
        mockWriter.writeTlong(Writer.DAY * 240);
        mockWriter.writeZFloat(124.0f);
        mockWriter.writeZDouble(45.0d);
        mockWriter.writeZFloat(349.4f);
        mockWriter.writeZDouble(498.3214313d);
        mockWriter.writeZFloat(-874.312f);
        mockWriter.writeZDouble(-9819.33423854328d);

        assertEquals((byte)0x1234, mockReader.readByte());
        assertEquals(100, mockReader.readInt());

        assertEquals(9999, mockReader.readVInt());

        assertEquals(-8888, mockReader.readZInt());

        assertEquals(0x00FFFFFFL, mockReader.readLong());

        assertEquals(0x00FFFFFEL, mockReader.readVLong());

        assertEquals(0x00F8FFFEL, mockReader.readZLong());

        assertEquals("hello,你好", mockReader.readString());

        assertEquals(5*Writer.SECOND , mockReader.readTLong());
        assertEquals(240*Writer.DAY, mockReader.readTLong());

        assertEquals(124.0f, mockReader.readZFloat());
        assertEquals(45.0d, mockReader.readZDouble());
        assertEquals(349.4f, mockReader.readZFloat());
        assertEquals(498.3214313d , mockReader.readZDouble());
        assertEquals(-874.312f, mockReader.readZFloat());
        assertEquals(-9819.33423854328d, mockReader.readZDouble());

        System.out.println("RW-test pass");
    }


}