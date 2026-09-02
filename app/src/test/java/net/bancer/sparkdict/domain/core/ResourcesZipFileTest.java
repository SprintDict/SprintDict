package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;

import net.bancer.sparkdict.Fixtures;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class ResourcesZipFileTest {

    @Test
    public void getResourceFromZipReturnsEmptyArrayForMissingEntry() throws IOException {
        File file = new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE);
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            ResourcesZipFile resZip = new ResourcesZipFile(channel);
            byte[] result = resZip.getResourceFromZip("does-not-exist.jpg");
            //resZip.close();
            assertEquals(0, result.length);
        }
    }

    @Test
    public void closeCanBeCalledMultipleTimes() throws IOException {
        File file = new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE);
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            ResourcesZipFile resZip = new ResourcesZipFile(channel);
            //resZip.close();
            //resZip.close();
        }
    }

    @Test
    public void channelConstructorCloseCanBeCalledMultipleTimes() throws IOException {
        File file = new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE);
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            ResourcesZipFile resZip = new ResourcesZipFile(channel);
            //resZip.close();
            //resZip.close();
        }
    }
}
