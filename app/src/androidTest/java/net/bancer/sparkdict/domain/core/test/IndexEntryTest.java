/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import junit.framework.TestCase;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.mocks.Mocks;

/**
 * @author valera
 *
 */
public class IndexEntryTest extends TestCase {
	
	//private static final BookInfo MUELLER_BOOK_INFO = new BookInfo(MockObjects.MUELLER_IFO_PATH);
	//private static final BookInfo BSE_BOOK_INFO = new BookInfo(MockObjects.BSE_IFO_PATH);
	
	private IndexEntry testEntry_Abaddon;
	private IndexEntry testEntry_A;
	private IndexEntry testEntry_a;

	/**
	 * @param name
	 */
	public IndexEntryTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		testEntry_Abaddon = new IndexEntry("Abaddon", 26108, 110, 16);
		testEntry_A = new IndexEntry("A", 22712, 1867, 10);
		testEntry_a = new IndexEntry("a", 24579, 381, 10);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		testEntry_Abaddon = null;
		testEntry_A = null;
		testEntry_a = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntry#compareTo(net.bancer.sparkdict.domain.core.IndexEntry)}.
	 */
	public void testCompareToMuellerDictIndexEntry() {
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON) == 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABACUS) > 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABAFT) < 0);
		assertTrue(testEntry_A.compareTo(testEntry_a) < 0);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntry#compareTo(net.bancer.sparkdict.domain.core.IndexEntry)}.
	 */
	public void testCompareToBseDictIndexEntry() {
		assertTrue(Mocks.BSE_INDEX_ENTRY_1.compareTo(Mocks.BSE_INDEX_ENTRY_1) == 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_2.compareTo(Mocks.BSE_INDEX_ENTRY_1) > 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_2.compareTo(Mocks.BSE_INDEX_ENTRY_3) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_0.compareTo(Mocks.BSE_INDEX_ENTRY_1) < 0);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntry#compareTo(java.lang.String, int)}.
	 */
	public void testCompareToStringInt() throws IllegalArgumentException {
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), IndexEntry.WORD_MATCH) == 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), IndexEntry.PREFIX_MATCH) == 0);
		try {
			assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), -1) == 0);
			fail("IllegalArgumentException had to be thrown but was not!");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntry#compareWordStringTo(java.lang.String)}.
	 */
	public void testCompareWordStringTo() {
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), IndexEntry.WORD_MATCH) == 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABACUS.getLemma(), IndexEntry.WORD_MATCH) > 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABAFT.getLemma(), IndexEntry.WORD_MATCH) < 0);
		assertTrue(testEntry_A.compareTo(testEntry_a.getLemma(), IndexEntry.WORD_MATCH) < 0);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntry#compareToPrefix(java.lang.String)}.
	 */
	public void testCompareToPrefix() {
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_FIRST.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) < 0);
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_ABACUS.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) == 0);
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_ABADDON.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) == 0);
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_ABAFT.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) == 0);
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_LAST.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) > 0);
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_A.compareTo(Mocks.MUELLER_INDEX_ENTRY_a.getLemma(), IndexEntry.PREFIX_MATCH) == 0);
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.IndexEntry#compareToPrefix(java.lang.String)}.
	 */
	public void testCompareToPrefixBSE() {
		assertEquals(0, Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("Соба", IndexEntry.PREFIX_MATCH));
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("соба", IndexEntry.PREFIX_MATCH) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("СОБА", IndexEntry.PREFIX_MATCH) > 0);
		
		assertEquals(0, Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("Собат", IndexEntry.PREFIX_MATCH));
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("собат", IndexEntry.PREFIX_MATCH) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("СОБАТ", IndexEntry.PREFIX_MATCH) > 0);
		
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("Собау", IndexEntry.PREFIX_MATCH) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("собау", IndexEntry.PREFIX_MATCH) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("СОБАУ", IndexEntry.PREFIX_MATCH) > 0);
		
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("Собас", IndexEntry.PREFIX_MATCH) > 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("собас", IndexEntry.PREFIX_MATCH) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_SOBAT.compareTo("СОБАС", IndexEntry.PREFIX_MATCH) > 0);
	}

}
