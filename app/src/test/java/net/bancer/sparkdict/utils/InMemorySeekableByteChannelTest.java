package net.bancer.sparkdict.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.bancer.sparkdict.domain.utils.InMemorySeekableByteChannel;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

public class InMemorySeekableByteChannelTest {

    @Test
    public void testRead() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(data)) {
            ByteBuffer buffer = ByteBuffer.allocate(3);
            int bytesRead = channel.read(buffer);
            assertEquals(3, bytesRead);
            assertArrayEquals(new byte[]{1, 2, 3}, buffer.array());
            assertEquals(3, channel.position());
        }
    }

    @Test
    public void testReadInMultipleChunks() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(data)) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            assertEquals(2, channel.read(buffer));
            assertArrayEquals(new byte[]{1, 2}, buffer.array());
            buffer.clear();
            assertEquals(2, channel.read(buffer));
            assertArrayEquals(new byte[]{3, 4}, buffer.array());
            buffer.clear();
            assertEquals(1, channel.read(buffer));
            assertEquals(5, buffer.get(0));
            assertEquals(5, channel.position());
        }
    }

    @Test
    public void testReadReturnsEndOfFile() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2})) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            assertEquals(2, channel.read(buffer));
            assertEquals(-1, channel.read(buffer));
        }
    }

    @Test
    public void testReadWithEmptyBuffer() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2})) {
            ByteBuffer buffer = ByteBuffer.allocate(0);
            assertEquals(0, channel.read(buffer));
            assertEquals(0, channel.position());
        }
    }

    @Test
    public void testPosition() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2, 3})) {
            channel.position(2);
            assertEquals(2, channel.position());
        }
    }

    @Test
    public void testPositionAtEndOfFile() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2, 3})) {
            channel.position(channel.size());
            assertEquals(3, channel.position());
            ByteBuffer buffer = ByteBuffer.allocate(1);
            assertEquals(-1, channel.read(buffer));
        }
    }

    @Test
    public void testPositionBeyondEndOfFile() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2, 3})) {
            channel.position(10);
            assertEquals(10, channel.position());
            ByteBuffer buffer = ByteBuffer.allocate(1);
            assertEquals(-1, channel.read(buffer));
        }
    }

    @Test
    public void testPositionNegativeThrowsException() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2, 3})) {
            try {
                channel.position(-1);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Expected.
            }
        }
    }

    @Test
    public void testSize() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2, 3, 4})) {
            assertEquals(4, channel.size());
        }
    }

    @Test
    public void testIsOpen() {
        InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2});
        assertTrue(channel.isOpen());
        channel.close();
        assertFalse(channel.isOpen());
    }

    @Test
    public void testCloseIsIdempotent() {
        InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2});
        channel.close();
        channel.close();
        assertFalse(channel.isOpen());
    }

    @Test
    public void testReadAfterCloseThrowsException() throws IOException {
        InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2});
        channel.close();
        try {
            channel.read(ByteBuffer.allocate(1));
            fail("Expected ClosedChannelException");
        } catch (ClosedChannelException e) {
            // Expected.
        }
    }

    @Test
    public void testPositionAfterCloseThrowsException() throws IOException {
        InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2});
        channel.close();
        try {
            channel.position();
            fail("Expected ClosedChannelException");
        } catch (ClosedChannelException e) {
            // Expected.
        }
    }

    @Test
    public void testSetPositionAfterCloseThrowsException() throws IOException {
        InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2});
        channel.close();
        try {
            channel.position(1);
            fail("Expected ClosedChannelException");
        } catch (ClosedChannelException e) {
            // Expected.
        }
    }

    @Test
    public void testSizeAfterCloseThrowsException() throws IOException {
        InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2});
        channel.close();
        try {
            channel.size();
            fail("Expected ClosedChannelException");
        } catch (ClosedChannelException e) {
            // Expected.
        }
    }

    @Test
    public void testWriteThrowsNonWritableChannelException() {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2})) {
            try {
                channel.write(ByteBuffer.allocate(1));
                fail("Expected NonWritableChannelException");
            } catch (NonWritableChannelException e) {
                // Expected.
            }
        }
    }

    @Test
    public void testTruncateThrowsNonWritableChannelException() {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{1, 2})) {
            try {
                channel.truncate(1);
                fail("Expected NonWritableChannelException");
            } catch (NonWritableChannelException e) {
                // Expected.
            }
        }
    }

    @Test
    public void testReadAfterChangingPosition() throws IOException {
        try (InMemorySeekableByteChannel channel = new InMemorySeekableByteChannel(new byte[]{10, 20, 30, 40})) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            channel.position(1);
            assertEquals(2, channel.read(buffer));
            assertArrayEquals(new byte[]{20, 30}, buffer.array());
            assertEquals(3, channel.position());
        }
    }
}
