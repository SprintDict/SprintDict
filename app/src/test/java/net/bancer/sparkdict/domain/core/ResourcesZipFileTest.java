package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;

import net.bancer.sparkdict.Fixtures;

import org.junit.Before;
import org.junit.Test;

public class ResourcesZipFileTest {

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
    }

    @Test
    public void getResourceFromZipReturnsEmptyArrayForMissingEntry() {
        ResourcesZipFile resZip = new ResourcesZipFile(Fixtures.DUMMY_MULTI_RES_ZIP_FILE, dictionaryFiles);
        byte[] result = resZip.getResourceFromZip("does-not-exist.jpg");
        resZip.close();
        assertEquals(0, result.length);
    }

    @Test
    public void closeCanBeCalledMultipleTimes() {
        ResourcesZipFile resZip = new ResourcesZipFile(Fixtures.DUMMY_MULTI_RES_ZIP_FILE, dictionaryFiles);
        resZip.close();
    }

    @Test
    public void channelConstructorCloseCanBeCalledMultipleTimes() {
        ResourcesZipFile resZip = new ResourcesZipFile(Fixtures.DUMMY_MULTI_RES_ZIP_FILE, dictionaryFiles);
        resZip.close();
        resZip.close();
    }
}
