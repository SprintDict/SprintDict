/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import junit.framework.TestCase;

/**
 * @author valera
 *
 */
public class BookTest extends TestCase {
	
	private Book book;
	
	private Book bse;

	/**
	 * @param name
	 */
	public BookTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		book = new Book(new File("/mnt/sdcard/dictionaries/wordnet/wordnet.ifo"));
		bse = new Book(new File("/mnt/sdcard/dictionaries/bse/rus_bse.ifo"));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		book = null;
		bse = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#Book(java.io.File)}.
	 */
	public void testBook() {
		assertNotNull(book);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getInfo()}.
	 */
	public void testGetInfo() {
		BookInfo bookInfo = book.getInfo();
		assertNotNull(bookInfo);
		assertEquals("WordNet", bookInfo.getBookName());
		assertEquals("n", bookInfo.getSameTypeSequence());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#setEnabled(boolean)}.
	 */
	public void testSetEnabled() {
		boolean enabled = book.isEnabled();
		assertEquals(enabled, book.isEnabled());
		book.setEnabled(!enabled);
		assertEquals(!enabled, book.isEnabled());
		book.setEnabled(enabled);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#isEnabled()}.
	 */
	public void testIsEnabled() {
		assertTrue(book.isEnabled() == true || book.isEnabled() == false);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getBookName()}.
	 */
	public void testGetBookName() {
		assertEquals("WordNet", book.getBookName());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#toString()}.
	 */
	public void testToString() {
		assertTrue(book.toString().contains("WordNet"));
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getLexicalEntriesQuantity()}.
	 */
	public void testGetLexicalEntriesQuantity() {
		assertEquals(117659, book.getLexicalEntriesQuantity());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getLexicalEntry(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testGetLexicalEntry() throws DomainException {
		LexicalEntry entry = book.getLexicalEntry("15 May Organization");
		String expected = "<i><font color=\"#006600\">n</font></i><br>" +
				"<gloss>a terrorist organization formed in 1979 by a faction " +
				"of the Popular Front for the Liberation of Palestine but " +
				"disbanded in the 1980s when key members left to join a " +
				"faction of al-Fatah</gloss>";
		assertEquals(expected, entry.getDefinitions());
	}
	
	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getLexicalEntry(java.lang.String)}.
	 * @throws DomainException 
	 */
	public void testGetLexicalEntryWithMultipleIndexEntries() throws DomainException {
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

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#iterator()}.
	 */
	public void testIterator() {
		Iterator<IndexEntry> iterator = book.iterator();
		assertNotNull(iterator);
		assertTrue(iterator instanceof IndexEntriesIterator);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getSuggestions(java.lang.String)}.
	 */
	public void testGetSuggestions() {
		Vector<IndexEntry> suggestions = book.getSuggestions(".");
		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		assertEquals(".22 caliber", suggestions.get(0).getLemma());
		assertEquals(".38 caliber", suggestions.get(1).getLemma());
		assertEquals(".45 caliber", suggestions.get(2).getLemma());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.Book#getSuggestions(java.lang.String)}.
	 */
	public void testGetSuggestionsBSE() {
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
