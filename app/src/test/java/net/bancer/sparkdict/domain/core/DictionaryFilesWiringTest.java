package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import net.bancer.sparkdict.Fixtures;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
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
            return new InMemorySeekableByteChannel(STUB_IFO_BYTES.clone());
        }
        @Override public OutputStream createForWrite(String path) { throw new UnsupportedOperationException(); }
        @Override public boolean delete(String path) { throw new UnsupportedOperationException(); }
    };

    /**
     * Minimal read-only SeekableByteChannel backed by a byte array, just
     * enough for BookInfo's .ifo parsing (position + read + size). Write and
     * truncate are unsupported since nothing in this test needs them.
     */
    private static final class InMemorySeekableByteChannel implements SeekableByteChannel {

        private final byte[] data;
        private int position = 0;
        private boolean open = true;

        private InMemorySeekableByteChannel(byte[] data) {
            this.data = data;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            if (position >= data.length) {
                return -1;
            }
            int length = Math.min(dst.remaining(), data.length - position);
            dst.put(data, position, length);
            position += length;
            return length;
        }

        @Override
        public int write(ByteBuffer src) {
            throw new NonWritableChannelException();
        }

        @Override
        public long position() {
            return position;
        }

        @Override
        public SeekableByteChannel position(long newPosition) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            position = (int) newPosition;
            return this;
        }

        @Override
        public long size() {
            return data.length;
        }

        @Override
        public SeekableByteChannel truncate(long size) {
            throw new NonWritableChannelException();
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }
    }

    @Test
    public void bookInfoStoresSuppliedDictionaryFiles() {
        BookInfo info = new BookInfo("Stub/Stub.ifo", STUB);
        assertSame(STUB, info.getDictionaryFiles());
        assertEquals("Stub", info.getBookName());
    }

    @Test
    public void bookStoresSuppliedDictionaryFilesAndPassesItToBookInfo() {
        try (Book book = new Book(Fixtures.GCIDE_IFO_FILE, STUB)) {
            assertSame(STUB, book.getDictionaryFiles());
            assertSame(STUB, book.getInfo().getDictionaryFiles());
        }
    }

    @Test
    public void shelfThreadsSameDictionaryFilesInstanceIntoEveryBook() {
        Shelf shelf = new Shelf(new String[0], STUB);
        assertSame(STUB, shelf.getDictionaryFiles());
        for (Book book : shelf.getBooks()) {
            assertSame(STUB, book.getDictionaryFiles());
            assertSame(STUB, book.getInfo().getDictionaryFiles());
        }
    }
}
