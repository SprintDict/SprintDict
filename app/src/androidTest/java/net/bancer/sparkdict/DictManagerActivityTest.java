package net.bancer.sparkdict;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.mocks.Mocks;
import net.bancer.sparkdict.storage.SparkDictPreferences;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Integration test for {@link DictManagerActivity}.
 *
 * <p>Verifies context menu interactions, such as opening the "Details" menu option
 * and displaying dictionary metadata in an alert dialog.</p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DictManagerActivityTest {

    @Rule
    public ActivityScenarioRule<DictManagerActivity> activityScenarioRule =
        new ActivityScenarioRule<>(DictManagerActivity.class);

    /**
     * Sets up the test environment by persisting dictionary folder path preferences
     * so that the list of dictionaries is loaded in {@link DictManagerActivity}.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SparkDictPreferences preferences = new SparkDictPreferences(context);
        String uriString = "content://com.android.externalstorage.documents/tree/primary%3A" + Mocks.ROOT_FOLDER;
        preferences.save(SparkDictPreferences.PREF_DICT_ROOT_URI_NAME, uriString);
    }

    /**
     * Verifies that the dictionary list is displayed upon activity launch.
     */
    @Test
    public void test10DictListIsDisplayed() {
        activityScenarioRule.getScenario().onActivity(activity -> {
            ListView listView = activity.findViewById(R.id.dict_list);
            assertNotNull(listView);
        });
    }

    /**
     * Verifies that long-clicking a dictionary item opens the context menu,
     * and selecting "Details" displays an alert dialog showing the dictionary's information.
     */
    @Test
    public void test20SelectingDetailsDisplaysDictionaryInfoDialog() {
        onData(anything())
            .inAdapterView(withId(R.id.dict_list))
            .atPosition(0)
            .perform(longClick());
        onView(withText(R.string.menu_dict_details))
            .check(matches(isDisplayed()))
            .perform(click());
        onView(withText(R.string.dict_details_dialog_title))
            .check(matches(isDisplayed()));
        onView(withText(android.R.string.ok))
            .perform(click());
    }
}
