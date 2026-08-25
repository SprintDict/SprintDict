package net.bancer.sparkdict.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * IndexEntriesAdapter synchronises the visible list of suggestions with
 * the list of IndexEntry objects that match the user input.
 */
public class IndexEntriesAdapter extends ArrayAdapter<IndexEntry>
    implements TextWatcher {

    private final Vector<IndexEntry> entries;

    /**
     * Executor used to retrieve index entries in a background thread.
     * A single thread guarantees that suggestions for consecutive
     * keystrokes are processed in order and never overlap.
     */
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();

    /**
     * Handler used to post results back to the main application thread.
     */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String searchStr;

    private Filter mFilter;

    /**
     * Handle to the currently running (or most recently submitted)
     * retrieval task, used to cancel outdated work when new input arrives.
     */
    private Future<?> currentTask;

    /**
     * Constructor.
     *
     * @param context caller context.
     * @param entries empty container for IndexEntries.
     */
    public IndexEntriesAdapter(Context context, Vector<IndexEntry> entries) {
        super(context, R.layout.list_item, entries);
        this.entries = entries;
    }

    /**
     * Not implemented.
     */
    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * Not implemented.
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Performs a search for suggestions after the user typed a letter in the
     * search field.
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0 && !s.toString().equals(searchStr)) {
            searchStr = s.toString();
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(true);
            }
            if (!searchStr.isEmpty()) {
                currentTask = searchExecutor.submit(new IndexEntriesRetriever(s.toString()));
            }
        }
    }

	/**
	 * Returns the filter used by the autocomplete view.
	 *
	 * @return the custom filter.
	 */
    @Override
    @NotNull
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new IndexEntriesFilter();
        }
        return mFilter;
    }

    /**
     * Stops any pending background work and releases the executor. Must be
     * called by the hosting component (e.g. from the Activity's
     * {@code onDestroy()}) to avoid leaking a background thread.
     */
    public void shutdown() {
        searchExecutor.shutdownNow();
    }

    /**
     * No actual filtering is needed because the dataset is changed while
     * retrieving index entries. The class is created here to override
     * unwanted behaviour in the super class.
     */
    private class IndexEntriesFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (prefix != null) {
                results.values = entries;
                results.count = entries.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    /**
     * Looks up matching index entries in a background thread and publishes
     * each enabled book's suggestions to the main application thread as
     * they become available.
     */
    private class IndexEntriesRetriever implements Runnable {

        private final String search;

        private IndexEntriesRetriever(String search) {
            this.search = search;
        }

        /**
         * Retrieves suggestions for the current search text from each enabled book
         * and publishes the results to the main application thread.
         */
        @Override
        public void run() {
            SparkDictActivity activity = ((SparkDictActivity) IndexEntriesAdapter.this.getContext());
            ArrayList<Book> books = activity.getShelf().getBooks();
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                if (book.isEnabled() && !Thread.currentThread().isInterrupted()) {
                    Vector<IndexEntry> suggestions = book.getSuggestions(search);
                    publishProgress(suggestions);
                }
            }
        }

        /**
         * Posts newly found suggestions to the main application thread.
         *
         * @param suggestions suggestions found for the current book.
         */
        private void publishProgress(final Vector<IndexEntry> suggestions) {
            mainHandler.post(() -> onProgressUpdate(suggestions));
        }

        /**
         * Removes suggestions that do not fit what is entered in the
         * search field and adds new suggestions.
         *
         * @param suggestions suggestions found for the current book.
         */
        private void onProgressUpdate(Vector<IndexEntry> suggestions) {
            synchronized (entries) {
                for (int i = entries.size() - 1; i >= 0; i--) {
                    if (!entries.get(i).getLemma().toLowerCase().startsWith(search.toLowerCase())) {
                        entries.remove(i);
                    }
                }
                if (!suggestions.isEmpty()) {
                    Collections.sort(suggestions);
                    for (IndexEntry entry : suggestions) {
                        int key = Collections.binarySearch(entries, entry);
                        if (key < 0) {
                            entries.add(-(key) - 1, entry);
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}
