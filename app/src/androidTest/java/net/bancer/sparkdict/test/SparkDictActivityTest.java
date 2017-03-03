package net.bancer.sparkdict.test;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.views.SearchInputField;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SparkDictActivityTest extends
		ActivityInstrumentationTestCase2<SparkDictActivity> {
	
	private static final String SEARCH_STRING = "interface";

	public static final int INIT_ARTICLES_COUNT = 0;

	private SparkDictActivity mActivity;
	private SearchInputField mInputTextView;
	private ImageButton mSearchButton;
	private LinearLayout mArticlesList;
	private Shelf mShelf;

	private String mIntputText;

	public SparkDictActivityTest() {
		super("net.bancer.sparkdict", SparkDictActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setActivityInitialTouchMode(false);
		
		mActivity = getActivity();
		
		mInputTextView = (SearchInputField) mActivity.findViewById(R.id.searchTextView);
		mSearchButton = (ImageButton) mActivity.findViewById(R.id.searchButton);
		mArticlesList = (LinearLayout) mActivity.findViewById(R.id.articles_list);
		
		mShelf = (Shelf) mActivity.getShelf();
	}
	
	public void testPreConditions() {
		assertTrue(mInputTextView.getAdapter() != null);
		assertTrue(mSearchButton != null);
		assertTrue(mArticlesList != null);
		assertTrue(mArticlesList.getChildCount() == INIT_ARTICLES_COUNT);
		assertTrue(mShelf != null);
	}
	
	public void testInputTextByPressingEnterKey() {
		new Runnable() {		
			@Override
			public void run() {
				mInputTextView.requestFocus();
				mInputTextView.setText(SEARCH_STRING);
				assertTrue(mIntputText.equals(SEARCH_STRING));
				SparkDictActivityTest.this.sendKeys(KeyEvent.KEYCODE_ENTER);
				mIntputText = mInputTextView.getText().toString();
				assertTrue(mIntputText.equals(""));
				assertTrue(mArticlesList.getChildCount() > INIT_ARTICLES_COUNT);
			}
		};
	}
}
