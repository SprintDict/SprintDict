package net.bancer.sparkdict;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.logging.Logger;
import net.bancer.sparkdict.storage.SparkDictPreferences;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * BaseActivity provides common methods and configuration data for different
 * activities.
 */
public abstract class BaseActivity extends Activity {

    /**
     * Tag to identify SparkDict (for debug).
     */
    protected static final String TAG = "SparkDict";

    protected SparkDictPreferences preferences;

    protected Logger logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SparkDictApplication app = (SparkDictApplication) getApplication();
        logger = app.getLogger();
        preferences = app.getPreferences();
        WindowCompat.enableEdgeToEdge(getWindow());
    }

    /**
     * Applies system bar insets to the specified view as padding.
     *
     * @param targetView the view to which insets should be applied.
     */
    void applyWindowInsets(View targetView) {
        ViewCompat.setOnApplyWindowInsetsListener(
            targetView,
            (view, windowInsets) -> {
                Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                );
                view.setPadding(
                    insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom
                );
                return windowInsets;
            }
        );
    }

    /**
     * Retrieves path to dictionaries from shared preferences.
     *
     * @return path to dictionaries.
     */
    protected String getDictPathFromPrefs() {
        String key = SparkDictPreferences.PREF_DICT_ROOT_URI_NAME;
        String dictPath = preferences.getString(key).trim();
        if (dictPath.isEmpty()) {
            logger.error(TAG, "Dictionaries path in preferences is empty");
        }
        return dictPath;
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
        return ((SparkDictApplication) getApplication()).getShelf();
    }

    /**
     * Refreshes Shelf to ensure that the list of enabled dictionaries is
     * always up-to-date.
     */
    protected void refreshShelf() {
        ((SparkDictApplication) getApplication()).refreshShelf();
    }

    /**
     * Adds a word to the recent search history list. If the list is full
     * then the first item is removed. Maximum size of the list is 100 words.
     *
     * @param word The word to be added.
     */
    protected void addToRecentHistory(String word) {
        ((SparkDictApplication) getApplication()).addToRecentHistory(word);
    }

    /**
     * Retrieves a list of recent search history.
     *
     * @return LinkedList<String> Linked list of recent search history.
     */
    protected LinkedList<String> getRecentHistory() {
        return ((SparkDictApplication) getApplication()).getRecentHistory();
    }

    /**
     * Saves the list of recent search history into shared preferences.
     *
     * @return boolean `true` if the recent history was saved, else `false`
     */
    protected boolean saveRecentHistory() {
        return ((SparkDictApplication) getApplication()).saveRecentHistory();
    }
}
