package net.bancer.sparkdict.domain.core;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Represents a chunk of data in a dictionary file.
 */
class Chunk {

    /**
     * Offset of the chunk in the dictionary file.
     */
    public int offset;

    /**
     * Size of the chunk in bytes.
     */
    public int size;

    /**
     * Creates a chunk.
     *
     * @param o Offset of the chunk in the dictionary file.
     * @param s Size of the chunk in bytes.
     */
    public Chunk(int o, int s) {
        offset = o;
        size = s;
    }
}

/**
 * DictZipFile is an abstraction of <dictionary name>.dict.dz file.
 */
public class DictZipFile implements Closeable {

    private static final int FHCRC = 2;

    private static final int FEXTRA = 4;

    private static final int FNAME = 8;

    private static final int FCOMMENT = 16;

    /**
     * First byte of the gzip file magic number.
     *
     * <p>The hexadecimal value {@code 0x1F} is represented as {@code 31}
     * when stored in Java's signed {@code byte} type.</p>
     */
    private static final byte GZIP_MAGIC_1 = 31;

    /**
     * Second byte of the gzip file magic number.
     *
     * <p>The hexadecimal value {@code 0x8B} is represented as {@code -117}
     * when stored in Java's signed {@code byte} type.</p>
     */
    private static final byte GZIP_MAGIC_2 = -117;

    /**
     * Gzip compression method identifier for DEFLATE.
     * In the gzip header, the byte immediately after the two-byte magic number
     * is the CM (Compression Method) field:
     * Offset  Size  Field
     * 0       1     ID1 = 0x1F
     * 1       1     ID2 = 0x8B
     * 2       1     CM  = 8 (DEFLATE)
     * 3       1     FLG
     */
    private static final byte GZIP_DEFLATE_METHOD = 8;

    /**
     * Offset of the chunk length field within the dictzip extra field.
     */
    private static final int DICTZIP_CHUNK_LENGTH_OFFSET = 6;

    /**
     * Offset of the chunk count field within the dictzip extra field.
     */
    private static final int DICTZIP_CHUNK_COUNT_OFFSET = 8;

    /**
     * Offset of the first chunk size within the dictzip extra field.
     */
    private static final int DICTZIP_CHUNK_SIZES_OFFSET = 10;

    /**
     * First byte of the dictzip extra-field identifier.
     */
    private static final byte DICTZIP_EXTENSION_ID_1 = 'R';

    /**
     * Second byte of the dictzip extra-field identifier.
     */
    private static final byte DICTZIP_EXTENSION_ID_2 = 'A';

    private List<Chunk> chunks;

    /**
     * Contents of <dictionary name>.dict.dz file.
     */
    private SeekableByteChannel dzFileChannel;

    private int pos;

    private int chlen = 0;

    private int pointerPosition;

    /**
     * Opens a dictionary .dict.dz file and initialises its decompression state,
     * reading through a channel instead of a filesystem path.
     *
     * @param file Relative path to dictionary's .dict.dz data.
     * @param dictionaryFiles the DictionaryFiles to read .dict.dz file.
     */
    public DictZipFile(String file, DictionaryFiles dictionaryFiles) throws IOException {
        dzFileChannel = dictionaryFiles.openForRead(file);
        initFromChannel(dzFileChannel);
    }

    /**
     * Initialises the dictionary zip file from the specified channel.
     *
     * @param channel the channel containing the dictionary zip file data
     * @throws IOException if an I/O error occurs while reading the gzip header
     */
    private void initFromChannel(SeekableByteChannel channel) throws IOException {
        this.dzFileChannel = channel;
        pos = 0;
        pointerPosition = 0;
        chunks = new ArrayList<>();
        this.readGZipHeader();
    }

    /**
     * Reads data from the current position into the specified buffer.
     *
     * <p>The requested data may span multiple compressed chunks. The required
     * chunks are decompressed and combined before the requested range is copied
     * into the destination buffer.</p>
     *
     * @param buff Buffer into which the data is read.
     * @param size Number of bytes to read.
     * @throws IOException If an error occurs while reading or decompressing a chunk.
     */
    private void read(byte[] buff, int size) throws IOException {
        if (size <= 0) {
            return;
        }
        int firstChunk = this.pos / this.chlen;
        int offset = this.pos - firstChunk * this.chlen;
        int lastChunk = (this.pos + size) / this.chlen;
        /*
         * int finish = 0;
         * int npos = 0;
         * finish = offset+size;
         * npos = this.pos+size;
         */
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        for (int i = firstChunk; i <= lastChunk; i++) {
            byteStream.write(this.readChunk(i));
        }
        byte[] buf = byteStream.toByteArray();
        System.arraycopy(buf, offset, buff, 0, size);
    }

    /**
     * Sets the current read position.
     *
     * @param pos Absolute position in the dictionary file.
     */
    private void seek(int pos) {
        this.pos = pos;
    }

