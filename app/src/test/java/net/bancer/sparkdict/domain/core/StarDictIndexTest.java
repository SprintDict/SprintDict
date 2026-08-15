package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import net.bancer.sparkdict.Fixtures;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class StarDictIndexTest {

    @Test
    public void getFileNameReturnsIdxFileName() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            bookInfo.getFileBaseName() + ".idx",
            index.getFileName()
        );
    }

    @Test
    public void getLexicalEntryOffsetFieldSizeInBytesReturnsFourFor32BitIndex() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            4,
            index.getLexicalEntryOffsetFieldSizeInBytes()
        );
    }

    @Test
    public void getLexicalEntrySizeFieldInBytesReturnsFour() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            4,
            index.getLexicalEntrySizeFieldInBytes()
        );
    }

    @Test
    public void getBookInfoReturnsBookInfo() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(bookInfo, index.getBookInfo());
    }

    @Test
    public void getFileBaseNameReturnsBookInfoFileBaseName() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            bookInfo.getFileBaseName(),
            index.getFileBaseName()
        );
    }

    @Test
    public void retrieveIndexEntryReturnsFirstEntry() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        IndexEntry entry = index.retrieveIndexEntry(0);
        assertNotNull(entry);
        assertEquals("-able", entry.getLemma());
        assertEquals(0, entry.getWordDataOffset());
        assertEquals(929, entry.getWordDataSize());
        assertEquals(14, entry.getLengthInBytes());
    }

    @Test
    public void retrieveIndexEntryReturnsNullWhenPositionIsAtEndOfFile() throws IOException {
        File ifoFile = new File(Fixtures.GCIDE_IFO_FILE);
        BookInfo bookInfo = new BookInfo(ifoFile);
        StarDictIndex index = new StarDictIndex(bookInfo);
        File idxFile = new File(Fixtures.GCIDE_IDX_FILE);
        IndexEntry entry = index.retrieveIndexEntry(idxFile.length());
        assertNull(entry);
    }
}
