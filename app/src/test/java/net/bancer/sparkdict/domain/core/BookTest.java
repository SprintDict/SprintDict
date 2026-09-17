package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.bancer.sparkdict.Fixtures;
import net.bancer.sparkdict.domain.utils.DomainException;

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

    @Test
    public void testGetLexicalEntryFirst() throws DomainException {
        LexicalEntry lexicalEntry;
        try (Book book = new Book(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles)) {
            lexicalEntry = book.getLexicalEntry("-able");
        }
        assertEquals("-able", lexicalEntry.getLemma());
        String definitions = "<p><b style=\"color: #00b\">-able</b> <i>(-ȧbl)</i>." +
            " [F. <span style=\"color: #8B4513\">-able</span>," +
            " L. <span style=\"color: #8B4513\">-abilis</span>.]" +
            " An adjective suffix now usually in a passive sense; able to be; fit to be;" +
            " expressing capacity or worthiness in a passive sense;" +
            " as, <span style=\"color: 33a\">mov<i>able</i>, able to be moved; amend<i>able</i>," +
            " able to be amended; blam<i>able</i>, fit to be blamed; sal<i>able</i>.</span></p>" +
            "<p>The form <altsp><span style=\"color: #00b\">-ible</span></altsp> is used in the same sense.</p>" +
            "<p>☞ It is difficult to say when we are not to use -<i>able</i> instead of <i>-ible</i>." +
            " “Yet a rule may be laid down as to when we are to use it. To all verbs, then, from" +
            " the Anglo-Saxon, to all based on the uncorrupted infinitival stems of Latin verbs" +
            " of the first conjugation, and to all substantives, whencesoever sprung," +
            " we annex -<i>able</i> only.”&nbsp;&nbsp;<small>Fitzed. Hall.</small></p>";
        assertEquals(definitions, lexicalEntry.getDefinitions());
    }

    @Test
    public void testGetLexicalEntryLast() throws DomainException {
        LexicalEntry lexicalEntry;
        try (Book book = new Book(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles)) {
            lexicalEntry = book.getLexicalEntry("zythum");
        }
        assertEquals("zythum", lexicalEntry.getLemma());
        String definitions = "<p>Ø<b style=\"color: #00b\">Zythum</b> <i>(zĭthŭm)</i>," +
            " <i style=\"color: #a00\">n.</i>" +
            " [L., fr. Gr. ζῦθος a kind of beer; -- so called by the Egyptians.]" +
            " A kind of ancient malt beverage; a liquor made from malt and wheat." +
            "&nbsp;&nbsp;<altsp>[Written also <asp>zythem</asp>.]</altsp></p>" +
            "<p><!-- End of definitions section of the dictionary --></p>";
        assertEquals(definitions, lexicalEntry.getDefinitions());
    }
}
