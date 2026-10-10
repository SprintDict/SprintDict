package net.bancer.sparkdict.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link LockableScrollView} to verify child focus and rectangle scroll suppression.
 */
@RunWith(AndroidJUnit4.class)
public class LockableScrollViewTest {

    /**
     * Verifies that requestChildFocus on LockableScrollView does not alter scroll position.
     */
    @Test
    public void testRequestChildFocusDoesNotScroll() {
        Context context = ApplicationProvider.getApplicationContext();
        LockableScrollView scrollView = new LockableScrollView(context);
        View child = new View(context);
        scrollView.addView(child);
        int initialScrollY = scrollView.getScrollY();
        scrollView.requestChildFocus(child, child);
        assertEquals(initialScrollY, scrollView.getScrollY());
    }

    /**
     * Verifies that requestChildRectangleOnScreen returns false to suppress automatic scrolling.
     */
    @Test
    public void testRequestChildRectangleOnScreenReturnsFalse() {
        Context context = ApplicationProvider.getApplicationContext();
        LockableScrollView scrollView = new LockableScrollView(context);
        View child = new View(context);
        scrollView.addView(child);
        boolean handled = scrollView.requestChildRectangleOnScreen(child, new Rect(0, 0, 100, 100), true);
        assertFalse(handled);
    }
}
