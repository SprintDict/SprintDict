package net.bancer.sparkdict.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.storage.SafDictionaryFilesFactory;
import net.bancer.sparkdict.storage.SparkDictPreferences;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Vector;

/**
 * SuggestionsProvider provides suggestions for Quick Search widget.
 * Android Tutorial: Adding Search Suggestions:
 * <a href="http://www.grokkingandroid.com/android-tutorial-adding-suggestions-to-search/">...</a>
 */
public class SuggestionsProvider extends ContentProvider {

    private static final String[] COLUMNS = new String[]{
        BaseColumns._ID,
        SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
    };
    private static final int SEARCH_LEXICAL_ENTRY = 0;
    private static final int SEARCH_INDEX_ENTRIES = 1;
    /**
     * Full class name of SuggestionsProvider.
     */
    public static String AUTHORITY = "net.bancer.sparkdict.providers.SuggestionsProvider";
    /**
     * URI to identify requests to SparkDict.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/dictionary");
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    private ArrayList<Book> books;
    private TreeSet<IndexEntry> suggestions;

    public SuggestionsProvider() {
        //SharedPreferences prefs = getContext().getSharedPreferences("SparkDict", Context.MODE_PRIVATE);
        //String key = SparkDictPreferences.PREF_DICT_ROOT_URI_NAME;
        //String result = prefs.getString(key, "");
    }

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions
        matcher.addURI(AUTHORITY, "dictionary", SEARCH_LEXICAL_ENTRY);
        // to get suggestions
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_INDEX_ENTRIES);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        SharedPreferences prefs = context.getSharedPreferences("SparkDict", Context.MODE_PRIVATE);

        String keyDictPath = SparkDictPreferences.PREF_DICT_ROOT_URI_NAME;
        String dictPath = prefs.getString(keyDictPath, "");

        String keyEnabledDicts = context.getString(R.string.enabled_dicts);
        String strEnabledDicts = prefs.getString(keyEnabledDicts, "");

        String[] enabledDicts = strEnabledDicts.split("\\|\\|");

        DictionaryFiles dictionaryFiles = SafDictionaryFilesFactory.create(context);
        Shelf shelf = new Shelf(enabledDicts, dictionaryFiles);
        books = shelf.getBooks();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case SEARCH_INDEX_ENTRIES:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
                }
                return searchSuggestions(selectionArgs[0]);
            case SEARCH_LEXICAL_ENTRY:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor searchSuggestions(String word) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        if (word != null) {
            getSuggestions().clear();
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                if (book.isEnabled()) {
                    Vector<IndexEntry> tmp = book.getSuggestions(word);
                    suggestions.addAll(tmp);
                }
            }
            int id = 0;
            for (IndexEntry nextWord : suggestions) {
                cursor.addRow(new Object[]{(long) id, nextWord, nextWord});
                id++;
            }
        }
        return cursor;
    }

    private TreeSet<IndexEntry> getSuggestions() {
        if (suggestions == null) {
            suggestions = new TreeSet<>();
        }
        return suggestions;
    }

    /**
     * Not implemented yet. Throws UnsupportedOperationException.
     */
    private Cursor search(String string) {
        throw new UnsupportedOperationException("Not implemented yet.");
//    	String[] columns = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1};
//    	MatrixCursor cursor = new MatrixCursor(columns);
//
//		return cursor;
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
