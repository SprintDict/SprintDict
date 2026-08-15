package net.bancer.sparkdict.domain.core.test;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.File;

import junit.framework.TestCase;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntriesIterator;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SuggestionsFinderTest extends TestCase {

	private IndexEntriesIterator muellerFinder;
	private IndexEntriesIterator bseFinder;

	@Before
	public void setUp() {
		muellerFinder = (IndexEntriesIterator) new Book(new File(Mocks.MUELLER_IFO_PATH)).iterator();
		bseFinder = (IndexEntriesIterator) new Book(new File(Mocks.BSE_IFO_PATH)).iterator();
	}

	@Test
	public void testNext() throws DomainException {
		IndexEntry indexEntry;
		indexEntry = muellerFinder.nextSuggestion("A");
		assertEquals("A", indexEntry.getLemma());

		indexEntry = muellerFinder.nextSuggestion("A");
		assertEquals("a", indexEntry.getLemma());

		indexEntry = muellerFinder.nextSuggestion("'ca"); // first
		assertEquals("'cause", indexEntry.getLemma());

		indexEntry = muellerFinder.nextSuggestion("ус"); // last
		assertEquals("усил.", indexEntry.getLemma());

		indexEntry = muellerFinder.nextSuggestion("non-existant word");
		assertNull(indexEntry);

		indexEntry = bseFinder.nextSuggestion("Юань");
		assertEquals("Юань (ден. единица КНР)", indexEntry.getLemma());

		indexEntry = bseFinder.nextSuggestion("Москва (столица СССР)");
		assertEquals("Москва (столица СССР)", indexEntry.getLemma());

		indexEntry = bseFinder.nextSuggestion("...Биоз");
		assertEquals("...Биоз", indexEntry.getLemma());

		indexEntry = bseFinder.nextSuggestion("Яёи");
		assertEquals("Яёи культура", indexEntry.getLemma());
	}
}
