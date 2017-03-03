package net.bancer.sparkdict;

import java.util.ArrayList;
import java.util.LinkedList;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.Shelf;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Gravity;
import android.widget.Toast;

/**
 * BaseActivity provides common methods and configuration data for different
 * activities.
 * 
 * @author Valerij Bancer
 *
 */
public abstract class BaseActivity extends Activity {

	private static final String RECENT_HISTORY_PREF_KEY = "recent.history";
	private static final String RECENT_HISTORY_WORDS_SEPARATOR = "::";
	private static final int RECENT_HISTORY_MAX_SIZE = 100;
	
	/**
	 * Tag to identify SparkDict (for debug).
	 */
	protected static final String TAG = "SparkDict";
	
	/**
	 * The name of SparkDict shared preferences.
	 */
	protected static final String PREFS_NAME = "SparkDict";
	
	private static Shelf shelf;
	private static LinkedList<String> recentHistory;

	/**
	 * Retrieves path to dictionaries from shared preferences.
	 * 
	 * @return path to dictionaries.
	 */
	protected String getDictPathFromPrefs() {
        String key = getString(R.string.menu_dict_path);
		String dictPath = getStrSharedPreference(key);
        return dictPath.trim();
	}

	/**
	 * Retrieves a string array of titles of enabled dictionaries from shared
	 * preferences.
	 * 
	 * @return a string array of titles of enabled dictionaries.
	 */
	protected String[] getEnabledDictsFromPrefs() {
		String strEnabledDicts = getStrSharedPreference(getString(R.string.enabled_dicts));
		return strEnabledDicts.split("\\|\\|");
	}

	/**
	 * Retrieves SparkDict shared preferences.
	 * 
	 * @return SparkDict shared preferences.
	 */
	protected SharedPreferences getSharedPreferences() {
		return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
	 * Get string value from shared preferences identified by key.
	 * 
	 * @param key 	shared preference key.
	 * @return		string value of the shared preference.
	 */
	protected String getStrSharedPreference(String key) {
		SharedPreferences settings = getSharedPreferences();
		String result = settings.getString(key, "");
		return result;
	}
	
	/**
	 * Get float value from shared preferences identified by key.
	 * 
	 * @param key 	shared preference key.
	 * @return		float value of the shared preference.
	 */
	public float getFloatSharedPreference(String key) {
		SharedPreferences settings = getSharedPreferences();
		float result = settings.getFloat(key, 20.0f);
		return result;
		
	}

	/**
	 * Saves a string value to shared preferences identifying it by the key.
	 * 
	 * @param key		key of the shared preference to be saved.
	 * @param value		string value to be saved.
	 * @return			`true` if the value was saved, else `false`.
	 */
	protected boolean saveSharedPreference(String key, String value) {
		SharedPreferences settings = getSharedPreferences();
		Editor editor = settings.edit();
		editor.putString(key, value);
		boolean isSaved = editor.commit();
		return isSaved;
	}
	
	/**
	 * Saves a float value to shared preferences identifying it by the key.
	 * 
	 * @param key		key of the shared preference to be saved.
	 * @param value		float value to be saved.
	 * @return			`true` if the value was saved, else `false`.
	 */
	public boolean saveSharedPreference(String key, float value) {
		SharedPreferences settings = getSharedPreferences();
		Editor editor = settings.edit();
		editor.putFloat(key, value);
		boolean isSaved = editor.commit();
		return isSaved;
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
		if(shelf == null) {
			//String dictPath = getDictPathFromPrefs();
			//String[] enabledDicts = getEnabledDictsFromPrefs();
			//shelf = new Shelf(dictPath, enabledDicts);
			refreshShelf();
		}		
		return shelf;
	}
	
	/**
	 * Refreshes Shelf to ensure that the list of enabled dictionaries is
	 * always up-to-date.
	 */
	protected void refreshShelf() {
		String dictPath = getDictPathFromPrefs();
		String[] enabledDicts = getEnabledDictsFromPrefs();
		//shelf.setDictPath(dictPath);
		//shelf.setEnabledDicts(enabledDicts);
		//shelf.putBooksOnShelf();
		shelf = new Shelf(dictPath, enabledDicts);
	}
	
//	protected void restoreSharedPreferences() {
//		
//	}
	
	/**
	 * Adds a word to the recent search history list. If the list is full
	 * then the first item is removed. Maximum size of the list is 100 words.
	 * 
	 * @param word	 	The word to be added.
	 */
	protected void addToRecentHistory(String word) {
		getRecentHistory().remove(word);
		if(recentHistory.size() == RECENT_HISTORY_MAX_SIZE) {
			recentHistory.removeLast();
		}
		recentHistory.addFirst(word);
	}

	/**
	 * Retrieves a list of recent search history.
	 * 
	 * @return LinkedList<String> Linked list of recent search history. 
	 */
	protected LinkedList<String> getRecentHistory() {
		if(recentHistory == null) {
			recentHistory = new LinkedList<String>();
			String history = getStrSharedPreference(RECENT_HISTORY_PREF_KEY);
			if(history.length() > 0) {
				String[] historyArr = history.split(RECENT_HISTORY_WORDS_SEPARATOR);
				for(int i = 0; i < historyArr.length; i++) {
					recentHistory.offer(historyArr[i]);
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
	protected boolean saveRecentHistory() {
		String historyStr = "";
		for (int i = 0; i < getRecentHistory().size(); i++) {
			if(i != 0) {
				historyStr += RECENT_HISTORY_WORDS_SEPARATOR;
			}
			historyStr += recentHistory.get(i).toString();
		}
		return saveSharedPreference(RECENT_HISTORY_PREF_KEY, historyStr);
	}
}
