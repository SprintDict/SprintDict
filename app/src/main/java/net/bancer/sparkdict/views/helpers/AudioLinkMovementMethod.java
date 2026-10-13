package net.bancer.sparkdict.views.helpers;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * AudioLinkMovementMethod handles touch events for clickable spans inside definitions text,
 * preventing square selection highlights from being drawn over audio buttons.
 */
public class AudioLinkMovementMethod extends LinkMovementMethod {

    /**
     * Singleton instance of AudioLinkMovementMethod.
     */
    private static AudioLinkMovementMethod instance;

    /**
     * Retrieves the singleton instance of AudioLinkMovementMethod.
     *
     * @return AudioLinkMovementMethod instance.
     */
    public static MovementMethod getInstance() {
        if (instance == null) {
            instance = new AudioLinkMovementMethod();
        }
        return instance;
    }

    /**
     * Handles touch events on clickable spans, removing selection highlights for audio buttons.
     *
     * @param widget the text view receiving the touch event.
     * @param buffer the spannable text containing clickable spans.
     * @param event the touch event to handle.
     * @return true if a clickable span handled the event; otherwise, the result from the superclass.
     */
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            ClickableSpan targetLink = getClickableSpan(widget, buffer, event);
            if (targetLink != null) {
                handleClickableSpan(widget, buffer, targetLink, action);
                return true;
            }
            Selection.removeSelection(buffer);
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    /**
     * Finds the clickable span at the touch event's coordinates.
     *
     * @param widget the text view receiving the touch event.
     * @param buffer the spannable text containing clickable spans.
     * @param event the touch event to process.
     * @return the clickable span at the touch position, or null if none is found.
     */
    private ClickableSpan getClickableSpan(TextView widget, Spannable buffer, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();
        x += widget.getScrollX();
        y += widget.getScrollY();
        Layout layout = widget.getLayout();
        if (layout == null) {
            return null;
        }
        int line = layout.getLineForVertical(y);
        int offset = layout.getOffsetForHorizontal(line, x);
        ClickableSpan[] links = buffer.getSpans(offset, offset, ClickableSpan.class);
        if (links.length == 0) {
            return null;
        }
        return links[0];
    }

    /**
     * Handles a touch event on a clickable span.
     *
     * @param widget the text view containing the span.
     * @param buffer the spannable text containing the span.
     * @param targetLink the clickable span to handle.
     * @param action the touch event action.
     */
    private void handleClickableSpan(
        TextView widget,
        Spannable buffer,
        ClickableSpan targetLink,
        int action
    ) {
        if (action == MotionEvent.ACTION_UP) {
            targetLink.onClick(widget);
        } else if (action == MotionEvent.ACTION_DOWN) {
            if (targetLink instanceof UnrecognizedTagsHandler.AudioButtonSpan) {
                Selection.removeSelection(buffer);
            } else {
                int start = buffer.getSpanStart(targetLink);
                int stop = buffer.getSpanEnd(targetLink);
                Selection.setSelection(buffer, start, stop);
            }
        }
    }
}
