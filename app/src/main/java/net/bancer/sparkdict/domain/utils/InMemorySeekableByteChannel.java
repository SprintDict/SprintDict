package net.bancer.sparkdict.domain.utils;

import net.bancer.sparkdict.domain.core.DictionaryFiles;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Minimal read-only SeekableByteChannel backed by an in-memory byte array.
 *
 * <p>Used by {@link DictionaryFiles#readFully} to serve repeated
 * random-access reads without holding any live connection to the file's
 * original storage -- see that method's documentation for why this
 * exists. Write and truncate are unsupported since nothing that constructs
 * this class needs them.</p>
 */
public class InMemorySeekableByteChannel implements SeekableByteChannel {

    private final byte[] data;
    private int position = 0;
    private boolean open = true;

    public InMemorySeekableByteChannel(byte[] data) {
        this.data = data;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();
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
    public long position() throws IOException {
        ensureOpen();
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        ensureOpen();
        if (newPosition < 0 || newPosition > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid position: " + newPosition);
        }
        position = (int) newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        ensureOpen();
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

    private void ensureOpen() throws ClosedChannelException {
        if (!open) {
            throw new ClosedChannelException();
        }
    }
}
