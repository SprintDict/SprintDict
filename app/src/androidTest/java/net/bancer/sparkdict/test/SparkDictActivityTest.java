package net.bancer.sparkdict.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.views.SearchInputField;

import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SparkDictActivityTest {
	
	private static final String SEARCH_STRING = "interface";

	public static final int INIT_ARTICLES_COUNT = 0;

	@Rule
	public ActivityScenarioRule<SparkDictActivity> mActivity = new ActivityScenarioRule<>(SparkDictActivity.class);
	private SearchInputField mInputTextView;
	private ImageButton mSearchButton;
	private LinearLayout mArticlesList;
	private Shelf mShelf;

	@Before
	public void setUp() {
		mActivity.getScenario().onActivity(activity -> {
			this.mInputTextView = activity.findViewById(R.id.searchTextView);
			this.mSearchButton = activity.findViewById(R.id.searchButton);
			this.mArticlesList = activity.findViewById(R.id.articles_list);
			this.mShelf = activity.getShelf();
		});
	}

	@Test
	public void testPreConditions() {
		assertNotNull(mInputTextView.getAdapter());
		assertNotNull(mSearchButton);
		assertNotNull(mArticlesList);
		assertEquals(INIT_ARTICLES_COUNT, mArticlesList.getChildCount());
		assertNotNull(mShelf);
	}

	@Test
	public void testInputTextByPressingEnterKey() {
		onView(withId(R.id.searchTextView))
			.perform(typeText(SEARCH_STRING));
		onView(withId(R.id.searchTextView))
			.check(matches(withText(SEARCH_STRING)));
		onView(withId(R.id.searchTextView))
			.perform(pressImeActionButton());
		onView(withId(R.id.searchTextView))
			.check(matches(withText("")));
	}
}
