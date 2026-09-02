package net.bancer.sparkdict.domain.core.test;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class IndexEntryFinderTest extends TestCase {

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() throws DomainException {
        Context context = ApplicationProvider.getApplicationContext();
        dictionaryFiles = SafDictionaryFilesFactory.create(context);
    }

    @Test
    public void testBinarySearchInMueller() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorMueller = new IndexEntriesIterator(bookInfo);
        IndexEntry ie = iteratorMueller.findIndexEntry("abacus");
        assertEquals("abacus", ie.getLemma());

        ie = iteratorMueller.findIndexEntry("_жарг.");
        assertEquals("_жарг.", ie.getLemma());

        ie = iteratorMueller.findIndexEntry("'cause");//first
        assertEquals("'cause", ie.getLemma());

        ie = iteratorMueller.findIndexEntry("усил.");//last
        assertEquals("усил.", ie.getLemma());

        ie = iteratorMueller.findIndexEntry("non-existent word");
        assertNull(ie);
    }

    @Test
    public void testBinarySearchInBse() throws DomainException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH_RELATIVE, dictionaryFiles);
        IndexEntriesIterator iteratorBSE = new IndexEntriesIterator(bookInfo);
        IndexEntry ie = iteratorBSE.findIndexEntry("...Биоз"); //first
        assertEquals("...Биоз", ie.getLemma());

        ie = iteratorBSE.findIndexEntry("Яёи культура"); //last
        assertEquals("Яёи культура", ie.getLemma());

        ie = iteratorBSE.findIndexEntry("Юань (монг. династия)");
        assertEquals("Юань (монг. династия)", ie.getLemma());

        ie = iteratorBSE.findIndexEntry("Юань (совет)");
        assertEquals("Юань (совет)", ie.getLemma());

        ie = iteratorBSE.findIndexEntry("Юань Мэй");
        assertEquals("Юань Мэй", ie.getLemma());

        ie = iteratorBSE.findIndexEntry("Москва (столица СССР)");
        assertEquals("Москва (столица СССР)", ie.getLemma());
    }
}
