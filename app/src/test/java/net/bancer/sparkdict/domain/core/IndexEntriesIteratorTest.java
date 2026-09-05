package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.bancer.sparkdict.Fixtures;
import net.bancer.sparkdict.domain.utils.DomainException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class IndexEntriesIteratorTest {

    private IndexEntriesIterator iterator;

    private DictionaryFiles dictionaryFiles;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        Fixtures.buildSparkDictIndex();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Fixtures.deleteSparkDictIndex();
    }

    @Test(expected = DomainException.class)
    public void constructorThrowsDomainExceptionWhenPathDoesNotExist() throws DomainException {
        new IndexEntriesIterator(
            new BookInfo("/path/that/does/not/exist/stardict.ifo", dictionaryFiles)
        );
    }

    @Test
    public void constructorThrowsDomainExceptionWhenIndexFileDoesNotExist() {
        BookInfo bookInfo = new BookInfo(
            Fixtures.TEST_DATA_PATH + "all-fields-ifo/stardict.ifo",
            dictionaryFiles
        );
        try {
            new IndexEntriesIterator(bookInfo);
            fail("Expected DomainException");
        } catch (DomainException e) {
            assertEquals(
                "Cannot get quantity of `Test Dictionary` dictionary SparkDictIndex entries.",
                e.getMessage()
            );
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Before
    public void setUp() throws DomainException {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE, dictionaryFiles);
        iterator = new IndexEntriesIterator(bookInfo);
    }

    @Test
    public void hasNextReturnsTrueWhenEntriesRemain() {
        assertTrue(iterator.hasNext());
    }

    @Test
    public void nextReturnsEntriesInIndexOrder() {
        IndexEntry first = iterator.next();
        assertNotNull(first);
        assertEquals("-able", first.getLemma());
        IndexEntry second = iterator.next();
        assertNotNull(second);
        assertEquals("-ably", second.getLemma());
        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    public void nextReturnsAllEntries() {
        List<IndexEntry> entries = new ArrayList<>();
        while (iterator.hasNext()) {
            entries.add(iterator.next());
        }
        assertFalse(entries.isEmpty());
        for (int i = 1; i < entries.size(); i++) {
            assertTrue(
                "Entries are not sorted at index " + i,
                entries.get(i - 1).compareTo(entries.get(i)) <= 0
            );
        }
    }

    @Test
    public void hasNextReturnsFalseAfterLastEntry() {
        while (iterator.hasNext()) {
            iterator.next();
        }
        assertFalse(iterator.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void nextThrowsExceptionWhenThereAreNoMoreEntries() {
        while (iterator.hasNext()) {
            iterator.next();
        }
        iterator.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeThrowsUnsupportedOperationException() {
        iterator.remove();
    }

    @Test
    public void nextSuggestionReturnsFirstMatchingEntry() throws DomainException {
        IndexEntry entry = iterator.nextSuggestion("aard");
        assertNotNull(entry);
        assertEquals("aard-wolf", entry.getLemma());
        assertEquals(0, entry.compareTo("aard", IndexEntry.PREFIX_MATCH));
    }

    @Test
    public void nextSuggestionReturnsSubsequentMatchingEntries() throws DomainException {
        IndexEntry first = iterator.nextSuggestion("aard");
        assertNotNull(first);
        assertEquals("aard-wolf", first.getLemma());
        assertEquals(0, first.compareTo("aard", IndexEntry.PREFIX_MATCH));
        IndexEntry second = iterator.nextSuggestion("aard");
        assertNotNull(second);
        assertEquals("aardvark", second.getLemma());
        assertEquals(0, second.compareTo("aard", IndexEntry.PREFIX_MATCH));
        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    public void nextSuggestionReturnsNullWhenPrefixDoesNotMatch() throws DomainException {
        assertNull(iterator.nextSuggestion("this-prefix-does-not-exist"));
    }

    @Test
    public void nextSuggestionStartsFromBeginningWhenPrefixChanges()
        throws DomainException {
        IndexEntry first = iterator.nextSuggestion("aard");
        assertNotNull(first);
        assertEquals("aard-wolf", first.getLemma());
        IndexEntry other = iterator.nextSuggestion("able");
        assertNotNull(other);
        assertEquals("able", other.getLemma());
        assertEquals(0, other.compareTo("able", IndexEntry.PREFIX_MATCH));
    }

    @Test
    public void nextSuggestionDoesNotReturnMoreThanMaximumSuggestions()
        throws DomainException {
        int suggestionCount = 0;
        while (iterator.nextSuggestion("a") != null) {
            suggestionCount++;
        }
        assertEquals(IndexEntriesIterator.MAX, suggestionCount);
    }

    @Test
    public void nextSuggestionReturnsNullWhenThereAreNoMoreMatchingEntries()
        throws DomainException {
        String prefix = "a";
        IndexEntry entry;
        do {
            entry = iterator.nextSuggestion(prefix);
        } while (entry != null);
        assertNull(iterator.nextSuggestion(prefix));
    }

    @Test
    public void nextSuggestionRestoresCursorWhenNextEntryDoesNotMatch() throws DomainException {
        IndexEntry first = iterator.nextSuggestion("abaculus");
        assertEquals("abaculus", first.getLemma());
        IndexEntry next = iterator.nextSuggestion("abaculus");
        assertNull(next);
    }

    @Test
    public void findIndexEntryReturnsMatchingEntry() throws DomainException {
        IndexEntry entry = iterator.findIndexEntry("aardvark");
        assertNotNull(entry);
        assertEquals(
            0,
            entry.compareTo("aardvark", IndexEntry.WORD_MATCH)
        );
    }

    @Test
    public void findIndexEntryReturnsNullWhenLemmaDoesNotExist() throws DomainException {
        assertNull(iterator.findIndexEntry("this-word-does-not-exist"));
    }

    @Test
    public void findIndexEntryReturnsFirstEntryWhenLemmaHasMultipleEntries() throws DomainException {
        IndexEntry entry = iterator.findIndexEntry("a");
        assertNotNull(entry);
        assertEquals(0, entry.compareTo("a", IndexEntry.WORD_MATCH));
    }
}
