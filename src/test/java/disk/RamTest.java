package disk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
public class RamTest {

    @Test
    public void testSimpleGetAndPut() throws IOException {
        RamWriter writer = new RamWriter();
        writer.writeInt(899);
        RamReader reader = new RamReader(writer.toByteBuffers());
        assertEquals(899, reader.readInt());
    }

    @Test
    public void testLargeGetAndPut() throws IOException {
        RamWriter writer = new RamWriter();

        for(int i = 0; i < 3000; i++) {
            writer.writeInt(i);
        }

        for(int i = 0; i < 5000; i++) {
            writer.writeVInt(i);
        }

        RamReader reader = new RamReader(writer.toByteBuffers());

        for(int i = 0 ; i < 3000; i++) {
            assertEquals(i,reader.readInt());
        }

        for(int i = 0; i < 5000; i++) {
            assertEquals(i, reader.readVInt());
        }

        assertFalse(reader.hasRemaining());

    }
}
