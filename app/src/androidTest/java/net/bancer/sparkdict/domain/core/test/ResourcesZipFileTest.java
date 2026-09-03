package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.ResourcesZipFile;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResourcesZipFileTest {

    private DictionaryFiles dictionaryFiles;

    @Before
    public void setUp() throws DomainException {
        Context context = ApplicationProvider.getApplicationContext();
        dictionaryFiles = SafDictionaryFilesFactory.create(context);
    }

    @Test
    public void testGetResourceFromZip() {
        ResourcesZipFile resZipFile = new ResourcesZipFile(Mocks.CAMBRIDGE_RES_ZIP_PATH_RELATIVE, dictionaryFiles);
        byte[] image = resZipFile.getResourceFromZip("x_abacus.jpg");
        resZipFile.close();
        assertEquals(37891, image.length);
    }
}
