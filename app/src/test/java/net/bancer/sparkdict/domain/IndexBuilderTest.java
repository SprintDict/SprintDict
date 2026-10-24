package net.bancer.sparkdict.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.bancer.sparkdict.Fixtures;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.FileDictionaryFiles;
import net.bancer.sparkdict.domain.utils.DomainException;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link IndexBuilder}.
 */
public class IndexBuilderTest {

    private DictionaryFiles dictionaryFiles;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
    }

    /**
     * Verifies that building an index for a single book completes successfully
     * and reports progress through the listener.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Test
    public void testBuildIndexForSingleBook() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicInteger lastIndexed = new AtomicInteger(0);
        AtomicInteger totalArticles = new AtomicInteger(0);

        IndexBuilderListener listener = new IndexBuilderListener() {
            @Override
            public void onProgress(int indexed, int total) {
                lastIndexed.set(indexed);
                totalArticles.set(total);
            }

            @Override
            public void onIndexingError(String dictionaryName, DomainException e) {
            }

            @Override
            public void onIndexingComplete() {
                complete.set(true);
                latch.countDown();
            }
        };

        try (Book book = new Book(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles)) {
            IndexBuilder indexBuilder = new IndexBuilder(listener, book);
            indexBuilder.start();
            boolean finished = latch.await(10, TimeUnit.SECONDS);
            assertTrue("Index building timed out", finished);
            assertTrue("Indexing complete was not triggered", complete.get());
            assertEquals(book.getLexicalEntriesQuantity(), totalArticles.get());
            assertEquals(totalArticles.get(), lastIndexed.get());
        }
    }

    /**
     * Verifies that building an index for a list of books completes successfully
     * and reports progress through the listener.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Test
    public void testBuildIndexForListOfBooks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicInteger lastIndexed = new AtomicInteger(0);
        AtomicInteger totalArticles = new AtomicInteger(0);

        IndexBuilderListener listener = new IndexBuilderListener() {
            @Override
            public void onProgress(int indexed, int total) {
                lastIndexed.set(indexed);
                totalArticles.set(total);
            }

            @Override
            public void onIndexingError(String dictionaryName, DomainException e) {
            }

            @Override
            public void onIndexingComplete() {
                complete.set(true);
                latch.countDown();
            }
        };

        try (Book book = new Book(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles)) {
            java.util.List<Book> books = java.util.Collections.singletonList(book);
            IndexBuilder indexBuilder = new IndexBuilder(listener, books);
            indexBuilder.start();
            boolean finished = latch.await(10, TimeUnit.SECONDS);
            assertTrue("Index building timed out", finished);
            assertTrue("Indexing complete was not triggered", complete.get());
            assertEquals(book.getLexicalEntriesQuantity(), totalArticles.get());
            assertEquals(totalArticles.get(), lastIndexed.get());
        }
    }
}
