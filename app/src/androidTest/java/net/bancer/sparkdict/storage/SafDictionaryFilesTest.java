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

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SafDictionaryFilesTest {

    private static final String TEMP_FILE_NAME = "SafDictionaryFilesTest-" + System.currentTimeMillis() + ".tmp";
    Context context;

    private SafDictionaryFiles dictionaryFiles;
    private File temporaryFile;

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
        if (temporaryFile != null && temporaryFile.exists()) {
            assertTrue(
                "Could not delete temporary test file: " + temporaryFile,
                temporaryFile.delete()
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
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3A" + Mocks.ROOT_FOLDER + Mocks.MUELLER_FOLDER;
        Uri treeUri = Uri.parse(uriString);
        SafDictionaryFiles dictionaryFiles = new SafDictionaryFiles(context, treeUri);
        List<String> paths = dictionaryFiles.findDictionaryMetaFilePaths();
        assertEquals(0, paths.size());
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
        File file = new File(Mocks.MUELLER_IFO_PATH);
        assertTrue(
            "Test dictionary file does not exist: " + file,
            file.isFile()
        );
        byte[] expected = readBytes(file);
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
                "mueller/does-not-exist-" + System.currentTimeMillis()
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
                "Expected a 'folder/name' path, got: " + "Mueller7GPL" + BookInfo.INFO_FILE_EXTENTION,
                e.getMessage()
            );
        }
    }

    @Test
    public void createForWriteCreatesFileInExistingDirectory()
        throws IOException {
        String path = "mueller/" + TEMP_FILE_NAME;
        temporaryFile = new File(Mocks.MUELLER_DICT_PATH, TEMP_FILE_NAME);
        byte[] expected = {1, 2, 3, 4, 5};
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(expected);
        }
        assertTrue(temporaryFile.isFile());
        assertArrayEquals(expected, readBytes(temporaryFile));
    }

    @Test
    public void createForWriteOverwritesExistingTemporaryFile()
        throws IOException {
        String path = "mueller/" + TEMP_FILE_NAME;
        temporaryFile = new File(Mocks.MUELLER_DICT_PATH, TEMP_FILE_NAME);
        byte[] first = {1, 2, 3, 4};
        byte[] second = {5, 6};
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(first);
        }
        assertArrayEquals(first, readBytes(temporaryFile));
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(second);
        }
        assertArrayEquals(second, readBytes(temporaryFile));
    }

    @Test
    public void createForWriteCreatesFileWithExactName()
        throws IOException {
        String fileName = "SafDictionaryFilesTest-" + System.currentTimeMillis() + ".dict.dz";
        String path = "mueller/" + fileName;
        temporaryFile = new File(Mocks.MUELLER_DICT_PATH, fileName);
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(1);
        }
        assertTrue(temporaryFile.isFile());
        assertFalse(
            new File(
                Mocks.MUELLER_DICT_PATH,
                fileName + ".dict"
            ).exists()
        );
    }

    @Test
    public void createForWriteThrowsIOExceptionForInvalidPath() {
        try {
            dictionaryFiles.createForWrite("invalid-file");
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals(
                "Expected a 'folder/name' path, got: invalid-file",
                e.getMessage()
            );
        }
    }

    @Test
    public void deleteReturnsFalseForMissingFile() {
        assertFalse(
            dictionaryFiles.delete(
                "mueller/does-not-exist-" + System.currentTimeMillis()
            )
        );
    }

    @Test
    public void deleteDeletesTemporaryFile() throws IOException {
        String path = "mueller/" + TEMP_FILE_NAME;
        temporaryFile = new File(Mocks.MUELLER_DICT_PATH, TEMP_FILE_NAME);
        try (OutputStream output = dictionaryFiles.createForWrite(path)) {
            output.write(1);
        }
        assertTrue(temporaryFile.exists());
        assertTrue(dictionaryFiles.delete(path));
        assertFalse(temporaryFile.exists());
        temporaryFile = null;
    }

    @Test
    public void deleteReturnsFalseForInvalidPath() {
        assertFalse(dictionaryFiles.delete("invalid-file"));
    }

    @Test
    public void deleteDoesNotDeleteExistingDictionaryFile() {
        assertFalse(
            dictionaryFiles.delete(
                "mueller/does-not-exist-" + System.currentTimeMillis()
            )
        );
        assertTrue(new File(Mocks.MUELLER_IFO_PATH).isFile());
    }

    private byte[] readFirstBytes(File file, int size) throws IOException {
        byte[] data = new byte[size];
        try (InputStream input = Files.newInputStream(file.toPath())) {
            int offset = 0;
            while (offset < data.length) {
                int count = input.read(
                    data,
                    offset,
                    data.length - offset
                );
                if (count == -1) {
                    break;
                }
                offset += count;
            }
            if (offset != data.length) {
                byte[] result = new byte[offset];
                System.arraycopy(data, 0, result, 0, offset);
                return result;
            }
        }
        return data;
    }

    private byte[] readBytes(File file) throws IOException {
        return readFirstBytes((file), (int) file.length());
    }
}
