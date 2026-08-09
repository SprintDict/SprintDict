package net.bancer.sparkdict.domain.core;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides access to resources stored in a dictionary {@code res.zip} archive.
 *
 * <p>The archive contains dictionary resources such as audio files and
 * pictures. The ZIP file is opened when this object is created and remains
 * open until {@link #close()} is called.</p>
 */
public class ResourcesZipFile {

    private static final String TAG = "ResourcesZipFile";

    /**
     * ZIP archive containing the dictionary resources (audio and pictures).
     */
    private ZipFile zipFile = null;

    /**
     * Opens a dictionary's res.zip file and initializes its decompression state.
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
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) {
            return new byte[0];
        }
        try (InputStream input = zipFile.getInputStream(entry)) {
            byte[] result = new byte[(int) entry.getSize()];
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
        } catch (IOException e) {
            Log.e(TAG, "Cannot read ZIP entry: " + entryName, e);
            return new byte[0];
        }
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
    }
}
