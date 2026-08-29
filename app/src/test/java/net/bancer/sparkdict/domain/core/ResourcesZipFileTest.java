package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.bancer.sparkdict.Fixtures;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class ResourcesZipFileTest {

    @Test
    public void channelConstructorReturnsSameResourceAsFileConstructor() throws IOException {
        File resZip = new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE);
        ResourcesZipFile viaFile = new ResourcesZipFile(resZip);
        byte[] expected = viaFile.getResourceFromZip("pic/aplander.jpg");
        viaFile.close();
        try (FileChannel channel = FileChannel.open(resZip.toPath(), StandardOpenOption.READ)) {
            ResourcesZipFile viaChannel = new ResourcesZipFile(channel);
            byte[] actual = viaChannel.getResourceFromZip("pic/aplander.jpg");
            viaChannel.close();
            assertTrue(expected.length > 0);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void getResourceFromZipReturnsEmptyArrayForMissingEntry() {
        ResourcesZipFile resZip = new ResourcesZipFile(new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE));
        byte[] result = resZip.getResourceFromZip("does-not-exist.jpg");
        resZip.close();
        assertEquals(0, result.length);
    }

    @Test
    public void closeCanBeCalledMultipleTimes() {
        ResourcesZipFile resZip = new ResourcesZipFile(new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE));
        resZip.close();
        resZip.close();
    }

    @Test
    public void channelConstructorCloseCanBeCalledMultipleTimes() throws IOException {
        try (
            FileChannel channel = FileChannel.open(
                new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE).toPath(),
                StandardOpenOption.READ
            )
        ) {
            ResourcesZipFile resZip = new ResourcesZipFile(channel);
            resZip.close();
            resZip.close();
        }
    }
}
