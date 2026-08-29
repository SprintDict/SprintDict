package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import net.bancer.sparkdict.Fixtures;
import net.bancer.sparkdict.domain.utils.DomainException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class DictZipFileTest {

    private DictZipFile dictZipFile;

    private Book book;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        Fixtures.buildSparkDictIndex();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Fixtures.deleteSparkDictIndex();
    }

    private IndexEntry findIndexEntry(String lemma) throws DomainException {
        IndexEntriesIterator iterator = (IndexEntriesIterator) book.iterator();
        return iterator.findIndexEntry(lemma);
    }

    @Before
    public void setUp() throws IOException {
        dictZipFile = new DictZipFile(Fixtures.GCIDE_DICT_DZ_FILE);
        book = new Book(new File(Fixtures.GCIDE_IFO_FILE));
    }

    @After
    public void tearDown() {
        dictZipFile.close();
    }

    @Test(expected = IOException.class)
    public void constructorHandlesMissingFile() throws IOException {
        DictZipFile dictZipFile = new DictZipFile(Fixtures.TEST_DATA_PATH + "does-not-exist.dict.dz");
        dictZipFile.close();
    }

    @Test
    public void constructorHandlesNonGzipFile() {
        try {
            new DictZipFile(Fixtures.GCIDE_IFO_FILE);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Not a gzipped file", e.getMessage());
        }
    }

    @Test
    public void readReturnsDictionaryEntry() throws DomainException, IOException {
        IndexEntry indexEntry = findIndexEntry("aardvark");
        byte[] actual = dictZipFile.read(
            indexEntry.getWordDataOffset(),
            indexEntry.getWordDataSize()
        );
        assertEquals(indexEntry.getWordDataSize(), actual.length);
    }

    @Test
    public void readReturnsExpectedDictionaryEntry() throws DomainException, IOException {
        IndexEntry indexEntry = findIndexEntry("aardvark");
        byte[] actual = dictZipFile.read(
            indexEntry.getWordDataOffset(),
            indexEntry.getWordDataSize()
        );
        String expectedDefinition = "<p>Ø<b style=\"color: #00b\">aardvark</b> <i>(ärdvärk)</i>," +
            " <i style=\"color: #a00\">n.</i> [D., earth-pig.]" +
            " <span style=\"color: #00b\">(Zool.)</span>" +
            " An edentate mammal, of the genus <span style=\"color: #8B4513\">Orycteropus</span>" +
            " (<span style=\"color: #8B4513\">Orycteropus afer</span>), somewhat resembling a pig," +
            " common in some parts of Southern Africa.  It is a nocturnal <isa>ungulate</isa>," +
            " burrows in the ground with its powerful claws, and feeds entirely on ants and" +
            " termites, which it catches with its long, extensile, slimy tongue.  It is the sole" +
            " extant representative of the order <ord>Tubulidentata</ord>." +
            "  <altsp>[Spelled also <asp>Aard-vark</asp>.]</altsp> \n" +
            "\n" +
            "<b>Syn. --</b>ant bear, anteater," +
            " <span style=\"color: #8B4513\">Orycteropus afer</span>, oryctere, orycterope</p>";
        String definition = new String(actual, StandardCharsets.UTF_8);
        assertEquals(expectedDefinition, definition);
    }

    @Test
    public void readCanReadSeveralEntries() throws DomainException, IOException {
        IndexEntry first = findIndexEntry("aardvark");
        IndexEntry second = findIndexEntry("abandon");
        byte[] firstData = dictZipFile.read(
            first.getWordDataOffset(),
            first.getWordDataSize()
        );
        byte[] secondData = dictZipFile.read(
            second.getWordDataOffset(),
            second.getWordDataSize()
        );
        assertEquals(first.getWordDataSize(), firstData.length);
        assertEquals(second.getWordDataSize(), secondData.length);
    }

    @Test
    public void readZeroBytesReturnsEmptyArray() throws IOException {
        assertArrayEquals(
            new byte[0],
            dictZipFile.read(0, 0)
        );
    }

    @Test
    public void readCanBeCalledRepeatedly() throws DomainException, IOException {
        IndexEntry first = findIndexEntry("aardvark");
        IndexEntry second = findIndexEntry("abandon");
        byte[] firstData = dictZipFile.read(
            first.getWordDataOffset(),
            first.getWordDataSize()
        );
        dictZipFile.read(
            second.getWordDataOffset(),
            second.getWordDataSize()
        );
        byte[] firstDataAgain = dictZipFile.read(
            first.getWordDataOffset(),
            first.getWordDataSize()
        );
        assertArrayEquals(firstData, firstDataAgain);
    }

    @Test
    public void closeCanBeCalledMultipleTimes() {
        dictZipFile.close();
        dictZipFile.close();
    }

    @Test
    public void channelConstructorReturnsSameDataAsPathConstructor() throws DomainException, IOException {
        IndexEntry indexEntry = findIndexEntry("aardvark");
        byte[] expected = dictZipFile.read(
            indexEntry.getWordDataOffset(),
            indexEntry.getWordDataSize()
        );
        try (
            FileChannel channel = FileChannel.open(
                new File(Fixtures.GCIDE_DICT_DZ_FILE).toPath(),
                StandardOpenOption.READ
            )
        ) {
            DictZipFile viaChannel = new DictZipFile(channel);
            byte[] actual = viaChannel.read(
                indexEntry.getWordDataOffset(),
                indexEntry.getWordDataSize()
            );
            viaChannel.close();
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void channelConstructorCanReadSeveralEntries() throws DomainException, IOException {
        IndexEntry first = findIndexEntry("aardvark");
        IndexEntry second = findIndexEntry("abandon");
        try (FileChannel channel = FileChannel.open(
            new File(Fixtures.GCIDE_DICT_DZ_FILE).toPath(), StandardOpenOption.READ)) {
            DictZipFile viaChannel = new DictZipFile(channel);
            byte[] firstData = viaChannel.read(first.getWordDataOffset(), first.getWordDataSize());
            byte[] secondData = viaChannel.read(second.getWordDataOffset(), second.getWordDataSize());
            byte[] firstDataAgain = viaChannel.read(first.getWordDataOffset(), first.getWordDataSize());
            viaChannel.close();
            assertEquals(first.getWordDataSize(), firstData.length);
            assertEquals(second.getWordDataSize(), secondData.length);
            assertArrayEquals(firstData, firstDataAgain);
        }
    }

    @Test
    public void channelConstructorHandlesNonGzipFile() throws IOException {
        try (FileChannel channel = FileChannel.open(
            new File(Fixtures.GCIDE_IFO_FILE).toPath(), StandardOpenOption.READ)) {
            try {
                new DictZipFile(channel);
                fail("Expected IOException");
            } catch (IOException e) {
                assertEquals("Not a gzipped file", e.getMessage());
            }
        }
    }

    @Test
    public void channelConstructorCloseCanBeCalledMultipleTimes() throws IOException {
        try (FileChannel channel = FileChannel.open(
            new File(Fixtures.GCIDE_DICT_DZ_FILE).toPath(), StandardOpenOption.READ)) {
            DictZipFile viaChannel = new DictZipFile(channel);
            viaChannel.close();
            viaChannel.close();
        }
    }
}
