package net.bancer.sparkdict.adapters;

import java.util.ArrayList;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.Book;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

/**
 * DictManagerItemsAdapter synchronises the visible list of dictionaries with
 * list of Books objects.
 * 
 * @author Valerij Bancer
 *
 */
public class DictManagerItemsAdapter extends BaseAdapter implements
		/*implements OnLongClickListener*/ OnItemClickListener/*, OnClickListener*/ {

	private ArrayList<Book> items;
	private Context context;

	/**
	 * Constructor.
	 * 
	 * @param context	caller context.
	 * @param items 	array list of books.
	 */
	public DictManagerItemsAdapter(Context context, ArrayList<Book> items) {
		this.context = context;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Constructs dictionaries list item with a checkbox to enable/disable the 
	 * dictionary, dictionary title and an arrow button to move the dictionary
	 * up.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.dict_manager_item, null);
		}
		Book book = items.get(position);
		
		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.dict_manager_checkbox);
		checkBox.setText(book.getBookName());
		checkBox.setChecked(book.isEnabled());
		
		ImageView arrowUp = (ImageView) convertView.findViewById(R.id.dict_manager_move_up);
		arrowUp.setTag("" + position); // save position as a tag
		return convertView;
	}

	/**
	 * Enables or disables all dictionaries and puts or removes checkmarks into
	 * the checkboxes.
	 * 
	 * @param selected `true` to enable dictionaries, `false` to disable.
	 */
	public void setCheckedAll(boolean selected) {
		for (Book book : items) {
			book.setEnabled(selected);
		}
		notifyDataSetChanged();
	}

	/**
	 * Changes the state of clicked dictionary from enabled to disabled or from
	 * disable to enabled.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean currentState = items.get(position).isEnabled();
		items.get(position).setEnabled(!currentState);
		notifyDataSetChanged();
	}
}
