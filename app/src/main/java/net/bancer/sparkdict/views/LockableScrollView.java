package net.bancer.sparkdict.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * LockableScrollView extends {@link ScrollView} to suppress automatic focus-driven
 * scrolling when child views gain focus, preventing unexpected viewport jumping.
 */
public class LockableScrollView extends ScrollView {

    /**
     * Constructor.
     *
     * @param context application context.
     */
    public LockableScrollView(Context context) {
        super(context);
    }

    /**
     * Constructor.
     *
     * @param context application context.
     * @param attrs   view attributes.
     */
    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor.
     *
     * @param context  application context.
     * @param attrs    view attributes.
     * @param defStyle default style to apply to this view.
     */
    public LockableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Updates view group focus state while suppressing automatic scrolling when a child view
     * gains focus to prevent unexpected viewport jumping when audio icons or links are tapped.
     *
     * @param child   child view requesting focus.
     * @param focused focused view.
     */
    @Override
    public void requestChildFocus(View child, View focused) {
        int oldX = getScrollX();
        int oldY = getScrollY();
        super.requestChildFocus(child, focused);
        scrollTo(oldX, oldY);
    }

    /**
     * Suppresses automatic rectangle scrolling when a child view requests a rectangle on screen.
     *
     * @param child     child view requesting rectangle.
     * @param rectangle target rectangle in child coordinates.
     * @param immediate {@code true} for immediate scrolling.
     * @return {@code false} to suppress automated scrolling.
     */
    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        return false;
    }
}
