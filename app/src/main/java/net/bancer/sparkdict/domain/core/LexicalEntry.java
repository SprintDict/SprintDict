package net.bancer.sparkdict.domain.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

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
	 * Retrieves the resource identified by the resource name. The resource is
	 * read from a file or from a database into the bytes array. The resource
	 * could be an image or audio.
	 * 
	 * @param resourceName
	 *            resource to be read.
	 * @return resource in the form of bytes array.
	 */
	public byte[] getResource(String resourceName) {
		byte[] result = null;
		String path = bookInfo.getDirPath() + File.separator + "res"
				+ File.separator + resourceName;
		File file = new File(path);
		if (file.exists()) {
			RandomAccessFile raf = null;
			try {
				raf = new RandomAccessFile(path, "r");
				result = new byte[(int) raf.length()];
				raf.read(result);
			} catch (IOException e) {
				result = new byte[0];
			} finally {
				if (raf != null) {
					try {
						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println(file + " does not exist");
			File f = new File(bookInfo.getDirPath() + File.separator + "res.zip");
			if(f.exists()) {
				System.out.println(f + " exists");
				System.out.println("file size: " + f.length());
				ZipFile zip = null;
//				try {
//					
//					zip = new ZipFile(f);
//					
//					System.out.println("zip encoding: " + zip.getEncoding());
//					//zip = new ZipFile(bookInfo.getDirPath() + File.separator + "res.zip");
//					//System.out.println("zip name: " + zip.getName());
//					//System.out.println("zipfile size: " + zip.size());
//					//zip.entries();
//					//ZipEntry zipEntry = zip.getEntry("res/" + resourceName);
//					ZipArchiveEntry zipEntry = zip.getEntry("res/" + resourceName);
//					if(zipEntry != null) {
//						System.out.println("zipentry size: " + zipEntry.getSize());
//						result = new byte[(int) zipEntry.getSize()];
//						InputStream is = zip.getInputStream(zipEntry);
//						int bytesRead = is.read(result);
//						System.out.println("bytes read: " + bytesRead);
//					} else {
//						System.out.println("zipentry is null");
//						result = new byte[0];
//					}
//				} catch (ZipException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} finally {
//					if(zip != null) {
//						try {
//							zip.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
				
//				try {
//					ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
//					System.out.println("zis available: " + zis.available());
//					int s = 0;
//					ZipEntry ze;
//					while((ze = zis.getNextEntry()) != null) {
//						if(ze.getName().equals("res/" + resourceName)) {
//							System.out.println("FOUND!");
//						}
//						s++;
//					}
//					System.out.println("nothing found");
//					System.out.println("entries: " + s);
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
//				try {
//					Process process = Runtime.getRuntime().exec("unzip -p res.zip \"res/" +
//							"z_abacus.wav" +
//							"\"");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			} else {
				// read resources database
				result = new byte[0]; // TODO: correct size by getting it from idx
			}
		}
		return result;
	}
}
