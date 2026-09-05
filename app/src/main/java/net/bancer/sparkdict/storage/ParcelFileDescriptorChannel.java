package net.bancer.sparkdict.storage;

import android.os.ParcelFileDescriptor;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Wraps a {@link SeekableByteChannel} derived from a
 * {@link ParcelFileDescriptor}, retaining strong references to the
 * ParcelFileDescriptor and the intermediate stream that produced the
 * channel for as long as this wrapper is held.
 *
 * <p>Both {@code ParcelFileDescriptor} and {@code FileInputStream}/
 * {@code FileOutputStream} rely on finalisers/CloseGuard to close their
 * underlying file descriptor if garbage collected while still "open" --
 * exactly the bug already found and fixed twice elsewhere in this migration
 * (see {@code DictZipFile} and {@code StarDictIndex}). Discarding either
 * intermediate object here, keeping only the derived channel, would
 * reintroduce that same bug a third time.</p>
 */
final class ParcelFileDescriptorChannel implements SeekableByteChannel {

    private final ParcelFileDescriptor parcelFileDescriptor; // keep-alive only
    private final Closeable underlyingStream;                // keep-alive only
    private final SeekableByteChannel channel;

    ParcelFileDescriptorChannel(ParcelFileDescriptor parcelFileDescriptor, Closeable underlyingStream, SeekableByteChannel channel) {
        this.parcelFileDescriptor = parcelFileDescriptor;
        this.underlyingStream = underlyingStream;
        this.channel = channel;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return channel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return channel.write(src);
    }

    @Override
    public long position() throws IOException {
        return channel.position();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        channel.position(newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        return channel.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        channel.truncate(size);
        return this;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        try {
            channel.close();
        } finally {
            try {
                underlyingStream.close();
            } finally {
                parcelFileDescriptor.close();
            }
        }
    }
}
