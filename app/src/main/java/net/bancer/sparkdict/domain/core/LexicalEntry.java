package net.bancer.sparkdict.domain.core;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.bancer.sparkdict.domain.parsers.IParser;
import net.bancer.sparkdict.domain.parsers.ParsingStrategyFactory;

/**
 * LexicalEntry is the entry in a dictionary of information about a word
 * (syn: dictionary entry).
 * 
 * @author Valerij Bancer
 *
 */
public class LexicalEntry {

	private static final String TAG = "LexicalEntry";

	private static ParsingStrategyFactory parsersFactory = ParsingStrategyFactory.getInstance();

	private String lemma;

	private String definitions;

	private String dictTitle;

	private BookInfo bookInfo;

	/**
	 * Constructor.
	 * 
	 * @param lemma			lemma of the lexical entry.
	 * @param dataBlocks	bytes array of data from <dictionary name>.dict file.
	 * @param bookInfo		BookInfo object.
	 */
	public LexicalEntry(String lemma, byte[] dataBlocks, BookInfo bookInfo) {
		this.bookInfo = bookInfo;
		this.lemma = lemma;
		this.dictTitle = bookInfo.getBookName();
		String dataTypes = bookInfo.getSameTypeSequence();
		if(dataTypes == null) {
			dataTypes = "";
		}
		char[] dataType = dataTypes.toCharArray();
		if(dataType.length < 1) {
			setDefinitions(dataBlocks);
		} else if(dataType.length > 1) {
			setDefinitions(dataBlocks, dataType);
		} else if(dataType.length == 1) {
			setDefinitions(dataBlocks, dataType[0]);
		}
	}

	private void setDefinitions(byte[] dataBlocks) {
		definitions = "";
		int dataBlockStart = 0;
		int dataBlockLength = 0;
		char type = 'm';
		for(int i = 0; i <= dataBlocks.length; i++) {
			if(dataBlocks[i] == dataBlockStart) {
				type = (char) dataBlocks[i];
			}
			if(i == dataBlocks.length || dataBlocks[i] == StarDictIndex.SEPARATOR) {
				byte[] data = new byte[dataBlockLength-1];
				System.arraycopy(dataBlocks, dataBlockStart, data, 0, dataBlockLength-1);
				IParser parser = parsersFactory.getParser(type);
				definitions += parser.parse(data);
				dataBlockStart = i + 1;
				dataBlockLength = 0;
			} else {
				dataBlockLength++;
			}
		}
	}

	private void setDefinitions(byte[] dataBlocks, char[] dataTypes) {
		definitions = "";			
		int dataBlockStart = 0;
		int dataBlockLength = 0;
		int dataBlockIdx = 0;
		for(int i = 0; i <= dataBlocks.length; i++) {
			if(i == dataBlocks.length || dataBlocks[i] == StarDictIndex.SEPARATOR) {
				byte[] data = new byte[dataBlockLength];
				System.arraycopy(dataBlocks, dataBlockStart, data, 0, dataBlockLength);
				IParser parser = parsersFactory.getParser(dataTypes[dataBlockIdx]);
				definitions += parser.parse(data);
				dataBlockStart = i + 1;
				dataBlockLength = 0;
				dataBlockIdx++;
			} else {
				dataBlockLength++;
			}
		}
	}

	private void setDefinitions(byte[] dataBlock, char dataType) {
		IParser parser = parsersFactory.getParser(dataType);
		definitions = parser.parse(dataBlock);
	}

	/**
	 * Lemma getter.
	 * 
	 * @return lemma of the lexical entry.
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * Definitions getter.
	 * 
	 * @return the definitions of the lexical entry.
	 */
	public String getDefinitions() {
		return definitions;
	}

	/**
	 * Dictionary title getter.
	 * 
	 * @return dictionary title.
	 */
	public CharSequence getDictTitle() {
		return dictTitle;
	}

	/**
	 * LexicalEntry as a string (for debug).
	 */
	@Override
	public String toString() {
		return "[" + dictTitle + "," + lemma + "," + definitions + "]";
	}

	public void setDefinitions(String definitions) {
		this.definitions = definitions;
	}

	/**
	 * Retrieves the resource identified by the specified resource name.
	 *
	 * <p>If a {@code res.zip} archive exists in the dictionary directory, the
	 * resource is extracted from the archive. Otherwise, the resource is read
	 * from the {@code res} directory.</p>
	 *
	 * @param resourceName name of the resource to retrieve.
	 * @return resource contents as a byte array, or an empty byte array if the resource cannot be found or read.
	 */
	public byte[] getResource(String resourceName) {
		File zipFile = new File(bookInfo.getDirPath(), "res.zip");
		if(zipFile.exists()) {
			return extractResourceFromZipFile(zipFile, resourceName);
		}
		String path = bookInfo.getDirPath() + File.separator + "res" + File.separator + resourceName;
		File file = new File(path);
		if (file.exists()) {
			return readResourceFile(path);
		} else {
			Log.e(TAG, file + " does not exist");
		}
		return new byte[0];
	}

	/**
	 * Reads the contents of the specified resource file.
	 *
	 * @param path path to the resource file.
	 * @return resource contents as a byte array, or an empty byte array if the file cannot be read.
	 */
	private byte[] readResourceFile(String path) {
		byte[] result = new byte[0];
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path, "r");
			result = new byte[(int) raf.length()];
			raf.read(result);
		} catch (IOException e) {
			Log.e(TAG, "Cannot read resource file: " + path);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					Log.e(TAG, "Cannot close resource file: " + path);
				}
			}
		}
		return result;
	}

	/**
	 * Extracts the specified resource from a ZIP archive.
	 *
	 * <p>The ZIP archive is opened for the duration of this method and closed
	 * before the method returns. The resource is expected to be located under
	 * the {@code res/} directory inside the archive.</p>
	 *
	 * @param zipFile ZIP archive containing the resource.
	 * @param resourceName name of the resource to extract.
	 * @return resource contents as a byte array, or an empty byte array if the
	 *         archive cannot be opened or the resource cannot be read.
	 */
	private byte[] extractResourceFromZipFile(File zipFile, String resourceName) {
		byte[] result = new byte[0];
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			return getResourceFromZip(zip, resourceName);
		} catch (IOException e) {
			Log.e(TAG, "Cannot open resource ZIP: " + zipFile, e);
		} finally {
			if(zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					Log.e(TAG, "Cannot close resource ZIP", e);
				}
			}
		}
		return result;
	}

	/**
	 * Retrieves a resource from an already opened ZIP archive.
	 *
	 * <p>The resource is expected to be located under the {@code res/} directory
	 * inside the archive. The returned byte array contains the decompressed
	 * contents of the ZIP entry.</p>
	 *
	 * @param resourceZip open ZIP archive containing the resource.
	 * @param resourceName name of the resource to retrieve.
	 * @return resource contents as a byte array, or an empty byte array if the
	 *         specified entry does not exist or cannot be read.
	 */
	private byte[] getResourceFromZip(ZipFile resourceZip, String resourceName) {
		String entryName = "res/" + resourceName;
		ZipEntry entry = resourceZip.getEntry(entryName);
		if (entry == null) {
			return new byte[0];
		}
		try (InputStream input = resourceZip.getInputStream(entry)) {
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
}
