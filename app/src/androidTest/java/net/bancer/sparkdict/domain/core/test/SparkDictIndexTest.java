package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.SparkDictIndex;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class SparkDictIndexTest {

    private SparkDictIndex indexMueller;
    private SparkDictIndex indexBSE;

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dictionaryFiles = SafDictionaryFilesFactory.create(context);
        indexMueller = new SparkDictIndex(new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles));
        indexBSE = new SparkDictIndex(new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles));
    }

    @Test
    public void testGetSize() throws IOException {
        assertEquals(Mocks.MUELLER_DICT_SIZE, indexMueller.getSize());
        assertEquals(Mocks.BSE_DICT_SIZE, indexBSE.getSize());
    }

    @Test
    public void testGetIndexEntry() throws IOException {
        assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), indexMueller.getIndexEntry(0).getLemma());
        assertEquals(Mocks.MUELLER_INDEX_ENTRY_LAST.getLemma(), indexMueller.getIndexEntry(Mocks.MUELLER_DICT_SIZE - 1).getLemma());
        assertNull(indexMueller.getIndexEntry(Mocks.MUELLER_DICT_SIZE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexEntryIOException() throws IOException {
        indexMueller.getIndexEntry(-1);
    }

    @Test
    public void testGetBookName() {
        assertEquals(Mocks.MUELLER_DICT_NAME, indexMueller.getBookName());
        assertEquals(Mocks.BSE_DICT_NAME, indexBSE.getBookName());
    }
}
