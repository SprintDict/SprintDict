package net.bancer.sparkdict.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.Shelf;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Filter;

/**
 * IndexEntriesAdapter synchronises the visible list of suggestions with
 * the list of IndexEntry objects that match the user input.
 * 
 * @author Valerij Bancer
 *
 */
public class IndexEntriesAdapter extends ArrayAdapter<IndexEntry>
		implements TextWatcher {

	private String searchStr;
	private Filter mFilter;
	private Vector<IndexEntry> entries;
	private IndexEntriesRetriever worker;
	
	/**
	 * Constructor.
	 * 
	 * @param context	caller context.
	 * @param shelf		shelf containing all the Books.
	 * @param entries	empty container for IndexEntries.
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
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}
	
	/**
	 * Performs a search for suggestions after the user typed a letter in the 
	 * search field.
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(s.length() > 0 && !s.toString().equals(searchStr)) {
			searchStr = s.toString();		
			if (worker != null && worker.getStatus() == AsyncTask.Status.RUNNING) {
				worker.cancel(true);
			}
			if(!searchStr.equals("")) {
				worker = new IndexEntriesRetriever();
				worker.execute(s.toString());
			}
		}
	}
	
	@Override
	public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new IndexEntriesFilter();
        }
        return mFilter;
    }

	
	/**
	 * No actual filtering is needed because the dataset is changed while
	 * retrieving index entries. The class is created here to override
	 * unwanted behaviour in the super class.
	 * 
	 * @author Valera
	 *
	 */
	private class IndexEntriesFilter extends Filter {
		
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();
			if(prefix != null) {
				results.values = entries;
				results.count = entries.size();
			}
			return results;
		}
		
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
			if(results != null && results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
        }
	}
	

	private class IndexEntriesRetriever extends AsyncTask<String, Vector<IndexEntry>, Boolean> {

		private String search = "";
		
		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(String... str) {
			search = str[0];
			ArrayList<Book> books = ((SparkDictActivity)IndexEntriesAdapter.this.getContext()).getShelf().getBooks();
			for(int i = 0; i < books.size(); i++){
				Book book = books.get(i);
				if(book.isEnabled() && !IndexEntriesRetriever.this.isCancelled()){
					Vector<IndexEntry> entries = book.getSuggestions(str[0]);
					publishProgress(entries);
				}
			}
			return true;
		}
		
		/**
		 * Removes suggestions that does not fit to what is entered in the 
		 * search field and adds new suggestions.
		 */
		@Override
		protected void onProgressUpdate(Vector<IndexEntry>... suggestions) {
			super.onProgressUpdate(suggestions);
			synchronized (entries) {
				for (int i = entries.size() - 1; i >= 0; i--) {
					if(!entries.get(i).getLemma().toLowerCase().startsWith(search.toLowerCase())) {
						entries.remove(i);
					}
				}
				if(suggestions[0].size() > 0) {
					for (IndexEntry entry : suggestions[0]) {
						Collections.sort(suggestions[0]);
						int key = Collections.binarySearch(entries, entry);
						if(key < 0) {
							entries.add(-(key) - 1, entry);
						}
					}
				}
			}
			notifyDataSetChanged();
		}
	}
}
