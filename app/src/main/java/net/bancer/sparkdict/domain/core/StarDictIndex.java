package net.bancer.sparkdict.domain.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * StarDictIndex is an abstraction of <dictionary name>.idx file.
 */
public class StarDictIndex {

    /**
     * Byte used to separate data type from data itself in <dictionary name>.dict file.
     */
    public static final char SEPARATOR = '\0';

    /**
     * Number of bits in one byte.
     */
    public static final int BITS_IN_BYTE = 8;

    private static final int BUFFER_SIZE = 1024; // 1kb buffer

    /**
     * Index offset size in bytes.
     * <p>
     * If the dictionary file version is "3.0.0" and "idxoffsetbits=64",
     * wordDataOffset is being 64-bits unsigned number in network byte order.
     * Otherwise, it is 32-bits.
     */
    private final int lexicalEntryOffsetFieldSizeInBytes;

    /**
     * Word Data Size in bytes;
     * <p>
     * wordDataSize should be 32-bits unsigned number in network byte order.
     */
    private final int lexicalEntrySizeFieldInBytes;

    private final String fileName;

    private byte[] starDictBuffer = null;
    private SeekableByteChannel starDictFile = null;

    private BookInfo bookInfo;

    /**
     * Constructor.
     *
     * @param info BookInfo object.
     */
    public StarDictIndex(BookInfo info) {
        this(info.getFileBaseName(), info.getIdxOffsetBits());
        this.bookInfo = info;
    }

    /**
     * Constructor for callers that already have an open, seekable channel
     * to the .idx data (for example, one opened via a Storage Access
     * Framework document) instead of a filesystem path.
     *
     * @param info    BookInfo object.
     * @param channel an open, readable, seekable channel over the .idx data.
     */
    public StarDictIndex(BookInfo info, SeekableByteChannel channel) {
        this(info.getFileBaseName(), info.getIdxOffsetBits());
        this.bookInfo = info;
        this.starDictFile = channel;
    }

    private StarDictIndex(String dictionaryFileBaseName, int idxOffsetBits) {
        this.lexicalEntryOffsetFieldSizeInBytes = idxOffsetBits / BITS_IN_BYTE;
        this.lexicalEntrySizeFieldInBytes = 4; // = 32/8
        this.fileName = dictionaryFileBaseName + ".idx";
    }

    private byte[] getStarDictBuffer() {
        if (starDictBuffer == null) {
            starDictBuffer = new byte[BUFFER_SIZE];
        }
        return starDictBuffer;
    }

    /**
     * Lazily opens the .idx file via this book's {@link DictionaryFiles},
     * working identically whether that resolves to a real filesystem path
     * or a Storage Access Framework document.
     */
    private SeekableByteChannel getStarDictFile() throws IOException {
        if (starDictFile == null) {
            starDictFile = bookInfo.getDictionaryFiles().openForRead(fileName);
        }
        return starDictFile;
    }

    /**
     * Retrieves index entry that starts at the provided position in
     * <dictionary name>.dict file.
     *
     * @param startPosition position where index entry starts.
     * @return IndexEntry object.
     * @throws IOException           if there was a problem reading data file.
     */
    public IndexEntry retrieveIndexEntry(long startPosition) throws IOException {
        int sizeRead;
        synchronized (getStarDictFile()) {
            getStarDictFile().position(startPosition);
            sizeRead = getStarDictFile().read(ByteBuffer.wrap(getStarDictBuffer(), 0, BUFFER_SIZE));
        }
        if (sizeRead > 0) {
            int bufferIndex = 0;
            while (bufferIndex < sizeRead) {
                if (getStarDictBuffer()[bufferIndex] == SEPARATOR) {
                    int indexEntryLength = bufferIndex + 1 + lexicalEntryOffsetFieldSizeInBytes + lexicalEntrySizeFieldInBytes;
                    return retrieveIndexEntry(getStarDictBuffer(), indexEntryLength);
                } else {
                    bufferIndex++;
                }
            }
        }
        return null;
    }

    /**
     * Creates an index entry by parsing its word, data offset, and data size
     * from the beginning of the specified buffer.
     *
     * @param buffer Buffer containing the index entry.
     * @param length Length of the index entry in bytes.
     * @return Parsed index entry.
     */
    private IndexEntry retrieveIndexEntry(byte[] buffer, int length) {
        int start = 0;
        int wordLength = length - 1 - lexicalEntryOffsetFieldSizeInBytes - lexicalEntrySizeFieldInBytes;
        String word = new String(buffer, start, wordLength, StandardCharsets.UTF_8);
        int dataOffsetStart = start + wordLength + 1;
        int dataOffset = bytesToInt(buffer, dataOffsetStart, lexicalEntryOffsetFieldSizeInBytes);
        int dataSizeStart = dataOffsetStart + lexicalEntryOffsetFieldSizeInBytes;
        int dataSize = bytesToInt(buffer, dataSizeStart, lexicalEntrySizeFieldInBytes);
        return new IndexEntry(word, dataOffset, dataSize, length);
    }

    /**
     * Converts a sequence of bytes to an integer.
     *
     * @param bytesArray Byte array containing the value.
     * @param start Start position of the value in the byte array.
     * @param length Number of bytes to convert.
     * @return Integer representation of the specified bytes.
     */
    private int bytesToInt(byte[] bytesArray, int start, int length) {
        return (int) bytesToLong(bytesArray, start, length);
    }

    /**
     * Converts a sequence of bytes to a long value.
     *
     * <p>Bytes are interpreted in big-endian order.</p>
     *
     * @param bytesArray Byte array containing the value.
     * @param start Start position of the value in the byte array.
     * @param length Number of bytes to convert.
     * @return Long representation of the specified bytes.
     */
    private long bytesToLong(byte[] bytesArray, int start, int length) {
        long result = 0;
        for (int j = start; j < start + length; j++) {
            result <<= 8;
            result |= bytesArray[j] & 0xff;
        }
        return result;
    }

    /**
     * Index file name getter.
     *
     * @return index file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Lexical entry offset field size in bytes getter.
     *
     * @return lexical entry offset field size in bytes
     */
    public int getLexicalEntryOffsetFieldSizeInBytes() {
        return lexicalEntryOffsetFieldSizeInBytes;
    }

    /**
     * Lexical entry size field in bytes getter.
     *
     * @return lexical entry size field in bytes.
     */
    public int getLexicalEntrySizeFieldInBytes() {
        return lexicalEntrySizeFieldInBytes;
    }

    /**
     * Book info getter.
     *
     * @return book info object.
     */
    public BookInfo getBookInfo() {
        return bookInfo;
    }

    /**
     * File base name getter.
     *
     * @return full path to the dictionary file without extension.
     */
    public String getFileBaseName() {
        return bookInfo.getFileBaseName();
    }
}
