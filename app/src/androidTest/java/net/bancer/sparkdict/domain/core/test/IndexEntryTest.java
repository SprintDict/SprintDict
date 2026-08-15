package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.Before;
import org.junit.Test;

public class IndexEntryTest {

	private IndexEntry testEntry_Abaddon;
	private IndexEntry testEntry_A;
	private IndexEntry testEntry_a;

	@Before
	public void setUp() {
		testEntry_Abaddon = new IndexEntry("Abaddon", 26108, 110, 16);
		testEntry_A = new IndexEntry("A", 22712, 1867, 10);
		testEntry_a = new IndexEntry("a", 24579, 381, 10);
	}

	@Test
	public void testCompareToMuellerDictIndexEntry() {
		assertEquals(0, testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON));
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABACUS) > 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABAFT) < 0);
		assertTrue(testEntry_A.compareTo(testEntry_a) < 0);
	}

	@Test
	public void testCompareToBseDictIndexEntry() {
		IndexEntry entry = new IndexEntry("Юань (монг. династия)", 280788620, 3297, 46);
		assertEquals(0, Mocks.BSE_INDEX_ENTRY_1.compareTo(entry));
		assertTrue(Mocks.BSE_INDEX_ENTRY_2.compareTo(Mocks.BSE_INDEX_ENTRY_1) > 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_2.compareTo(Mocks.BSE_INDEX_ENTRY_3) < 0);
		assertTrue(Mocks.BSE_INDEX_ENTRY_0.compareTo(Mocks.BSE_INDEX_ENTRY_1) < 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCompareToStringInt() throws IllegalArgumentException {
		assertEquals(0, testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), IndexEntry.WORD_MATCH));
		assertEquals(0, testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), IndexEntry.PREFIX_MATCH));
		assertEquals(0, testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), -1));
	}

	@Test
	public void testCompareWordStringTo() {
		assertEquals(0, testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), IndexEntry.WORD_MATCH));
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABACUS.getLemma(), IndexEntry.WORD_MATCH) > 0);
		assertTrue(testEntry_Abaddon.compareTo(Mocks.MUELLER_INDEX_ENTRY_ABAFT.getLemma(), IndexEntry.WORD_MATCH) < 0);
		assertTrue(testEntry_A.compareTo(testEntry_a.getLemma(), IndexEntry.WORD_MATCH) < 0);
	}

	@Test
	public void testCompareToPrefix() {
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_FIRST.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) < 0);
		assertEquals(0, Mocks.MUELLER_INDEX_ENTRY_ABACUS.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH));
		assertEquals(0, Mocks.MUELLER_INDEX_ENTRY_ABADDON.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH));
		assertEquals(0, Mocks.MUELLER_INDEX_ENTRY_ABAFT.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH));
		assertTrue(Mocks.MUELLER_INDEX_ENTRY_LAST.compareTo(Mocks.PREFIX_ABA, IndexEntry.PREFIX_MATCH) > 0);
		assertEquals(0, Mocks.MUELLER_INDEX_ENTRY_A.compareTo(Mocks.MUELLER_INDEX_ENTRY_a.getLemma(), IndexEntry.PREFIX_MATCH));
	}

	@Test
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
