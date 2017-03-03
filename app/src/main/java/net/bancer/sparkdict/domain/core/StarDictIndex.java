package net.bancer.sparkdict.domain.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

/**
 * StarDictIndex is an abstraction of <dictionary name>.idx file.
 * 
 * @author Valerij Bancer
 *
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

	private byte[] starDictBuffer = null;
	private RandomAccessFile starDictFile = null;

	
	/**
	 * Index offset size in bytes.
	 *
	 * If the dictionary file version is "3.0.0" and "idxoffsetbits=64",
	 * wordDataOffset is be 64-bits unsigned number in network byte order.
	 * Otherwise it is 32-bits.
	 */
	private int lexicalEntryOffsetFieldSizeInBytes;

	/**
	 * Word Data Size in bytes;
	 * 
	 * wordDataSize should be 32-bits unsigned number in network byte order.
	 */
	private int lexicalEntrySizeFieldInBytes;

	private String fileName;

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

	private StarDictIndex(String dictionaryFileBaseName, int idxOffsetBits) {
		this.lexicalEntryOffsetFieldSizeInBytes = idxOffsetBits/BITS_IN_BYTE;
		this.lexicalEntrySizeFieldInBytes = 4; // = 32/8
		this.fileName = dictionaryFileBaseName + ".idx";
	}

	private byte[] getStarDictBuffer() {
		if(starDictBuffer == null) {
			starDictBuffer = new byte[BUFFER_SIZE];
		}
		return starDictBuffer;
	}
	
	private RandomAccessFile getStarDictFile() throws FileNotFoundException {
		if(starDictFile == null) {
			starDictFile = new RandomAccessFile(fileName, "r");
		}
		return starDictFile;
	}

	/**
	 * Retrieves index entry that starts at the provided position in
	 * <dictionary name>.dict file.
	 * 
	 * @param startPosition position where index entry starts.
	 * @return				IndexEntry object.
	 * @throws IOException	if there was a problem reading data file.
	 * @throws FileNotFoundException	if the data file was not found.
	 */
	public IndexEntry retrieveIndexEntry(long startPosition)
			throws IOException, FileNotFoundException {
		//System.out.println("start position: " + startPosition);
		int sizeRead = -1;
		synchronized (getStarDictFile()) {
			getStarDictFile().seek(startPosition);
			sizeRead = getStarDictFile().read(getStarDictBuffer(), 0, BUFFER_SIZE);
		}
		//System.out.println("size read: " + sizeRead);
		if(sizeRead > 0) {
			int bufferIndex = 0;
			while(bufferIndex < sizeRead) {
				if(getStarDictBuffer()[bufferIndex] == SEPARATOR) {
					int indexEntryLength = bufferIndex + 1 + lexicalEntryOffsetFieldSizeInBytes + lexicalEntrySizeFieldInBytes;
					return retrieveIndexEntry(getStarDictBuffer(), 0, indexEntryLength);
				} else {
					bufferIndex++;
				}
			}
		}
		//System.out.println("start position: " + startPosition);
		//System.out.println("dict: " + bookInfo.getBookName());
		//System.out.println("size read: " + sizeRead);
		//System.out.println("file length: " + getStarDictFile().length());
		//System.out.println("file pointer: " + getStarDictFile().getFilePointer());
		return null;
	}

	private IndexEntry retrieveIndexEntry(byte[] buffer, int start,
			int length) {
		int wordLength = length - 1 - lexicalEntryOffsetFieldSizeInBytes - lexicalEntrySizeFieldInBytes;
		String word = "";
		try {
			word = new String(buffer, start, wordLength, "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		int dataOffsetStart = start + wordLength + 1;
		int dataOffset = bytesToInt(buffer, dataOffsetStart, lexicalEntryOffsetFieldSizeInBytes);
				
		int dataSizeStart = dataOffsetStart + lexicalEntryOffsetFieldSizeInBytes;
		int dataSize = bytesToInt(buffer, dataSizeStart, lexicalEntrySizeFieldInBytes);

		return new IndexEntry(word, dataOffset, dataSize, length);
	}

	/**
	 * @param bytesArray
	 * @param start
	 * @param length
	 * @return
	 */
	private int bytesToInt(byte[] bytesArray, int start, int length) {
		return (int) bytesToLong(bytesArray, start, length);
	}

	/**
	 * @param bytesArray
	 * @param start
	 * @param length
	 * @return
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
	 * @return  lexical entry size field in bytes.
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
