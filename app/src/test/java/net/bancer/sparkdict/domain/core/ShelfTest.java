package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.bancer.sparkdict.Fixtures;

import org.junit.Test;

import java.util.ArrayList;

public class ShelfTest {

    @Test
    public void testNonexistentDictPathReturnsEmptyBookList() {
        String dictPath = Fixtures.TEST_DATA_PATH + "does-not-exist";
        String[] enabledDicts = new String[0];
        Shelf shelf = new Shelf(dictPath, enabledDicts);
        assertTrue(shelf.getBooks().isEmpty());
    }

    @Test
    public void testConstructorPathWithoutDictionaries() {
        String dictPath = System.getProperty("user.dir");
        String[] enabledDicts = new String[0];
        Shelf shelf = new Shelf(dictPath, enabledDicts);
        ArrayList<Book> books = shelf.getBooks();
        assertEquals(0, books.size());
    }

    @Test
    public void testTotalLexicalEntriesQuantity() {
        String[] enabledDicts = new String[0];
        Shelf shelf = new Shelf(Fixtures.TEST_DATA_PATH, enabledDicts);
        assertEquals(4, shelf.getBooks().size());
        int count = shelf.getTotalLexicalEntriesQuantity();
        assertEquals(108633, count);
    }

    @Test
    public void testBooksAreSortedAlphabeticallyAllDisabled() {
        String[] enabledDicts = new String[0];
        Shelf shelf = new Shelf(Fixtures.TEST_DATA_PATH, enabledDicts);
        ArrayList<Book> books = shelf.getBooks();
        assertEquals(4, books.size());
        assertEquals("GNU Collaborative International Dictionary of English", books.get(0).getBookName());
        assertEquals("Test Dictionary", books.get(1).getBookName());
        assertEquals("Test Dummy Dictionary", books.get(2).getBookName());
        assertEquals("Test Multitype Dictionary", books.get(3).getBookName());
    }

    @Test
    public void testBooksAreSortedAlphabeticallyOneEnabled() {
        String[] enabledDicts = new String[]{"Test Dummy Dictionary"};
        Shelf shelf = new Shelf(Fixtures.TEST_DATA_PATH, enabledDicts);
        ArrayList<Book> books = shelf.getBooks();
        assertEquals(4, books.size());
        assertEquals("Test Dummy Dictionary", books.get(0).getBookName());
        assertEquals("GNU Collaborative International Dictionary of English", books.get(1).getBookName());
        assertEquals("Test Dictionary", books.get(2).getBookName());
        assertEquals("Test Multitype Dictionary", books.get(3).getBookName());
    }

    @Test
    public void testCloseResources() {
        String[] enabledDicts = new String[0];
        Shelf shelf = new Shelf(Fixtures.TEST_DATA_PATH, enabledDicts);
        shelf.closeResources();
    }
}
