package net.bancer.sparkdict.domain.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Shelf is place where all dictionaries (books) are located.
 */
public class Shelf {

    private ArrayList<Book> books;

    private final String dictPath;

    /**
     * Array containing titles of enabled dictionaries.
     */
    private final String[] enabledDicts;

    /**
     * Constructor.
     *
     * @param dictPath     path to the dictionaries.
     * @param enabledDicts string array of the enabled dictionaries titles.
     */
    public Shelf(String dictPath, String[] enabledDicts) {
        this.dictPath = dictPath;
        this.enabledDicts = enabledDicts;
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
     * @return HashMap of all books.
     */
    private HashMap<String, Book> constructBooksMap() {
        // Use hashmap in order to make later sorting easier
        HashMap<String, Book> booksMap = new HashMap<>();
        ArrayList<File> infoFiles = findDictMetaFiles();
        for (int i = 0; i < infoFiles.size(); i++) {
            Book dic = new Book(infoFiles.get(i));
            booksMap.put(dic.getBookName(), dic);
        }
        return booksMap;
    }

    /**
     * Finds dictionary metadata files in the configured dictionary path.
     * {@code .ifo} files. If the dictionary path or one of its subdirectories
     * cannot be listed, the corresponding entries are skipped.</p>
     *
     * @return a list of dictionary metadata files
     */
    private ArrayList<File> findDictMetaFiles() {
        ArrayList<File> result = new ArrayList<>();
        File[] dictFolders = new File(dictPath).listFiles();
        if (dictFolders != null) {
            for (File dictFolder : dictFolders) {
                if (dictFolder.isDirectory()) {
                    File[] dictFiles = dictFolder.listFiles();
                    if (dictFiles != null) {
                        for (File file : dictFiles) {
                            if (file.toString().endsWith(BookInfo.INFO_FILE_EXTENTION)) {
                                result.add(file);
                            }
                        }
                    }
                }
            }
        } //else {
            //TODO: log "Failed to list files in " + dictPath
        //}
        return result;
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
            book.closeResources();
        }
    }
}
