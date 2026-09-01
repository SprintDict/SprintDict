package net.bancer.sparkdict.domain.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SafDictionaryFiles;
import net.bancer.sparkdict.storage.SparkDictPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Vector;

/**
 * Exercises Shelf/Book exclusively through the Storage Access Framework
 * backend (SafDictionaryFiles), independent of the rest of the app's UI and
 * background-worker plumbing.
 *
 * <p>This is a deliberate isolation test: it reuses the exact same
 * persisted tree Uri and real dictionary files the app itself uses (see
 * {@link SparkDictPreferences#PREF_DICT_ROOT_URI_NAME}), but drives
 * {@link Shelf}/{@link Book} directly on the test's own thread -- no
 * Espresso, no AsyncTask/ThreadPoolExecutor concurrency, no UI. If a
 * {@code ClosedChannelException} reproduces here, the cause lives in the
 * SAF/domain.core plumbing itself; if it does not, the cause is more likely
 * tied to concurrency or a lifecycle event specific to the real UI flow.</p>
 *
 * <p>Requires the SAF folder picker to have already been run at least once
 * on this device/emulator, so a tree Uri is persisted in SharedPreferences --
 * the same precondition the real app has. There is no way to fabricate this
 * grant programmatically: constructing a tree Uri directly (e.g. via
 * {@code DocumentsContract.buildTreeDocumentUri}) would still be rejected
 * with a SecurityException, since SAF grants are tracked independently of
 * MANAGE_EXTERNAL_STORAGE. Tests are skipped, not failed, if no Uri is
 * persisted.</p>
 */
@RunWith(AndroidJUnit4.class)
public class ShelfTest {

    private SafDictionaryFiles safDictionaryFiles;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(SparkDictPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = preferences.getString(SparkDictPreferences.PREF_DICT_ROOT_URI_NAME, "");
        assumeFalse(
            "No SAF folder has been selected yet on this device -- run the folder picker once before running this test.",
            uriString.isEmpty()
        );
        safDictionaryFiles = new SafDictionaryFiles(context, Uri.parse(uriString));
    }

    @Test
    public void discoversDictionariesThroughSaf() {
        Shelf shelf = new Shelf(null, new String[0], safDictionaryFiles);
        assertNotNull(shelf.getBooks());
        assertFalse("Expected at least one dictionary to be discovered via SAF", shelf.getBooks().isEmpty());
        Book wordNet = findBookByName(shelf, Mocks.WORDNET_DICT_NAME);
        assertNotNull("Expected to find WordNet via SAF discovery", wordNet);
        assertEquals(117659, wordNet.getLexicalEntriesQuantity());
    }

    @Test
    public void readsLexicalEntryThroughSaf() throws DomainException {
        Book wordNet = requireBook(Mocks.WORDNET_DICT_NAME);
        LexicalEntry entry = wordNet.getLexicalEntry("15 May Organization");
        String expected = "<i><font color=\"#006600\">n</font></i><br>"
            + "<gloss>a terrorist organization formed in 1979 by a faction "
            + "of the Popular Front for the Liberation of Palestine but "
            + "disbanded in the 1980s when key members left to join a "
            + "faction of al-Fatah</gloss>";
        assertEquals(expected, entry.getDefinitions());
    }

    @Test
    public void readsSuggestionsThroughSaf() {
        Book wordNet = requireBook(Mocks.WORDNET_DICT_NAME);
        Vector<IndexEntry> suggestions = wordNet.getSuggestions(".");
        assertNotNull(suggestions);
        assertEquals(3, suggestions.size());
        assertEquals(".22 caliber", suggestions.get(0).getLemma());
        assertEquals(".38 caliber", suggestions.get(1).getLemma());
        assertEquals(".45 caliber", suggestions.get(2).getLemma());
    }

    /**
     * Repeats the same lookup many times on the same Book instance, mirroring
     * several keystrokes/searches in the real app, but single-threaded. If
     * ClosedChannelException reproduces here, repeated use alone is enough
     * to trigger it -- concurrency is not a required ingredient.
     */
    @Test
    public void repeatedLookupsOnSameBookDoNotCloseTheChannel() throws DomainException {
        Book wordNet = requireBook(Mocks.WORDNET_DICT_NAME);
        for (int i = 0; i < 50; i++) {
            LexicalEntry entry = wordNet.getLexicalEntry("15 May Organization");
            assertNotNull("Lookup #" + i + " unexpectedly returned null", entry);
        }
    }

    /**
     * Same repeated-lookup stress test, but against the dictionary actually
     * implicated in the ClosedChannelException from SparkDictActivityTest.
     */
    @Test
    public void repeatedLookupsOnCambridgeDoNotCloseTheChannel() throws DomainException {
        Book cambridge = findBook(Mocks.CAMBRIDGE_DICT_NAME);
        assumeFalse("Cambridge dictionary not present on this device", cambridge == null);
        for (int i = 0; i < 50; i++) {
            LexicalEntry entry = cambridge.getLexicalEntry("interface");
            assertNotNull("Lookup #" + i + " unexpectedly returned null", entry);
            assertEquals("interface", entry.getLemma());
        }
    }

    /**
     * Interleaves lookups across two Book instances on the same thread,
     * mirroring how the suggestion adapter and the Enter-triggered search
     * both query dictionaries in close succession -- without any real
     * background-thread concurrency.
     */
    @Test
    public void interleavedLookupsAcrossTwoBooksDoNotCloseTheChannel() throws DomainException {
        Book wordNet = requireBook(Mocks.WORDNET_DICT_NAME);
        Book cambridge = findBook(Mocks.CAMBRIDGE_DICT_NAME);
        assumeFalse("Cambridge dictionary not present on this device", cambridge == null);
        for (int i = 0; i < 20; i++) {
            assertNotNull(wordNet.getLexicalEntry("15 May Organization"));
            assertNotNull(cambridge.getLexicalEntry("interface"));
        }
    }

    private Book requireBook(String name) {
        Book book = findBook(name);
        assertNotNull("Expected to find `" + name + "` via SAF discovery", book);
        return book;
    }

    private Book findBook(String name) {
        Shelf shelf = new Shelf(null, new String[0], safDictionaryFiles);
        return findBookByName(shelf, name);
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
