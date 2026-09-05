package net.bancer.sparkdict;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import net.bancer.sparkdict.domain.core.Shelf;
import net.bancer.sparkdict.storage.SparkDictPreferences;
import net.bancer.sparkdict.views.SearchInputField;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SparkDictActivityTest {

    @Rule
    public ActivityScenarioRule<SparkDictActivity> mActivity = new ActivityScenarioRule<>(SparkDictActivity.class);

    @Test
    public void testPreConditions() {
        mActivity.getScenario().onActivity(activity -> {
            SearchInputField mInputTextView = activity.findViewById(R.id.searchTextView);
            ImageButton mSearchButton = activity.findViewById(R.id.searchButton);
            ProgressBar mSearchProgress = activity.findViewById(R.id.search_progress);
            LinearLayout mArticlesList = activity.findViewById(R.id.articles_list);
            Shelf mShelf = activity.getShelf();
            assertNotNull(mInputTextView.getAdapter());
            assertNotNull(mSearchButton);
            assertNotNull(mSearchProgress);
            assertNotNull(mArticlesList);
            assertEquals(0, mArticlesList.getChildCount());
            assertNotNull(mShelf);
        });
    }

    @Test
    public void testInputTextByPressingEnterKey() throws InterruptedException {
        onView(withId(R.id.searchTextView))
            .check(matches(withText("")));
        // Assert that the progress bar is not visible before the search.
        onView(withId(R.id.search_progress))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.searchTextView))
            .perform(typeText("interface"));
        onView(withId(R.id.searchTextView))
            .check(matches(withText("interface")));
        onView(withId(R.id.searchTextView))
            .perform(pressImeActionButton());
        Thread.sleep(1000);
        onView(withId(R.id.searchTextView))
            .check(matches(withText("")));
        // Assert that the progress bar is not visible after the search.
        onView(withId(R.id.search_progress))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void testFindOnPage() throws InterruptedException {
        // search definitions of "go"
        onView(withId(R.id.searchTextView))
            .perform(typeText("go"));
        onView(withId(R.id.searchTextView))
            .perform(pressImeActionButton());
        Thread.sleep(2000);
        // check that the spinning will is not displayed any more
        onView(withId(R.id.search_progress))
            .check(matches(not(isDisplayed())));
        // click on the 3 dots menu button
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText("Find On Page"))
            .perform(click());
        // enter "went" into "find on page" input
        onView(withId(R.id.find_on_page_edit_text))
            .perform(typeText("went"));
        // click on ▼ button
        onView(withId(R.id.find_on_page_next_btn))
            .perform(click());
        // check that "went" is highlighted
        onView(allOf(
            withId(R.id.definitions_body),
            hasHighlightedWord("went")
        ))
            .check(matches(isDisplayed()));
        // enter "gone" into "find on page" input
        onView(withId(R.id.find_on_page_edit_text))
            .perform(replaceText("gone"));
        // click on ▲ button
        onView(withId(R.id.find_on_page_previous_btn))
            .perform(click());
        // check that "gone" is highlighted
        onView(allOf(
            withId(R.id.definitions_body),
            hasHighlightedWord("gone")
        ))
            .check(matches(isDisplayed()));
        // click on ✕ button
        onView(withId(R.id.find_on_page_close_btn))
            .perform(click());
    }

    /**
     * Creates a matcher that checks whether a {@link TextView} contains the specified
     * word with a {@link BackgroundColorSpan} applied to it.
     *
     * @param word The word to check for.
     * @return A matcher that matches a {@link TextView} containing the highlighted word.
     */
    private static Matcher<View> hasHighlightedWord(String word) {
        return new TypeSafeMatcher<View>() {

            @Override
            protected boolean matchesSafely(View view) {
                TextView textView = (TextView) view;
                if (!(textView.getText() instanceof Spanned)) {
                    return false;
                }
                Spanned text = (Spanned) textView.getText();
                int start = text.toString().indexOf(word);
                if (start < 0) {
                    return false;
                }
                int end = start + word.length();
                return text.getSpans(start, end, BackgroundColorSpan.class).length > 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("contains highlighted \"" + word + "\"");
            }
        };
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
        SharedPreferences preferences = context.getSharedPreferences(SparkDictPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        Map<String, ?> originalPreferences = new HashMap<>(preferences.getAll());
        preferences.edit()
            .remove(SparkDictPreferences.PREF_DICT_ROOT_URI_NAME)
            .commit();
        return originalPreferences;
    }

    private static void restorePreferences(Map<String, ?> originalPreferences) {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(SparkDictPreferences.PREFS_NAME, Context.MODE_PRIVATE);
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
