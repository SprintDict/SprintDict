package net.bancer.sparkdict.domain.core.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.StarDictIndex;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class StarDictIndexTest extends TestCase {

    private BookInfo muellerInfo;

    private BookInfo bseInfo;

    private StarDictIndex muellerStarDictIndex;

    private StarDictIndex bseStarDictIndex;

    @Before
    public void setUp() throws Exception {
        muellerInfo = new BookInfo(Mocks.MUELLER_IFO_PATH);
        muellerStarDictIndex = new StarDictIndex(muellerInfo);
        bseInfo = new BookInfo(Mocks.BSE_IFO_PATH);
        bseStarDictIndex = new StarDictIndex(bseInfo);
    }

    @Test
    public void testStarDictIndex() {
        assertEquals(Mocks.MUELLER_BASE_PATH + Mocks.IDX_EXT, muellerStarDictIndex.getFileName());
        assertEquals(muellerInfo.getIdxOffsetBits() / StarDictIndex.BITS_IN_BYTE, muellerStarDictIndex.getLexicalEntryOffsetFieldSizeInBytes());

        assertNotNull(bseInfo);
        assertNotNull(bseStarDictIndex);
        assertEquals(Mocks.BSE_BASE_PATH + Mocks.IDX_EXT, bseStarDictIndex.getFileName());
        assertEquals(bseInfo.getIdxOffsetBits() / StarDictIndex.BITS_IN_BYTE, bseStarDictIndex.getLexicalEntryOffsetFieldSizeInBytes());
    }

    @Test
    public void testRetrieveIndexEntryFromMuellerDictionary() throws IOException {
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
    }

    @Test
    public void testRetrieveIndexEntryFromBseDictionary() throws IOException {
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
    }
}
