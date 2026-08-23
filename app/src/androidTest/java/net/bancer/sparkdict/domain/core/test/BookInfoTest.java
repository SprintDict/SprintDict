package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class BookInfoTest {

    private BookInfo muellerBookInfo;

    @Before
    public void setUp() {
        muellerBookInfo = new BookInfo(new File(Mocks.MUELLER_IFO_PATH));
    }

    @Test
    public void testPreConditions() {
        File dir = new File(Mocks.ROOT_PATH);
        assertTrue(Mocks.ROOT_PATH + " does not exists", dir.exists());
        assertTrue(Mocks.ROOT_PATH + " is not a directory", dir.isDirectory());
        assertTrue(Mocks.ROOT_PATH + " is not readable", dir.canRead());

        File muellerDir = new File(Mocks.MUELLER_DICT_PATH);
        assertTrue(muellerDir + " does not exists", muellerDir.exists());
        assertTrue(muellerDir + " is not a directory", muellerDir.isDirectory());
        assertTrue(muellerDir + " is not readable", muellerDir.canRead());

        File muellerIfo = new File(Mocks.MUELLER_IFO_PATH);
        assertTrue(muellerIfo + " does not exists", muellerIfo.exists());
        assertTrue(muellerIfo + " is not a file", muellerIfo.isFile());
        assertTrue(muellerIfo + " is not readable", muellerIfo.canRead());

        File bseDir = new File(Mocks.BSE_DICT_PATH);
        assertTrue(bseDir + " does not exists", bseDir.exists());
        assertTrue(bseDir + " is not a directory", bseDir.isDirectory());
        assertTrue(bseDir + " is not readable", bseDir.canRead());

        File bseIfo = new File(Mocks.BSE_IFO_PATH);
        assertTrue(bseIfo + " does not exists", bseIfo.exists());
        assertTrue(bseIfo + " is not a file", bseIfo.isFile());
        assertTrue(bseIfo + " is not readable", bseIfo.canRead());
    }

    @Test
    public void testBookInfoString() {
        BookInfo bseBookInfo = new BookInfo(Mocks.BSE_IFO_PATH);
        assertNotNull(bseBookInfo);
        assertEquals("2.4.2", bseBookInfo.getVersion());
        assertEquals(95058, bseBookInfo.getWordCount());
        assertEquals(3861800, bseBookInfo.getIdxFileSize());
        assertEquals("Большая Советская Энциклопедия", bseBookInfo.getBookName());
        assertEquals("2009.01.30", bseBookInfo.getDate());
        assertEquals("x", bseBookInfo.getSameTypeSequence());
        assertEquals("Copyright: Converted by swaj under GNU Public License; Version: 1.0", bseBookInfo.getDescription());
    }

    @Test
    public void testBookInfoFile() {
        assertNotNull(muellerBookInfo);
        assertEquals("2.4.2", muellerBookInfo.getVersion());
        assertEquals(46198, muellerBookInfo.getWordCount());
        assertEquals(806372, muellerBookInfo.getIdxFileSize());
        assertEquals("Mueller7GPL", muellerBookInfo.getBookName());
        assertEquals("2004.03.09", muellerBookInfo.getDate());
        assertEquals("tm", muellerBookInfo.getSameTypeSequence());
    }

    @Test
    public void testToString() {
        assertTrue(muellerBookInfo.toString().contains("Dictionary name: Mueller7GPL"));
    }

    @Test
    public void testGetFileBaseName() {
        assertEquals(Mocks.MUELLER_BASE_PATH, muellerBookInfo.getFileBaseName());
    }

    @Test
    public void testGetPathToDictFile() {
        assertEquals(Mocks.MUELLER_BASE_PATH + Mocks.DICT_EXT, muellerBookInfo.getPathToDictFile());
    }
}
