package net.bancer.sparkdict;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.FileDictionaryFiles;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;
import net.bancer.sparkdict.storage.SparkDictPreferences;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * BaseActivity provides common methods and configuration data for different
 * activities.
 */
public abstract class BaseActivity extends Activity {

    protected SparkDictPreferences preferences;

    /**
     * Tag to identify SparkDict (for debug).
     */
    protected static final String TAG = "SparkDict";

    private static final String RECENT_HISTORY_PREF_KEY = "recent.history";
    private static final String RECENT_HISTORY_WORDS_SEPARATOR = "::";
    private static final int RECENT_HISTORY_MAX_SIZE = 100;
    private static Shelf shelf;
    private static LinkedList<String> recentHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new SparkDictPreferences(this);
    }

    /**
     * Retrieves path to dictionaries from shared preferences.
     *
     * @return path to dictionaries.
     */
    protected String getDictPathFromPrefs() {
        String key = getString(R.string.menu_dict_path);
        String dictPath = preferences.getString(key);
        return dictPath.trim();
    }

    /**
     * Retrieves a string array of titles of enabled dictionaries from shared
     * preferences.
     *
     * @return a string array of titles of enabled dictionaries.
     */
    protected String[] getEnabledDictsFromPrefs() {
        String strEnabledDicts = preferences.getString(getString(R.string.enabled_dicts));
        return strEnabledDicts.split("\\|\\|");
    }

    /**
     * Displays a long toast message.
     *
     * @param msg string message to be displayed.
     */
    public void showLongToast(String msg) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Gets the list of books from the shelf.
     *
     * @return the list of books from the shelf.
     */
    protected ArrayList<Book> getBooks() {
        Shelf shelf = getShelf();
        return shelf.getBooks();
    }

    /**
     * Shelf getter.
     *
     * @return shelf containing all books.
     */
    public Shelf getShelf() {
        if (shelf == null) {
            refreshShelf();
        }
        return shelf;
    }

    /**
     * Refreshes Shelf to ensure that the list of enabled dictionaries is
     * always up-to-date.
     */
    protected void refreshShelf() {
        String dictPath = getDictPathFromPrefs();
        String[] enabledDicts = getEnabledDictsFromPrefs();
        DictionaryFiles dictionaryFiles = createDictionaryFiles(dictPath);
        shelf = new Shelf(dictPath, enabledDicts, dictionaryFiles);
    }

    /**
     * Creates the {@link DictionaryFiles} used to resolve dictionary files,
     * choosing between the legacy {@code java.io.File}-backed implementation
     * and the Storage-Access-Framework-backed one according to
     * {@link AppConfig#USE_SAF_STORAGE}.
     *
     * <p>Falls back to the legacy implementation even when the flag is on, if
     * no Storage Access Framework folder has been selected yet -- e.g. right
     * after flipping the flag on a device that hasn't run the folder picker
     * since. This avoids crashing on a missing preference in exchange for a
     * silently-empty shelf, which would be a much more confusing failure mode
     * for a flag with no settings UI to explain it.</p>
     *
     * @param dictPath legacy filesystem path, used for the fallback and by
     *                  the non-SAF implementation.
     * @return the DictionaryFiles to use.
     */
    private DictionaryFiles createDictionaryFiles(String dictPath) {
        if (AppConfig.USE_SAF_STORAGE) {
            return SafDictionaryFilesFactory.create(this);
        }
        return new FileDictionaryFiles(dictPath);
    }

    /**
     * Adds a word to the recent search history list. If the list is full
     * then the first item is removed. Maximum size of the list is 100 words.
     *
     * @param word The word to be added.
     */
    protected void addToRecentHistory(String word) {
        getRecentHistory().remove(word);
        if (recentHistory.size() == RECENT_HISTORY_MAX_SIZE) {
            recentHistory.removeLast();
        }
        recentHistory.addFirst(word);
    }

    /**
     * Retrieves a list of recent search history.
     *
     * @return LinkedList<String> Linked list of recent search history.
     */
    protected LinkedList<String> getRecentHistory() {
        if (recentHistory == null) {
            recentHistory = new LinkedList<>();
            String history = preferences.getString(RECENT_HISTORY_PREF_KEY);
            if (!history.isEmpty()) {
                String[] historyArr = history.split(RECENT_HISTORY_WORDS_SEPARATOR);
                for (String s : historyArr) {
                    recentHistory.offer(s);
                }
            }

        }
        return recentHistory;
    }

    /**
     * Saves the list of recent search history into shared preferences.
     *
     * @return boolean `true` if the recent history was saved, else `false`
     */
    protected boolean saveRecentHistory() {
        StringBuilder historyStr = new StringBuilder();
        for (int i = 0; i < getRecentHistory().size(); i++) {
            if (i != 0) {
                historyStr.append(RECENT_HISTORY_WORDS_SEPARATOR);
            }
            historyStr.append(recentHistory.get(i));
        }
        return preferences.save(RECENT_HISTORY_PREF_KEY, historyStr.toString());
    }
}
