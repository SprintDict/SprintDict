package net.bancer.sparkdict.storage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SafDictionaryFilesTest {

    private static final String TEMP_FILE_NAME = "SafDictionaryFilesTest-" + System.currentTimeMillis() + ".tmp";
    Context context;

    private SafDictionaryFiles dictionaryFiles;

    private String temporaryFile;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // String as stored in PREF_DICT_ROOT_URI_NAME
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3A" + Mocks.ROOT_FOLDER;
        Uri treeUri = Uri.parse(uriString);
        dictionaryFiles = new SafDictionaryFiles(context, treeUri);
    }

    @After
    public void tearDown() {
        if (temporaryFile != null && dictionaryFiles.exists(temporaryFile)) {
            assertTrue(
                "Could not delete temporary test file: " + temporaryFile,
                dictionaryFiles.delete(temporaryFile)
            );
        }
    }

    @Test
    public void findDictionaryMetaFilePathsNonExisting() {
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3Anon-existing-path";
        Uri treeUri = Uri.parse(uriString);
        SafDictionaryFiles dictionaryFiles = new SafDictionaryFiles(context, treeUri);
        List<String> paths = dictionaryFiles.findDictionaryMetaFilePaths();
        assertEquals(0, paths.size());
    }

    @Test
    public void findDictionaryMetaFilePathsMuellerFolder() {
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3A" + Mocks.ROOT_FOLDER + "/" + Mocks.MUELLER_FOLDER;
        Uri treeUri = Uri.parse(uriString);
        SafDictionaryFiles dictionaryFiles = new SafDictionaryFiles(context, treeUri);
        List<String> paths = dictionaryFiles.findDictionaryMetaFilePaths();
        assertEquals(4, paths.size());
    }

    @Test
    public void findDictionaryMetaFilePathsNotFolder() {
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3A" + Mocks.MUELLER_IFO_PATH_RELATIVE;
        Uri treeUri = Uri.parse(uriString);
        SafDictionaryFiles dictionaryFiles = new SafDictionaryFiles(context, treeUri);
        List<String> paths = dictionaryFiles.findDictionaryMetaFilePaths();
        assertEquals(0, paths.size());
    }

    @Test
    public void findDictionaryMetaFilePathsDocumentsFolder() {
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3ADocuments";
        Uri treeUri = Uri.parse(uriString);
        SafDictionaryFiles dictionaryFiles = new SafDictionaryFiles(context, treeUri);
        List<String> paths = dictionaryFiles.findDictionaryMetaFilePaths();
        assertEquals(0, paths.size());
    }

    @Test
    public void findDictionaryMetaFilePathsFindsExistingDictionaries() {
        List<String> paths = dictionaryFiles.findDictionaryMetaFilePaths();
        assertNotNull(paths);
        assertTrue(paths.contains(Mocks.MUELLER_IFO_PATH_RELATIVE));
        assertTrue(paths.contains(Mocks.BSE_IFO_PATH_RELATIVE));
        assertTrue(paths.contains(Mocks.WORDNET_IFO_PATH_RELATIVE));
        assertTrue(paths.contains(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE));
        assertEquals(4, paths.size());
    }

    @Test
    public void openForReadReadsExistingIfoFile() throws IOException {
        try (SeekableByteChannel channel = dictionaryFiles.openForRead(Mocks.MUELLER_IFO_PATH_RELATIVE)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            assertEquals(buffer.capacity(), channel.read(buffer));
            buffer.flip();
            String content = new String(
                buffer.array(),
                buffer.position(),
                buffer.remaining(),
                StandardCharsets.UTF_8
            );
            assertTrue(content.contains("bookname=Mueller7GPL"));
        }
    }

    @Test
    public void openForReadSupportsSeeking() throws IOException {
        byte[] expected = readAllBytes(Mocks.MUELLER_IFO_PATH_RELATIVE);
        assertTrue(expected.length > 4);
        try (SeekableByteChannel channel = dictionaryFiles.openForRead(Mocks.MUELLER_IFO_PATH_RELATIVE)) {
            channel.position(2);
            ByteBuffer buffer = ByteBuffer.allocate(2);
            assertEquals(2, channel.read(buffer));
            buffer.flip();
            byte[] actual = new byte[2];
            buffer.get(actual);
            assertArrayEquals(
                new byte[]{expected[2], expected[3]},
                actual
            );
        }
    }

    @Test
    public void openForReadThrowsFileNotFoundExceptionForMissingFile() {
        try {
            dictionaryFiles.openForRead(
                Mocks.MUELLER_FOLDER + "/does-not-exist-" + System.currentTimeMillis()
            );
            fail("Expected FileNotFoundException");
        } catch (IOException e) {
            assertTrue(e instanceof FileNotFoundException);
        }
    }

    @Test
    public void openForReadThrowsIOExceptionForInvalidPath() {
        try {
            dictionaryFiles.openForRead("Mueller7GPL" + BookInfo.INFO_FILE_EXTENTION);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals(
                "Mueller7GPL" + BookInfo.INFO_FILE_EXTENTION,
                e.getMessage()
            );
        }
    }

    @Test
    public void createForWriteCreatesFileInExistingDirectory() throws IOException {
        String path = Mocks.MUELLER_FOLDER + "/" + TEMP_FILE_NAME;
        byte[] expected = {1, 2, 3, 4, 5};
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(expected);
        }
        assertTrue(dictionaryFiles.exists(path));
        try (SeekableByteChannel channel = dictionaryFiles.openForRead(path)) {
            ByteBuffer buffer = ByteBuffer.allocate(expected.length);
            while (buffer.hasRemaining()) {
                int read = channel.read(buffer);
                assertTrue("Unexpected end of file while verifying written bytes", read != -1);
            }
            assertArrayEquals(expected, buffer.array());
        }
    }

    @Test
    public void createForWriteOverwritesExistingTemporaryFile() throws IOException {
        String path = Mocks.MUELLER_FOLDER + "/" + TEMP_FILE_NAME;
        temporaryFile = path;
        byte[] first = {1, 2, 3, 4};
        byte[] second = {5, 6};
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(first);
        }
        assertArrayEquals(first, readAllBytes(path));
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(second);
        }
        assertArrayEquals(second, readAllBytes(path));
    }

    @Test
    public void createForWriteCreatesFileWithExactName() throws IOException {
        String fileName = "SafDictionaryFilesTest-" + System.currentTimeMillis() + Book.DICT_FILE_EXTENSION;
        String path = Mocks.MUELLER_FOLDER + "/" + fileName;
        temporaryFile = path;
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(1);
        }
        assertTrue(dictionaryFiles.exists(path));
        assertFalse(dictionaryFiles.exists(Mocks.MUELLER_FOLDER + "/" + fileName + ".dict"));
    }

    @Test
    public void createForWriteThrowsIOExceptionForInvalidPath() {
        try {
            dictionaryFiles.createForWrite("invalid-file");
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals(
                "Cannot create or find document: invalid-file",
                e.getMessage()
            );
        }
    }

    @Test
    public void deleteReturnsFalseForMissingFile() {
        assertFalse(
            dictionaryFiles.delete(
                Mocks.MUELLER_FOLDER + "/does-not-exist-" + System.currentTimeMillis()
            )
        );
    }

    @Test
    public void deleteDeletesTemporaryFile() throws IOException {
        String path = Mocks.MUELLER_FOLDER + "/" + TEMP_FILE_NAME;
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(1);
        }
        assertTrue(dictionaryFiles.exists(path));
        assertTrue(dictionaryFiles.delete(path));
        assertFalse(dictionaryFiles.exists(path));
    }

    @Test
    public void deleteReturnsFalseForInvalidPath() {
        assertFalse(dictionaryFiles.delete("invalid-file"));
    }

    @Test
    public void deleteDoesNotDeleteExistingDictionaryFile() {
        assertFalse(
            dictionaryFiles.delete(
                Mocks.MUELLER_FOLDER + "/does-not-exist-" + System.currentTimeMillis()
            )
        );
        assertTrue(new File(Mocks.MUELLER_IFO_PATH).isFile());
    }

    private byte[] readAllBytes(String path) throws IOException {
        try (SeekableByteChannel channel = dictionaryFiles.openForRead(path)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            while (buffer.hasRemaining()) {
                int read = channel.read(buffer);
                assertTrue("Unexpected end of file while reading: " + path, read != -1);
            }
            return buffer.array();
        }
    }
}
