package net.bancer.sparkdict;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.bancer.sparkdict.adapters.IndexEntriesAdapter;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.views.LexicalEntriesListView;
import net.bancer.sparkdict.views.SearchInputField;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SparkDictActivity is the main activity.
 */
public class SparkDictActivity extends BaseActivity
    implements OnClickListener, OnKeyListener, OnItemClickListener {

    public static final String SEARCH_INTENT = "net.bancer.sparkdict.SEARCH";

    private static final String KEY_SEARCH_STR = "SEARCH_STR";

    private static final String KEY_ARTICLES_SCROLL_POSITION = "ARTICLES_SCROLL_POSITION";

    private static final String KEY_FOCUSED_WORD = "FOCUSED_WORD";

    private static final String KEY_FIND_ON_PAGE_STR = "KEY_FIND_ON_PAGE_STR";

    /**
     * Key to identify the array of definitions visibility field values of
     * all lexical entry views in order to restore their state after screen
     * orientation change.
     */
    private static final String KEY_DEFINITIONS_VISIBILITY = "KEY_EXPANDED_DEFINITIONS_POSITIONS";

    /**
     * Executor used to perform dictionary searches in a background thread.
     */
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();

    /**
     * Handler used to execute tasks on the main application thread.
     */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SearchInputField inputTextView;

    private ScrollView scrollView;

    private LexicalEntriesListView lexicalEntriesListView;

    private EditText findOnPageInput;

    private LinearLayout findOnPageView;

    private ArrayList<LexicalEntry> articles = new ArrayList<>();

    private ProgressBar searchProgress;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkDictPath();
        initLayout();
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            @SuppressWarnings("unchecked") final ArrayList<LexicalEntry> data = (ArrayList<LexicalEntry>) getLastNonConfigurationInstance();
            if (data != null && !data.isEmpty()) {
                articles = data;
                lexicalEntriesListView.addAll(articles);
                restoreDefinitionsViewsState(savedInstanceState);
                restoreScrollPosition(savedInstanceState);
                //String searchStr = savedInstanceState.getString(KEY_SEARCH_STR);
                restoreHighlight(savedInstanceState);
            }
        } else {
            // initialise members with default values for a new instance
            processIntent(getIntent());
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkDictPath();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveRecentHistory();
    }

    @Override
    protected void onDestroy() {
        searchExecutor.shutdownNow();
        getShelf().closeResources();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveDefinitionsViewsState(outState);
        saveScrollPosition(outState);
        if (findOnPageView.getVisibility() == View.VISIBLE) {
            saveFindOnPageBarState(outState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        // Handle search action
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

//			String query1 = intent.getStringExtra(SearchManager.QUERY);
//			Cursor cursor = managedQuery(SuggestionsProvider.CONTENT_URI, null, null,
//	                new String[] {query1}, null);
//			if(cursor != null) {
//				System.out.println("cursor count: " + cursor.getCount());
//			}

            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
        // Handle lexical entry hyperlink click
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String query = data.getSchemeSpecificPart().substring(2);
                doSearch(query);
            }
        }
        // Handle searchable dialogue suggestion click
        if (SparkDictActivity.SEARCH_INTENT.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String query = data.getLastPathSegment();
                doSearch(query);
            }
        }
    }

    private void initLayout() {
        setContentView(R.layout.activity_spark_dict);

        inputTextView = findViewById(R.id.searchTextView);
        inputTextView.setOnKeyListener(this);

        IndexEntriesAdapter adapter = new IndexEntriesAdapter(this, new Vector<>());
        inputTextView.setAdapter(adapter);
        inputTextView.addTextChangedListener(adapter);
        // Start to display a list of suggestions after 1 letter typed
        inputTextView.setThreshold(1);
        inputTextView.setOnItemClickListener(this);

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

        searchProgress = findViewById(R.id.search_progress);

        scrollView = findViewById(R.id.articles_scroll_view);

        lexicalEntriesListView = findViewById(R.id.articles_list);
        lexicalEntriesListView.setOnClickListener(this);

        findOnPageInput = findViewById(R.id.find_on_page_edit_text);
        findOnPageView = findViewById(R.id.find_on_page_layout);
    }

    /**
     * Checks whether a dictionary path is configured in the preferences.
     *
     * <p>If no path is configured, displays a dialogue prompting the user to
     * select a dictionary path.</p>
     */
    private void checkDictPath() {
        String dictPath = getDictPathFromPrefs();
        if (dictPath.trim().isEmpty()) {
            showNoPathSetDialog();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return articles;
    }

    /**
     * Saves definitions visibility field value of all lexical entry
     * views into a Bundle in  order to restore their state after the screen
     * orientation changes or any other system configuration change occurs.
     *
     * @param outState Bundle in which to place saved state.
     */
    private void saveDefinitionsViewsState(Bundle outState) {
        int[] expandedLexicalEntryViews = lexicalEntriesListView.getExpandedLexicalEntryViewsVisibility();
        outState.putIntArray(KEY_DEFINITIONS_VISIBILITY, expandedLexicalEntryViews);
    }

    private void saveScrollPosition(Bundle outState) {
        int[] coordinates = new int[]{
            scrollView.getScrollX(),
            scrollView.getScrollY()
        };
        outState.putIntArray(KEY_ARTICLES_SCROLL_POSITION, coordinates);
    }

    private void saveFindOnPageBarState(Bundle outState) {
        outState.putString(KEY_SEARCH_STR, lexicalEntriesListView.getFocusedWord());
        int[] coordinates = lexicalEntriesListView.getFocusedWordPosition();
        outState.putIntArray(KEY_FOCUSED_WORD, coordinates);
        String searchStr = lexicalEntriesListView.getFocusedWord();
        outState.putString(KEY_FIND_ON_PAGE_STR, searchStr);
    }

    /**
     * Restores collapsed/expanded state of all definitions.
     *
     * @param savedInstanceState Bundle with values to be restored.
     */
    private void restoreDefinitionsViewsState(Bundle savedInstanceState) {
        final int[] visibilities = savedInstanceState.getIntArray(KEY_DEFINITIONS_VISIBILITY);
        if (visibilities != null) {
            lexicalEntriesListView.restoreDefinitionsVisibility(visibilities);
        }
    }

    /**
     * <a href="http://eliasbland.wordpress.com/2011/07/28/how-to-save-the-position-of-a-scrollview-when-the-orientation-changes-in-android/">...</a>
     *
     * @param savedInstanceState As the activity is being re-initialised after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle).
     */
    private void restoreScrollPosition(Bundle savedInstanceState) {
        final int[] coordinates = savedInstanceState.getIntArray(KEY_ARTICLES_SCROLL_POSITION);
        if (coordinates != null) {
            scrollView.post(() -> scrollView.scrollTo(coordinates[0], coordinates[1]));
        }
    }

    private void restoreHighlight(Bundle savedInstanceState) {
        int[] coordinates = savedInstanceState.getIntArray(KEY_FOCUSED_WORD);
        if (coordinates != null) {
            String searchStr = savedInstanceState.getString(KEY_SEARCH_STR);
            findOnPageView.setVisibility(View.VISIBLE);
            findOnPageInput.setText("");
            findOnPageInput.requestFocus();
            findOnPageInput.setText(searchStr);
            lexicalEntriesListView.restore(searchStr, coordinates[0], coordinates[1], coordinates[2]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.activity_spark_dict, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_recent_history) {
            startActivity(new Intent(this, RecentHistoryActivity.class));
            return true;
        }
        if (itemId == R.id.menu_find_on_page) {
            openFindOnPageBar();
            return true;
        }
        if (itemId == R.id.menu_manage_dictionaries) {
            startDictManager(DictManagerActivity.DO_NOT_START_SUB_ACTIVITY);
            return true;
        }
        if (itemId == R.id.menu_settings) {
            startActivity(new Intent(this, DictPreferencesActivity.class));
            return true;
        }
        if (itemId == R.id.search_dialog) {
            onSearchRequested();
            return true;
        }
        if (itemId == R.id.menu_expand_all) {
            lexicalEntriesListView.expandAll();
            return true;
        }
        if (itemId == R.id.menu_collapse_all) {
            lexicalEntriesListView.collapseAll();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void startDictManager(int subactivity) {
        Intent intent = new Intent(this, DictManagerActivity.class);
        intent.putExtra(DictManagerActivity.SUB_ACTIVITY, subactivity);
        startActivity(intent);
    }

    /**
     * Displays a non-cancelable dialogue asking the user whether to set the dictionary path.
     *
     * <p>If the user chooses "Set Path", the directory picker is started. If the user chooses
     * "Exit", the activity is closed.</p>
     */
    private void showNoPathSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.prompt_to_set_path)
            .setCancelable(false)
            .setPositiveButton(
                R.string.set_path,
                // If user chooses "Yes" to set path then start directory picker.
                (dialog, which) -> startDictManager(DictManagerActivity.START_DIR_PICKER)
            )
            .setNegativeButton(
                R.string.exit,
                // If user chooses "Exit" to set path then close the application.
                (dialog, which) -> finish()
            )
            .show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.searchButton) {
            doSearch(inputTextView.getText().toString());
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            doSearch(inputTextView.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof TextView) {
            doSearch(((TextView) view).getText().toString());
        }
    }

    private void doSearch(String searchStr) {
        lexicalEntriesListView.removeAllViews();
        searchProgress.setVisibility(View.VISIBLE);
        searchExecutor.execute(new SearchWorker(searchStr));
        hideKeyboard(inputTextView);
    }

    /**
     * Hides the soft keyboard associated with the specified view.
     *
     * @param v The view whose window token is used to hide the keyboard.
     */
    public void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * Requests focus for the specified view and displays the soft keyboard.
     *
     * @param view The view that should receive keyboard input.
     */
    private void showKeyboard(View view) {
        view.requestFocus();
        WindowInsetsController controller = view.getWindowInsetsController();
        if (controller != null) {
            controller.show(WindowInsets.Type.ime());
        }
    }

    /**
     * Displays the find-on-page bar, clears its input field, and opens the
     * soft keyboard for entering a search term.
     */
    private void openFindOnPageBar() {
        findOnPageView.setVisibility(View.VISIBLE);
        findOnPageInput.setText("");
        findOnPageInput.post(() -> showKeyboard(findOnPageInput));
    }

    /**
     * Closes the find-on-page bar, clears the search input, hides the soft
     * keyboard, and removes the current on-screen search results.
     *
     * @param v The view that triggered the action.
     */
    public void onCloseFindOnPageBarButtonClick(View v) {
        findOnPageInput.setText("");
        findOnPageView.setVisibility(View.GONE);
        hideKeyboard(v);
        lexicalEntriesListView.clearSearchOnScreenResults();
    }

    /**
     * Finds and displays the next occurrence of the entered word on the page.
     *
     * @param v The view that triggered the action.
     */
    public void onFindNextWordOnPageButtonClick(View v) {
        lexicalEntriesListView.findNextOnScreen(findOnPageInput.getText().toString());
    }

    /**
     * Finds and displays the previous occurrence of the entered word on the page.
     *
     * @param v The view that triggered the action.
     */
    public void onFindPreviousWordOnPageButtonClick(View v) {
        lexicalEntriesListView.findPreviousOnScreen(findOnPageInput.getText().toString());
    }

    /**
     * Performs a search in all dictionaries in the background thread and updates
     * the user interface with the search results on the main application thread.
     */
    private class SearchWorker implements Runnable {

        /**
         * Search term used to retrieve lexical entries from the enabled dictionaries.
         */
        private final String lemma;

        /**
         * Indicates whether the search found at least one lexical entry.
         */
        private Boolean result;

        /**
         * Creates a search worker for the specified search term.
         *
         * @param lemma search term to look up in the enabled dictionaries.
         */
        private SearchWorker(String lemma) {
            this.lemma = lemma;
        }

        /**
         * Performs the dictionary search in the background thread and posts the
         * search completion callback to the main application thread.
         */
        @Override
        public void run() {
            result = doInBackground();
            mainHandler.post(() -> onPostExecute(result));
        }

        /**
         * Performs lexical entries search in all dictionaries and publishes
         * progress one by one.
         */
        private Boolean doInBackground() {
            boolean atLeastOneEntryFound = false;
            articles.clear();
            for (Book book : getShelf().getBooks()) {
                if (book.isEnabled()) {
                    LexicalEntry entry = null;
                    try {
                        entry = book.getLexicalEntry(lemma);
                    } catch (DomainException e) {
                        Log.e(TAG, "Cannot retrieve '" + lemma + "' from " + book.getBookName(), e);
                    }
                    if (entry != null) {
                        articles.add(entry);
                        publishProgress(entry);
                        if (!atLeastOneEntryFound) {
                            SparkDictActivity.this.addToRecentHistory(lemma);
                            atLeastOneEntryFound = true;
                        }
                    }
                }
            }
            return atLeastOneEntryFound;
        }

        /**
         * Posts a search result to the main application thread for display.
         *
         * @param entry lexical entry found by the background search.
         */
        private void publishProgress(final LexicalEntry entry) {
            mainHandler.post(() -> onProgressUpdate(entry));
        }

        /**
         * Updates view with found lexical entry, dismisses progress spinner
         * dialogue, initiates progress spinner.
         *
         * @param entry Lexical entry to be added to the view.
         */
        private void onProgressUpdate(LexicalEntry entry) {
            lexicalEntriesListView.add(entry);
            inputTextView.setText("");
        }

        /**
         * Terminates progress spinner, displays a toast if
         * no lexical entries were found.
         *
         * @param atLeastOneEntryFound `true` if at least one entry was found.
         */
        private void onPostExecute(Boolean atLeastOneEntryFound) {
            if (!atLeastOneEntryFound) {
                showLongToast(getString(R.string.nothing_found));
            }
            // hide progress spinner
            searchProgress.setVisibility(View.GONE);
        }
    }
}
