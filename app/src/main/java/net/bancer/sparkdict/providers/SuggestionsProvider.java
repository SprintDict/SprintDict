package net.bancer.sparkdict.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import android.util.Log;

import androidx.annotation.NonNull;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictApplication;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.Shelf;

import java.util.TreeSet;
import java.util.Vector;

/**
 * Suggestions provider for SparkDict.
 */
public class SuggestionsProvider extends ContentProvider {

    /**
     * The columns we'll include in the cursor.
     */
    private static final String[] COLUMNS = new String[]{
        BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2,
        SearchManager.SUGGEST_COLUMN_ICON_1,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
        SearchManager.SUGGEST_COLUMN_QUERY
    };

    private static final int SEARCH_LEXICAL_ENTRY = 0;

    private static final int SEARCH_INDEX_ENTRIES = 1;

    public static final String TAG = "SuggestionsProvider";

    /**
     * Full class name of SuggestionsProvider.
     */
    public static String AUTHORITY = "net.bancer.sparkdict.providers.SuggestionsProvider";

    private static final UriMatcher sURIMatcher = buildUriMatcher();

    public SuggestionsProvider() {
    }

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions
        matcher.addURI(AUTHORITY, "dictionary", SEARCH_LEXICAL_ENTRY);
        matcher.addURI(AUTHORITY, "dictionary/*", SEARCH_LEXICAL_ENTRY);
        // to get suggestions
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_INDEX_ENTRIES);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_INDEX_ENTRIES);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
        @NonNull Uri uri,
        String[] projection,
        String selection,
        String[] selectionArgs,
        String sortOrder
    ) {
        Log.i("SuggestionsProvider", "QUERY: " + uri);
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case SEARCH_INDEX_ENTRIES:
                String query;
                if (selectionArgs != null && selectionArgs.length > 0) {
                    query = selectionArgs[0];
                } else {
                    query = uri.getLastPathSegment();
                }
                return searchSuggestions(query);
            case SEARCH_LEXICAL_ENTRY:
                String lemma;
                if (selectionArgs != null && selectionArgs.length > 0) {
                    lemma = selectionArgs[0];
                } else {
                    lemma = uri.getLastPathSegment();
                }
                return search(lemma);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    /**
     * Searches for suggestions for a given word.
     *
     * @param word The word to search for.
     * @return A cursor containing the search results.
     */
    private Cursor searchSuggestions(String word) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        Context context = getContext();
        if (context == null) {
            return cursor;
        }
        SparkDictApplication app = (SparkDictApplication) context.getApplicationContext();
        Shelf shelf = app.getShelf();
        if (word != null && !word.isEmpty() && shelf != null) {
            String iconUri = "android.resource://" + context.getPackageName() + "/" + R.drawable.ic_launcher_sparkdict;
            String description = context.getString(R.string.search_description);
            TreeSet<IndexEntry> suggestions = new TreeSet<>();
            for (Book book : shelf.getBooks()) {
                if (book.isEnabled()) {
                    Vector<IndexEntry> tmp = book.getSuggestions(word);
                    suggestions.addAll(tmp);
                }
            }
            int id = 0;
            for (IndexEntry nextWord : suggestions) {
                String lemma = nextWord.getLemma();
                cursor.addRow(new Object[]{(long) id, lemma, description, iconUri, lemma, lemma});
                id++;
            }
        }
        return cursor;
    }

    /**
     * Searches for a word in the dictionary.
     *
     * @param word The word to search for.
     * @return A cursor containing the search results.
     */
    private Cursor search(String word) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        Context context = getContext();
        if (context == null) {
            return cursor;
        }
        SparkDictApplication app = (SparkDictApplication) context.getApplicationContext();
        Shelf shelf = app.getShelf();
        if (word != null && !word.isEmpty() && shelf != null) {
            String iconUri = "android.resource://" + context.getPackageName() + "/" + R.drawable.ic_launcher_sparkdict;
            String description = context.getString(R.string.search_description);
            int id = 0;
            for (Book book : shelf.getBooks()) {
                if (book.isEnabled()) {
                    try {
                        if (book.getLexicalEntry(word) != null) {
                            cursor.addRow(new Object[]{(long) id, word, description, iconUri, word, word});
                            id++;
                        }
                    } catch (Exception e) {
                        app.getLogger().error(TAG, "Error searching for " + word, e);
                    }
                }
            }
        }
        return cursor;
    }

    /**
     * Not implemented yet. Throws UnsupportedOperationException.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Not implemented. Throws UnsupportedOperationException.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Cannot insert words into SparkDict.");
    }

    /**
     * Not implemented. Throws UnsupportedOperationException.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Cannot update words in SparkDict.");
    }

    /**
     * Not implemented. Throws UnsupportedOperationException.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Cannot delete words from SparkDict.");
    }
}
