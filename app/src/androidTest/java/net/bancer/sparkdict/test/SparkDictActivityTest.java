package net.bancer.sparkdict.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static net.bancer.sparkdict.BaseActivity.PREFS_NAME;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.DictManagerActivity;
import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.views.SearchInputField;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SparkDictActivityTest {

    public static final int INIT_ARTICLES_COUNT = 0;

    private static final String SEARCH_STRING = "interface";

    @Rule
    public ActivityScenarioRule<SparkDictActivity> mActivity = new ActivityScenarioRule<>(SparkDictActivity.class);

    private SearchInputField mInputTextView;

    private ImageButton mSearchButton;

    private ProgressBar mSearchProgress;

    private LinearLayout mArticlesList;

    private Shelf mShelf;

    @Before
    public void setUp() {
        mActivity.getScenario().onActivity(activity -> {
            this.mInputTextView = activity.findViewById(R.id.searchTextView);
            this.mSearchButton = activity.findViewById(R.id.searchButton);
            this.mSearchProgress = activity.findViewById(R.id.search_progress);
            this.mArticlesList = activity.findViewById(R.id.articles_list);
            this.mShelf = activity.getShelf();
        });
    }

    @Test
    public void testPreConditions() {
        assertNotNull(mInputTextView.getAdapter());
        assertNotNull(mSearchButton);
        assertNotNull(mSearchProgress);
        assertNotNull(mArticlesList);
        assertEquals(INIT_ARTICLES_COUNT, mArticlesList.getChildCount());
        assertNotNull(mShelf);
    }

    @Test
    public void testInputTextByPressingEnterKey() {
        onView(withId(R.id.searchTextView))
            .check(matches(withText("")));
        // Assert that the progress bar is not visible before the search.
        onView(withId(R.id.search_progress))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.searchTextView))
            .perform(typeText(SEARCH_STRING));
        onView(withId(R.id.searchTextView))
            .check(matches(withText(SEARCH_STRING)));
        onView(withId(R.id.searchTextView))
            .perform(pressImeActionButton());
        onView(withId(R.id.searchTextView))
            .check(matches(withText("")));
        // Assert that the progress bar is not visible after the search.
        onView(withId(R.id.search_progress))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testNoPathSetDialogExitFinishesActivity() {
        Map<String, ?> originalPreferences = backupPreferencesAndRemoveDictionariesPath();
        try (ActivityScenario<SparkDictActivity> scenario = ActivityScenario.launch(SparkDictActivity.class)) {
            onView(withText(R.string.prompt_to_set_path))
                .check(matches(isDisplayed()));
            onView(withText(R.string.set_path))
                .check(matches(isDisplayed()));
            onView(withText(R.string.exit))
                .check(matches(isDisplayed()));
            onView(withText(R.string.exit))
                .perform(click());
            scenario.onActivity(activity ->
                assertTrue(activity.isFinishing())
            );
        } finally {
            restorePreferences(originalPreferences);
        }
    }

    @Test
    public void testNoPathSetDialogSetPathStartsDirectoryPicker() {
        Map<String, ?> originalPreferences = backupPreferencesAndRemoveDictionariesPath();
        Intents.init();
        try (ActivityScenario<SparkDictActivity> ignored = ActivityScenario.launch(SparkDictActivity.class)) {
            onView(withText(R.string.set_path))
                .perform(click());
            intended(allOf(
                hasComponent(DictManagerActivity.class.getName()),
                hasExtra(
                    DictManagerActivity.SUB_ACTIVITY,
                    DictManagerActivity.START_DIR_PICKER
                )
            ));
        } finally {
            Intents.release();
            restorePreferences(originalPreferences);
        }
    }

    @NonNull
    private static Map<String, ?> backupPreferencesAndRemoveDictionariesPath() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, ?> originalPreferences = new HashMap<>(preferences.getAll());
        preferences.edit()
            .remove(context.getString(R.string.menu_dict_path))
            .commit();
        return originalPreferences;
    }

    private static void restorePreferences(Map<String, ?> originalPreferences) {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        for (Map.Entry<String, ?> entry : originalPreferences.entrySet()) {
            Object value = entry.getValue();
            System.out.println(entry.getKey()+":"+value);
            if (value instanceof String) {
                editor.putString(entry.getKey(), (String) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), (Boolean) value);
            } else if (value instanceof Integer) {
                editor.putInt(entry.getKey(), (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(entry.getKey(), (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(entry.getKey(), (Float) value);
            } else if (value instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> stringSet = (Set<String>) value;
                editor.putStringSet(entry.getKey(), stringSet);
            }
        }
        editor.commit();
    }
}
