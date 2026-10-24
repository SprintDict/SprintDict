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
import net.bancer.sparkdict.domain.IndexBuilder;
import net.bancer.sparkdict.domain.IndexBuilderListener;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.logging.ConsoleLogger;
import net.bancer.sparkdict.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * IndexEntriesAdapter synchronises the visible list of suggestions with
 * the list of IndexEntry objects that match the user input.
 */
public class IndexEntriesAdapter extends ArrayAdapter<String> implements TextWatcher {

    protected static final String TAG = "IndexEntriesAdapter";

    private final Vector<String> entries;

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

    private Logger logger;

    /**
     * Constructor.
     *
     * @param context caller context.
     * @param entries empty container for IndexEntries.
     */
    public IndexEntriesAdapter(Context context, Vector<String> entries) {
        this(context, entries, new ConsoleLogger());
    }

    public IndexEntriesAdapter(Context context, Vector<String> entries, Logger logger) {
        super(context, R.layout.list_item, entries);
        this.entries = entries;
        this.logger = logger;
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
                Shelf shelf = getShelf();
                IndexEntriesRetriever retriever = new IndexEntriesRetriever(shelf, s.toString());
                currentTask = searchExecutor.submit(retriever);
            }
        }
    }

    @VisibleForTesting
    protected Shelf getShelf() {
        return ((SparkDictActivity) getContext()).getShelf();
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

        private final Shelf shelf;
        private final String search;

        private IndexEntriesRetriever(Shelf shelf, String search) {
            this.shelf = shelf;
            this.search = search;
        }

        /**
         * Retrieves suggestions for the current search text from each enabled book
         * and publishes the results to the main application thread.
         *
         * <p>Checks whether any enabled dictionaries are missing their index files.
         * If missing, builds those index files with progress reporting before
         * looking up suggestions.</p>
         */
        @Override
        public void run() {
            if (!buildMissingIndexes()) {
                return;
            }
            ArrayList<Book> books = shelf.getBooks();
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                if (book.isEnabled() && !Thread.currentThread().isInterrupted()) {
                    Vector<IndexEntry> suggestions = book.getSuggestions(search);
                    publishProgress(suggestions);
                }
            }
        }

        /**
         * Checks whether any enabled dictionaries in the shelf are missing their
         * index files and builds those index files synchronously with progress reporting.
         *
         * @return {@code true} if index building completed or was not needed,
         *         or {@code false} if the current thread was interrupted while waiting.
         */
        private boolean buildMissingIndexes() {
            List<Book> booksMissingIndexes = shelf.getBooksMissingSparkDictIndex();
            if (booksMissingIndexes.isEmpty()) {
                return true;
            }
            IndexBuilderListener listener = new SparkDictActivityIndexBuilderListener(getContext());
            IndexBuilder builder = new IndexBuilder(listener, booksMissingIndexes);
            builder.start();
            try {
                builder.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            return true;
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
                    if (!entries.get(i).toLowerCase().startsWith(search.toLowerCase())) {
                        entries.remove(i);
                    }
                }
                if (!suggestions.isEmpty()) {
                    Collections.sort(suggestions);
                    for (IndexEntry entry : suggestions) {
                        int key = Collections.binarySearch(entries, entry.getLemma(), IndexEntry.LEMMA_COMPARATOR);
                        if (key < 0) {
                            entries.add(-(key) - 1, entry.getLemma());
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
    }
}
