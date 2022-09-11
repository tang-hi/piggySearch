package store;

import disk.DiskReader;
import disk.DiskWriter;
import disk.Reader;
import disk.Writer;
import index.IndexFileNames;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class Directory implements Closeable {

    final Path directory;
    private final AtomicLong nextTempFileCounter = new AtomicLong();

    public Directory(Path path) throws IOException {
        if(Files.isDirectory(path)) {
            Files.createDirectories(path);
        }
        directory = path.toRealPath();
    }

    /**
     * Returns names of all files stored in this directory. The output must be in sorted (UTF-16,
     * java's {@link String#compareTo}) order.
     *
     * @throws IOException in case of I/O error
     */
    public String[] listAll() throws IOException{
        List<String> entries = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                entries.add(name);
            }
        }

        String[] array = entries.toArray(new String[entries.size()]);
        // Directory.listAll javadocs state that we sort the results here, so we don't let filesystem
        // specifics leak out of this abstraction:
        Arrays.sort(array);
        return array;
    }

    /**
     * Removes an existing file in the directory.
     *
     * <p>This method must throw either {@link NoSuchFileException} or {@link FileNotFoundException}
     * if {@code name} points to a non-existing file.
     *
     * @param name the name of an existing file.
     * @throws IOException in case of I/O error
     */
    public void deleteFile(String name) throws IOException {
        try {
            Files.delete(directory.resolve(name));
        } catch (NoSuchFileException | FileNotFoundException e) {
            throw e;
        }
    }



    public Writer createOutput(String name) throws IOException {
        return new DiskWriter(directory.resolve(name).toAbsolutePath().toString());
    }

    /**
     * Creates a new, empty, temporary file in the directory and returns an {@link IndexOutput}
     * instance for appending data to this file.
     *
     * <p>The temporary file name (accessible via {@link IndexOutput#getName()}) will start with
     * {@code prefix}, end with {@code suffix} and have a reserved file extension {@code .tmp}.
     */
    public Writer createTempOutput(String prefix, String suffix) throws IOException {
        String name = getTempFileName(prefix, suffix, nextTempFileCounter.getAndIncrement());
        return createOutput(name);
    }

    /**
     * Opens a stream for reading an existing file.
     *
     * <p>This method must throw either {@link NoSuchFileException} or {@link FileNotFoundException}
     * if {@code name} points to a non-existing file.
     *
     * @param name the name of an existing file.
     * @throws IOException in case of I/O error
     */
    public Reader openInput(String name) throws IOException {
        return new DiskReader(directory.resolve(name).toAbsolutePath().toString());
    }

    public static String getTempFileName(String prefix, String suffix, long counter) {
        return IndexFileNames.segmentFileName(
                prefix, suffix + "_" + Long.toString(counter, Character.MAX_RADIX), "tmp");
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode());
    }

    @Override
    public void close() throws IOException {

    }
}