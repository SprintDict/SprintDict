package net.bancer.sparkdict.domain.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Vector;

/**
 * SparkDictIndex is an abstraction of <dictionary name>.sparkdict.idx file.
 * SparkDictIndex is very simple. It contains a sequence of 4 bytes elements
 * containing pointers to the index entries start at stardict index.
 */
public class SparkDictIndex implements IObservable {

    /**
     * File extension of the additional index file.
     */
    public static final String FILE_EXTENSION = ".sparkdict.idx";

    /**
     * Tag used to notify observer that a new SparkDict index entry has been created.
     */
    public static final String ARTICLES_INDEXED_TAG = "articlesIndexed";

    /**
     * The size of a single index entry in <dictionary name>.sparkdict.idx file.
     */
    public static final int INDEX_ENTRY_SIZE = 4;

    private static final int BUFFER_SIZE = 4096; // 4kb buffer

    private final Vector<IObserver> observers;

    private int articlesIndexed = 0;

    private final StarDictIndex starDictIndex;

    private final DictionaryFiles dictionaryFiles;

    private SeekableByteChannel sparkDictReadOnlyFile = null;

    private final byte[] sparkDictbuffer;

    /**
     * Constructor.
     *
     * @param bookInfo BookInfo object.
     */
    public SparkDictIndex(BookInfo bookInfo) {
        starDictIndex = new StarDictIndex(bookInfo);
        dictionaryFiles = bookInfo.getDictionaryFiles();
        observers = new Vector<>();
        sparkDictbuffer = new byte[SparkDictIndex.INDEX_ENTRY_SIZE];
    }

