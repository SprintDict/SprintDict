package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.bancer.sparkdict.Fixtures;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class SparkDictIndexTest {

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
    }

    @Test
    public void intToByteArrayReturnsBigEndianByteArray() {
        assertArrayEquals(
            new byte[] {0x01, 0x02, 0x03, 0x04},
            SparkDictIndex.intToByteArray(0x01020304)
        );
    }

    @Test
    public void intToByteArrayHandlesNegativeValue() {
        assertArrayEquals(
            new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD, (byte) 0xFC},
            SparkDictIndex.intToByteArray(0xFFFEFDFC)
        );
    }

    @Test
    public void byteArrayToIntReturnsInteger() {
        assertEquals(
            0x01020304,
            SparkDictIndex.byteArrayToInt(
                new byte[] {0x01, 0x02, 0x03, 0x04}
            )
        );
    }

    @Test
    public void byteArrayToIntHandlesNegativeValue() {
        assertEquals(
            0xFFFEFDFC,
            SparkDictIndex.byteArrayToInt(
                new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0xFD, (byte) 0xFC}
            )
        );
    }

    @Test
    public void getBookNameReturnsBookName() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        assertEquals(
            bookInfo.getBookName(),
            index.getBookName()
        );
    }

    @Test
    public void buildIndexCreatesIndexWithExpectedSize() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
        assertEquals(bookInfo.getWordCount(), index.getSize());
        assertNotNull(index.getIndexEntry(0));
        assertNotNull(index.getIndexEntry(index.getSize() - 1));
        assertTrue(index.delete());
    }

    @Test
    public void getIndexEntryReturnsFirstEntry() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
        IndexEntry entry = index.getIndexEntry(0);
        assertEquals("-able", entry.getLemma());
        assertTrue(index.delete());
    }

    @Test
    public void getIndexEntryReturnsNullWhenIdIsBeyondIndex() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
        assertNull(index.getIndexEntry(index.getSize()));
        assertTrue(index.delete());
    }

    private static class TestObserver implements IObserver {

        private String tag;
        private int value;

        @Override
        public void update(Object field, int value) {
            this.tag = field.toString();
            this.value = value;
        }
    }

    @Test
    public void registerObserverReceivesArticlesIndexedNotification() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        TestObserver observer = new TestObserver();
        index.registerObserver(observer);
        index.buildIndex();
        assertEquals(SparkDictIndex.ARTICLES_INDEXED_TAG, observer.tag);
        assertEquals(index.getSize(), observer.value);
        assertTrue(index.delete());
    }

    @Test
    public void removeObserverStopsNotifications() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        TestObserver observer = new TestObserver();
        index.registerObserver(observer);
        index.removeObserver(observer);
        index.notifyObservers();
        assertNull(observer.tag);
    }
}
