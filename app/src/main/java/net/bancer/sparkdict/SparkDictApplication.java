package net.bancer.sparkdict;

import android.app.Application;

import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.logging.AndroidLogger;
import net.bancer.sparkdict.logging.Logger;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;
import net.bancer.sparkdict.storage.SparkDictPreferences;

import java.util.LinkedList;

/**
 * Custom Application class for SprintDict to manage global state.
 */
public class SparkDictApplication extends Application {

    private static final String RECENT_HISTORY_PREF_KEY = "recent.history";

    private static final String RECENT_HISTORY_WORDS_SEPARATOR = "::";

    private static final int RECENT_HISTORY_MAX_SIZE = 100;

    private Shelf shelf;

    private LinkedList<String> recentHistory;

    private Logger logger;

    private SparkDictPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        logger = new AndroidLogger();
        preferences = new SparkDictPreferences(this);
    }

    public Logger getLogger() {
        return logger;
    }

    public SparkDictPreferences getPreferences() {
        return preferences;
    }

    /**
     * Shelf getter.
     *
     * @return shelf containing all books.
     */
    public synchronized Shelf getShelf() {
        if (shelf == null) {
            refreshShelf();
        }
        return shelf;
    }

    /**
     * Refreshes Shelf to ensure that the list of enabled dictionaries is
     * always up-to-date.
     */
    public synchronized void refreshShelf() {
        if (shelf != null) {
            shelf.closeResources();
        }
        DictionaryFiles dictionaryFiles = SafDictionaryFilesFactory.create(this, logger);
        String strEnabledDicts = preferences.getString(getString(R.string.enabled_dicts));
        String[] enabledDicts = strEnabledDicts.split("\\|\\|");
        shelf = new Shelf(enabledDicts, dictionaryFiles, logger);
    }

    /**
     * Adds a word to the recent search history list. If the list is full
     * then the first item is removed. Maximum size of the list is 100 words.
     *
     * @param word The word to be added.
     */
    public synchronized void addToRecentHistory(String word) {
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
    public synchronized LinkedList<String> getRecentHistory() {
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
    public synchronized boolean saveRecentHistory() {
        StringBuilder historyStr = new StringBuilder();
        LinkedList<String> history = getRecentHistory();
        for (int i = 0; i < history.size(); i++) {
            if (i != 0) {
                historyStr.append(RECENT_HISTORY_WORDS_SEPARATOR);
            }
            historyStr.append(history.get(i));
        }
        return preferences.save(RECENT_HISTORY_PREF_KEY, historyStr.toString());
    }
}
