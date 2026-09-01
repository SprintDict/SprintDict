package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.ResourcesZipFile;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFiles;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

@RunWith(AndroidJUnit4.class)
public class ResourcesZipFileTest {

    private SafDictionaryFiles safDictionaryFiles;

    @Before
    public void setUp() throws DomainException {
        Context context = ApplicationProvider.getApplicationContext();
        safDictionaryFiles = SafDictionaryFilesFactory.create(context);
    }

    @Test
    public void testGetResourceFromZip() throws IOException {
        String resZipPath = Mocks.CAMBRIDGE_RES_ZIP_PATH_RELATIVE;
        SeekableByteChannel channel = safDictionaryFiles.openForRead(resZipPath);
        ResourcesZipFile resZipFile = new ResourcesZipFile(channel);
        byte[] image = resZipFile.getResourceFromZip("x_abacus.jpg");
        resZipFile.close();
        assertEquals(37891, image.length);
    }
}
