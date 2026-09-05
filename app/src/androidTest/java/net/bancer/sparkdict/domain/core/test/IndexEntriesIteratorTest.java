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
    public void testHasNextInBse() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        iteratorBSE.findIndexEntry("Яя (река)");
        assertTrue(iteratorBSE.hasNext());
    }

    @Test
    public void testHasNextInCambridge() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        iteratorWordnet.findIndexEntry("abacus");
        assertTrue(iteratorWordnet.hasNext());
    }

    @Test
    public void testHasNextInMueller() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        iteratorWordnet.findIndexEntry("abacus");
        assertTrue(iteratorWordnet.hasNext());
    }

    @Test
    public void testHasNextInWordnet() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        iteratorWordnet.findIndexEntry("15 May Organization");
        assertTrue(iteratorWordnet.hasNext());
    }

    @Test
    public void testHasNextOnLastElement() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry("Яёи культура");
        assertEquals("Яёи культура", entry.getLemma());
        assertFalse(iteratorBSE.hasNext());
    }

    @Test
    public void testNext() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        iteratorWordnet.findIndexEntry("15 May Organization");
        IndexEntry entry = iteratorWordnet.next();
        assertEquals("1530s", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInBse() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iteratorBSE.hasNext()) {
            entry = iteratorBSE.next();
        }
        assertNotNull(entry);
        assertEquals("Яёи культура", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInCambridge() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iteratorBSE.hasNext()) {
            entry = iteratorBSE.next();
        }
        assertNotNull(entry);
        assertEquals("↑Zoos and wildlife reserves", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInMueller() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iteratorBSE.hasNext()) {
            entry = iteratorBSE.next();
        }
        assertNotNull(entry);
        assertEquals("усил.", entry.getLemma());
    }

    @Test
    public void testNextUntilLastInWordnet() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = null;
        while (iteratorBSE.hasNext()) {
            entry = iteratorBSE.next();
        }
        assertNotNull(entry);
        assertEquals("zymotic", entry.getLemma());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        iteratorWordnet.remove();
    }

    @Test
    public void testNextSuggestion() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorWordnet.nextSuggestion(".");
        assertEquals(".22 caliber", entry.getLemma());
    }

    @Test
    public void testNextSuggestionBSE() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.nextSuggestion("Собат");
        assertEquals("Собат", entry.getLemma());

        entry = iteratorBSE.nextSuggestion("собат");
        assertNull(entry);

        entry = iteratorBSE.nextSuggestion("СОБАТ");
        assertNull(entry);

        entry = iteratorBSE.nextSuggestion("...Биоз");
        assertEquals("...Биоз", entry.getLemma());

        entry = iteratorBSE.nextSuggestion("Яёи культура");
        assertEquals("Яёи культура", entry.getLemma());
    }

    @Test
    public void testFindIndexEntry() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorWordnet.findIndexEntry("15 May Organization");
        assertNotNull(entry);
        assertEquals("15 May Organization", entry.getLemma());
        assertEquals(906, entry.getWordDataOffset());
        assertEquals(213, entry.getWordDataSize());

        bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma());
        assertNotNull(entry);
        assertEquals(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryFirst() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma());
        assertEquals(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());

        bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorMueller = new IndexEntriesIterator(bookInfo);
        entry = iteratorMueller.findIndexEntry(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma());
        assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLast() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_LAST.getLemma());
        assertEquals(Mocks.BSE_INDEX_ENTRY_LAST.getLemma(), entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInBse() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry("Яёи культура");
        assertEquals("Яёи культура", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInCambridge() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry("↑Zoos and wildlife reserves");
        assertEquals("↑Zoos and wildlife reserves", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInMueller() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry("усил.");
        assertEquals("усил.", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryLastInWordnet() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorBSE.findIndexEntry("zymotic");
        assertEquals("zymotic", entry.getLemma());
    }

    @Test
    public void testFindIndexEntryNonUnique() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorWordnet = new IndexEntriesIterator(bookInfo);
        IndexEntry entry = iteratorWordnet.findIndexEntry("put away");
        assertNotNull(entry);
        assertEquals("put away", entry.getLemma());
        assertEquals(12519419, entry.getWordDataOffset());
        assertEquals(208, entry.getWordDataSize());
    }
}
