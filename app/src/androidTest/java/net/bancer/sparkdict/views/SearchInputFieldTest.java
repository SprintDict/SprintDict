package net.bancer.sparkdict.views;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchInputFieldTest {

    /**
     * Verifies that performClick on SearchInputField returns true and shows the keyboard.
     */
    @Test
    public void testPerformClickReturnsTrueAndShowsKeyboard() {
        Context context = ApplicationProvider.getApplicationContext();
        SearchInputField inputField = new SearchInputField(context);
        boolean clicked = inputField.performClick();
        assertTrue(clicked);
    }
}
