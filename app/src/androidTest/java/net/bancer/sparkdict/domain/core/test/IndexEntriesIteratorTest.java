package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.File;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class IndexEntriesIteratorTest {

	private IndexEntriesIterator iteratorWordnet;

	private IndexEntriesIterator iteratorBSE;

	private IndexEntriesIterator iteratorMueller;

	@Before
	public void setUp() throws DomainException {
		iteratorWordnet = new IndexEntriesIterator(new BookInfo(new File(Mocks.WORDNET_IFO_PATH)));
		iteratorBSE = new IndexEntriesIterator(new BookInfo(new File(Mocks.BSE_IFO_PATH)));
		iteratorMueller = new IndexEntriesIterator(new BookInfo(Mocks.MUELLER_IFO_PATH));
	}

	@Test
	public void testHasNext() throws DomainException {
		iteratorWordnet.findIndexEntry("15 May Organization");
		assertTrue(iteratorWordnet.hasNext());

		iteratorBSE.findIndexEntry("Яя (река)");
		assertTrue(iteratorBSE.hasNext());
	}

	@Test
	public void testHasNextOnLastElement() throws DomainException {
		IndexEntry entry = iteratorBSE.findIndexEntry("Яёи культура");
		assertEquals("Яёи культура", entry.getLemma());
		assertFalse(iteratorBSE.hasNext());
	}

	@Test
	public void testNext() throws DomainException {
		iteratorWordnet.findIndexEntry("15 May Organization");
		IndexEntry entry = iteratorWordnet.next();
		assertEquals("1530s", entry.getLemma());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemove() {
		iteratorWordnet.remove();
	}

	@Test
	public void testNextSuggestion() throws DomainException {
		IndexEntry entry = iteratorWordnet.nextSuggestion(".");
		assertEquals(".22 caliber", entry.getLemma());
	}

	@Test
	public void testNextSuggestionBSE() throws DomainException {
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
		IndexEntry entry = iteratorWordnet.findIndexEntry("15 May Organization");
		assertNotNull(entry);
		assertEquals("15 May Organization", entry.getLemma());
		assertEquals(906, entry.getWordDataOffset());
		assertEquals(213, entry.getWordDataSize());

		entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma());
		assertNotNull(entry);
		assertEquals(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma(), entry.getLemma());
	}

	@Test
	public void testFindFirst() throws DomainException {
		IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma());
		assertEquals(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());

		entry = iteratorMueller.findIndexEntry(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma());
		assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());
	}

	@Test
	public void testFindLast() throws DomainException {
		IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_LAST.getLemma());
		assertEquals(Mocks.BSE_INDEX_ENTRY_LAST.getLemma(), entry.getLemma());
	}

	@Test
	public void testFindIndexEntryNonUnique() throws DomainException {
		IndexEntry entry = iteratorWordnet.findIndexEntry("put away");
		assertNotNull(entry);
		assertEquals("put away", entry.getLemma());
		assertEquals(12519419, entry.getWordDataOffset());
		assertEquals(208, entry.getWordDataSize());
	}
}
