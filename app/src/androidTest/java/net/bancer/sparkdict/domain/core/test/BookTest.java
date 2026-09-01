package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFiles;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookTest {

    private SafDictionaryFiles safDictionaryFiles;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        safDictionaryFiles = SafDictionaryFilesFactory.create(context);
    }

    @Test
    public void testGetInfo() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        BookInfo bookInfo = book.getInfo();
        assertNotNull(bookInfo);
        assertEquals("WordNet", bookInfo.getBookName());
        assertEquals("n", bookInfo.getSameTypeSequence());
    }

    @Test
    public void testGetInfoFromWordnet() {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        BookInfo bookInfo = book.getInfo();
        assertNotNull(bookInfo);
        assertEquals("WordNet", bookInfo.getBookName());
        assertEquals("n", bookInfo.getSameTypeSequence());
    }

    @Test
    public void testGetInfoFromCambridge() {
        Book book = new Book(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, safDictionaryFiles);
        BookInfo bookInfo = book.getInfo();
        assertNotNull(bookInfo);
        assertEquals("Cambridge Advanced Learners Dictionary 3th Ed. (En-En)", bookInfo.getBookName());
        assertEquals("x", bookInfo.getSameTypeSequence());
    }

    @Test
    public void testSetEnabled() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        boolean enabled = book.isEnabled();
        assertEquals(enabled, book.isEnabled());
        book.setEnabled(!enabled);
        assertEquals(!enabled, book.isEnabled());
        book.setEnabled(enabled);
    }

    @Test
    public void testSetEnabledWordnet() {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        boolean enabled = book.isEnabled();
        assertEquals(enabled, book.isEnabled());
        book.setEnabled(!enabled);
        assertEquals(!enabled, book.isEnabled());
        book.setEnabled(enabled);
    }

    @Test
    public void testGetBookName() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        assertEquals("WordNet", book.getBookName());
    }

    @Test
    public void testGetBookNameFromWordnet() {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        assertEquals("WordNet", book.getBookName());
    }

    @Test
    public void testToString() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        assertTrue(book.toString().contains("WordNet"));
    }

    @Test
    public void testToStringWordnet() {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        assertTrue(book.toString().contains("WordNet"));
    }

    @Test
    public void testGetLexicalEntriesQuantity() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        assertEquals(117659, book.getLexicalEntriesQuantity());
    }

    @Test
    public void testGetLexicalEntriesQuantityFromWordnet() {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        assertEquals(117659, book.getLexicalEntriesQuantity());
    }

    @Test
    public void testGetLexicalEntry() throws DomainException {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        LexicalEntry entry = book.getLexicalEntry("15 May Organization");
        String expected = "<i><font color=\"#006600\">n</font></i><br>" +
            "<gloss>a terrorist organization formed in 1979 by a faction " +
            "of the Popular Front for the Liberation of Palestine but " +
            "disbanded in the 1980s when key members left to join a " +
            "faction of al-Fatah</gloss>";
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void testGetLexicalEntryFromWordnet() throws DomainException {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        LexicalEntry entry = book.getLexicalEntry("15 May Organization");
        String expected = "<i><font color=\"#006600\">n</font></i><br>" +
            "<gloss>a terrorist organization formed in 1979 by a faction " +
            "of the Popular Front for the Liberation of Palestine but " +
            "disbanded in the 1980s when key members left to join a " +
            "faction of al-Fatah</gloss>";
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void testGetLexicalEntryEmptyFromWordnet() throws DomainException {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        LexicalEntry entry = book.getLexicalEntry("");
        assertNull(entry);
    }

    @Test
    public void testGetLexicalEntryFromCambridge() throws DomainException {
        Book book = new Book(Mocks.CAMBRIDGE_IFO_PATH_RELATIVE, safDictionaryFiles);
        LexicalEntry entry = book.getLexicalEntry("abacus");
        String expected = "<big>abacus</big><br><br><b>abacus</b>" +
            " <font color=\"#808080\"> </font>" +
            "<font color=\"#006600\">UK</font>" +
            " <object data=\"z_uka____012.wav\">z_uka____012.wav</object>" +
            " <font color=\"#006600\">US</font>" +
            " <object data=\"z_abacus.wav\">z_abacus.wav</object>" +
            " <font color=\"#008B8B\">[</font>" +
            "<font color=\"#008B8B\">ˈæb.ə.kəs</font>" +
            "<font color=\"#008B8B\">]</font>" +
            " <font color=\"#FFA500\"> noun </font>" +
            "&nbsp;&nbsp;<font color=\"#FF4500\">countable</font>" +
            " <font color=\"#BC8F8F\">[</font>" +
            "<font color=\"#2F4F4F\"><b>abacuses</b></font>" +
            "<font color=\"#BC8F8F\">]</font>" +
            "<br><blockquote>" +
            "<img src=\"x_abacus.jpg\">" +
            "<br> a square or rectangular frame holding an arrangement of small balls on metal" +
            " rods or wires, which is used for counting, adding and subtracting&nbsp;&nbsp;" +
            "</blockquote>" +
            "<br><blockquote><blockquote><blockquote>" +
            "<font color=\"#2F4F4F\"><font color=\"%s\">Thesaurus</font><sup>+</sup>: </font>" +
            "[Weighing, measuring and counting devices]" +
            "</blockquote></blockquote></blockquote>";
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void testGetLexicalEntryFromMueller() throws DomainException {
        Book book = new Book(Mocks.MUELLER_IFO_PATH_RELATIVE, safDictionaryFiles);
        LexicalEntry entry = book.getLexicalEntry("abacus");
        String expected = "ˈæbəkəs\n" +
            "_n. (_pl. -es [Iz], -ci) 1> _ист. счёты" +
            "<br><br>2> _архит. абак(а), верхняя часть капители";
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void testGetLexicalEntryWithMultipleIndexEntries() throws DomainException {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        String expected = "<i><font color=\"#006600\">v</font></i><br>" +
            "<b>&#8226; put away</b><br>" +
            "<b>&#8226; put aside</b><br>" +
            "<gloss>turn away from and put aside, perhaps temporarily; " +
            "&quot;it&apos;s time for you to put away childish " +
            "things&quot;</gloss><br><br>" +
            "<i><font color=\"#006600\">v</font></i><br>" +
            "<b>&#8226; put away</b><br>" +
            "<b>&#8226; put to sleep</b><br>" +
            "<gloss>kill gently, as with an injection; &quot;the cat was " +
            "very ill and we had to put it to sleep&quot;</gloss><br><br>" +
            "<i><font color=\"#006600\">v</font></i><br>" +
            "<b>&#8226; put away</b><br>" +
            "<b>&#8226; put aside</b><br>" +
            "<gloss>stop using; &quot;the children were told to put away " +
            "their toys&quot;; &quot;the students put away their " +
            "notebooks&quot;</gloss>";
        LexicalEntry entry = book.getLexicalEntry("put away");
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void testGetLexicalEntryWithMultipleIndexEntriesFromWordnet() throws DomainException {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        String expected = "<i><font color=\"#006600\">v</font></i><br>" +
            "<b>&#8226; put away</b><br>" +
            "<b>&#8226; put aside</b><br>" +
            "<gloss>turn away from and put aside, perhaps temporarily; " +
            "&quot;it&apos;s time for you to put away childish " +
            "things&quot;</gloss><br><br>" +
            "<i><font color=\"#006600\">v</font></i><br>" +
            "<b>&#8226; put away</b><br>" +
            "<b>&#8226; put to sleep</b><br>" +
            "<gloss>kill gently, as with an injection; &quot;the cat was " +
            "very ill and we had to put it to sleep&quot;</gloss><br><br>" +
            "<i><font color=\"#006600\">v</font></i><br>" +
            "<b>&#8226; put away</b><br>" +
            "<b>&#8226; put aside</b><br>" +
            "<gloss>stop using; &quot;the children were told to put away " +
            "their toys&quot;; &quot;the students put away their " +
            "notebooks&quot;</gloss>";
        LexicalEntry entry = book.getLexicalEntry("put away");
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void testIterator() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        Iterator<IndexEntry> iterator = book.iterator();
        assertNotNull(iterator);
        assertTrue(iterator instanceof IndexEntriesIterator);
    }

    @Test
    public void testIteratorFromWordnet() {
        Book book = new Book(Mocks.WORDNET_IFO_PATH_RELATIVE, safDictionaryFiles);
        Iterator<IndexEntry> iterator = book.iterator();
        assertNotNull(iterator);
        assertTrue(iterator instanceof IndexEntriesIterator);
    }

    @Test
    public void testGetSuggestions() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        Vector<IndexEntry> suggestions = book.getSuggestions(".");
        assertNotNull(suggestions);
        assertEquals(3, suggestions.size());
        assertEquals(".22 caliber", suggestions.get(0).getLemma());
        assertEquals(".38 caliber", suggestions.get(1).getLemma());
        assertEquals(".45 caliber", suggestions.get(2).getLemma());
    }

    @Test
    public void testGetSuggestionsFromWordnet() {
        Book book = new Book(new File(Mocks.WORDNET_IFO_PATH));
        Vector<IndexEntry> suggestions = book.getSuggestions(".");
        assertNotNull(suggestions);
        assertEquals(3, suggestions.size());
        assertEquals(".22 caliber", suggestions.get(0).getLemma());
        assertEquals(".38 caliber", suggestions.get(1).getLemma());
        assertEquals(".45 caliber", suggestions.get(2).getLemma());
    }

    @Test
    public void testGetSuggestionsBSE() {
        Book bse = new Book(new File(Mocks.BSE_IFO_PATH));
        Vector<IndexEntry> suggestions = bse.getSuggestions("Собат");
        assertNotNull(suggestions);
        assertEquals("Собат", suggestions.get(0).getLemma());

        suggestions = bse.getSuggestions("собат");
        assertNotNull(suggestions);
        assertEquals("Собат", suggestions.get(0).getLemma());

        suggestions = bse.getSuggestions("СОБАТ");
        assertNotNull(suggestions);
        assertEquals("Собат", suggestions.get(0).getLemma());
    }
}
