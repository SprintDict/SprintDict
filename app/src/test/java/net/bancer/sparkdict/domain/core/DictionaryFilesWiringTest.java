package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.bancer.sparkdict.Fixtures;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class DictionaryFilesWiringTest {

    private static final byte[] STUB_IFO_BYTES = (
        "StarDict's dict ifo file\n" +
            "version=2.4.2\n" +
            "bookname=Stub\n" +
            "wordcount=0\n"
    ).getBytes(StandardCharsets.UTF_8);

    private static final DictionaryFiles STUB = new DictionaryFiles() {
        @Override public List<String> findDictionaryMetaFilePaths() {
            return Collections.singletonList("Stub/Stub.ifo");
        }
        @Override public SeekableByteChannel openForRead(String path) {
            return new SeekableInMemoryByteChannel(STUB_IFO_BYTES.clone());
        }
        @Override public OutputStream createForWrite(String path) { throw new UnsupportedOperationException(); }
        @Override public boolean delete(String path) { throw new UnsupportedOperationException(); }
    };

    @Test
    public void bookInfoStoresSuppliedDictionaryFiles() {
        BookInfo info = new BookInfo("Stub/Stub.ifo", STUB);
        assertSame(STUB, info.getDictionaryFiles());
        assertEquals("Stub", info.getBookName());
    }

    @Test
    public void bookInfoDefaultsToFileDictionaryFilesWhenNotSupplied() {
        BookInfo info = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        assertTrue(info.getDictionaryFiles() instanceof FileDictionaryFiles);
    }

    @Test
    public void bookStoresSuppliedDictionaryFilesAndPassesItToBookInfo() {
        Book book = new Book(new File(Fixtures.GCIDE_IFO_FILE), STUB);
        assertSame(STUB, book.getDictionaryFiles());
        assertSame(STUB, book.getInfo().getDictionaryFiles());
    }

    @Test
    public void shelfThreadsSameDictionaryFilesInstanceIntoEveryBook() {
        Shelf shelf = new Shelf(Fixtures.TEST_DATA_PATH, new String[0], STUB);
        assertSame(STUB, shelf.getDictionaryFiles());
        for (Book book : shelf.getBooks()) {
            assertSame(STUB, book.getDictionaryFiles());
            assertSame(STUB, book.getInfo().getDictionaryFiles());
        }
    }
}