    /**
     * Closes the dictionary file and releases its underlying resources.
     *
     * <p>If the dictionary file is not currently open, this method does nothing.
     * After the file is closed, the internal file reference is cleared so that
     * the dictionary file can be reopened when it is needed again.</p>
     */
    @Override
    public void close() {
        if (dzFileChannel != null) {
            try {
                dzFileChannel.close();
            } catch (IOException e) {
                //TODO: log "Cannot close .dict.dz channel"
            } finally {
                dzFileChannel = null;
            }
        }
    }

    /**
     * Reads and validates the gzip header and extracts dictzip chunk metadata.
     *
     * <p>The gzip header is validated for the expected magic number and
     * compression method. The dictzip extension is then parsed to determine
     * the uncompressed chunk length and the compressed size and offset of
     * each chunk. Optional filename, comment, and header CRC fields are
     * read and discarded when present.</p>
     *
     * @throws IOException If the gzip header is invalid, the compression
     *                     method is unsupported, the dictzip extension is missing or invalid,
     *                     or an error occurs while reading the header.
     */
    private void readGZipHeader() throws IOException {
        readGZipMagic();
        readCompressionMethod();
        byte flags = readHeaderFlags();
        readGZipHeaderFields();
        if ((flags & FEXTRA) == 0) {
            throw new IOException("Missing dictzip extension");
        }
        readDictZipExtension();
        if ((flags & FNAME) != 0) {
            //Read and discard a null-terminated string containing the filename
            readNullTerminatedHeaderField();
        }
        if ((flags & FCOMMENT) != 0) {
            //Read and discard a null-terminated string containing a comment
            readNullTerminatedHeaderField();
        }
        if ((flags & FHCRC) != 0) {
            readHeaderCrc();
        }
    }

    /**
     * Reads and validates the gzip magic number.
     *
     * @throws IOException If the file does not contain a valid gzip magic number.
     */
    private void readGZipMagic() throws IOException {
        byte[] buffer = new byte[2];
        channelRead(buffer);
        pointerPosition += 2;
        if (buffer[0] != GZIP_MAGIC_1 || buffer[1] != GZIP_MAGIC_2) {
            throw new IOException("Not a gzipped file");
        }
    }

    /**
     * Reads and validates the gzip compression method.
     *
     * @throws IOException If the compression method is not supported.
     */
    private void readCompressionMethod() throws IOException {
        byte compressionMethod = channelReadByte();
        pointerPosition += 1;
        if (compressionMethod != GZIP_DEFLATE_METHOD) {
            throw new IOException("Unknown compression method");
        }
    }

    /**
     * Reads the gzip header flags.
     *
     * @return Gzip header flags.
     * @throws IOException If the flags cannot be read.
     */
    private byte readHeaderFlags() throws IOException {
        byte flags = channelReadByte();
        pointerPosition += 1;
        return flags;
    }

    /**
     * Skips the fixed gzip header fields following the flags (MTIME, XFL, OS).
     *
     * <p>These fields contain the modification time, extra flags, and operating
     * system identifier. They are not required by this class.</p>
     *
     * @throws IOException If the header fields cannot be read.
     */
    private void readGZipHeaderFields() throws IOException {
        channelSkip(6);
        pointerPosition += 6;
    }

    /**
     * Reads the gzip extra field from the dictionary file.
     *
     * <p>The extra field length is read from the two-byte {@code XLEN} header
     * field, followed by the extra field data itself.</p>
     *
     * @return The contents of the gzip extra field.
     * @throws IOException If an error occurs while reading the extra field.
     */
    private byte[] readExtraField() throws IOException {
        int xlen = readUnsignedShort();
        byte[] extra = new byte[xlen];
        channelRead(extra);
        pointerPosition += 2 + xlen;
        return extra;
    }

    /**
     * Reads and parses the dictzip extra field.
     *
     * <p>The dictzip extension contains the uncompressed chunk length and
     * the compressed size of each chunk. The resulting chunks are added to
     * {@link #chunks}.</p>
     *
     * @throws IOException If the dictzip extension is missing or cannot be
     *                     parsed.
     */
    private void readDictZipExtension() throws IOException {
        byte[] extra = readExtraField();
        int extensionOffset = findDictZipExtension(extra, extra.length);
        chlen = readUnsignedShort(extra, extensionOffset + DICTZIP_CHUNK_LENGTH_OFFSET);
        int chunkCount = readUnsignedShort(extra, extensionOffset + DICTZIP_CHUNK_COUNT_OFFSET);
        int chunkOffset = DICTZIP_CHUNK_SIZES_OFFSET;
        int position = 0;
        for (int i = 0; i < chunkCount; i++) {
            int chunkSize = readUnsignedShort(extra, extensionOffset + chunkOffset);
            chunks.add(new Chunk(position, chunkSize));
            position += chunkSize;
            chunkOffset += 2;
        }
    }

