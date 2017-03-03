/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.File;

import junit.framework.TestCase;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;

/**
 * @author valera
 *
 */
public class SuggestionsFinderTest extends TestCase {

	private IndexEntriesIterator muellerFinder;
	//private SuggestionsFinder muellerFinderCa;
	private IndexEntriesIterator bseFinder;

	/**
	 * @param name
	 */
	public SuggestionsFinderTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
//		finder = new SuggestionsFinder(new StarDictIndex(new BookInfo(BookInfoTest.MUELLER_IFO_PATH)), "A");
//		finder2 = new SuggestionsFinder(new StarDictIndex(new BookInfo(BookInfoTest.MUELLER_IFO_PATH)), "'ca");
		
		muellerFinder = (IndexEntriesIterator) new Book(new File(Mocks.MUELLER_IFO_PATH)).iterator();
		//muellerFinderA.setSearchStr("A");
		
		//muellerFinderCa = (SuggestionsFinder) new Book(new File(BookInfoTest.MUELLER_IFO_PATH)).iterator();
		//muellerFinderCa.setSearchStr("'ca");
		
		bseFinder = (IndexEntriesIterator) new Book(new File(Mocks.BSE_IFO_PATH)).iterator();
		//bseFinderYuan.setSearchStr("Юань");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		muellerFinder = null;
		//muellerFinderCa = null;
		bseFinder = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#SuggestionsFinder(net.bancer.sparkdict.domain.core.StarDictIndex, java.lang.String)}.
	 */
	public void testSuggestionsFinder() {
		assertNotNull(muellerFinder);
		//assertNotNull(muellerFinderCa);
		assertNotNull(bseFinder);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntriesIterator#next1()}.
	 * @throws DomainException 
	 */
	public void testNext() throws DomainException {
		IndexEntry indexEntry = null;		
		//for (int i = 0; i <= SuggestionsFinder.MAX; i++) {
			indexEntry = muellerFinder.nextSuggestion("A");
			//assertTrue(indexEntry.compareTo("A", IndexEntry.PREFIX_MATCH) == 0);
			assertEquals("A", indexEntry.getLemma());
		//}
		
		indexEntry = muellerFinder.nextSuggestion("A");
		assertEquals("a", indexEntry.getLemma());
		
		indexEntry = muellerFinder.nextSuggestion("'ca"); // first
		assertEquals("'cause", indexEntry.getLemma());
		
		indexEntry = muellerFinder.nextSuggestion("ус"); // last
		assertEquals("усил.", indexEntry.getLemma());
		
		indexEntry = muellerFinder.nextSuggestion("non-existant word");
		assertNull(indexEntry);
		
		indexEntry = bseFinder.nextSuggestion("Юань");
		assertEquals("Юань (ден. единица КНР)", indexEntry.getLemma());
		
		indexEntry = bseFinder.nextSuggestion("Москва (столица СССР)");
		assertEquals("Москва (столица СССР)", indexEntry.getLemma());
		
		indexEntry = bseFinder.nextSuggestion("...Биоз");
		assertEquals("...Биоз", indexEntry.getLemma());
		
		indexEntry = bseFinder.nextSuggestion("Яёи");
		assertEquals("Яёи культура", indexEntry.getLemma());
		
//		indexEntry = finder.next();
//		assertNull(indexEntry);
//		
//		indexEntry = finder2.next();
//		assertTrue(indexEntry.compareTo("'ca", IndexEntry.PREFIX_MATCH) == 0);
	}

}
