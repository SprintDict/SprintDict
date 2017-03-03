package net.bancer.sparkdict.domain.core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import net.bancer.sparkdict.domain.utils.DomainException;

import android.util.Log;

/**
 * Book is an abstraction of a dictionary containing lexical entries and index
 * entries.
 * 
 * @author Valerij Bancer
 *
 */
public class Book implements Iterable<IndexEntry> {

	/**
	 * Extension of the compressed dictionary file: <dictionary name>.dict.dz
	 */
	private static final String DICT_FILE_EXTENSION = ".dict.dz";

	/**
	 * BookInfo object.
	 */
	private BookInfo bookInfo;

	/**
	 * Flag indicating whether the dictionary is enabled (searchable) or not.
	 */
	private boolean enabled = false;

	/**
	 * DictZipFile object.
	 */
	private DictZipFile dzFile = null;

	/**
	 * Index entries iterator.
	 */
	private Iterator<IndexEntry> indexEntriesIterator;

	/**
	 * Constructor.
	 * 
	 * @param infoFile book info as java.io.File object
	 * @throws IllegalArgumentException if the parameter is null.
	 */
	public Book(File infoFile) {
		if(infoFile == null){
			throw new IllegalArgumentException("infoFile must not be null");
		}
		bookInfo = new BookInfo(infoFile);
	}

	/**
	 * BookInfo getter.
	 * @return	bookInfo object.
	 */
	public BookInfo getInfo() {
		return bookInfo;
	}

	/**
	 * Enabled flag field mutator.
	 * 
	 * @param enabled	`true` to enable, `false` to disable.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Enabled flag field getter.
	 * 
	 * @return	`true` if the book is enabled, else `false`.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Lexical entry retriever. Retrieves and constructs LexicalEntry 
	 * corresponding to the index entry provided as the parameter.
	 * 
	 * @param idxEntry	index entry for what the lexical entry is requested.
	 * @return			LexicalEntry object or `null`.
	 */
	private LexicalEntry getLexicalEntry(IndexEntry idxEntry) {
		LexicalEntry result = null;
		if(dzFile == null){
			dzFile = new DictZipFile(bookInfo.getFileBaseName() + DICT_FILE_EXTENSION);
		}
		byte[] buffer = new byte[idxEntry.getWordDataSize()];
		try {
			buffer = dzFile.read(idxEntry.getWordDataOffset(), idxEntry.getWordDataSize());
			String lemma = idxEntry.getLemma();
			result = new LexicalEntry(lemma, buffer, bookInfo);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		} finally {
			buffer = null;
		}
		return result;
	}

	/**
	 * Book name getter.
	 * 
	 * @return	the book title.
	 */
	public String getBookName() {
		return bookInfo.getBookName();
	}

	/**
	 * String representation of the book.
	 */
	@Override
	public String toString() {
		return bookInfo.toString() + "Enabled: " + enabled + "\n";
	}

	/**
	 * Getter of the quantity of lexical entries.
	 * 
	 * @return	the quantity of lexical entries in this dictionary.
	 */
	public int getLexicalEntriesQuantity() {
		return bookInfo.getWordCount();
	}

	/**
	 * Lexical entry retriever. Retrieves and constructs LexicalEntry 
	 * that matches the string provided as the parameter.
	 * 
	 * @param lemma		lemma of the lexical entry to be retrieved.
	 * @return			LexicalEntry if the match is found, else `null`.
	 * @throws DomainException 
	 */
	public LexicalEntry getLexicalEntry(String lemma) throws DomainException {
		LexicalEntry result = null;
		IndexEntriesIterator iterator = (IndexEntriesIterator) iterator();
		if(iterator == null) {
			//TODO: send notification - probably index file is missing.
			return result;
		}
		IndexEntry indexEntry = iterator.findIndexEntry(lemma);
		while(indexEntry != null && indexEntry.getLemma().equals(lemma)) {
			LexicalEntry lexicalEntry = getLexicalEntry(indexEntry);
			if (result == null) {
				result = lexicalEntry;
			} else {
				String definitions = result.getDefinitions() + "<br><br>" + lexicalEntry.getDefinitions();
				result.setDefinitions(definitions);
			}
			indexEntry = iterator.next();
		}
		return result;
	}

