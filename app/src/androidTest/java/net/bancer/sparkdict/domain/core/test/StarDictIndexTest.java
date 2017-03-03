/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.StarDictIndex;
import net.bancer.sparkdict.mocks.Mocks;
import junit.framework.TestCase;

/**
 * @author valera
 *
 */
public class StarDictIndexTest extends TestCase {
	
	private BookInfo muellerInfo;
	
	private BookInfo bseInfo;

	private StarDictIndex muellerStarDictIndex;

	private StarDictIndex bseStarDictIndex;

	/**
	 * @param name
	 */
	public StarDictIndexTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		muellerInfo = new BookInfo(Mocks.MUELLER_IFO_PATH);
		muellerStarDictIndex 	= new StarDictIndex(muellerInfo);
		
		
		bseInfo 	= new BookInfo(Mocks.BSE_IFO_PATH);
		bseStarDictIndex 		= new StarDictIndex(bseInfo);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.StarDictIndex#StarDictIndex(net.bancer.sparkdict.domain.core.BookInfo)}.
	 */
	public void testStarDictIndex() {
		assertNotNull(muellerInfo);
		assertNotNull(muellerStarDictIndex);
		assertEquals(Mocks.MUELLER_BASE_PATH + Mocks.IDX_EXT, muellerStarDictIndex.getFileName());
		assertEquals(muellerInfo.getIdxOffsetBits()/StarDictIndex.BITS_IN_BYTE, muellerStarDictIndex.getLexicalEntryOffsetFieldSizeInBytes());
		
		assertNotNull(bseInfo);
		assertNotNull(bseStarDictIndex);
		assertEquals(Mocks.BSE_BASE_PATH + Mocks.IDX_EXT, bseStarDictIndex.getFileName());
		assertEquals(bseInfo.getIdxOffsetBits()/StarDictIndex.BITS_IN_BYTE, bseStarDictIndex.getLexicalEntryOffsetFieldSizeInBytes());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.StarDictIndex#retrieveIndexEntry(long)}.
	 */
	public void testRetrieveIndexEntryFromMuellerDictionary() {
		try {
			IndexEntry indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_1);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_ABACUS.getLemma(), indexEntry.getLemma());
			
			indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_2);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_ABADDON.getLemma(), indexEntry.getLemma());
			
			indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_3);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_ABAFT.getLemma(), indexEntry.getLemma());
			
			indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_4);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_A.getLemma(), indexEntry.getLemma());
			
			indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_5);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_a.getLemma(), indexEntry.getLemma());
			
			indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_FIRST);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), indexEntry.getLemma());
			
			indexEntry = muellerStarDictIndex.retrieveIndexEntry(Mocks.MUELLER_INDEX_ENTRY_START_LAST);
			assertNotNull(indexEntry);
			assertEquals(Mocks.MUELLER_INDEX_ENTRY_LAST.getLemma(), indexEntry.getLemma());
			
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.StarDictIndex#retrieveIndexEntry(long)}.
	 */
	public void testRetrieveIndexEntryFromBseDictionary() {
		try {			
			IndexEntry indexEntry = bseStarDictIndex.retrieveIndexEntry(Mocks.BSE_INDEX_ENTRY_START_1);
			assertNotNull(indexEntry);
			assertEquals(Mocks.BSE_INDEX_ENTRY_1.getLemma(), indexEntry.getLemma());
			
			indexEntry = bseStarDictIndex.retrieveIndexEntry(Mocks.BSE_INDEX_ENTRY_START_2);
			assertNotNull(indexEntry);
			assertEquals(Mocks.BSE_INDEX_ENTRY_2.getLemma(), indexEntry.getLemma());
			
			indexEntry = bseStarDictIndex.retrieveIndexEntry(Mocks.BSE_INDEX_ENTRY_START_3);
			assertNotNull(indexEntry);
			assertEquals(Mocks.BSE_INDEX_ENTRY_3.getLemma(), indexEntry.getLemma());
			
			indexEntry = bseStarDictIndex.retrieveIndexEntry(Mocks.BSE_INDEX_ENTRY_START_4);
			assertNotNull(indexEntry);
			assertEquals(Mocks.BSE_INDEX_ENTRY_SOBAT.getLemma(), indexEntry.getLemma());
			
			indexEntry = bseStarDictIndex.retrieveIndexEntry(Mocks.BSE_INDEX_ENTRY_START_FIRST);
			assertNotNull(indexEntry);
			assertEquals(Mocks.BSE_INDEX_ENTRY_FIRST.getLemma(), indexEntry.getLemma());
			
			indexEntry = bseStarDictIndex.retrieveIndexEntry(Mocks.BSE_INDEX_ENTRY_START_LAST);
			assertNotNull(indexEntry);
			assertEquals(Mocks.BSE_INDEX_ENTRY_LAST.getLemma(), indexEntry.getLemma());
			
//			Log.d("xx", "lemma\t\t" + indexEntry.getLemma());
//			Log.d("xx", "WordDataOffset\t" + indexEntry.getWordDataOffset());
//			Log.d("xx", "WordDataSize\t" + indexEntry.getWordDataSize());
//			Log.d("xx", "LengthInBytes\t" + indexEntry.getLengthInBytes());
			
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
