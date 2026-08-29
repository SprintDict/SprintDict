package net.bancer.sparkdict.domain.core;

import android.util.Log;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides access to resources stored in a dictionary {@code res.zip} archive.
 *
 * <p>The archive contains dictionary resources such as audio files and
 * pictures. The ZIP file is opened when this object is created and remains
 * open until {@link #close()} is called.</p>
 *
 * <p>Two backends are used depending on how this object was constructed:
 * {@code java.util.zip.ZipFile} for the path-based constructor, since it is
 * the fast, native-backed implementation this class has always used and
 * some real dictionaries' res.zip archives are large enough that a slower
 * implementation is noticeably worse on removable storage; and Apache
 * Commons Compress's {@code ZipFile} for the channel-based constructor,
 * since {@code java.util.zip.ZipFile} has no way to open from a channel at
 * all. Only one of the two backend fields is ever non-null at a time.</p>
 */
public class ResourcesZipFile {

    private static final String TAG = "ResourcesZipFile";

    /**
     * ZIP archive containing the dictionary resources (audio and pictures).
     */
    private ZipFile zipFile = null;

    private org.apache.commons.compress.archivers.zip.ZipFile compressZipFile = null;

    /**
     * Opens a dictionary's res.zip file and initialises its decompression state.
     *
     * @param file full path to the res.zip file.
     */
    public ResourcesZipFile(File file) {
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            Log.e(TAG, "Cannot open resource ZIP: " + file, e);
            close();
        }
    }

    /**
     * Opens a dictionary's res.zip archive through an already-open, seekable
     * channel instead of a filesystem path.
     *
     * <p>This constructor exists so callers that only have a
     * {@link SeekableByteChannel} (for example, one backed by a Storage
     * Access Framework document) can use this class without copying the
     * archive to local storage first. Error handling matches
     * {@link #ResourcesZipFile(File)}: a failure to open is logged and
     * leaves this object in the same closed state rather than throwing.</p>
     *
     * <p><b>Known open issue:</b> Commons Compress's central-directory scan
     * has been observed to be dramatically slower than
     * {@code java.util.zip.ZipFile} on a large res.zip served from slow
     * removable storage. This constructor is not yet exercised by any
     * production code path, and its real-world performance against a slow
     * SAF-backed document has not been verified. Investigate before relying
     * on it -- a buffering wrapper around the supplied channel is the likely
     * fix if the same slowness shows up here.</p>
     *
     * @param channel an open, readable, seekable channel over the res.zip data.
     */
    public ResourcesZipFile(SeekableByteChannel channel) {
        try {
            compressZipFile = org.apache.commons.compress.archivers.zip.ZipFile.builder()
                .setSeekableByteChannel(channel)
                .setIgnoreLocalFileHeader(true)
                .get();
        } catch (IOException e) {
            Log.e(TAG, "Cannot open resource ZIP from channel", e);
            close();
        }
    }

    /**
     * Retrieves a resource from an already opened ZIP archive.
     *
     * <p>The resource is expected to be located under the {@code res/} directory
     * inside the archive. The returned byte array contains the decompressed
     * contents of the ZIP entry.</p>
     *
     * @param resourceName name of the resource to retrieve.
     * @return resource contents as a byte array, or an empty byte array if the
     *         specified entry does not exist or cannot be read.
     */
    public byte[] getResourceFromZip(String resourceName) {
        String entryName = "res/" + resourceName;
        if (zipFile != null) {
            ZipEntry entry = zipFile.getEntry(entryName);
            if (entry == null) {
                return new byte[0];
            }
            try (InputStream input = zipFile.getInputStream(entry)) {
                return readEntry(input, entryName, entry.getSize());
            } catch (IOException e) {
                Log.e(TAG, "Cannot read ZIP entry: " + entryName, e);
                return new byte[0];
            }
        }
        if (compressZipFile != null) {
            ZipArchiveEntry entry = compressZipFile.getEntry(entryName);
            if (entry == null) {
                return new byte[0];
            }
            try (InputStream input = compressZipFile.getInputStream(entry)) {
                return readEntry(input, entryName, entry.getSize());
            } catch (IOException e) {
                Log.e(TAG, "Cannot read ZIP entry: " + entryName, e);
                return new byte[0];
            }
        }
        return new byte[0];
    }

    /**
     * Reads the specified number of bytes from a ZIP entry input stream.
     *
     * @param input the input stream containing the ZIP entry data
     * @param entryName the name of the ZIP entry, used for logging
     * @param size the expected size of the ZIP entry in bytes
     * @return the entry data, or an empty array if the end of the stream is
     *         reached before the expected number of bytes are read
     * @throws IOException if an I/O error occurs while reading the entry
     */
    private byte[] readEntry(InputStream input, String entryName, long size) throws IOException {
        byte[] result = new byte[(int) size];
        int offset = 0;
        while (offset < result.length) {
            int bytesRead = input.read(result, offset, result.length - offset);
            if (bytesRead == -1) {
                break;
            }
            offset += bytesRead;
        }
        if (offset != result.length) {
            Log.e(TAG, "Unexpected end of ZIP entry: " + entryName);
            return new byte[0];
        }
        return result;
    }

    /**
     * Closes the dictionary's resources file and releases its underlying resources.
     *
     * <p>If the resources file is not currently open, this method does nothing.
     * After the file is closed, the internal file reference is cleared so that
     * the resources file can be reopened when it is needed again.</p>
     */
    public void close() {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                Log.e(TAG, "Cannot close dictionary res.zip file", e);
            } finally {
                zipFile = null;
            }
        }
        if (compressZipFile != null) {
            try {
                compressZipFile.close();
            } catch (IOException e) {
                Log.e(TAG, "Cannot close dictionary res.zip file", e);
            } finally {
                compressZipFile = null;
            }
        }
    }
}
