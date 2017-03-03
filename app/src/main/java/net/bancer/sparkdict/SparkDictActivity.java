package net.bancer.sparkdict;

import java.util.ArrayList;
import java.util.Vector;

import net.bancer.sparkdict.adapters.IndexEntriesAdapter;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IndexEntry;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.domain.utils.DomainException;
import net.bancer.sparkdict.views.LexicalEntriesListView;
import net.bancer.sparkdict.views.SearchInputField;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ZoomControls;

/**
 * SparkDictActivity is the main activity.
 * 
 * @author Valerij Bancer
 *
 */
public class SparkDictActivity extends BaseActivity 
		implements OnClickListener, OnKeyListener, OnItemClickListener {

	private static final int DIALOG_NO_PATH_SET = 434354331;
	private static final int DIALOG_SEARCHING = 434354332;
	
	private static final String KEY_SEARCH_STR 						= "SEARCH_STR";
	private static final String KEY_ARTICLES_SCROLL_POSITION 		= "ARTICLES_SCROLL_POSITION";
	private static final String KEY_FOCUSED_WORD 					= "FOCUSED_WORD";
	private static final String KEY_FIND_ON_PAGE_STR 				= "KEY_FIND_ON_PAGE_STR";
	
	/**
	 * Key to identify the array of definitions visibility field values of
	 * all lexical entry views in order to restore their state after screen
	 * orientation change.
	 */
	private static final String KEY_DEFINITIONS_VISIBILITY = "KEY_EXPANDED_DEFINITIONS_POSITIONS";
	
	public static final String SEARCH_INTENT = "net.bancer.sparkdict.SEARCH";
	
	private SearchInputField inputTextView;
	private ImageButton searchButton;
	private ScrollView scrollView;
	private LexicalEntriesListView lexicalEntriesListView;
	private ZoomControls zoomControls;
	private EditText findOnPageInput;
	private LinearLayout findOnPageView;
	
	private Runnable zoomHider = new Runnable() {
		@Override
		public void run() {
			zoomControls.setVisibility(View.GONE);
		}
	};
	
	private String dictPath;
	private ArrayList<LexicalEntry> articles = new ArrayList<LexicalEntry>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //restoreSharedPreferences();
        checkDictPath();
        initLayout();
        
        // Check whether we're recreating a previously destroyed instance
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			@SuppressWarnings("unchecked")
			final ArrayList<LexicalEntry> data = (ArrayList<LexicalEntry>) getLastNonConfigurationInstance();
			if(data != null && !data.isEmpty()) {
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
        //System.out.println("onCreate");
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveDefinitionsViewsState(outState);
		saveScrollPosition(outState);
		if (findOnPageView.getVisibility() == View.VISIBLE) {
			saveFindOnPageBarState(outState);
		}
		//System.out.println("onSaveInstanceState");
	}

	@Override
	protected void onNewIntent(Intent intent) {
    	setIntent(intent);
    	processIntent(intent);
    }
    
    private void processIntent(Intent intent) {
    	//System.out.println("intent action: " + intent.getAction());
    	// Handle search action
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			//System.out.println("uri: " + intent.getData().toString());

//			String query1 = intent.getStringExtra(SearchManager.QUERY);
//			Cursor cursor = managedQuery(SuggestionsProvider.CONTENT_URI, null, null,
//	                new String[] {query1}, null);
//			if(cursor != null) {
//				System.out.println("cursor count: " + cursor.getCount());
//			}
			
			String query = intent.getStringExtra(SearchManager.QUERY);
			//System.out.println("query: " + query);
			doSearch(query);
		}
		// Handle lexical entry hyperlink click
		if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			//System.out.println("uri: " + intent.getData().toString());
			Uri data = intent.getData();
			if(data != null) {
				String query = data.getSchemeSpecificPart().substring(2);
				System.out.println("query: " + query);
				doSearch(query);
			}
		}
		// Handle searchable dialog suggestion click
		if(SparkDictActivity.SEARCH_INTENT.equals(intent.getAction())) {
			Uri data = intent.getData();
			if(data != null) {				
				String query = data.getLastPathSegment();
				doSearch(query);
			}
		}
	}

	private void initLayout() {
		
		// enable progress spinner in the title bar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	    requestWindowFeature(Window.FEATURE_PROGRESS);
		
		setContentView(R.layout.activity_spark_dict);
		
		inputTextView = (SearchInputField)findViewById(R.id.searchTextView);
		inputTextView.setOnKeyListener(this);
		
		IndexEntriesAdapter adapter = new IndexEntriesAdapter(this, new Vector<IndexEntry>());
		//Log.d("SparkDictActivity", "shelf: " + getShelf());
		inputTextView.setAdapter(adapter);
		inputTextView.addTextChangedListener(adapter);
		// Start to display a list of suggestions after 1 letter typed
		inputTextView.setThreshold(1);
		inputTextView.setOnItemClickListener(this);
		
		searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        
        scrollView = (ScrollView) findViewById(R.id.articles_scroll_view);
        
        lexicalEntriesListView = (LexicalEntriesListView) findViewById(R.id.articles_list);
        lexicalEntriesListView.setOnClickListener(this);
        
        zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
		zoomControls.setOnZoomInClickListener(lexicalEntriesListView.getZoomInClickListener());
		zoomControls.setOnZoomOutClickListener(lexicalEntriesListView.getZoomOutClickListener());
        zoomControls.setVisibility(View.GONE);
        
        //findOnPageBar = new FindOnPageBar(this);

		findOnPageInput = (EditText) findViewById(R.id.find_on_page_edit_text);
		findOnPageView = (LinearLayout) findViewById(R.id.find_on_page_layout);
	}

	private void checkDictPath() {
        dictPath = getDictPathFromPrefs();
        if(dictPath.trim().equals("")) {
        	showDialog(DIALOG_NO_PATH_SET);
        }
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		final ArrayList<LexicalEntry> data = articles;
		return data;
	}

	/**
	 * Saves definitions visibility field value of all lexical entry
	 * views into a Bundle in  order to restore their state after the screen 
	 * orientation changes or any other system configuration change occurs.
	 * 
	 * @param outState	Bundle in which to place saved state.
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
	 * http://eliasbland.wordpress.com/2011/07/28/how-to-save-the-position-of-a-scrollview-when-the-orientation-changes-in-android/
	 * @param savedInstanceState
	 */
	private void restoreScrollPosition(Bundle savedInstanceState) {
		final int[] coordinates = savedInstanceState.getIntArray(KEY_ARTICLES_SCROLL_POSITION);
		if(coordinates != null) {
			scrollView.post(new Runnable() {
				@Override
				public void run() {
					scrollView.scrollTo(coordinates[0], coordinates[1]);					
				}
			});
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
    	switch (item.getItemId()) {
    		case R.id.menu_recent_history:
    			startActivity(new Intent(this, RecentHistoryActivity.class));
    			return true;
    		case R.id.menu_find_on_page:
    			openFindOnPageBar();
    			//showKeyboard(findOnPageEditText);
    			return true;
    		case R.id.menu_manage_dictionaries:
    			startDictManager(DictManagerActivity.DO_NOT_START_SUB_ACTIVITY);
    			return true;
    		case R.id.menu_settings:
    			startActivity(new Intent(this, DictPreferencesActivity.class));
    			return true;
    		case R.id.search_dialog:
    			onSearchRequested();
    			return true;
    		case R.id.menu_expand_all:
    			lexicalEntriesListView.expandAll();
    			return true;
    		case R.id.menu_collapse_all:
    			lexicalEntriesListView.collapseAll();
    			return true;
    		default:
    			return super.onMenuItemSelected(featureId, item);
    	}
    }

	private void startDictManager(int subactivity) {
		Intent intent = new Intent(this, DictManagerActivity.class);
		intent.putExtra(DictManagerActivity.SUB_ACTIVITY, subactivity);
		startActivity(intent);
	}

    @Override
	protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch (id) {
			case DIALOG_NO_PATH_SET:
				dialog = buildSetDictPathDialog();
				break;
			case DIALOG_SEARCHING:
				dialog = buildProgressDialog();
				break;
			default:
				dialog = null;
		}
    	return dialog;
    }

	private ProgressDialog buildProgressDialog() {
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getString(R.string.searching));
		progressDialog.setIndeterminate(true);
		return progressDialog;
	}

	private Dialog buildSetDictPathDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.prompt_to_set_path))
			.setCancelable(false)
			.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startDictManager(DictManagerActivity.START_DIR_PICKER);
				}
			})
			.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SparkDictActivity.this.finish();
				}
			});
		return builder.create();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.searchButton:
				doSearch(inputTextView.getText().toString());
				break;
			case R.id.definitions_body:
				zoomControls.setVisibility(View.VISIBLE);
				zoomControls.postDelayed(zoomHider, 2000);
				hideKeyboard(v);
				break;
			default:
				break;
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
		if(view instanceof TextView) {
			doSearch(((TextView)view).getText().toString());
		}
	}

	private void doSearch(String searchStr) {
		//Thread.dumpStack();
		lexicalEntriesListView.removeAllViews();
		showDialog(DIALOG_SEARCHING);
		new SearchWorker().execute(searchStr);
		hideKeyboard(inputTextView);
	}

	public void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) 
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	private void showKeyboard(View v) {
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		InputMethodManager imm = (InputMethodManager) 
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(v, 0);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	private void openFindOnPageBar() {
		findOnPageView.setVisibility(View.VISIBLE);
		findOnPageInput.setText("");
		findOnPageInput.requestFocus();
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) zoomControls.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
		zoomControls.setLayoutParams(params);
	}
	
	public void onCloseFindOnPageBarButtonClick(View v) {
		findOnPageInput.setText("");
		findOnPageView.setVisibility(View.GONE);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) zoomControls.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
		zoomControls.setLayoutParams(params);
		hideKeyboard(v);
		lexicalEntriesListView.clearSearchOnScreenResults();
	}
	
	public void onFindNextWordOnPageButtonClick(View v) {
		lexicalEntriesListView.findNextOnScreen(findOnPageInput.getText().toString());
	}
	
	public void onFindPreviousWordOnPageButtonClick(View v) {
		lexicalEntriesListView.findPreviousOnScreen(findOnPageInput.getText().toString());
	}
	
	/**
	 * Performs a search in all dictionaries in the background thread and updates
	 * the view in the UI thread.
	 * 
	 * @author Valerij Bancer
	 *
	 */
	private class SearchWorker extends AsyncTask<String, LexicalEntry, Boolean> {
		
		private String lemma;

		/**
		 * Performs lexical entries search in all dictionaries and publishes
		 * progress one by one.
		 */
		@Override
		protected Boolean doInBackground(String... lemma) {
			this.lemma = lemma[0];
			LexicalEntry entry = null;
			boolean atLeastOneEntryFound = false;
			articles.clear();
			for(Book book : getShelf().getBooks()) {
				if(book.isEnabled()) {
					try {
						entry = book.getLexicalEntry(lemma[0]);
					} catch (DomainException e) {
						Log.e(TAG, "Cannot retrieve '" + lemma[0] + "' from " + book.getBookName(), e);
					}
					if(entry != null) {
						articles.add(entry);
						publishProgress(entry);
						if(!atLeastOneEntryFound) {
							SparkDictActivity.this.addToRecentHistory(lemma[0]);
							atLeastOneEntryFound = true;
						}
					}
				}
			}
			return atLeastOneEntryFound;
		}

		/**
		 * Updates view with found lexical entry, dismisses progress spinner
		 * dialog, initiates progress spinner in the title bar.
		 */
		@Override
		protected void onProgressUpdate(LexicalEntry... entry) {
			lexicalEntriesListView.add(entry[0]);
			dismissDialog(DIALOG_SEARCHING);
			inputTextView.setText("");
			// display progress spinner in the title bar
			setProgressBarIndeterminateVisibility(true);
		    setProgressBarVisibility(true);
		}
		
		/**
		 * Terminates progress spinner in the title bar, displays a toast if
		 * no lexical entries were found.
		 */
		@Override
		protected void onPostExecute(Boolean atLeastOneEntryFound) {
			if(!atLeastOneEntryFound) {
				dismissDialog(DIALOG_SEARCHING);
				showLongToast(getString(R.string.nothing_found));
			}
			// hide progress spinner in the title bar
			setProgressBarIndeterminateVisibility(false);
		    setProgressBarVisibility(false);
		}
	}

}