    /**
     * Converts integer to an array of bytes.
     * <p>
     * Adopted from: <a href="http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa">...</a>
     *
     * @param value integer to be converted into array of bytes.
     * @return    4-bytes array representing the provided integer.
     */
    public static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
    }

    /**
     * Converts 4-bytes array to an integer.
     * <p>
     * Adopted from: <a href="http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa">...</a>
     *
     * @param b 4-bytes array
     * @return    integer that was encoded by the provided array of bytes.
     */
    public static int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
            + ((b[1] & 0xFF) << 16)
            + ((b[2] & 0xFF) << 8)
            + (b[3] & 0xFF);
    }

    /**
     * Inspects <dictionary name>.idx file and creates new <dictionary
     * name>.sparkdict.idx file.
     *
     * @throws IOException If an I/O error occurs while reading the StarDict index
	 *     or writing the SparkDict index.
     */
    public void buildIndex() throws IOException {
        try (
            SeekableByteChannel starDictIdx = dictionaryFiles.openForRead(starDictIndex.getFileName());
            OutputStream sparkDictIdx = dictionaryFiles.createForWrite(starDictIndex.getFileBaseName() + FILE_EXTENSION)
        ) {
            parseBookIndex(starDictIndex, starDictIdx, sparkDictIdx);
        }
    }

    /**
     * Parses the StarDict index file and creates the corresponding SparkDict
     * index file, both resolved through this book's {@link DictionaryFiles}.
     *
     * <p>The write side only ever appends sequentially -- the original
     * {@code sparkDictPointer} tracking variable was dead code, since nothing
     * ever seeks the write handle a second time -- so a plain
     * {@link OutputStream} from {@link DictionaryFiles#createForWrite} is
     * sufficient; no random-access write capability is needed.</p>
     *
     * @param bookIndex StarDict index to parse.
     * @param starDictIdx channel to StarDict index file.
     * @param sparkDictIdx SparkDict output to be written to file.
     * @throws IOException If an I/O error occurs while reading the StarDict index
     * or writing the SparkDict index.
     */
    private void parseBookIndex(
        StarDictIndex bookIndex,
        SeekableByteChannel starDictIdx,
        OutputStream sparkDictIdx
    ) throws IOException {
        long starDictPointer = 0;
        byte[] starDictIdxBuffer = new byte[BUFFER_SIZE];
        starDictIdx.position(starDictPointer);
        int sizeRead = starDictIdx.read(ByteBuffer.wrap(starDictIdxBuffer, 0, BUFFER_SIZE));
        while (sizeRead > 0) {
            int wordStart = 0;
            int currentPosition = 0;
            while (currentPosition < sizeRead) {
                if (starDictIdxBuffer[currentPosition] == StarDictIndex.SEPARATOR) {
                    // Length = position of separator + 1 byte occupied by separator +
                    // index offset bytes size + data bytes size - start position
                    int length = currentPosition + 1 + bookIndex.getLexicalEntryOffsetFieldSizeInBytes() + bookIndex.getLexicalEntrySizeFieldInBytes() - wordStart;
                    if (wordStart + length <= sizeRead) {
                        writePointerToSparkdictIndex(starDictPointer, sparkDictIdx);
                        starDictPointer += length;
                    }
                    currentPosition = wordStart + length; // Move the pointer further
                    wordStart = currentPosition;
                } else {
                    currentPosition++;
                }
            }
            starDictIdx.position(starDictPointer);
            sizeRead = starDictIdx.read(ByteBuffer.wrap(starDictIdxBuffer, 0, BUFFER_SIZE));
        }
    }

    private void writePointerToSparkdictIndex(long pointer, OutputStream sparkDictIdx) throws IOException {
        sparkDictIdx.write(intToByteArray((int) pointer));
        articlesIndexed++;
        notifyObservers();
    }

    /**
     * @see IObservable#registerObserver(IObserver)
     */
    @Override
    public void registerObserver(IObserver o) {
        observers.add(o);
    }

    /**
     * @see IObservable#removeObserver(IObserver)
     */
    @Override
    public void removeObserver(IObserver o) {
        observers.remove(o);
    }

    /**
     * Notifies observers that "articles indexed" event has occurred.
     *
     * @see IObservable#notifyObservers()
     */
    @Override
    public void notifyObservers() {
        for (int i = 0; i < observers.size(); i++) {
            IObserver observer = observers.get(i);
            observer.update(ARTICLES_INDEXED_TAG, articlesIndexed);
        }
    }

    /**
     * Lazily opens the .sparkdict.idx file for random-access reads via this
     * book's {@link DictionaryFiles}.
     *
     * @return SparkDict index file opened for reading.
     * @throws IOException
     */
    private SeekableByteChannel getSparkDictReadOnlyFile() throws IOException {
        if (sparkDictReadOnlyFile == null) {
            String uri = starDictIndex.getFileBaseName() + SparkDictIndex.FILE_EXTENSION;
            sparkDictReadOnlyFile = dictionaryFiles.openForRead(uri);
        }
        return sparkDictReadOnlyFile;
    }

    /**
     * Gets the quantity of index entries.
     *
     * @return the quantity of index entries in the <dictionary name>.sparkdict.idx file.
     * @throws IOException If the SparkDict index file cannot be accessed.
     */
    public long getSize() throws IOException {
        return getSparkDictReadOnlyFile().size() / SparkDictIndex.INDEX_ENTRY_SIZE;
    }

    /**
     * Retrieves IndexEntry by provided sequence number.
     *
     * @param id sequence number of the index entry.
     * @return IndexEntry that is number `id` counting from the beginning of the
     * index file.
     * @throws IOException If the SparkDict index file cannot be accessed.
     */
    public IndexEntry getIndexEntry(long id) throws IOException {
        getSparkDictReadOnlyFile().position(id * SparkDictIndex.INDEX_ENTRY_SIZE);
        int sizeRead = getSparkDictReadOnlyFile().read(ByteBuffer.wrap(sparkDictbuffer));
        if (sizeRead > 0) {
            long startPosition = SparkDictIndex.byteArrayToInt(sparkDictbuffer);
            return starDictIndex.retrieveIndexEntry(startPosition);
        } else {
            return null;
        }
    }

    public String getBookName() {
        return starDictIndex.getBookInfo().getBookName();
    }

    public boolean delete() {
        String file = starDictIndex.getFileBaseName() + SparkDictIndex.FILE_EXTENSION;
        return dictionaryFiles.delete(file);
    }
}