    /**
     * Finds the dictzip extension within the gzip extra field.
     *
     * @param extra Gzip extra field data.
     * @param xlen  Length of the extra field.
     * @return Offset of the dictzip extension within {@code extra}.
     * @throws IOException If the dictzip extension cannot be found.
     */
    private int findDictZipExtension(byte[] extra, int xlen) throws IOException {
        int offset = 0;
        while (true) {
            int length = readUnsignedShort(extra, offset + 2);
            if (
                extra[offset] == DICTZIP_EXTENSION_ID_1 &&
                extra[offset + 1] == DICTZIP_EXTENSION_ID_2
            ) {
                return offset;
            }
            offset = 4 + length;
            if (offset > xlen) {
                throw new IOException("Missing dictzip extension");
            }
        }
    }

    /**
     * Reads an unsigned 16-bit little-endian value from the dictionary file.
     *
     * @return Unsigned 16-bit value.
     * @throws IOException If an error occurs while reading the value.
     */
    private int readUnsignedShort() throws IOException {
        int low = channelReadUnsignedByte();
        int high = channelReadUnsignedByte();
        return low + 256 * high;
    }

    /**
     * Reads an unsigned 16-bit little-endian value from a byte array.
     *
     * @param buffer Byte array containing the value.
     * @param offset Offset of the value in the byte array.
     * @return Unsigned 16-bit value.
     */
    private int readUnsignedShort(byte[] buffer, int offset) {
        int low = (buffer[offset] & 0xff);
        int high = (buffer[offset + 1] & 0xff);
        return low + 256 * high;
    }

    /**
     * Reads and discards a null-terminated gzip header field.
     *
     * @throws IOException If the field cannot be read.
     */
    private void readNullTerminatedHeaderField() throws IOException {
        byte value;
        do {
            value = channelReadByte();
            pointerPosition += 1;
        } while (value != 0);
    }

    /**
     * Reads and discards the gzip header CRC (16-bit).
     *
     * @throws IOException If the CRC cannot be read.
     */
    private void readHeaderCrc() throws IOException {
        channelSkip(2);
        pointerPosition += 2;
    }

    /**
     * Reads and decompresses a dictionary data chunk.
     *
     * @param n Zero-based index of the chunk to read.
     * @return Decompressed chunk data, or {@code null} if the specified chunk
     * index is outside the available chunks.
     * @throws IOException If an error occurs while seeking to or reading the
     *                     compressed chunk data.
     */
    private byte[] readChunk(int n) throws IOException {
        if (n >= this.chunks.size()) {
            return null;
        }
        dzFileChannel.position(this.pointerPosition + this.chunks.get(n).offset);
        int size = this.chunks.get(n).size;
        byte[] buff = new byte[size];
        channelRead(buff);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InflaterOutputStream gz = new InflaterOutputStream(bos, new Inflater(true));
        gz.write(buff);
        return bos.toByteArray();
    }

    /**
     * Reads part of the compressed dictionary file.
     *
     * @param offset uncompressed file offset where to start reading.
     * @param size   uncompressed size to be read.
     * @return byte array of the specified size.
     * @throws IOException if there was problem with reading the file.
     */
    public byte[] read(int offset, int size) throws IOException {
        byte[] result = new byte[size];
        seek(offset);
        read(result, size);
        return result;
    }

    // --- Channel primitives, replacing the RandomAccessFile-specific calls
    // that used to live inline above. Each mirrors the exact discard/EOF
    // behaviour the RandomAccessFile-based code had. ---

    /**
     * Reads bytes from the channel into the specified buffer.
     *
     * @param buffer the buffer to fill with bytes read from the channel
     * @throws IOException if an I/O error occurs while reading from the channel
     */
    private void channelRead(byte[] buffer) throws IOException {
        dzFileChannel.read(ByteBuffer.wrap(buffer));
    }

    /**
     * Reads a single byte from the channel.
     *
     * @return the byte read from the channel
     * @throws EOFException if the end of the channel is reached before a byte can be read
     * @throws IOException if an I/O error occurs while reading from the channel
     */
    private byte channelReadByte() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        int n = dzFileChannel.read(buf);
        if (n < 1) {
            throw new EOFException();
        }
        return buf.get(0);
    }

    /**
     * Reads a single unsigned byte from the channel.
     *
     * @return the unsigned byte value in the range 0 to 255
     * @throws EOFException if the end of the channel is reached before a byte can be read
     * @throws IOException if an I/O error occurs while reading from the channel
     */
    private int channelReadUnsignedByte() throws IOException {
        return channelReadByte() & 0xff;
    }

    /**
     * Advances the channel position by the specified number of bytes.
     *
     * @param n the number of bytes to skip
     * @throws IOException if an I/O error occurs while accessing the channel
     */
    private void channelSkip(int n) throws IOException {
        dzFileChannel.position(dzFileChannel.position() + n);
    }
}
