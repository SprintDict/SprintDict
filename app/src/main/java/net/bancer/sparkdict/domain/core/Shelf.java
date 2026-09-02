package net.bancer.sparkdict.domain.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Shelf is place where all dictionaries (books) are located.
 */
public class Shelf {

    private ArrayList<Book> books;


    /**
     * Array containing titles of enabled dictionaries.
     */
    private final String[] enabledDicts;

    private final DictionaryFiles dictionaryFiles;

    /**
     * Constructor.
     *
     * @param enabledDicts string array of the enabled dictionaries titles.
     * @param dictionaryFiles the DictionaryFiles to associate with this shelf.
     */
    public Shelf(String[] enabledDicts, DictionaryFiles dictionaryFiles) {
        this.enabledDicts = enabledDicts;
        this.dictionaryFiles = dictionaryFiles;
        putBooksOnShelf();
    }

    /**
     * Creates a list of all dictionaries. Enabled dictionaries are ordered
     * according to values saved in preferences. Disabled dictionaries are sorted
     * alphabetically.
     */
    private void putBooksOnShelf() {
        HashMap<String, Book> booksMap = constructBooksMap();
        books = new ArrayList<>();
        // iterate through enabled dictionaries while removing them from hashmap
        // and adding to arraylist
        for (String enabledDict : enabledDicts) {
            Book book = booksMap.remove(enabledDict);
            if (book != null) {
                book.setEnabled(true);
                books.add(book);
            }
        }
        // Sort remaining dictionaries in alphabetical order.
        ArrayList<Book> remainingBooks = new ArrayList<>(booksMap.values());
        remainingBooks.sort(
            Comparator.comparing(Book::getBookName, String.CASE_INSENSITIVE_ORDER)
        );
        books.addAll(remainingBooks);
    }

    /**
     * Constructs Book objects and puts them into a HashMap.
     *
     * <p>Discovery now goes uniformly through {@code dictionaryFiles} for
     * both backends, instead of a File-specific scan -- see
     * {@code FileDictionaryFiles#findDictionaryMetaFilePaths()} and
     * {@code SafDictionaryFiles#findDictionaryMetaFilePaths()}.</p>
     *
     * @return HashMap of all books.
     */
    private HashMap<String, Book> constructBooksMap() {
        // Use hashmap in order to make later sorting easier
        HashMap<String, Book> booksMap = new HashMap<>();
        List<String> ifoPaths = dictionaryFiles.findDictionaryMetaFilePaths();
        for (String ifoPath : ifoPaths) {
            Book dic = new Book(ifoPath, dictionaryFiles);
            booksMap.put(dic.getBookName(), dic);
        }
        return booksMap;
    }

    public DictionaryFiles getDictionaryFiles() {
        return dictionaryFiles;
    }

    /**
     * Books getter.
     *
     * @return array list of Books.
     */
    public ArrayList<Book> getBooks() {
        return books;
    }

    /**
     * Calculates the total quantity of lexical entries in all dictionaries.
     *
     * @return total quantity of lexical entries in all dictionaries.
     */
    public int getTotalLexicalEntriesQuantity() {
        int total = 0;
        for (Book book : books) {
            total += book.getLexicalEntriesQuantity();
        }
        return total;
    }

    /**
     * Closes the resources held by all books in the shelf.
     *
     * <p>If a book has no open resources, its resource clean-up method does nothing.</p>
     */
    public void closeResources() {
        for (Book book : getBooks()) {
            book.close();
        }
    }
}
