package net.bancer.sparkdict.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * LockableScrollView extends {@link ScrollView} to suppress automatic focus-driven
 * or child-rectangle-driven scrolling while preserving user touch scrolling.
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

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        return false;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        // Suppress automatic scrolling when a child view (e.g. DefinitionsView on long press) gains focus
    }
}
