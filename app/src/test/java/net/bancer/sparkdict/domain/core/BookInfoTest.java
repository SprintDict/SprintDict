package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import net.bancer.sparkdict.Fixtures;

import org.junit.Test;

import java.io.File;

public class BookInfoTest {

    @Test
    public void constructorRejectsNullFile() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new BookInfo((File) null));
        assertEquals("infoFile must not be null or empty", exception.getMessage());
    }

    @Test
    public void constructorRejectsNonIfoFile() {
        File idxFile = new File(Fixtures.GCIDE_IDX_FILE);
        assertThrows(
            IllegalArgumentException.class,
            () -> new BookInfo(idxFile)
        );
    }

    @Test
    public void constructorHandlesMissingFile() {
        String path = Fixtures.TEST_DATA_PATH + "missing/missing.ifo";
        BookInfo bookInfo = new BookInfo(path);
        assertEquals(path, bookInfo.getFileBaseName() + BookInfo.INFO_FILE_EXTENTION);
        assertEquals(
            Fixtures.TEST_DATA_PATH + "missing",
            bookInfo.getDirPath()
        );
    }

    @Test
    public void constructorWithPathParsesGcideFile() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        assertEquals("3.0.0", bookInfo.getVersion());
        assertEquals(
            "GNU Collaborative International Dictionary of English",
            bookInfo.getBookName()
        );
        assertEquals(108121, bookInfo.getWordCount());
        assertEquals(1932870, bookInfo.getIdxFileSize());
        assertEquals(32, bookInfo.getIdxOffsetBits());
        assertEquals("h", bookInfo.getSameTypeSequence());
    }

    @Test
    public void constructorWithFileParsesGcideFile() {
        BookInfo bookInfo = new BookInfo(new File(Fixtures.GCIDE_IFO_FILE));
        assertEquals("3.0.0", bookInfo.getVersion());
        assertEquals(
            "GNU Collaborative International Dictionary of English",
            bookInfo.getBookName()
        );
        assertEquals(108121, bookInfo.getWordCount());
        assertEquals(1932870, bookInfo.getIdxFileSize());
        assertEquals(32, bookInfo.getIdxOffsetBits());
        assertEquals("h", bookInfo.getSameTypeSequence());
    }

    @Test
    public void getFileBaseNameReturnsGcidePathWithoutExtension() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        assertEquals(
            Fixtures.GCIDE_IFO_FILE.substring(
                0,
                Fixtures.GCIDE_IFO_FILE.length() - 4
            ),
            bookInfo.getFileBaseName()
        );
    }

    @Test
    public void getPathToDictFileReturnsGcideDictPath() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        assertEquals(
            Fixtures.GCIDE_IFO_FILE.substring(
                0,
                Fixtures.GCIDE_IFO_FILE.length() - 4
            ) + ".dict.dz",
            bookInfo.getPathToDictFile()
        );
    }

    @Test
    public void getDirPathReturnsGcideDirectory() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        assertEquals(
            new File(Fixtures.GCIDE_IFO_FILE).getParent(),
            bookInfo.getDirPath()
        );
    }

    @Test
    public void toStringReturnsGcideInformation() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        assertEquals(
            "\n"
                + "Version: 3.0.0\n"
                + "Dictionary name: GNU Collaborative International Dictionary of English\n"
                + "Words: 108121\n"
                + "Synonyms: 11466\n"
                + "Index file size: 1932870\n"
                + "Index offset bits: 32\n"
                + "Author: null\n"
                + "Email: null\n"
                + "Website: null\n"
                + "Description: null\n"
                + "Date: null\n"
                + "Same type sequence: h\n"
                + "Dictionary type: null\n"
                + "Path: " + Fixtures.GCIDE_IFO_FILE + "\n",
            bookInfo.toString()
        );
    }

    @Test
    public void constructorParsesAllFields() {
        BookInfo bookInfo = new BookInfo(Fixtures.ALL_FIELDS_IFO_FILE);
        assertEquals("2.4.2", bookInfo.getVersion());
        assertEquals("Test Dictionary", bookInfo.getBookName());
        assertEquals(456, bookInfo.getWordCount());
        assertEquals(789, bookInfo.getIdxFileSize());
        assertEquals(64, bookInfo.getIdxOffsetBits());
        assertEquals("Test description", bookInfo.getDescription());
        assertEquals("2026-08-15", bookInfo.getDate());
        assertEquals("gm", bookInfo.getSameTypeSequence());
    }

    @Test
    public void toStringReturnsAllFields() {
        BookInfo bookInfo = new BookInfo(Fixtures.ALL_FIELDS_IFO_FILE);
        assertEquals(
            "\n"
                + "Version: 2.4.2\n"
                + "Dictionary name: Test Dictionary\n"
                + "Words: 456\n"
                + "Synonyms: 123\n"
                + "Index file size: 789\n"
                + "Index offset bits: 64\n"
                + "Author: Test Author\n"
                + "Email: test@example.com\n"
                + "Website: https://example.com\n"
                + "Description: Test description\n"
                + "Date: 2026-08-15\n"
                + "Same type sequence: gm\n"
                + "Dictionary type: idxoffsetbits64\n"
                + "Path: " + Fixtures.ALL_FIELDS_IFO_FILE + "\n",
            bookInfo.toString()
        );
    }
}
