package net.bancer.sparkdict.domain.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.bancer.sparkdict.domain.utils.DomainException;


/**
 * IndexEntriesIterator retrieves index entries from the index file.
 * 
 * @author Valerij Bancer
 *
 */
public class IndexEntriesIterator implements Iterator<IndexEntry> {

	/**
	 * Maximum number of suggestions per dictionary.
	 */
	public static final int MAX = 40;
	
	/**
	 * Quantity of index entries already found.
	 */
	private int count = 0;
	
	/**
	 * Position of the last retrieved index entry. Cursor starts from 0.
	 */
	private long cursor = -1;

	/**
	 * Number of lexical entries in the dictionary.
	 */
	private long size = 0;

	private String lastSearchedSuggestion = "";

	private SparkDictIndex sparkDictIndex;

	/**
	 * Constructor.
	 * 
	 * @param bookInfo
	 *            BookInfo object.
	 * @throws DomainException 
	 */
	public IndexEntriesIterator(BookInfo bookInfo) throws DomainException {
		sparkDictIndex = new SparkDictIndex(bookInfo);
		try {
			size = sparkDictIndex.getSize();
		} catch (FileNotFoundException e) {
			String message = "Cannot get quantity  of `" + bookInfo.getBookName()
					+ "` dictionary SparkDictIndex entries.";
			throw new DomainException(message, e);
		} catch (IOException e) {
			String message = "Cannot get quantity  of `" + bookInfo.getBookName()
					+ "` dictionary SparkDictIndex entries.";
			throw new DomainException(message, e);
		}
	}

	/**
	 * Returns true if there is at least one more element, false otherwise.
	 * 
	 * @see Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return (cursor < size - 1);
	}

	/**
	 * Returns the next object and advances the iterator.
	 * 
	 * @see Iterator#next()
	 */
	@Override
	public IndexEntry next() throws NoSuchElementException {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		cursor++;
		try {
			return sparkDictIndex.getIndexEntry(cursor);
		} catch (FileNotFoundException e) {
			String message = "Cannot get next index entry of `"
					+ sparkDictIndex.getBookName()
					+ "` dictionary SparkDictIndex; cursor: " + cursor
					+ ", size: " + size;
			//Log.e(TAG, message, e);
			e.printStackTrace();
		} catch (IOException e) {
			String message = "Cannot get next index entry of `"
					+ sparkDictIndex.getBookName()
					+ "` dictionary SparkDictIndex; cursor: " + cursor
					+ ", size: " + size;
			//Log.e(TAG, message, e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Not implemented. Throws UnsupportedOperationException.
	 * 
	 * @see Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves the next suggestion matching the provided prefix. If there was
	 * no previous request to this method or the previous request was for another
	 * prefix then the first match is returned.
	 * 
	 * @param prefix	start of the lemma to be matched against.
	 * @return			index entry starting with provided prefix.
	 * @throws DomainException 
	 */
	public IndexEntry nextSuggestion(String prefix) throws DomainException {
		if(!prefix.equals(lastSearchedSuggestion )) {
			IndexEntry entry = null;
			try {
				entry = findFirstMatchedByPrefix(prefix);
			} catch (FileNotFoundException e) {
				String message = "Cannot get next suggestion from `"
						+ sparkDictIndex.getBookName()
						+ "` dictionary SparkDictIndex; cursor: " + cursor
						+ ", size: " + size;
				throw new DomainException(message, e);
			} catch (IOException e) {
				String message = "Cannot get next suggestion from `"
						+ sparkDictIndex.getBookName()
						+ "` dictionary SparkDictIndex; cursor: " + cursor
						+ ", size: " + size;
				throw new DomainException(message, e);
			}
			lastSearchedSuggestion = prefix;
			return entry;
		} else if(count <= MAX && hasNext()) {
			count++;
			IndexEntry entry = next();
			if(entry.compareTo(prefix, IndexEntry.PREFIX_MATCH) == 0) {
				return entry;
			} else {
				cursor--;
				return null;
			}
		}
		return null;
	}

	private IndexEntry findFirstMatchedByPrefix(String query) throws FileNotFoundException, IOException {
		count = 1;
		IndexEntry result = null;
		IndexEntry indexEntry = null;
		long min = 0;
		long max = size - 1;
		while(min <= max) {
			long mid = (min + max)/2;
			indexEntry = sparkDictIndex.getIndexEntry(mid);
			if(indexEntry == null) {
//				System.out.println("query: " + query);
//				System.out.println("mid: " + mid);
//				System.out.println("max: " + max);
				//TODO: send notification - probably index file is missing.
				return null;
			}
			if(indexEntry.compareTo(query, IndexEntry.PREFIX_MATCH) == 0) {
				result = indexEntry;
				cursor = mid;
				max = mid - 1;
			} else if(indexEntry.compareTo(query, IndexEntry.PREFIX_MATCH) < 0) {
				min = mid + 1;
			} else if(indexEntry.compareTo(query, IndexEntry.PREFIX_MATCH) > 0) {
				max = mid - 1;
			}
		}
		return result;
	}
	
	/**
	 * Retrieves the first IndexEntry matching the provided lemma.
	 * 
	 * @param lemma
	 *            entry word to be found.
	 * @return index entry for the specified lemma.
	 * @throws DomainException 
	 */
	public IndexEntry findIndexEntry(String lemma) throws DomainException {
		long min = 0;
		long max = size - 1;
		while (min <= max) {
			long mid = (min + max) / 2;
			try {
				//System.out.println("mid: " + mid);
				IndexEntry indexEntry = sparkDictIndex.getIndexEntry(mid);
				if(indexEntry == null) {
					//TODO: send notification - probably index file is missing.
					return null;
				}
				if (indexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) == 0) {
					//System.out.println("mid: " + mid);
					//System.out.println("lemma: " + lemma);
					//System.out.println("entry: " + indexEntry.toString());
					//System.out.println("offset: " + indexEntry.getWordDataOffset());
					cursor = mid;
					long previousIdx = mid - 1;
					if (previousIdx > 0) {
						IndexEntry previousIndexEntry = sparkDictIndex
								.getIndexEntry(previousIdx);
						while (previousIndexEntry != null
								&& previousIndexEntry.compareTo(lemma,
										IndexEntry.WORD_MATCH) == 0) {
							cursor = previousIdx;
							indexEntry = previousIndexEntry;
							previousIdx--;
							previousIndexEntry = sparkDictIndex
									.getIndexEntry(previousIdx);
						}
					}
					return indexEntry;
				} else if (indexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) < 0) {
					min = mid + 1;
				} else if (indexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) > 0) {
					max = mid - 1;
				}
			} catch (FileNotFoundException e) {
				String message = "Cannot get index entry from `"
						+ sparkDictIndex.getBookName()
						+ "` dictionary SparkDictIndex; cursor: " + cursor
						+ ", size: " + size;
				throw new DomainException(message, e);
			} catch (IOException e) {
				String message = "Cannot get index entry from `"
						+ sparkDictIndex.getBookName()
						+ "` dictionary SparkDictIndex; cursor: " + cursor
						+ ", size: " + size + ", mid: " + mid + ", min: " + min + ", max: " + max;
				throw new DomainException(message, e);
			}
		}
		return null;
	}
}
