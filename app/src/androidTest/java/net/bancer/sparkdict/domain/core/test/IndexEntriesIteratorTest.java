/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.File;

import junit.framework.TestCase;
import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;

/**
 * @author valera
 *
 */
public class IndexEntriesIteratorTest extends TestCase {

	private IndexEntriesIterator iteratorWordnet;
	
	private IndexEntriesIterator iteratorBSE;
	
	private IndexEntriesIterator iteratorMueller;

	/**
	 * @param name
	 */
	public IndexEntriesIteratorTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		iteratorWordnet = new IndexEntriesIterator(new BookInfo(new File(Mocks.WORDNET_IFO_PATH)));
		iteratorBSE = new IndexEntriesIterator(new BookInfo(new File(Mocks.BSE_IFO_PATH)));
		iteratorMueller = new IndexEntriesIterator(new BookInfo(Mocks.MUELLER_IFO_PATH));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		iteratorWordnet = null;
		iteratorBSE = null;
		iteratorMueller = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#IndexEntriesIterator(net.bancer.sparkdict.domain.core.BookInfo)}.
	 */
	public void testIndexEntriesIterator() {
		assertNotNull(iteratorWordnet);
		assertNotNull(iteratorBSE);
		assertNotNull(iteratorMueller);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#hasNext()}.
	 * @throws DomainException 
	 */
	public void testHasNext() throws DomainException {
		iteratorWordnet.findIndexEntry("15 May Organization");
		assertTrue(iteratorWordnet.hasNext());
		
		iteratorBSE.findIndexEntry("Яя (река)");
		assertTrue(iteratorBSE.hasNext());
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#hasNext()}.
	 * @throws DomainException 
	 */
	public void testHasNextOnLastElement() throws DomainException {
		IndexEntry entry = iteratorBSE.findIndexEntry("Яёи культура");
		assertEquals("Яёи культура", entry.getLemma());
		assertFalse(iteratorBSE.hasNext());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#next()}.
	 * @throws DomainException 
	 */
	public void testNext() throws DomainException {
		iteratorWordnet.findIndexEntry("15 May Organization");
		IndexEntry entry = iteratorWordnet.next();
		assertEquals("1530s", entry.getLemma());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#remove()}.
	 */
	public void testRemove() {
		try {
			iteratorWordnet.remove();
			fail("Expected exception UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			// test passed
		}
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#nextSuggestion(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testNextSuggestion() throws DomainException {
		IndexEntry entry = iteratorWordnet.nextSuggestion(".");
		assertEquals(".22 caliber", entry.getLemma());
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#nextSuggestion(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testNextSuggestionBSE() throws DomainException {
		IndexEntry entry = iteratorBSE.nextSuggestion("Собат");
		assertEquals("Собат", entry.getLemma());
//		System.out.println("lemma\t\t" + entry.getLemma());
//		System.out.println("WordDataOffset\t" + entry.getWordDataOffset());
//		System.out.println("WordDataSize\t" + entry.getWordDataSize());
//		System.out.println("LengthInBytes\t" + entry.getLengthInBytes());
		
		entry = iteratorBSE.nextSuggestion("собат");
		assertNull(entry);
		
		entry = iteratorBSE.nextSuggestion("СОБАТ");
		assertNull(entry);
		
		entry = iteratorBSE.nextSuggestion("...Биоз");
		assertEquals("...Биоз", entry.getLemma());
		
		entry = iteratorBSE.nextSuggestion("Яёи культура");
		assertEquals("Яёи культура", entry.getLemma());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#findIndexEntry(java.lang.String)}.
	 * @throws DomainException 
	 */
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
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#findIndexEntry(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testFindFirst() throws DomainException {
		IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma());
		assertEquals(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());
		
		entry = iteratorMueller.findIndexEntry(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma());
		assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), entry.getLemma());
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#findIndexEntry(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testFindLast() throws DomainException {
		IndexEntry entry = iteratorBSE.findIndexEntry(Mocks.BSE_INDEX_ENTRY_LAST.getLemma());
		assertEquals(Mocks.BSE_INDEX_ENTRY_LAST.getLemma(), entry.getLemma());
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#findIndexEntry(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testFindIndexEntryNonUnique() throws DomainException {
		IndexEntry entry = iteratorWordnet.findIndexEntry("put away");
		assertNotNull(entry);
		assertEquals("put away", entry.getLemma());
		assertEquals(12519419, entry.getWordDataOffset());
		assertEquals(208, entry.getWordDataSize());
	}
}