	/**
	 * Constructs SparkDictIndex.
	 * 
	 * @param observer
	 *            observer to be attached to SparkDictIndex object.
	 * @throws DomainException
	 */
	public void buildSparkDictIndex(IObserver observer) throws DomainException {
		SparkDictIndex sparkDictIndex = new SparkDictIndex(bookInfo);
		sparkDictIndex.registerObserver(observer);
		try {
			sparkDictIndex.buildIndex();
		} catch (IOException e) {
			throw new DomainException("Cannot build index for `"
					+ bookInfo.getBookName() + "` dictionary.", e);
		}
	}

	/**
	 * Returns an Iterator for index entries.
	 */
	@Override
	public Iterator<IndexEntry> iterator() {
		if(indexEntriesIterator == null) {
			try {
				indexEntriesIterator = new IndexEntriesIterator(bookInfo);
			} catch (DomainException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return indexEntriesIterator;
	}

	/**
	 * Retrieves a list of index entries which lemmas starts with provided prefix.
	 * 
	 * @param prefix	prefix to be matched.
	 * @return			a collection of index entries.
	 */
	public Vector<IndexEntry> getSuggestions(String prefix) {
		Vector<IndexEntry> result = new Vector<IndexEntry>(IndexEntriesIterator.MAX);
		IndexEntriesIterator iterator = (IndexEntriesIterator) iterator();
		if(iterator == null) {
			//TODO: send notification - probably index file is missing.
			return result;
		}
		String[] prefixVariations;
		if(isAsciiString(prefix)) {
			prefixVariations = new String[1];
			prefixVariations[0] = prefix;
		} else { // get suggestions for all variations of the prefix (aspROvided, lowercase, UPPERCASE, Capitalized)
			prefixVariations = getPrefixVariations(prefix);
		}
		for (int i = 0; i < prefixVariations.length; i++) {
			if(prefixVariations[i] != null) {
				try {
					IndexEntry entry = iterator
							.nextSuggestion(prefixVariations[i]);
					while (entry != null) {
						result.add(entry);
						entry = iterator.nextSuggestion(prefixVariations[i]);
					}
				} catch (DomainException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	/**
	 * Figures out what different strings could match the provided string
	 * if some of the letters would be in upper case.
	 * 
	 * @param prefix	string to be matched.
	 * @return			strings array of different variations of the prefix.
	 */
	private static String[] getPrefixVariations(String prefix) {
		String[] result = new String[4];
		result[0] = prefix;
		result[1] = prefix.toLowerCase();
		result[2] = prefix.toUpperCase();
		result[3] = capitalizeString(prefix);
		for (int i = 1; i < result.length; i++) {
			if(prefix.equals(result[i])) {
				result[i] = null;
			}
		}
		return result;
	}

	/**
	 * Converts the first letter of every word in the provided string to upper case.
	 * 
	 * @param prefix	string to be capitalized.
	 * @return			capitalized string.
	 */
	private static String capitalizeString(String prefix) {
		char[] chars = prefix.toLowerCase().toCharArray();
		boolean previousCharIsLetter = false;
		for (int i = 0; i < chars.length; i++) {
			if(Character.isLetter(chars[i])) {
				if(!previousCharIsLetter) {
					chars[i] = Character.toUpperCase(chars[i]);
				}
				previousCharIsLetter = true;
			} else {
				previousCharIsLetter = false;
			}
		}
		return String.valueOf(chars);
	}
	
	/**
	 * Checks if the string contains only ASCII characters.
	 * 
	 * @param str	string to be verified.
	 * @return		`true` if the string contains only ASCII characters, else `false`.
	 */
	private static boolean isAsciiString(String str) {
		int length = str.length();
		for (int i = 0; i < length; i++) {
			char c = str.charAt(i);
			if((int) c > 127) { // if contains non-ASCII character
				return false;
			}
		}
		return true;
	}
}
