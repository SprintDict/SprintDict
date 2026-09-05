package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.bancer.sparkdict.Fixtures;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Vector;

public class BookTest {

    private DictionaryFiles dictionaryFiles;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        Fixtures.buildSparkDictIndex();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Fixtures.deleteSparkDictIndex();
    }

    @Before
    public void setUp() {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
    }

    @Test
    public void testGetSuggestionsFromGcide() {
        Vector<IndexEntry> suggestions;
        try (Book book = new Book(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles)) {
            suggestions = book.getSuggestions("abac");
        }
        assertNotNull(suggestions);
        assertEquals(11, suggestions.size());
        assertEquals("abaca", suggestions.get(0).getLemma());
        assertEquals("abacinate", suggestions.get(1).getLemma());
        assertEquals("abacination", suggestions.get(2).getLemma());
        assertEquals("abaciscus", suggestions.get(3).getLemma());
        assertEquals("abacist", suggestions.get(4).getLemma());
        assertEquals("aback", suggestions.get(5).getLemma());
        assertEquals("abactinal", suggestions.get(6).getLemma());
        assertEquals("abaction", suggestions.get(7).getLemma());
        assertEquals("abactor", suggestions.get(8).getLemma());
        assertEquals("abaculus", suggestions.get(9).getLemma());
        assertEquals("abacus", suggestions.get(10).getLemma());
    }
}
