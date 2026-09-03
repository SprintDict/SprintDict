package net.bancer.sparkdict.domain.core;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Provides access to resources stored in a dictionary {@code res.zip} archive.
 *
 * <p>The archive contains dictionary resources such as audio files and
 * pictures. The ZIP file is opened when this object is created and remains
 * open until {@link #close()} is called.</p>
 */
public class ResourcesZipFile implements Closeable {

    private static final int EOCD_SIGNATURE = 0x06054b50;
    private static final int CENTRAL_DIRECTORY_SIGNATURE = 0x02014b50;
    private static final int LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50;

    private static final int COMPRESSION_STORED = 0;
    private static final int COMPRESSION_DEFLATED = 8;

    private static final int EOCD_MIN_SIZE = 22;
    private static final int EOCD_MAX_COMMENT_SIZE = 65535;

    /**
     * ZIP archive containing the dictionary resources (audio and pictures).
     */
    private SeekableByteChannel resZipFileChannel;

    private Map<String, ZipEntryInfo> entries;

    /**
     * Opens a dictionary's res.zip file and initialises its decompression state.
     *
     * @param file relative path to the res.zip file.
     * @param dictionaryFiles the DictionaryFiles to read res.zip.
     */
    public ResourcesZipFile(String file, DictionaryFiles dictionaryFiles) {
        try {
            this.resZipFileChannel = dictionaryFiles.openForRead(file);
            initialiseEntries();
        } catch (IOException e) {
            // res.zip is optional -- not every dictionary has one.
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
        try {
            ZipEntryInfo entry = entries.get(entryName);
            if (entry == null) {
                return new byte[0];
            }
            return readEntry(entry);
        } catch (IOException e) {
            //TODO: log "Cannot read ZIP entry: " + entryName
            return new byte[0];
        }
    }

    /**
     * Closes the dictionary's resources channel and releases its underlying resources.
     *
     * <p>If the resources channel is not currently open, this method does nothing.
     * After the channel is closed, the internal channel reference is cleared so that
     * the resources channel can be reopened when it is needed again.</p>
     */
    @Override
    public void close() {
        if (resZipFileChannel != null) {
            try {
                resZipFileChannel.close();
            } catch (IOException e) {
                //TODO: log "Cannot close .dict.dz channel"
            } finally {
                resZipFileChannel = null;
            }
        }
    }

    private static int getInt(ByteBuffer buffer) {
        return buffer.getInt();
    }

    private static int getIntAt(ByteBuffer buffer, int offset) {
        return buffer.getInt(offset);
    }

    private static int getUnsignedShort(ByteBuffer buffer) {
        return Short.toUnsignedInt(buffer.getShort());
    }

    private static long getUnsignedInt(ByteBuffer buffer) {
        return Integer.toUnsignedLong(buffer.getInt());
    }

    private static void skip(ByteBuffer buffer, int bytes) {
        buffer.position(buffer.position() + bytes);
    }

    /**
     * Reads and indexes the ZIP central directory.
     */
    private void initialiseEntries() throws IOException {
        if (entries != null) {
            return;
        }

        long fileSize = resZipFileChannel.size();

        if (fileSize < EOCD_MIN_SIZE) {
            throw new IOException("Invalid ZIP file: file is too small");
        }

        long eocdOffset = findEndOfCentralDirectory(fileSize);
        resZipFileChannel.position(eocdOffset);

        ByteBuffer eocd = readBuffer(EOCD_MIN_SIZE);

        int signature = getInt(eocd);
        if (signature != EOCD_SIGNATURE) {
            throw new IOException("Invalid ZIP end of central directory");
        }

        skip(eocd, 4); // disk number + central-directory disk number

        int entryCountOnDisk = getUnsignedShort(eocd);
        int entryCount = getUnsignedShort(eocd);

        long centralDirectorySize = getUnsignedInt(eocd);
        long centralDirectoryOffset = getUnsignedInt(eocd);

        int commentLength = getUnsignedShort(eocd);

        if (entryCountOnDisk != entryCount) {
            throw new IOException("Multi-disk ZIP files are not supported");
        }

        if (commentLength > 0) {
            // The comment is irrelevant to us.
        }

        if (centralDirectoryOffset + centralDirectorySize > fileSize) {
            throw new IOException("Invalid ZIP central directory");
        }

        if (entryCount == 0) {
            entries = new HashMap<>();
            return;
        }

        if (centralDirectorySize > Integer.MAX_VALUE) {
            throw new IOException("ZIP central directory is too large");
        }

        resZipFileChannel.position(centralDirectoryOffset);

        ByteBuffer directory = readBuffer((int) centralDirectorySize);
        Map<String, ZipEntryInfo> result = new HashMap<>(entryCount);

        for (int i = 0; i < entryCount; i++) {
            readCentralDirectoryEntry(directory, result);
        }

        entries = result;
    }

    /**
     * Finds the End of Central Directory record.
     */
    private long findEndOfCentralDirectory(long fileSize) throws IOException {
        long searchSize = Math.min(
            fileSize,
            EOCD_MIN_SIZE + EOCD_MAX_COMMENT_SIZE
        );

        long searchStart = fileSize - searchSize;

        resZipFileChannel.position(searchStart);

        ByteBuffer buffer = readBuffer((int) searchSize);

        for (int i = buffer.limit() - EOCD_MIN_SIZE; i >= 0; i--) {
            if (getIntAt(buffer, i) == EOCD_SIGNATURE) {
                return searchStart + i;
            }
        }

        throw new IOException("ZIP end of central directory not found");
    }

    /**
     * Reads one entry from the central directory.
     */
    private void readCentralDirectoryEntry(
        ByteBuffer buffer,
        Map<String, ZipEntryInfo> result
    ) throws IOException {
        int signature = getInt(buffer);

        if (signature != CENTRAL_DIRECTORY_SIGNATURE) {
            throw new IOException("Invalid ZIP central directory entry");
        }

        skip(buffer, 2); // version made by
        skip(buffer, 2); // version needed to extract

        int flags = getUnsignedShort(buffer);
        int compressionMethod = getUnsignedShort(buffer);

        skip(buffer, 2); // modification time
        skip(buffer, 2); // modification date
        skip(buffer, 4); // CRC-32

        long compressedSize = getUnsignedInt(buffer);
        long uncompressedSize = getUnsignedInt(buffer);

        int fileNameLength = getUnsignedShort(buffer);
        int extraFieldLength = getUnsignedShort(buffer);
        int commentLength = getUnsignedShort(buffer);

        skip(buffer, 2); // disk number start
        skip(buffer, 2); // internal attributes
        skip(buffer, 4); // external attributes

        long localHeaderOffset = getUnsignedInt(buffer);

        byte[] fileNameBytes = new byte[fileNameLength];
        buffer.get(fileNameBytes);

        String fileName = decodeFileName(fileNameBytes, flags);

        skip(buffer, extraFieldLength);
        skip(buffer, commentLength);

        result.put(
            fileName,
            new ZipEntryInfo(
                compressionMethod,
                compressedSize,
                uncompressedSize,
                localHeaderOffset
            )
        );
    }

    private String decodeFileName(byte[] bytes, int flags) {
        if ((flags & (1 << 11)) != 0) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and decompresses one ZIP entry.
     */
    private byte[] readEntry(ZipEntryInfo entry) throws IOException {
        resZipFileChannel.position(entry.localHeaderOffset);

        ByteBuffer localHeader = readBuffer(30);

        int signature = getInt(localHeader);

        if (signature != LOCAL_FILE_HEADER_SIGNATURE) {
            throw new IOException("Invalid ZIP local file header");
        }

        skip(localHeader, 2); // version needed
        int flags = getUnsignedShort(localHeader);
        int compressionMethod = getUnsignedShort(localHeader);

        if (compressionMethod != entry.compressionMethod) {
            throw new IOException("ZIP compression method mismatch");
        }

        skip(localHeader, 4); // modification time/date
        skip(localHeader, 4); // CRC-32
        skip(localHeader, 4); // compressed size
        skip(localHeader, 4); // uncompressed size

        int fileNameLength = getUnsignedShort(localHeader);
        int extraFieldLength = getUnsignedShort(localHeader);

        long dataOffset =
            entry.localHeaderOffset
                + 30L
                + fileNameLength
                + extraFieldLength;

        if (entry.compressedSize > Integer.MAX_VALUE) {
            throw new IOException("ZIP entry is too large");
        }

        resZipFileChannel.position(dataOffset);

        byte[] compressedData = readBytes((int) entry.compressedSize);

        if (compressionMethod == COMPRESSION_STORED) {
            return compressedData;
        }

        if (compressionMethod == COMPRESSION_DEFLATED) {
            return inflate(compressedData, entry.uncompressedSize);
        }

        throw new IOException(
            "Unsupported ZIP compression method: " + compressionMethod
        );
    }

    private byte[] inflate(byte[] compressedData, long uncompressedSize)
        throws IOException {

        if (uncompressedSize > Integer.MAX_VALUE) {
            throw new IOException("ZIP entry is too large");
        }

        Inflater inflater = new Inflater(true);
        inflater.setInput(compressedData);

        ByteArrayOutputStream output =
            new ByteArrayOutputStream((int) uncompressedSize);

        byte[] buffer = new byte[8192];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);

                if (count == 0) {
                    if (inflater.needsDictionary()) {
                        throw new IOException(
                            "ZIP entry requires an unsupported dictionary"
                        );
                    }

                    if (inflater.needsInput()) {
                        throw new IOException(
                            "Unexpected end of compressed ZIP entry"
                        );
                    }
                }

                output.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new IOException("Invalid deflated ZIP entry", e);
        } finally {
            inflater.end();
        }

        byte[] result = output.toByteArray();

        if (result.length != (int) uncompressedSize) {
            throw new IOException(
                "Unexpected ZIP entry size: expected "
                    + uncompressedSize
                    + ", got "
                    + result.length
            );
        }

        return result;
    }

    private ByteBuffer readBuffer(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.hasRemaining()) {
            int bytesRead = resZipFileChannel.read(buffer);

            if (bytesRead == -1) {
                throw new IOException("Unexpected end of ZIP file");
            }
        }

        buffer.flip();
        return buffer;
    }

    private byte[] readBytes(int size) throws IOException {
        ByteBuffer buffer = readBuffer(size);

        byte[] result = new byte[size];
        buffer.get(result);

        return result;
    }

    private static class ZipEntryInfo {

        private final int compressionMethod;
        private final long compressedSize;
        private final long uncompressedSize;
        private final long localHeaderOffset;

        private ZipEntryInfo(
            int compressionMethod,
            long compressedSize,
            long uncompressedSize,
            long localHeaderOffset
        ) {
            this.compressionMethod = compressionMethod;
            this.compressedSize = compressedSize;
            this.uncompressedSize = uncompressedSize;
            this.localHeaderOffset = localHeaderOffset;
        }
    }
}
