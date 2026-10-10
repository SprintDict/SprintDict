package net.bancer.sparkdict;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeUp;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import net.bancer.sparkdict.views.DefinitionsView;
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

    /**
     * Verifies that performClick on SearchInputField returns true and shows the keyboard
     * when the view is already focused.
     */
    @Test
    public void testClickSearchInputFieldWhenAlreadyFocused() {
        mActivity.getScenario().onActivity(activity -> {
            SearchInputField mInputTextView = activity.findViewById(R.id.searchTextView);
            mInputTextView.requestFocus();
            assertTrue(mInputTextView.isFocused());
            boolean result = mInputTextView.performClick();
            assertTrue(result);
        });
    }

    /**
     * Verifies the copy, paste, keyboard edit, and search scenario:
     * searches "abacus", copies "balls" from definitions, pastes it into searchTextView,
     * edits "balls" to "ball" via DEL key press, performs search, and asserts definition result.
     *
     * @throws InterruptedException if thread sleep is interrupted.
     */
    @Test
    public void testCopyPasteAndEditWithKeyboard() throws InterruptedException {
        // Search "abacus"
        onView(withId(R.id.searchTextView)).perform(typeText("abacus"));
        onView(withId(R.id.searchTextView)).perform(pressImeActionButton());
        Thread.sleep(2000);
        // Select "balls" and copy
        mActivity.getScenario().onActivity(activity ->
            activity.onCopySelected("balls")
        );
        // Paste contents to searchTextView input
        onView(withId(R.id.searchTextView)).perform(replaceText("balls"));
        onView(withId(R.id.searchTextView)).perform(click());
        // Edit "balls" by pressing "Back" key on the keyboard so that it becomes "ball"
        onView(withId(R.id.searchTextView)).perform(pressKey(KeyEvent.KEYCODE_DEL));
        onView(withId(R.id.searchTextView)).check(matches(withText("ball")));
        // Click search icon searchButton
        onView(withId(R.id.searchButton)).perform(click());
        Thread.sleep(2000);
        // Assert that "any object in the shape of a sphere" is displayed on the screen
        onView(withText(containsString("any object in the shape of a sphere")))
            .check(matches(isDisplayed()));
    }

    /**
     * Verifies that searching "bring", scrolling down, expanding the second dictionary,
     * scrolling a small amount to the audio icon, and clicking the audio icon does not cause unexpected scrolling.
     *
     * @throws InterruptedException if thread sleep is interrupted.
     */
    @Test
    public void testAudioClickDoesNotScrollScreen() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        SparkDictPreferences preferences = new SparkDictPreferences(context);
        String key = context.getString(R.string.enabled_dicts);
        String originalValue = preferences.getString(key);
        // It is important that Cambridge dictionary in the second one.
        String testValue = "Mueller7GPL||Cambridge Advanced Learners Dictionary 3th Ed. (En-En)||WordNet||Большая Советская Энциклопедия";
        preferences.save(key, testValue);
        SparkDictApplication app = (SparkDictApplication) context;
        app.refreshShelf();
        try {
            // Search "bring"
            onView(withId(R.id.searchTextView)).perform(typeText("bring"));
            onView(withId(R.id.searchTextView)).perform(pressImeActionButton());
            Thread.sleep(2000);
            // Scroll to the bottom
            onView(withId(R.id.articles_scroll_view)).perform(swipeUp());
            Thread.sleep(500);
            // Explicitly click on the second dictionary title ("Cambridge Advanced Learners Dictionary 3th Ed. (En-En)")
            onView(allOf(
                withId(R.id.dict_title),
                withText(containsString("Cambridge Advanced Learners Dictionary"))
            )).perform(click());
            Thread.sleep(1000);
            // Scroll a small amount (3-5 lines of text, ~200px) until the first audio icon below it becomes visible
            mActivity.getScenario().onActivity(activity -> {
                ScrollView scrollView = activity.findViewById(R.id.articles_scroll_view);
                scrollView.scrollBy(0, 200);
            });
            Thread.sleep(500);
            // Click that audio icon and assert scrollY does not unexpectedly jump
            mActivity.getScenario().onActivity(activity -> {
                ScrollView scrollView = activity.findViewById(R.id.articles_scroll_view);
                int scrollYBefore = scrollView.getScrollY();
                LinearLayout articlesList = activity.findViewById(R.id.articles_list);
                if (articlesList.getChildCount() >= 2) {
                    View secondEntry = articlesList.getChildAt(1);
                    DefinitionsView defView = secondEntry.findViewById(R.id.definitions_body);
                    if (defView != null && defView.getText() instanceof Spanned) {
                        Spanned text = (Spanned) defView.getText();
                        URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
                        if (spans.length > 0) {
                            spans[0].onClick(defView);
                        }
                    }
                }
                assertEquals(scrollYBefore, scrollView.getScrollY());
            });
        } finally {
            preferences.save(key, originalValue);
            app.refreshShelf();
        }
    }

    @Test
    public void testFindOnPage() throws InterruptedException {
        // search definitions of "go"
        onView(withId(R.id.searchTextView))
            .perform(typeText("go"));
        onView(withId(R.id.searchTextView))
            .perform(pressImeActionButton());
        Thread.sleep(3000);
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
        // click 6 times on ▼ button to navigate to subsequent occurrences
        for (int i = 0; i < 6; i++) {
            onView(withId(R.id.find_on_page_next_btn))
                .perform(click());
        }
        // check that "went" is highlighted and displayed on screen
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
        return new TypeSafeMatcher<>() {

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
