package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import net.bancer.sparkdict.Fixtures;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class StarDictIndexTest {

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
    }

    @Test
    public void getFileNameReturnsIdxFileName() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            bookInfo.getFileBaseName() + ".idx",
            index.getFileName()
        );
    }

    @Test
    public void getLexicalEntryOffsetFieldSizeInBytesReturnsFourFor32BitIndex() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            4,
            index.getLexicalEntryOffsetFieldSizeInBytes()
        );
    }

    @Test
    public void getLexicalEntrySizeFieldInBytesReturnsFour() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            4,
            index.getLexicalEntrySizeFieldInBytes()
        );
    }

    @Test
    public void getBookInfoReturnsBookInfo() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(bookInfo, index.getBookInfo());
    }

    @Test
    public void getFileBaseNameReturnsBookInfoFileBaseName() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(
            bookInfo.getFileBaseName(),
            index.getFileBaseName()
        );
    }

    @Test
    public void retrieveIndexEntryReturnsFirstEntry() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
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
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        File idxFile = new File(Fixtures.GCIDE_IDX_FILE);
        IndexEntry entry = index.retrieveIndexEntry(idxFile.length());
        assertNull(entry);
    }

    @Test
    public void channelConstructorRetrieveIndexEntryReturnsFirstEntry() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        IndexEntry entry = index.retrieveIndexEntry(0);
        assertNotNull(entry);
        assertEquals("-able", entry.getLemma());
        assertEquals(0, entry.getWordDataOffset());
        assertEquals(929, entry.getWordDataSize());
        assertEquals(14, entry.getLengthInBytes());
    }

    @Test
    public void channelConstructorRetrieveIndexEntryReturnsNullWhenPositionIsAtEndOfFile() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        File idxFile = new File(Fixtures.GCIDE_IDX_FILE);
        StarDictIndex index = new StarDictIndex(bookInfo);
        IndexEntry entry = index.retrieveIndexEntry(idxFile.length());
        assertNull(entry);
    }

    @Test
    public void channelConstructorDoesNotAffectMetadataGetters() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        StarDictIndex index = new StarDictIndex(bookInfo);
        assertEquals(bookInfo.getFileBaseName() + ".idx", index.getFileName());
        assertEquals(bookInfo.getFileBaseName(), index.getFileBaseName());
        assertEquals(bookInfo, index.getBookInfo());
        assertEquals(4, index.getLexicalEntryOffsetFieldSizeInBytes());
        assertEquals(4, index.getLexicalEntrySizeFieldInBytes());
    }
}
