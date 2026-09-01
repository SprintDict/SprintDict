package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFiles;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookInfoTest {

    private SafDictionaryFiles safDictionaryFiles;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        safDictionaryFiles = SafDictionaryFilesFactory.create(context);
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
        BookInfo muellerBookInfo = new BookInfo(new File(Mocks.MUELLER_IFO_PATH));
        assertNotNull(muellerBookInfo);
        assertEquals("2.4.2", muellerBookInfo.getVersion());
        assertEquals(46198, muellerBookInfo.getWordCount());
        assertEquals(806372, muellerBookInfo.getIdxFileSize());
        assertEquals("Mueller7GPL", muellerBookInfo.getBookName());
        assertEquals("2004.03.09", muellerBookInfo.getDate());
        assertEquals("tm", muellerBookInfo.getSameTypeSequence());
    }

    @Test
    public void testBookInfoFileMueller() {
        BookInfo muellerBookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, safDictionaryFiles);
        assertNotNull(muellerBookInfo);
        assertEquals("2.4.2", muellerBookInfo.getVersion());
        assertEquals(46198, muellerBookInfo.getWordCount());
        assertEquals(806372, muellerBookInfo.getIdxFileSize());
        assertEquals("Mueller7GPL", muellerBookInfo.getBookName());
        assertEquals("2004.03.09", muellerBookInfo.getDate());
        assertEquals("tm", muellerBookInfo.getSameTypeSequence());
    }

    @Test
    public void testBookInfoFileWordnet() {
        BookInfo muellerBookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        assertNotNull(muellerBookInfo);
        assertEquals("3.0.0", muellerBookInfo.getVersion());
        assertEquals(117659, muellerBookInfo.getWordCount());
        assertEquals(2186342, muellerBookInfo.getIdxFileSize());
        assertEquals("WordNet", muellerBookInfo.getBookName());
        assertEquals("2007.09.10", muellerBookInfo.getDate());
        assertEquals("n", muellerBookInfo.getSameTypeSequence());
    }

    @Test
    public void testBookInfoFileCambridge() {
        BookInfo muellerBookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, safDictionaryFiles);
        assertNotNull(muellerBookInfo);
        assertEquals("2.4.2", muellerBookInfo.getVersion());
        assertEquals(65235, muellerBookInfo.getWordCount());
        assertEquals(1377344, muellerBookInfo.getIdxFileSize());
        assertEquals("Cambridge Advanced Learners Dictionary 3th Ed. (En-En)", muellerBookInfo.getBookName());
        assertEquals("2011.05.22", muellerBookInfo.getDate());
        assertEquals("x", muellerBookInfo.getSameTypeSequence());
    }

    @Test
    public void testToString() {
        BookInfo muellerBookInfo = new BookInfo(new File(Mocks.MUELLER_IFO_PATH));
        assertTrue(muellerBookInfo.toString().contains("Dictionary name: Mueller7GPL"));
    }

    @Test
    public void testGetFileBaseName() {
        BookInfo muellerBookInfo = new BookInfo(new File(Mocks.MUELLER_IFO_PATH));
        assertEquals(Mocks.MUELLER_BASE_PATH, muellerBookInfo.getFileBaseName());
    }

    @Test
    public void testGetPathToDictFile() {
        BookInfo muellerBookInfo = new BookInfo(new File(Mocks.MUELLER_IFO_PATH));
        assertEquals(Mocks.MUELLER_BASE_PATH + Mocks.DICT_EXT, muellerBookInfo.getPathToDictFile());
    }
}
