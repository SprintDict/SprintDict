package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.core.SparkDictIndex;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class SparkDictIndexTest {

    private SparkDictIndex indexMueller;
    private SparkDictIndex indexBSE;

    @Before
    public void setUp() {
        indexMueller = new SparkDictIndex(new BookInfo(Mocks.MUELLER_IFO_PATH));
        indexBSE = new SparkDictIndex(new BookInfo(Mocks.BSE_IFO_PATH));
    }

    @After
    public void tearDown() {
        indexMueller.close();
        indexBSE.close();
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

    @Test(expected = IOException.class)
    public void testGetIndexEntryIOException() throws IOException {
        indexMueller.getIndexEntry(-1);
    }

    @Test
    public void testGetBookName() {
        assertEquals(Mocks.MUELLER_DICT_NAME, indexMueller.getBookName());
        assertEquals(Mocks.BSE_DICT_NAME, indexBSE.getBookName());
    }

    @Test(timeout = 2000)
    public void buildIndexAllWordsAreIndexedInCambridge() throws IOException {
        BookInfo bookInfo = new BookInfo(Mocks.CAMBRIDGE_IFO_PATH);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(65235, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i++) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
        }
    }

    @Test(timeout = 4500)
    public void buildIndexAllWordsAreIndexedInBse() throws IOException {
        BookInfo bookInfo = new BookInfo(Mocks.BSE_IFO_PATH);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(95058, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i++) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
        }
    }

    @Test(timeout = 2000)
    public void buildIndexAllWordsAreIndexedInMueller() throws IOException {
        BookInfo bookInfo = new BookInfo(Mocks.MUELLER_IFO_PATH);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(46198, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i++) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
        }
    }

    @Test(timeout = 5500)
    public void buildIndexAllWordsAreIndexedInWordnet() throws IOException {
        BookInfo bookInfo = new BookInfo(Mocks.WORDNET_IFO_PATH);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(117659, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i++) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
        }
    }

    @Test(timeout = 9000)
    public void buildIndexWordsHaveLexicalEntriesInCambridge() throws IOException {
        File file = new File(Mocks.CAMBRIDGE_IFO_PATH);
        BookInfo bookInfo = new BookInfo(file);
        Book book = new Book(file);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(65235, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i += 50) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
            LexicalEntry lexicalEntry = book.getLexicalEntry(indexEntry);
            assertEquals(lexicalEntry.getLemma(), lemma);
            // Check that definitions contain lemma, skipping lemmas consisting of more than one word.
            if (!lemma.contains(" ") && !lemma.contains("&") && !lemma.contains("'")) {
                assertTrue(
                    lemma + " is not contained in the definitions: " + lexicalEntry.getDefinitions(),
                    lexicalEntry.getDefinitions().toLowerCase().contains(lemma.toLowerCase())
                );
            }
        }
        book.closeResources();
    }

    @Test(timeout = 10000)
    public void buildIndexWordsHaveLexicalEntriesInBse() throws IOException {
        File file = new File(Mocks.BSE_IFO_PATH);
        BookInfo bookInfo = new BookInfo(file);
        Book book = new Book(file);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(95058, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i += 50) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
            LexicalEntry lexicalEntry = book.getLexicalEntry(indexEntry);
            assertEquals(lexicalEntry.getLemma(), lemma);
            // Check that definitions contain lemma, skipping lemmas consisting of more than one word.
            if (!lemma.contains(" ") && !lemma.contains("&")) {
                assertTrue(
                    lemma + " is not contained in the definitions: " + lexicalEntry.getDefinitions(),
                    lexicalEntry.getDefinitions().toLowerCase().contains(lemma.toLowerCase())
                );
            }
        }
        book.closeResources();
    }

    @Test(timeout = 3500)
    public void buildIndexWordsHaveLexicalEntriesInMueller() throws IOException {
        File file = new File(Mocks.MUELLER_IFO_PATH);
        BookInfo bookInfo = new BookInfo(file);
        Book book = new Book(file);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(46198, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i += 50) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
            LexicalEntry lexicalEntry = book.getLexicalEntry(indexEntry);
            assertEquals(lexicalEntry.getLemma(), lemma);
        }
        book.closeResources();
    }

    @Test(timeout = 7000)
    public void buildIndexWordsHaveLexicalEntriesInWordnet() throws IOException {
        File file = new File(Mocks.WORDNET_IFO_PATH);
        BookInfo bookInfo = new BookInfo(file);
        Book book = new Book(file);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
        int indexedCount = index.buildIndex();
        assertEquals(117659, indexedCount);
        assertEquals(bookInfo.getWordCount(), index.getSize());
        for (int i = 0; i < indexedCount; i += 50) {
            IndexEntry indexEntry = index.getIndexEntry(i);
            String lemma = indexEntry.getLemma();
            assertFalse(lemma.isEmpty());
            assertTrue(indexEntry.getWordDataSize() > 0);
            LexicalEntry lexicalEntry = book.getLexicalEntry(indexEntry);
            assertEquals(lexicalEntry.getLemma(), lemma);
        }
        index.close();
        book.closeResources();
    }
}
