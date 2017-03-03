/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.IOException;

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
public class IndexEntryFinderTest extends TestCase {
	
	private IndexEntriesIterator iteratorMueller;
	private IndexEntriesIterator iteratorBSE;

	/**
	 * @param name
	 */
	public IndexEntryFinderTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		iteratorMueller = new IndexEntriesIterator(new BookInfo(Mocks.MUELLER_IFO_PATH));
		iteratorBSE = new IndexEntriesIterator(new BookInfo(Mocks.BSE_IFO_PATH));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		iteratorMueller = null;
		iteratorBSE = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.finders.IndexEntryFinder#IndexEntryFinder(net.bancer.sparkdict.domain.core.StarDictIndex)}.
	 */
	public void testIndexEntryFinder() {
		assertNotNull(iteratorMueller);
		assertNotNull(iteratorBSE);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.finders.IndexEntryFinder#findIndexEntry(java.lang.String)}.
	 * @throws IOException 
	 * @throws DomainException 
	 */
	public void testBinarySearch() throws IOException, DomainException {
		IndexEntry ie = iteratorMueller.findIndexEntry("abacus");
		assertEquals("abacus", ie.getLemma());
		
		ie = iteratorMueller.findIndexEntry("_жарг.");
		assertEquals("_жарг.", ie.getLemma());
		
		ie = iteratorMueller.findIndexEntry("'cause");//first
		assertEquals("'cause", ie.getLemma());
		
		ie = iteratorMueller.findIndexEntry("усил.");//last
		assertEquals("усил.", ie.getLemma());
		
		ie = iteratorMueller.findIndexEntry("non-existent word");
		assertNull(ie);
		
		
		ie = iteratorBSE.findIndexEntry("...Биоз"); //first
		assertEquals("...Биоз", ie.getLemma());
		
		ie = iteratorBSE.findIndexEntry("Яёи культура"); //last
		assertEquals("Яёи культура", ie.getLemma());
		
		ie = iteratorBSE.findIndexEntry("Юань (монг. династия)");
		assertEquals("Юань (монг. династия)", ie.getLemma());
		
		ie = iteratorBSE.findIndexEntry("Юань (совет)");
		assertEquals("Юань (совет)", ie.getLemma());
		
		ie = iteratorBSE.findIndexEntry("Юань Мэй");
		assertEquals("Юань Мэй", ie.getLemma());
		
		ie = iteratorBSE.findIndexEntry("Москва (столица СССР)");
		assertEquals("Москва (столица СССР)", ie.getLemma());
	}

}
