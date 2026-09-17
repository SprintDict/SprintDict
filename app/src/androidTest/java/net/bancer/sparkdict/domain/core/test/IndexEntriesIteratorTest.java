package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.nio.channels.ClosedByInterruptException;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexEntriesIteratorTest {

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() throws DomainException {
        Context context = ApplicationProvider.getApplicationContext();
        dictionaryFiles = SafDictionaryFilesFactory.create(context);
    }

    @Test
    public void testHasNextInBse() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        iterator.findIndexEntry("Яя (река)");
        assertTrue(iterator.hasNext());
    }

    @Test
    public void testHasNextInCambridge() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        iterator.findIndexEntry("abacus");
        assertTrue(iterator.hasNext());
    }

    @Test
    public void testHasNextInMueller() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        iterator.findIndexEntry("abacus");
        assertTrue(iterator.hasNext());
    }

    @Test
    public void testHasNextInWordnet() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        iterator.findIndexEntry("15 May Organization");
        assertTrue(iterator.hasNext());
    }

    @Test
    public void testHasNextOnLastElement() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("Яёи культура");
        assertEquals("Яёи культура", entry.getLemma());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testNext() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        iterator.findIndexEntry("15 May Organization");
        IndexEntry entry = iterator.next();
        assertEquals("1530s", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInBse() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
        }
        assertNotNull(entry);
        assertEquals("Яёи культура", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInCambridge() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
        }
        assertNotNull(entry);
        assertEquals("↑Zoos and wildlife reserves", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInMueller() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
        }
        assertNotNull(entry);
        assertEquals("усил.", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInWordnet() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
        }
        assertNotNull(entry);
        assertEquals("zymotic", entry.getLemma());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        iterator.remove();
    }

    @Test
    public void testNextSuggestion() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.nextSuggestion(".");
        assertEquals(".22 caliber", entry.getLemma());
    }

    @Test
    public void testNextSuggestionBSE() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.nextSuggestion("Собат");
        assertEquals("Собат", entry.getLemma());

        entry = iterator.nextSuggestion("собат");
        assertNull(entry);

        entry = iterator.nextSuggestion("СОБАТ");
        assertNull(entry);

        entry = iterator.nextSuggestion("...Биоз");
        assertEquals("...Биоз", entry.getLemma());

        entry = iterator.nextSuggestion("Яёи культура");
        assertEquals("Яёи культура", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryBse() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma());
        assertNotNull(entry);
        assertEquals(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryWordnet() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("15 May Organization");
        assertNotNull(entry);
        assertEquals("15 May Organization", entry.getLemma());
        assertEquals(906, entry.getWordDataOffset());
        assertEquals(213, entry.getWordDataSize());
    }

    @Test
    public void testFindIndexEntryFirstBse() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma());
        assertEquals(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryFirstMueller() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorMueller = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorMueller.findIndexEntry(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma());
        assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLast() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry(Mocks.BSE_INDEX_ENTRY_LAST.getLemma());
        assertEquals(Mocks.BSE_INDEX_ENTRY_LAST.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInBse() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("Яёи культура");
        assertEquals("Яёи культура", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInCambridge() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("↑Zoos and wildlife reserves");
        assertEquals("↑Zoos and wildlife reserves", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInMueller() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("усил.");
        assertEquals("усил.", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInWordnet() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("zymotic");
        assertEquals("zymotic", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryNonUnique() throws DomainException, ClosedByInterruptException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iterator.findIndexEntry("put away");
        assertNotNull(entry);
        assertEquals("put away", entry.getLemma());
        assertEquals(12519419, entry.getWordDataOffset());
        assertEquals(208, entry.getWordDataSize());
    }
}
