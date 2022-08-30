package disk;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
public class DiskTest {

    private final Path path = Paths.get("src", "test", "resources", "temp").toAbsolutePath();

    @Test
    public void testLargeGetAndPut() throws IOException {
        DiskWriter writer = new DiskWriter(path.toString());

        for(int i = 0; i < 3000; i++) {
            writer.writeInt(i);
        }

        for(int i = 0; i < 5000; i++) {
            writer.writeVInt(i);
        }
        writer.flush();

        DiskReader reader = new DiskReader(path.toString());

        for(int i = 0 ; i < 3000; i++) {
            assertEquals(i,reader.readInt());
        }

        for(int i = 0; i < 5000; i++) {
            assertEquals(i, reader.readVInt());
        }

        assertFalse(reader.hasRemaining());
        reader.close();
    }

}
