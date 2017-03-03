package net.bancer.sparkdict.domain.core;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Shelf is place where all dictionaries (books) are located.
 * 
 * @author Valerij Bancer
 *
 */
public class Shelf {

	private ArrayList<Book> books;
	private String dictPath;
	
	/**
	 * Array containing titles of enabled dictionaries.
	 */
	private String[] enabledDicts;

	/**
	 * Constructor.
	 * 
	 * @param dictPath		path to the dictionaries.
	 * @param enabledDicts	string array of the enabled dictionaries titles.
	 */
	public Shelf(String dictPath, String[] enabledDicts) {
		this.dictPath = dictPath;
		this.enabledDicts = enabledDicts;
		putBooksOnShelf();
	}

	/**
	 * Creates a list of all dictionaries. Enabled dictionaries are ordered
	 * according to values saved in preferences. Disabled dictionaries are not
	 * ordered.
	 */
	private void putBooksOnShelf() {
		HashMap<String, Book> booksMap = constructBooksMap();
		books = new ArrayList<Book>();
		// iterate through enabled dictionaries while removing them from hashmap
		// and adding to arraylist
		for (int i = 0; i < enabledDicts.length; i++) {
			if(booksMap.containsKey(enabledDicts[i])) {
				Book book = booksMap.remove(enabledDicts[i]);
				book.setEnabled(true);
				books.add(book);
			}
		}
		// iterate the remaining hashmap while adding books to arraylist
		for(Book book : booksMap.values()) {
			books.add(book);
		}
	}

	/**
	 * Constructs Book objects and puts them into a HashMap.
	 * 
	 * @return HashMap of all books.
	 */
	private HashMap<String, Book> constructBooksMap() {
		// Use hashmap in order to make later sorting easier
		HashMap<String, Book> booksMap = new HashMap<String, Book>();
		ArrayList<File> infoFiles = findDictMetaFiles();
		for(int i = 0; i < infoFiles.size(); i++) {
			Book dic = new Book(infoFiles.get(i));
			booksMap.put(dic.getBookName(), dic);
		}
		return booksMap;
	}

	private ArrayList<File> findDictMetaFiles() {
		ArrayList<File> result = new ArrayList<File>();
		File[] dictFolders = new File(dictPath).listFiles();
		if (dictFolders != null) {
			for (File dictFolder : dictFolders) {
				if(dictFolder.isDirectory()) {
					File[] dictFiles = dictFolder.listFiles();
					if(dictFiles != null) {
						for (File file : dictFiles) {
							if(file.toString().endsWith(".ifo")) {
								result.add(file);
							}
						}
					}
				}
			}
		}
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
	 * Dictionaries path getter.
	 * 
	 * @return path to dictionaries.
	 */
	public String getDictPath() {
		return dictPath;
	}

//	private ArrayList<LexicalEntry> findExactMatchArticles(String searchStr) {
//		ArrayList<LexicalEntry> articles = new ArrayList<LexicalEntry>();
//		for(int i = 0; i < books.size(); i++){
//			Book book = books.get(i);
//			if(book.isEnabled()){
//				//ArrayList<Article> res = book.findArticles(searchStr);
//				//articles.addAll(res);
//				LexicalEntry res = book.getLexicalEntry(searchStr);
//				if(res != null) {
//					articles.add(res);
//				}
//			}
//		}
//		return articles;
//	}

	/**
	 * Creates additional index file for every available dictionary.
	 * 
	 * @param observer	observer object which must be notified about the progress of creating the additional indexes.
	 * @throws DomainException 
	 */
//	public void buildSparkDictIndexes(IObserver observer) throws DomainException {
//		int count = books.size();
//		for (int i = 0; i < count; i++) {
//			books.get(i).buildSparkDictIndex(observer);
//		}
//	}

	/**
	 * Enabled dictionaries setter.
	 * 
	 * @param enabledDicts string array of enabled dictionaries titles.
	 */
	public void setEnabledDicts(String[] enabledDicts) {
		this.enabledDicts = enabledDicts;
	}

	/**
	 * Dictionaries path setter.
	 * 
	 * @param dictPath path to dictionaries.
	 */
	public void setDictPath(String dictPath) {
		this.dictPath = dictPath;
	}
}
