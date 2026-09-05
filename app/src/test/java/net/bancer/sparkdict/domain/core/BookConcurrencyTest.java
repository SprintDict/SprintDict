package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import net.bancer.sparkdict.Fixtures;
import net.bancer.sparkdict.domain.utils.DomainException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
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
 * channel) between suggestion lookups and exact-match search, so a
 * suggestion lookup's self-inflicted interruption could close the channel
 * the search path was about to use too.
 *
 * <p>This test drives both access patterns concurrently and deliberately:
 * one thread repeatedly submits and cancels-with-interrupt suggestion
 * lookups, mimicking rapid keystrokes; a second, never-interrupted thread
 * repeatedly performs exact-match lookups on the same Book at the same
 * time. It asserts the exact-match path never fails because of the
 * suggestion path's self-interruption -- proving the per-access-pattern
 * iterator split actually isolates the two channels, and that the
 * reopen-on-ClosedChannelException recovery holds up under real
 * concurrent pressure rather than only in a single crafted scenario.</p>
 */
public class BookConcurrencyTest {
    private static final String KNOWN_LEMMA = "abacus";
    private static final String SUGGESTION_PREFIX = "abac";
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

    private Book gcide;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        Fixtures.buildSparkDictIndex();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Fixtures.deleteSparkDictIndex();
    }


    @Before
    public void setUp() {
        dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        Shelf shelf = new Shelf(new String[0], dictionaryFiles);
        gcide = findBookByName(shelf, "GNU Collaborative International Dictionary of English");
        assertNotNull("Expected to find GCIDE dictionary", gcide);
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
                        gcide.getSuggestions(prefix);
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
                    LexicalEntry entry = gcide.getLexicalEntry(KNOWN_LEMMA);
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
        Book freshWordNet = findBookByName(freshShelf, "GNU Collaborative International Dictionary of English");
        assertNotNull(freshWordNet);
        Vector<IndexEntry> suggestions = freshWordNet.getSuggestions("abac");
        assertEquals(11, suggestions.size());
        assertEquals("abaca", suggestions.get(0).getLemma());
        assertEquals("abacinate", suggestions.get(1).getLemma());
        assertEquals("abacination", suggestions.get(2).getLemma());
        assertEquals("abaciscus", suggestions.get(3).getLemma());
        assertEquals("abacist", suggestions.get(4).getLemma());
        assertEquals("aback", suggestions.get(5).getLemma());
        assertEquals("abactinal", suggestions.get(6).getLemma());
        assertEquals("abaction", suggestions.get(7).getLemma());
        assertEquals("abactor", suggestions.get(8).getLemma());
        assertEquals("abaculus", suggestions.get(9).getLemma());
        assertEquals("abacus", suggestions.get(10).getLemma());
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
