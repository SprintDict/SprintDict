package net.bancer.sparkdict;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Recent History activity displays the list of recently searched words or
 * phrases as a scrollable list. If the menu is invoked Clear History option
 * is available.
 * 
 * @author Valerij Bancer
 *
 */
public class RecentHistoryActivity extends BaseActivity implements OnItemClickListener {
	
	/**
	 * List of all recently searched items.
	 */
	private ListView listView;

	/**
	 * Creates the activity, retrieves history items and populates the view with
	 * them.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent_history);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			this, android.R.layout.simple_list_item_1, getRecentHistory()
		);
		listView = (ListView) findViewById(R.id.history_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	/**
	 * Creates a menu with "Clear History" option.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_recent_history, menu);
		return true;
	}

	/**
	 * Handles selection of "Clear History" menu options. Deletes all items
	 * from the history and starts SparkDictActivity.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_clear_recent_history:
				getRecentHistory().clear();
				((ArrayAdapter<String>)listView.getAdapter()).notifyDataSetChanged();
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Handles a click on an item from the history list. Passes the selected
	 * word or phrase to SparkDictActivity as a target for search action.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		String word = listView.getAdapter().getItem(position).toString();
		Intent intent = new Intent(Intent.ACTION_SEARCH);
		intent.putExtra(SearchManager.QUERY, word);
		intent.setClass(this, SparkDictActivity.class);
		startActivity(intent);
	}
}
