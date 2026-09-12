package net.bancer.sparkdict.domain.core;

import net.bancer.sparkdict.logging.ConsoleLogger;
import net.bancer.sparkdict.logging.Logger;

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

    private static final String TAG = "ResourcesZipFile";

    private static final int EOCD_SIGNATURE = 0x06054b50;
    private static final int CENTRAL_DIRECTORY_SIGNATURE = 0x02014b50;
    private static final int LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50;

    private static final int COMPRESSION_STORED = 0;
    private static final int COMPRESSION_DEFLATED = 8;

    private static final int EOCD_MIN_SIZE = 22;
    private static final int EOCD_MAX_COMMENT_SIZE = 65535;

    private static final int ZIP64_EOCD_LOCATOR_SIGNATURE = 0x07064b50;
    private static final int ZIP64_EOCD_SIGNATURE = 0x06064b50;
    private static final int ZIP64_EOCD_LOCATOR_SIZE = 20;
    private static final int ZIP64_MAGIC_SHORT = 0xFFFF;
    private static final long ZIP64_MAGIC = 0xFFFFFFFFL;

    /**
     * Relative path to the res.zip file.
     */
    private final String file;

    /**
     * ZIP archive containing the dictionary resources (audio and pictures).
     */
    private SeekableByteChannel resZipFileChannel;

    private Map<String, ZipEntryInfo> entries;

    /**
     * Logger writes messages to logs.
     */
    private final Logger logger;

    /**
     * Opens a dictionary's res.zip file and initialises its decompression state.
     *
     * @param file relative path to the res.zip file.
     * @param dictionaryFiles the DictionaryFiles to read res.zip.
     */
    public ResourcesZipFile(String file, DictionaryFiles dictionaryFiles) {
        this(file, dictionaryFiles, new ConsoleLogger());
    }

    /**
     * Opens a dictionary's res.zip file and initialises its decompression state.
     *
     * @param file relative path to the res.zip file.
     * @param dictionaryFiles the DictionaryFiles to read res.zip.
     * @param logger   logger to write messages to logs.
     */
    public ResourcesZipFile(String file, DictionaryFiles dictionaryFiles, Logger logger) {
        this.file = file;
        this.logger = logger;
        try {
            this.resZipFileChannel = dictionaryFiles.openForRead(file);
            initialiseEntries();
        } catch (IOException e) {
            logger.error(TAG, "Cannot open resource ZIP: " + file, e);
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
                String message = String.format(
                    "Could not find ZIP entry %s in %s. Entries count: %s",
                    entryName,
                    file,
                    entries.size()
                );
                logger.error(TAG, message);
                return new byte[0];
            }
            return readEntry(entry);
        } catch (IOException e) {
            String message = String.format(
                "Cannot read ZIP entry %s in %s",
                entryName,
                file
            );
            logger.error(TAG, message, e);
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
                logger.error(TAG, "Cannot close dictionary res.zip file", e);
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

        long entryCountOnDisk = getUnsignedShort(eocd);
        long entryCount = getUnsignedShort(eocd);

        long centralDirectorySize = getUnsignedInt(eocd);
        long centralDirectoryOffset = getUnsignedInt(eocd);

        int commentLength = getUnsignedShort(eocd);

        // A ZIP64 archive escapes any classic EOCD field that would otherwise
        // overflow its 16- or 32-bit width by setting it to its maximum value,
        // recording the real value instead in a separate ZIP64 End Of Central
        // Directory Record. The record is found via a fixed-size locator that
        // always immediately precedes the classic EOCD record. This is exactly
        // what happens once a dictionary's res.zip -- e.g. a large audio-heavy
        // archive exceeds 65535 entries.
        if (
            entryCountOnDisk == ZIP64_MAGIC_SHORT
            || entryCount == ZIP64_MAGIC_SHORT
            || centralDirectorySize == ZIP64_MAGIC
            || centralDirectoryOffset == ZIP64_MAGIC
        ) {
            Zip64EndOfCentralDirectory zip64Eocd = readZip64EndOfCentralDirectory(eocdOffset);
            entryCountOnDisk = zip64Eocd.entryCountOnDisk;
            entryCount = zip64Eocd.entryCount;
            centralDirectorySize = zip64Eocd.centralDirectorySize;
            centralDirectoryOffset = zip64Eocd.centralDirectoryOffset;
        }

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

        if (entryCount > Integer.MAX_VALUE) {
            throw new IOException("ZIP central directory has too many entries");
        }

        resZipFileChannel.position(centralDirectoryOffset);

        ByteBuffer directory = readBuffer((int) centralDirectorySize);
        Map<String, ZipEntryInfo> result = new HashMap<>((int) entryCount);

        for (long i = 0; i < entryCount; i++) {
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

    /**
     * Reads the ZIP64 End Of Central Directory Record, used instead of the
     * classic EOCD's 16-/32-bit fields whenever the true entry count, central
     * directory size, or central directory offset exceeds what those fields
     * can represent.
     *
     * @param eocdOffset absolute offset of the classic End Of Central
     *                   Directory record. The fixed-size ZIP64 locator always
     *                   sits immediately before it.
     */
    private Zip64EndOfCentralDirectory readZip64EndOfCentralDirectory(long eocdOffset) throws IOException {
        long locatorOffset = eocdOffset - ZIP64_EOCD_LOCATOR_SIZE;
        if (locatorOffset < 0) {
            throw new IOException("ZIP64 end of central directory locator not found");
        }

        resZipFileChannel.position(locatorOffset);
        ByteBuffer locator = readBuffer(ZIP64_EOCD_LOCATOR_SIZE);

        if (getInt(locator) != ZIP64_EOCD_LOCATOR_SIGNATURE) {
            throw new IOException("ZIP64 end of central directory locator not found");
        }
        skip(locator, 4); // disk number with the start of the ZIP64 EOCD record
        long zip64EocdOffset = locator.getLong();

        resZipFileChannel.position(zip64EocdOffset);
        ByteBuffer record = readBuffer(56); // fixed portion of the ZIP64 EOCD record

        if (getInt(record) != ZIP64_EOCD_SIGNATURE) {
            throw new IOException("Invalid ZIP64 end of central directory record");
        }
        skip(record, 8); // size of the remaining record
        skip(record, 2); // version made by
        skip(record, 2); // version needed to extract
        skip(record, 4); // number of this disk
        skip(record, 4); // number of the disk with the start of the central directory

        long entryCountOnDisk = record.getLong();
        long entryCount = record.getLong();
        long centralDirectorySize = record.getLong();
        long centralDirectoryOffset = record.getLong();

        return new Zip64EndOfCentralDirectory(
            entryCountOnDisk, entryCount, centralDirectorySize, centralDirectoryOffset
        );
    }

    private static class Zip64EndOfCentralDirectory {
        private final long entryCountOnDisk;
        private final long entryCount;
        private final long centralDirectorySize;
        private final long centralDirectoryOffset;

        private Zip64EndOfCentralDirectory(
            long entryCountOnDisk, long entryCount,
            long centralDirectorySize, long centralDirectoryOffset
        ) {
            this.entryCountOnDisk = entryCountOnDisk;
            this.entryCount = entryCount;
            this.centralDirectorySize = centralDirectorySize;
            this.centralDirectoryOffset = centralDirectoryOffset;
        }
    }
}
