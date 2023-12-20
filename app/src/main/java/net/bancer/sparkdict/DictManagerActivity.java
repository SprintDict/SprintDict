package net.bancer.sparkdict;

import java.util.ArrayList;

import net.bancer.sparkdict.adapters.DictManagerItemsAdapter;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.IObserver;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.domain.utils.DomainException;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/**
 * DictManagerActivity displays a list of all dictionaries, allows the user
 * to enable/disable the dictionaries, to change their order, to set the path to 
 * the dictionaries, to build the additional index files for all dictionaries.
 * 
 * @author Valerij Bancer
 *
 */
public class DictManagerActivity extends BaseActivity {

	public static final int FILE_PERMISSIONS_ACCESS_REQUEST = 34232131;
	public static final int PICK_DIRECTORY = 43522432;

	/**
	 * Root directory for a directory picker.
	 */
	private static final String MOUNT_DIR = "/mnt";
	
	/**
	 * Tag (key) used in Bundle extras to indicate that it contains a value
	 * indicating that another activity that is a child of DictManagerActivity
	 * must be started. This another activity could be started by selecting menu
	 * item from DictManagerActivity activity.
	 */
	protected static final String SUB_ACTIVITY = "SubActivityToStart";
	
	/**
	 * Flag used in Bundle extras to indicate that no subactivity to be started.
	 * @see net.bancer.sparkdict.DictManagerActivity#SUB_ACTIVITY
	 */
	protected static final int DO_NOT_START_SUB_ACTIVITY = 0;

	/**
	 * Flag used in Bundle extras to indicate that DirectoryPicker subactivity
	 * must be started.
	 * @see net.bancer.sparkdict.DictManagerActivity#SUB_ACTIVITY
	 */
	protected static final int START_DIR_PICKER = 1;
	private ListView listView;
	private DictManagerItemsAdapter adapter;
	
