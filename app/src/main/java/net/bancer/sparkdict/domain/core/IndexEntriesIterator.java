package net.bancer.sparkdict.domain.core;

import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.logging.ConsoleLogger;
import net.bancer.sparkdict.logging.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * IndexEntriesIterator retrieves index entries from the index file.
 */
public class IndexEntriesIterator implements Iterator<IndexEntry> {

    private static final String TAG = "IndexEntriesIterator";

    /**
     * Maximum number of suggestions per dictionary.
     */
    public static final int MAX = 40;

    /**
     * Number of lexical entries in the dictionary.
     */
    private final long size;

    private final SparkDictIndex sparkDictIndex;

    /**
     * Logger writes messages to logs.
     */
    private final Logger logger;

    /**
     * Quantity of index entries already found.
     */
    private int count = 0;

    /**
     * Position of the last retrieved index entry. Cursor starts from 0.
     */
    private long cursor = -1;

    private String lastSearchedSuggestion = "";

    /**
     * Constructor.
     *
     * @param bookInfo BookInfo object.
     * @param logger   Logger to write messages to logs.
     * @throws DomainException If the SparkDict index file cannot be opened or read.
     */
    public IndexEntriesIterator(BookInfo bookInfo, Logger logger) throws DomainException {
        this.logger = logger;
        sparkDictIndex = new SparkDictIndex(bookInfo);
        try {
            size = sparkDictIndex.getSize();
        } catch (IOException e) {
            String message = String.format(
                "Cannot get quantity of `%s` dictionary SparkDictIndex entries.",
                sparkDictIndex.getBookName()
            );
            throw new DomainException(message, e);
        }
    }

    /**
     * Constructor.
     *
     * @param bookInfo BookInfo object.
     * @throws DomainException If the SparkDict index file cannot be opened or read.
     */
    public IndexEntriesIterator(BookInfo bookInfo) throws DomainException {
        this(bookInfo, new ConsoleLogger());
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
            String message = String.format(
                "Current cursor position: %s, size: %s in %s",
                cursor,
                size,
                sparkDictIndex.getBookName()
            );
            throw new NoSuchElementException(message);
        }
        cursor++;
        try {
            return sparkDictIndex.getIndexEntry(cursor);
        } catch (IOException e) {
            String message = String.format(
                "Cannot get next index entry of `%s` dictionary SparkDictIndex; cursor: %s, size: %s",
                sparkDictIndex.getBookName(),
                cursor,
                size
            );
            logger.error(TAG, message, e);
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
     * @param prefix start of the lemma to be matched against.
     * @return index entry starting with provided prefix.
     * @throws DomainException If an error occurs while reading an index entry.
     */
    public IndexEntry nextSuggestion(String prefix) throws DomainException {
        if (!prefix.equals(lastSearchedSuggestion)) {
            IndexEntry entry;
            try {
                entry = findFirstMatchedByPrefix(prefix);
            } catch (IOException e) {
                String message = String.format(
                    "Cannot get next suggestion from `%s` dictionary SparkDictIndex; cursor: %s, size: %s",
                    sparkDictIndex.getBookName(),
                    cursor,
                    size
                );
                throw new DomainException(message, e);
            }
            lastSearchedSuggestion = prefix;
            return entry;
        } else if (count < MAX && hasNext()) {
            count++;
            IndexEntry entry = next();
            if (entry.compareTo(prefix, IndexEntry.PREFIX_MATCH) == 0) {
                return entry;
            } else {
                cursor--;
                return null;
            }
        }
        return null;
    }

    /**
     * Finds the first index entry matching the specified prefix.
     *
     * <p>Uses a binary search to locate a matching entry and continues searching
     * towards the beginning of the index to find the first matching entry.</p>
     *
     * @param query Prefix to search for.
     * @return First index entry matching the prefix, or {@code null} if no matching
     * entry is found or an index entry cannot be read.
     * @throws IOException If an error occurs while reading an index entry.
     */
    private IndexEntry findFirstMatchedByPrefix(String query) throws IOException {
        count = 1;
        IndexEntry result = null;
        IndexEntry indexEntry;
        long min = 0;
        long max = size - 1;
        while (min <= max) {
            long mid = (min + max) / 2;
            indexEntry = sparkDictIndex.getIndexEntry(mid);
            if (indexEntry == null) {
                //TODO: send notification - probably index file is missing.
                return null;
            }
            if (indexEntry.compareTo(query, IndexEntry.PREFIX_MATCH) == 0) {
                result = indexEntry;
                cursor = mid;
                max = mid - 1;
            } else if (indexEntry.compareTo(query, IndexEntry.PREFIX_MATCH) < 0) {
                min = mid + 1;
            } else if (indexEntry.compareTo(query, IndexEntry.PREFIX_MATCH) > 0) {
                max = mid - 1;
            }
        }
        return result;
    }

    /**
     * Retrieves the first IndexEntry matching the provided lemma.
     *
     * @param lemma entry word to be found.
     * @return index entry for the specified lemma.
     * @throws DomainException If the SparkDict index file cannot be opened or read.
     */
    public IndexEntry findIndexEntry(String lemma) throws DomainException {
        long min = 0;
        long max = size - 1;
        while (min <= max) {
            long mid = (min + max) / 2;
            try {
                IndexEntry indexEntry = sparkDictIndex.getIndexEntry(mid);
                if (indexEntry == null) {
                    //TODO: send notification - probably index file is missing.
                    return null;
                }
                if (indexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) == 0) {
                    cursor = mid;
                    long previousIdx = mid - 1;
                    if (previousIdx > 0) {
                        IndexEntry previousIndexEntry = sparkDictIndex.getIndexEntry(previousIdx);
                        while (
                            previousIndexEntry != null
                            && previousIndexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) == 0
                        ) {
                            cursor = previousIdx;
                            indexEntry = previousIndexEntry;
                            previousIdx--;
                            previousIndexEntry = sparkDictIndex.getIndexEntry(previousIdx);
                        }
                    }
                    return indexEntry;
                } else if (indexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) < 0) {
                    min = mid + 1;
                } else if (indexEntry.compareTo(lemma, IndexEntry.WORD_MATCH) > 0) {
                    max = mid - 1;
                }
            } catch (FileNotFoundException e) {
                String message = String.format(
                    "Cannot get index entry from `%s` dictionary SparkDictIndex; cursor: %s, size: %s",
                    sparkDictIndex.getBookName(),
                    cursor,
                    size
                );
                throw new DomainException(message, e);
            } catch (IOException e) {
                String message = String.format(
                    "Cannot get index entry from `%s` dictionary SparkDictIndex; cursor: %s, size: %s, mid: %s, min: %s, max: %s",
                    sparkDictIndex.getBookName(),
                    cursor,
                    size,
                    mid,
                    min,
                    max
                );
                throw new DomainException(message, e);
            }
        }
        return null;
    }
}
