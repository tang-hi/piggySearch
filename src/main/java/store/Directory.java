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

public abstract class Directory implements Closeable {
    /**
     * Returns names of all files stored in this directory. The output must be in sorted (UTF-16,
     * java's {@link String#compareTo}) order.
     *
     * @throws IOException in case of I/O error
     */
    public abstract String[] listAll() throws IOException;

    /**
     * Removes an existing file in the directory.
     *
     * <p>This method must throw either {@link NoSuchFileException} or {@link FileNotFoundException}
     * if {@code name} points to a non-existing file.
     *
     * @param name the name of an existing file.
     * @throws IOException in case of I/O error
     */
    public abstract void deleteFile(String name) throws IOException;

    /**
     * Returns the byte length of a file in the directory.
     *
     * <p>This method must throw either {@link NoSuchFileException} or {@link FileNotFoundException}
     * if {@code name} points to a non-existing file.
     *
     * @param name the name of an existing file.
     * @throws IOException in case of I/O error
     */
    public abstract long fileLength(String name) throws IOException;

    /**
     * Creates a new, empty file in the directory and returns an {@link IndexOutput} instance for
     * appending data to this file.
     *
     * <p>This method must throw {@link java.nio.file.FileAlreadyExistsException} if the file already
     * exists.
     *
     * @param name the name of the file to create.
     * @throws IOException in case of I/O error
     */
    public abstract IndexOutput createOutput(String name, IOContext context) throws IOException;

    /**
     * Creates a new, empty, temporary file in the directory and returns an {@link IndexOutput}
     * instance for appending data to this file.
     *
     * <p>The temporary file name (accessible via {@link IndexOutput#getName()}) will start with
     * {@code prefix}, end with {@code suffix} and have a reserved file extension {@code .tmp}.
     */
    public abstract IndexOutput createTempOutput(String prefix, String suffix, IOContext context)
            throws IOException;

    /**
     * Ensures that any writes to these files are moved to stable storage (made durable).
     *
     * <p>Lucene uses this to properly commit changes to the index, to prevent a machine/OS crash from
     * corrupting the index.
     *
     * @see #syncMetaData()
     */
    public abstract void sync(Collection<String> names) throws IOException;

    /**
     * Ensures that directory metadata, such as recent file renames, are moved to stable storage.
     *
     * @see #sync(Collection)
     */
    public abstract void syncMetaData() throws IOException;

    /**
     * Renames {@code source} file to {@code dest} file where {@code dest} must not already exist in
     * the directory.
     *
     * <p>It is permitted for this operation to not be truly atomic, for example both {@code source}
     * and {@code dest} can be visible temporarily in {@link #listAll()}. However, the implementation
     * of this method must ensure the content of {@code dest} appears as the entire {@code source}
     * atomically. So once {@code dest} is visible for readers, the entire content of previous {@code
     * source} is visible.
     *
     * <p>This method is used by IndexWriter to publish commits.
     */
    public abstract void rename(String source, String dest) throws IOException;

    /**
     * Opens a stream for reading an existing file.
     *
     * <p>This method must throw either {@link NoSuchFileException} or {@link FileNotFoundException}
     * if {@code name} points to a non-existing file.
     *
     * @param name the name of an existing file.
     * @throws IOException in case of I/O error
     */
    public abstract IndexInput openInput(String name, IOContext context) throws IOException;

    /**
     * Opens a checksum-computing stream for reading an existing file.
     *
     * <p>This method must throw either {@link NoSuchFileException} or {@link FileNotFoundException}
     * if {@code name} points to a non-existing file.
     *
     * @param name the name of an existing file.
     * @throws IOException in case of I/O error
     */
    public ChecksumIndexInput openChecksumInput(String name, IOContext context) throws IOException {
        return new BufferedChecksumIndexInput(openInput(name, context));
    }

    /**
     * Acquires and returns a {@link Lock} for a file with the given name.
     *
     * @param name the name of the lock file
     * @throws LockObtainFailedException (optional specific exception) if the lock could not be
     *     obtained because it is currently held elsewhere.
     * @throws IOException if any i/o error occurs attempting to gain the lock
     */
    public abstract Lock obtainLock(String name) throws IOException;

    /** Closes the directory. */
    @Override
    public abstract void close() throws IOException;

    /**
     * Copies an existing {@code src} file from directory {@code from} to a non-existent file {@code
     * dest} in this directory.
     */
    public void copyFrom(Directory from, String src, String dest, IOContext context)
            throws IOException {
        boolean success = false;
        try (IndexInput is = from.openInput(src, context);
             IndexOutput os = createOutput(dest, context)) {
            os.copyBytes(is, is.length());
            success = true;
        } finally {
            if (!success) {
                IOUtils.deleteFilesIgnoringExceptions(this, dest);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + Integer.toHexString(hashCode());
    }

    /**
     * Ensures this directory is still open.
     *
     * @throws AlreadyClosedException if this directory is closed.
     */
    protected void ensureOpen() throws AlreadyClosedException {}

    /**
     * Returns a set of files currently pending deletion in this directory.
     *
     * @lucene.internal
     */
    public abstract Set<String> getPendingDeletions() throws IOException;

    /**
     * Creates a file name for a temporary file. The name will start with {@code prefix}, end with
     * {@code suffix} and have a reserved file extension {@code .tmp}.
     *
     * @see #createTempOutput(String, String, IOContext)
     */
    protected static String getTempFileName(String prefix, String suffix, long counter) {
        return IndexFileNames.segmentFileName(
                prefix, suffix + "_" + Long.toString(counter, Character.MAX_RADIX), "tmp");
    }
}