	static final int PROGRESS_DIALOG = 3;
    //IndexBuilderThreadOld progressThread;
    IndexBuilder progressThread;
    ProgressDialog progressDialog;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Environment.isExternalStorageManager()) {
			Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
			Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
			startActivityForResult(intent, FILE_PERMISSIONS_ACCESS_REQUEST);
		}
		Bundle extras = getIntent().getExtras();
		if(extras != null && extras.getInt(SUB_ACTIVITY) == START_DIR_PICKER) {
			startDirPicker();
		}
		initLayout();
	}

	@Override
	public void onResume() {
		super.onResume();
		//initLayout();
	}

	private void initLayout() {
		setContentView(R.layout.activity_dict_manager);

		ArrayList<Book> books = getBooks();
		adapter = new DictManagerItemsAdapter(this, books);
		
		listView = (ListView) findViewById(R.id.dict_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(adapter);
	}
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater mi = getMenuInflater();
    	mi.inflate(R.menu.activity_dict_manager, menu);
    	return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_set_dict_path:
    			startDirPicker();
    			return true;
    		case R.id.menu_rebuild_index:
    			showDialog(PROGRESS_DIALOG);
    			/*ProgressDialog progressDialog;
    			progressDialog = new ProgressDialog(this);
    			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    			progressDialog.setMessage("Loading...");
    			progressDialog.setCancelable(false);
    			progressDialog.show();*/
    			return true;
    		default:
    			return super.onMenuItemSelected(featureId, item);
    	}
    }
        
	private void startDirPicker() {
		//Log.d(TAG, "Set dict path!");
		// Choose a directory using the system's file picker.
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		// Optionally, specify a URI for the directory that should be opened in
		// the system file picker when it loads.
		String uriToLoad = "/";
		intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
		startActivityForResult(intent, this.PICK_DIRECTORY);
	}
 
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent)	{
		super.onActivityResult(requestCode, resultCode, intent);
		// Save the path if DirectoryPicker successfully finished its job. 
		if(requestCode == this.PICK_DIRECTORY && resultCode == RESULT_OK) {
			if (intent != null) {
				saveDictPath(intent);
			}
		}
	}

	/**
	 * Extracts the folder that the user selected from the given intent object.
	 *
	 * @param intent
	 * @return
	 */
	private String extractSelectedFolder(Intent intent) {
		// The result data contains a URI for the document or directory that
		// the user selected.
		Uri uri = intent.getData();
		final String docId = DocumentsContract.getTreeDocumentId(uri);
		final String[] split = docId.split(":");
		final String type = split[0];
		// Get the path that was picked from intent returned by DirectoryPicker
		return Environment.getExternalStorageDirectory().toString() + "/" + split[1];
	}

	private void saveDictPath(Intent intent) {
		String value = this.extractSelectedFolder(intent);
		// Get the key that identifies the path in SharedPreferences
		String key = getString(R.string.menu_dict_path);
		// Save the path to SharedPreferences
		boolean isSaved = saveSharedPreference(key, value);
		String msg; 
		if(isSaved) { // Create message string for a toast
			msg = getString(R.string.dict_path_saved_msg, value);
		} else {
			msg = getString(R.string.dict_path_not_saved_msg, value);
		}
		refreshShelf();
		initLayout(); // Generate DictManagerActivity screen
		// Display a toast informing that path was saved/not saved
		showLongToast(msg);
	}
	
	/**
	 * Saves the ordered list of enabled dictionaries into shared preferences
	 * and refreshes Shelf object.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		String enabledDicts = "";
		int count = adapter.getCount();
		for(int i = 0; i < count; i++) {
			Book book = (Book) adapter.getItem(i);
			if(book.isEnabled()) {
				if(enabledDicts.length() > 1) {
					enabledDicts += "||";
				}
				enabledDicts += book.getBookName();
			}
		}
		boolean isSaved = saveSharedPreference(getString(R.string.enabled_dicts), enabledDicts);
		if (isSaved) {
			refreshShelf();
		}
	}

	/**
	 * "Select All" button handler. Checks all dictionaries.
	 * 
	 * @param v
	 */
	public void onSelectAllDicts(View v) {
		adapter.setCheckedAll(true);
	}

	/**
	 * "Unselect All" button handler. Un-checks all dictionaries.
	 * 
	 * @param v
	 */
	public void onUnselectAllDicts(View v) {
		adapter.setCheckedAll(false);
	}
	
	/**
	 * "Move Up" arrow image button handler. Moves the dictionary up the list.
	 * 
	 * @param v
	 */
	public void onMoveUp(View v) {
		int position = Integer.parseInt((String) v.getTag());
		if(position > 0) {
			ArrayList<Book> books = getBooks();
			Book book = books.get(position);
			books.remove(position);
			books.add(position - 1, book);
			adapter.notifyDataSetChanged();
		}	
	}
 
    @Override
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog = new ProgressDialog(DictManagerActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getString(R.string.rebuilding_index));
            progressDialog.setCancelable(false);
            return progressDialog;
        default:
            return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case PROGRESS_DIALOG:
        	progressThread = new IndexBuilder(handler, getShelf(), progressDialog);
            progressThread.start();
        }
    }
    

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int indexed = msg.arg1;
			int total = msg.arg2;
			progressDialog.setProgress(indexed);
			if (indexed >= total){
				dismissDialog(PROGRESS_DIALOG);
			}
		}
	};

	/**
	 * Sends notification about indexing error to the status bar.
	 * 
	 * @param message
	 *            message to be displayed.
	 * 
	 * @see http://developer.android.com/guide/topics/ui/notifiers/notifications.html#SimpleNotification
	 */
	private void sendNotification(String message) {
		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(this, DictManagerActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification note = new Notification(
				android.R.drawable.stat_sys_warning, message,
				System.currentTimeMillis());
		//TODO: migrate from deprecated setLatestEventInfo to Notification.Builder
		//note.setLatestEventInfo(this, getString(R.string.sprint_dict_indexing_error), message, pi);
		note.defaults |= Notification.DEFAULT_SOUND;
		note.flags |= Notification.FLAG_AUTO_CANCEL;
		mgr.notify(message.hashCode(), note);
	}
    
    private class IndexBuilder extends Thread implements IObserver {

		private Handler mHandler;
		private static final int MESSAGES_TIME_STEP = 100;
    	private ProgressDialog mProgressDialog;
    	private int articlesIndexed = 0; 
    	private int totalArticles = 0;
    	private long previousMessageTime = 0;
    	private long currentTime = 0;
    	private Shelf shelf;

		public IndexBuilder(Handler h, Shelf shelf, ProgressDialog dialog) {
			mHandler = h;
    		this.shelf = shelf;
    		mProgressDialog = dialog;
		}

		@Override
    	public void run() {
    		totalArticles = shelf.getTotalLexicalEntriesQuantity();
    		mProgressDialog.setProgress(0);
    		mProgressDialog.setMax(totalArticles);
    		int count = shelf.getBooks().size();
    		for (int i = 0; i < count; i++) {
    			try {
					shelf.getBooks().get(i).buildSparkDictIndex(this);
				} catch (DomainException e) {
					String message = getString(R.string.dict_cannot_be_indexed, shelf.getBooks().get(i).getBookName());
					Log.e(TAG, message, e);
					sendNotification(message);
				}
    		}
    	}

		@Override
    	public void update(Object field, int value) {
    		articlesIndexed++;

    		currentTime = System.currentTimeMillis();
			if(currentTime - previousMessageTime > MESSAGES_TIME_STEP || articlesIndexed >= totalArticles) {
				Message msg = mHandler.obtainMessage();
				msg.arg1 = articlesIndexed;
				msg.arg2 = totalArticles;
				mHandler.sendMessage(msg);
				previousMessageTime = currentTime;
			}
    	}
    }
}
