package net.bancer.sparkdict.domain;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IObserver;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.domain.utils.DomainException;

import java.util.Collections;
import java.util.List;

/**
 * Builds dictionary indexes in a background thread and reports progress,
 * errors and completion through a {@link IndexBuilderListener}, decoupled from any UI.
 */
public class IndexBuilder extends Thread implements IObserver {

    private static final int MESSAGES_TIME_STEP = 100;
    private final IndexBuilderListener listener;
    private final List<Book> books;
    private int articlesIndexed = 0;
    private int totalArticles = 0;
    private long previousMessageTime = 0;

    /**
     * Creates an index builder for all dictionaries in the specified shelf.
     *
     * @param listener listener that receives indexing progress, errors and
     *                 completion notifications
     * @param shelf    shelf containing the dictionaries whose indexes are to be built
     */
    public IndexBuilder(IndexBuilderListener listener, Shelf shelf) {
        this.listener = listener;
        this.books = shelf.getBooks();
    }

    /**
     * Creates an index builder for a single dictionary.
     *
     * @param listener listener that receives indexing progress, errors and
     *                 completion notifications
     * @param book     dictionary whose index is to be built
     */
    public IndexBuilder(IndexBuilderListener listener, Book book) {
        this.listener = listener;
        this.books = Collections.singletonList(book);
    }

    /**
     * Creates an index builder for a specific list of dictionaries.
     *
     * @param listener listener that receives indexing progress, errors and
     *                 completion notifications
     * @param books    list of dictionaries whose indexes are to be built
     */
    public IndexBuilder(IndexBuilderListener listener, List<Book> books) {
        this.listener = listener;
        this.books = books;
    }

    /**
     * Builds indexes for the assigned dictionaries and reports progress and
     * errors through the listener.
     */
    @Override
    public void run() {
        totalArticles = 0;
        int count = books.size();
        for (int i = 0; i < count; i++) {
            totalArticles += books.get(i).getLexicalEntriesQuantity();
        }
        listener.onProgress(articlesIndexed, totalArticles);
        for (int i = 0; i < count; i++) {
            try {
                books.get(i).buildSparkDictIndex(this);
            } catch (DomainException e) {
                String dictionaryName = books.get(i).getBookName();
                listener.onIndexingError(dictionaryName, e);
            }
        }
    }

    /**
     * Receives a progress update from the index building process and forwards
     * progress notifications to the listener at regular intervals.
     *
     * @param field field associated with the update
     * @param value value associated with the update
     */
    @Override
    public void update(Object field, int value) {
        articlesIndexed++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - previousMessageTime > MESSAGES_TIME_STEP || articlesIndexed >= totalArticles) {
            listener.onProgress(articlesIndexed, totalArticles);
            previousMessageTime = currentTime;
        }
        if (articlesIndexed >= totalArticles) {
            listener.onIndexingComplete();
        }
    }
}
