package net.bancer.sparkdict.domain.core;

import java.io.IOException;
import java.io.OutputStream;
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
}
