package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reproduces, deterministically and outside of any UI, the concurrency bug
 * behind the flaky ClosedChannelException seen in SparkDictActivityTest:
 * IndexEntriesAdapter cancels an in-flight suggestion lookup with
 * Future.cancel(true) on every keystroke, interrupting its worker thread.
 * SeekableByteChannel is an InterruptibleChannel -- interrupting a thread
 * blocked in a channel read closes that channel outright. Book used to
 * share a single IndexEntriesIterator (and therefore a single underlying
 * channel and cursor) between suggestion lookups and exact-match search, so
 * a suggestion lookup's self-inflicted interruption -- or simply its cursor
 * mutations -- could interfere with the search path on the same Book.
 *
 * <p>This test drives both access patterns concurrently and deliberately:
 * one thread repeatedly submits and cancels-with-interrupt suggestion
 * lookups for a changing prefix each time, mimicking a user actually typing
 * several different words letter by letter; a second, never-interrupted
 * thread repeatedly performs exact-match lookups on the same Book at the
 * same time. It asserts the exact-match path never fails or returns wrong
 * content because of the concurrent suggestion activity.</p>
 *
 * <p>Note this remains, by nature, a probabilistic test: a clean run does
 * not prove the underlying design is race-free, only that this run's
 * scheduling didn't happen to expose it. The prefix sequence below is
 * chosen specifically to make every submission do real, cursor-heavy work
 * (see IndexEntriesIterator#nextSuggestion) rather than mostly hitting its
 * cheap already-matched fast path, to make the race window as wide as
 * realistically possible.</p>
 */
@RunWith(AndroidJUnit4.class)
public class BookConcurrencyTest {

    private static final String KNOWN_LEMMA = "15 May Organization";

    private static final int SEARCH_ITERATIONS = 100;

    /**
     * Simulates typing several different words letter by letter -- each
     * entry is a fresh, distinct prefix from the previous one, forcing
     * IndexEntriesIterator#nextSuggestion to run a full binary search via
     * findFirstMatchedByPrefix() on (almost) every submission instead of
     * falling into its cheap "same prefix as last time" fast path.
     */
    private static final String[] SUGGESTION_PREFIXES = {
        "a", "ab", "aba", "abac", "abacu", "abacus",
        "r", "re", "rep", "repu", "repub", "republ", "republi", "republic",
        "i", "in", "int", "inte", "inter", "interf", "interfa", "interfac", "interface",
        "d", "di", "dic", "dict", "dicti", "diction", "dictiona", "dictionar", "dictionary",
        "s", "se", "sea", "sear", "searc", "search",
        "w", "wo", "wor", "word", "words", "wordn", "wordne", "wordnet",
    };

    private DictionaryFiles dictionaryFiles;

    private Book wordNet;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dictionaryFiles = SafDictionaryFilesFactory.create(context);
        Shelf shelf = new Shelf(new String[0], dictionaryFiles);
        wordNet = findBookByName(shelf, Mocks.WORDNET_DICT_NAME);
        assertNotNull("Expected to find WordNet under Mocks.ROOT_PATH", wordNet);
    }

    @Test
    public void searchSurvivesConcurrentlyCancelledSuggestionLookups() throws Exception {
        AtomicBoolean keepTyping = new AtomicBoolean(true);
        AtomicInteger searchFailures = new AtomicInteger(0);
        AtomicInteger searchSuccesses = new AtomicInteger(0);
        CountDownLatch typingStarted = new CountDownLatch(1);

        ExecutorService suggestionExecutor = Executors.newSingleThreadExecutor();

        // Simulates IndexEntriesAdapter.onTextChanged(): submit a suggestion
        // lookup for the next prefix in the typing sequence, cancel-with-interrupt
        // the previous one, repeat -- mirroring rapid keystrokes across several
        // different words rather than repeatedly querying the same prefix.
        Thread typingSimulator = new Thread(() -> {
            Future<?> currentTask = null;
            int prefixIndex = 0;
            typingStarted.countDown();
            while (keepTyping.get()) {
                String prefix = SUGGESTION_PREFIXES[prefixIndex % SUGGESTION_PREFIXES.length];
                prefixIndex++;

                if (currentTask != null && !currentTask.isDone()) {
                    currentTask.cancel(true);
                }
                currentTask = suggestionExecutor.submit(() -> {
                    try {
                        wordNet.getSuggestions(prefix);
                    } catch (RuntimeException e) {
                        // Being interrupted mid-lookup may surface as a
                        // wrapped/unchecked failure depending on exactly
                        // where the interruption landed. That is expected
                        // and is not itself what this test checks.
                    }
                });
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        // Simulates SearchWorker: never interrupted, just repeatedly
        // performs exact-match lookups on the same Book while the typing
        // simulator hammers the suggestion path concurrently.
        Thread searcher = new Thread(() -> {
            try {
                typingStarted.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            for (int i = 0; i < SEARCH_ITERATIONS; i++) {
                try {
                    LexicalEntry entry = wordNet.getLexicalEntry(KNOWN_LEMMA);
                    if (entry != null && KNOWN_LEMMA.equals(entry.getLemma())) {
                        searchSuccesses.incrementAndGet();
                    } else {
                        searchFailures.incrementAndGet();
                    }
                } catch (DomainException e) {
                    searchFailures.incrementAndGet();
                }
            }
        });

        typingSimulator.start();
        searcher.start();
        searcher.join(30_000);
        keepTyping.set(false);
        typingSimulator.join(5_000);

        suggestionExecutor.shutdownNow();
        suggestionExecutor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(
            "Exact-match search should never fail or return unexpected content "
                + "because of concurrently cancelled suggestion lookups on the same Book",
            0, searchFailures.get()
        );
        assertEquals(SEARCH_ITERATIONS, searchSuccesses.get());

        // Verify with a *fresh* Book/iterator, independent of whatever
        // cursor state the storm above may have left in wordNet's own
        // suggestions iterator -- this check is specifically about channel
        // isolation, not about IndexEntriesIterator's resume-from-cursor
        // bookkeeping under interruption, which is a separate concern.
        Shelf freshShelf = new Shelf(new String[0], dictionaryFiles);
        Book freshWordNet = findBookByName(freshShelf, Mocks.WORDNET_DICT_NAME);
        assertNotNull(freshWordNet);
        Vector<IndexEntry> suggestions = freshWordNet.getSuggestions(".");
        assertFalse(suggestions.isEmpty());
        assertEquals(".22 caliber", suggestions.get(0).getLemma());
        assertEquals(".38 caliber", suggestions.get(1).getLemma());
        assertEquals(".45 caliber", suggestions.get(2).getLemma());
    }

    private Book findBookByName(Shelf shelf, String name) {
        for (Book book : shelf.getBooks()) {
            if (name.equals(book.getBookName())) {
                return book;
            }
        }
        return null;
    }
}
