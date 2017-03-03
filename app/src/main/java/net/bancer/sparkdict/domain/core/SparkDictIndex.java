package net.bancer.sparkdict.domain.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;


/**
 * SparkDictIndex is an abstraction of <dictionary name>.sparkdict.idx file.
 * SparkDictIndex is very simple. It contains a sequence of 4 bytes elements
 * containing pointers to the index entries start at stardict index.
 * 
 * @author Valerij Bancer
 *
 */
public class SparkDictIndex implements IObservable {

	/**
	 * File extension of the additional index file.
	 */
	public static final String 	FILE_EXTENSION 		 = ".sparkdict.idx";
	
	/**
	 * Tag used to notify observer that a new SparkDict index entry has been created.
	 */
	public static final String 	ARTICLES_INDEXED_TAG = "articlesIndexed";

	/**
	 * The size of a single index entry in <dictionary name>.sparkdict.idx file.
	 */
	public static final int 	INDEX_ENTRY_SIZE 	= 4;
	private static final int 	BUFFER_SIZE 		= 4096; // 4kb buffer
	
	private Vector<IObserver> observers;
	private int articlesIndexed = 0;
	private StarDictIndex starDictIndex;
	//private RandomAccessFile sparkDictFile = null;
	//private byte[] sparkDictbuffer = null;
	private RandomAccessFile sparkDictReadOnlyFile = null;
	private byte[] sparkDictbuffer = null;

	/**
	 * Constructor.
	 * 
	 * @param bookInfo BookInfo object.
	 */
	public SparkDictIndex(BookInfo bookInfo) {
		starDictIndex = new StarDictIndex(bookInfo);
		observers = new Vector<IObserver>();
		//sparkDictbuffer = new byte[INDEX_ENTRY_SIZE];
		sparkDictbuffer = new byte[SparkDictIndex.INDEX_ENTRY_SIZE];
	}

	/**
	 * Inspects <dictionary name>.idx file and creates new <dictionary
	 * name>.sparkdict.idx file.
	 * 
	 * @throws IOException
	 */
	public void buildIndex() throws IOException {
		parseBookIndex(starDictIndex);
	}

	private void parseBookIndex(StarDictIndex bookIndex) throws IOException {
		long starDictPointer = 0;
		byte[] starDictIdxBuffer = new byte[BUFFER_SIZE];
		RandomAccessFile starDictIdx = new RandomAccessFile(bookIndex.getFileName(), "r");
		starDictIdx.seek(starDictPointer);
		int sizeRead = starDictIdx.read(starDictIdxBuffer, 0, BUFFER_SIZE);
		
		int sparkDictPointer = 0;
		String path = bookIndex.getFileBaseName() + FILE_EXTENSION;
		RandomAccessFile sparkDictIdx = new RandomAccessFile(path, "rw");
		sparkDictIdx.seek(sparkDictPointer);
		
		while(sizeRead > 0) {
			int wordStart = 0;
			int currentPosition = 0;
			while(currentPosition < sizeRead){
				if(starDictIdxBuffer[currentPosition] == StarDictIndex.SEPARATOR) {
					// Length = position of separator + 1 byte occupied by separator +
					// 			index offset bytes size + data bytes size - start position
					int length = currentPosition + 1 + bookIndex.getLexicalEntryOffsetFieldSizeInBytes() + bookIndex.getLexicalEntrySizeFieldInBytes() - wordStart;
					if(wordStart + length <= sizeRead) {
						writePointerToSparkdictIndex(starDictPointer, sparkDictIdx);
						sparkDictPointer += INDEX_ENTRY_SIZE;
						starDictPointer += length;
					}
					currentPosition = wordStart + length; // Move the pointer further
					wordStart = currentPosition;
				} else {
					currentPosition++;
				}
			}
			starDictIdx.seek(starDictPointer);
			sizeRead = starDictIdx.read(starDictIdxBuffer, 0, BUFFER_SIZE);
		}
		starDictIdx.close();
	}

	private void writePointerToSparkdictIndex(long pointer,
			RandomAccessFile spardictIdx) throws IOException {
		spardictIdx.write(intToByteArray((int)pointer));
		articlesIndexed++;
		notifyObservers();
	}

	/**
	 * Converts integer to an array of bytes.
	 * 
	 * Adopted from: http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa
	 * 
	 * @param value	integer to be converted into array of bytes.
	 * @return	4-bytes array representing the provided integer.
	 */
	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
	}

	/**
	 * Converts 4-bytes array to an integer.
	 * 
	 * Adopted from: http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa
	 * 
	 * @param b 4-bytes array
	 * @return	integer that was encoded by the provided array of bytes.
	 */
	public static final int byteArrayToInt(byte [] b) {
		return (b[0] << 24)
				+ ((b[1] & 0xFF) << 16)
				+ ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
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
		for(int i = 0; i < observers.size(); i++) {
			IObserver observer = observers.get(i);
			observer.update(ARTICLES_INDEXED_TAG, articlesIndexed);
		}
	}
	
	private RandomAccessFile getSparkDictReadOnlyFile() throws FileNotFoundException {
		if(sparkDictReadOnlyFile == null) {
			String uri = starDictIndex.getFileBaseName() + SparkDictIndex.FILE_EXTENSION;
			sparkDictReadOnlyFile = new RandomAccessFile(uri, "r");
		}
		return sparkDictReadOnlyFile;
	}
	
	/**
	 * Gets the quantity of index entries.
	 * 
	 * @return the quantity of index entries in the <dictionary
	 *         name>.sparkdict.idx file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public long getSize() throws FileNotFoundException, IOException {
		return getSparkDictReadOnlyFile().length()
				/ SparkDictIndex.INDEX_ENTRY_SIZE;
	}

	/**
	 * Retrieves IndexEntry by provided sequence number.
	 * 
	 * @param id
	 *            sequence number of the index entry.
	 * @return IndexEntry that is number `id` counting from the beginning of the
	 *         index file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public IndexEntry getIndexEntry(long id) throws FileNotFoundException,
			IOException {
		getSparkDictReadOnlyFile().seek(id*SparkDictIndex.INDEX_ENTRY_SIZE);
		int sizeRead = getSparkDictReadOnlyFile().read(sparkDictbuffer);
		if(sizeRead > 0) {
			long startPosition = SparkDictIndex.byteArrayToInt(sparkDictbuffer);
			//System.out.println("start: " + startPosition);
			return starDictIndex.retrieveIndexEntry(startPosition);
		} else {
			//System.out.println("size read: " + sizeRead);
			//System.out.println("id: " + id);
			return null;
		}
	}

	public String getBookName() {
		return starDictIndex.getBookInfo().getBookName();
	}
}
