package net.bancer.sparkdict.domain.core;

import net.bancer.sparkdict.domain.utils.InMemorySeekableByteChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.List;

/**
 * Abstracts how dictionary files are located and opened, so that Shelf,
 * Book, and BookInfo do not need to know whether dictionaries live on
 * ordinary filesystem paths or are reached through the Storage Access
 * Framework.
 *
 * <p><b>This interface is being introduced ahead of actually using it.</b>
 * For now, every class that accepts a DictionaryFiles instance still
 * performs its file I/O exactly as before, via {@code java.io.File} and raw
 * paths -- nothing calls into these methods yet. This exists purely to
 * establish the injection point (the constructor parameter) so a later step
 * can route the existing file-resolution logic through it, and introduce a
 * Storage-Access-Framework-backed implementation, without having to touch
 * every constructor signature again. Its exact method shape is expected to
 * be refined once real wiring begins -- in particular, whether paths here
 * end up being full paths (as used everywhere today) or root-relative names
 * (more natural for a SAF-backed implementation) is not yet settled.</p>
 */
public interface DictionaryFiles {

    /**
     * Lists dictionary metadata ({@code .ifo}) files found under this
     * instance's root location.
     *
     * @return paths of {@code .ifo} files found.
     */
    List<String> findDictionaryMetaFilePaths();

    /**
     * Opens a random-access, read-only channel to the specified file.
     *
     * @param path path of the file to open.
     * @return an open, readable, seekable channel.
     * @throws IOException if the file cannot be opened.
     */
    SeekableByteChannel openForRead(String path) throws IOException;

    /**
     * Reads a file fully into memory and returns a SeekableByteChannel backed
     * by that in-memory copy, rather than one backed by a live connection to
     * the file's storage.
     *
     * <p>Intended for small files that are read many times over an object's
     * lifetime with random access -- index files such as .idx/.sparkdict.idx
     * -- where a per-operation cost (e.g. a Binder round-trip to a Storage
     * Access Framework provider) can dominate when many small reads are made.
     * Loading once and serving all subsequent reads from memory turns many
     * small provider round-trips into a single bulk one.
     *
     * <p><b>Must never be used for large files</b> -- dictionary data
     * (.dict.dz) or resource archives (res.zip) can be hundreds of megabytes
     * or more. Those must keep using {@link #openForRead}, which streams
     * without ever materialising the whole file; see the earlier decision not
     * to copy res.zip for exactly this reason.
     *
     * <p>A channel returned by this method can never throw
     * {@link java.nio.channels.ClosedChannelException} due to an external
     * cause (e.g. a Storage Access Framework provider process dying) -- it
     * holds no live connection to anything once this method returns.
     *
     * @param path path of the file to read.
     * @return a SeekableByteChannel backed by an in-memory copy of the file's
     *         entire contents.
     * @throws IOException if the file cannot be read.
     */
    default SeekableByteChannel readFully(String path) throws IOException {
        byte[] data;
        try (SeekableByteChannel channel = openForRead(path)) {
            long size = channel.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("File too large to read fully into memory: " + path);
            }
            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            while (buffer.hasRemaining()) {
                int bytesRead = channel.read(buffer);
                if (bytesRead == -1) {
                    throw new IOException("Unexpected end of file while reading: " + path);
                }
            }
            data = buffer.array();
        }
        return new InMemorySeekableByteChannel(data);
    }

    /**
     * Opens a sequential output stream to create or overwrite the specified
     * file. The file is created if it does not already exist, and its
     * previous contents (if any) are discarded.
     *
     * @param path path of the file to create or overwrite.
     * @return an open output stream.
     * @throws IOException if the file cannot be created or opened.
     */
    OutputStream createForWrite(String path) throws IOException;

    /**
     * Deletes the specified file.
     *
     * @param path path of the file to delete.
     * @return {@code true} if the file existed and was deleted.
     */
    boolean delete(String path);

    /**
     * Checks whether the specified file exists, without opening it.
     *
     * <p>The default implementation opens and immediately closes the file,
     * which is correct but wasteful -- implementations that can check
     * existence more cheaply (see {@code SafDictionaryFiles}) should override
     * this.</p>
     *
     * @param path path of the file to check.
     * @return {@code true} if the file exists and can be resolved.
     */
    default boolean exists(String path) {
        try (SeekableByteChannel ignored = openForRead(path)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
