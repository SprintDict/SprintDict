package net.bancer.sparkdict.views.helpers;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * AudioImageSpan renders the audio icon (ic_audio_vol) and manages a circular background
 * animation that fades from light-grey back to black over 1 second when tapped.
 */
public class AudioImageSpan extends ReplacementSpan {

    /**
     * Default padding around the icon in density-independent pixels (dp).
     */
    private static final float PADDING_DP = 4.0f;

    /**
     * Animation duration in milliseconds.
     */
    private static final long ANIMATION_DURATION_MS = 1000L;

    /**
     * Start background colour for the click animation (light grey).
     */
    private static final int START_COLOR = Color.LTGRAY;

    /**
     * End background colour for the click animation (black).
     */
    private static final int END_COLOR = Color.BLACK;

    /**
     * Drawable representing the audio icon.
     */
    private final Drawable drawable;

    /**
     * Resource source name or URL associated with this audio span.
     */
    private final String source;

    /**
     * Paint used to draw the circular background.
     */
    private final Paint circlePaint;

    /**
     * Padding around the icon in pixels.
     */
    private final float paddingPx;

    /**
     * Current colour of the circular background.
     */
    private int currentCircleColor;

    /**
     * ValueAnimator handling the circular background colour transition.
     */
    private ValueAnimator animator;

    /**
     * Constructor.
     *
     * @param drawable audio icon drawable.
     * @param source   audio resource source name.
     * @param context  application context.
     */
    public AudioImageSpan(@NonNull Drawable drawable, @Nullable String source, @NonNull Context context) {
        this.drawable = drawable;
        this.source = source;
        this.currentCircleColor = END_COLOR;
        this.circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.circlePaint.setStyle(Paint.Style.FILL);
        float density = context.getResources().getDisplayMetrics().density;
        this.paddingPx = PADDING_DP * density;
    }

    /**
     * Returns the audio resource source name or URL.
     *
     * @return resource source name string.
     */
    @Nullable
    public String getSource() {
        return source;
    }

    /**
     * Returns the audio icon drawable.
     *
     * @return audio icon drawable instance.
     */
    @NonNull
    public Drawable getDrawable() {
        return drawable;
    }

    /**
     * Returns the current circular background colour.
     *
     * @return current circle colour integer ARGB value.
     */
    public int getCurrentCircleColor() {
        return currentCircleColor;
    }

    /**
     * Sets the current circular background colour directly (mainly used for testing).
     *
     * @param color ARGB colour value.
     */
    public void setCurrentCircleColor(int color) {
        this.currentCircleColor = color;
    }

    /**
     * Triggers the circular background animation, transitioning from light-grey back to black over 1 second.
     *
     * @param view target view to invalidate during animation frames.
     */
    public void animateBackground(@Nullable View view) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        currentCircleColor = START_COLOR;
        refreshSpan(view);
        animator = ValueAnimator.ofArgb(START_COLOR, END_COLOR);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.addUpdateListener(animation -> {
            currentCircleColor = (int) animation.getAnimatedValue();
            refreshSpan(view);
        });
        animator.start();
    }

    /**
     * Refreshes this span in the text and invalidates the view.
     *
     * @param view target view to refresh.
     */
    private void refreshSpan(@Nullable View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (textView.getText() instanceof Spannable) {
                Spannable spannable = (Spannable) textView.getText();
                int start = spannable.getSpanStart(this);
                int end = spannable.getSpanEnd(this);
                if (start >= 0 && end >= start) {
                    spannable.setSpan(this, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        if (view != null) {
            view.invalidate();
        }
    }

    /**
     * Calculates the width of the span and adjusts font metrics to accommodate
     * the audio icon and its circular background.
     *
     * @param paint       paint used to measure the text.
     * @param text        text containing the span.
     * @param start       start index of the span in the text.
     * @param end         end index of the span in the text.
     * @param fontMetrics font metrics to adjust, or null if not required.
     * @return width of the span in pixels, including padding.
     */
    @Override
    public int getSize(
        @NonNull Paint paint,
        CharSequence text,
        int start,
        int end,
        @Nullable Paint.FontMetricsInt fontMetrics
    ) {
        int iconWidth = drawable.getIntrinsicWidth();
        int iconHeight = drawable.getIntrinsicHeight();
        float radius = Math.max(iconWidth, iconHeight) / 2.0f + paddingPx;
        int diameter = Math.round(radius * 2.0f);
        if (fontMetrics != null) {
            int descent = paint.getFontMetricsInt().descent;
            int topNeeded = Math.round(descent - iconHeight / 2.0f - radius);
            int bottomNeeded = Math.round(descent - iconHeight / 2.0f + radius);
            if (fontMetrics.ascent > topNeeded) {
                fontMetrics.ascent = topNeeded;
            }
            if (fontMetrics.top > topNeeded) {
                fontMetrics.top = topNeeded;
            }
            if (fontMetrics.descent < bottomNeeded) {
                fontMetrics.descent = bottomNeeded;
            }
            if (fontMetrics.bottom < bottomNeeded) {
                fontMetrics.bottom = bottomNeeded;
            }
        }
        return diameter;
    }

    /**
     * Draws the circular background and audio icon on the canvas, vertically
     * aligned with the text baseline.
     *
     * @param canvas canvas on which to draw the span.
     * @param text   text containing the span.
     * @param start  start index of the span in the text.
     * @param end    end index of the span in the text.
     * @param x      horizontal position at which to draw the span.
     * @param top    top position of the line containing the span.
     * @param y      text baseline.
     * @param bottom bottom position of the line containing the span.
     * @param paint  paint used to draw the text.
     */
    @Override
    public void draw(
        @NonNull Canvas canvas,
        CharSequence text,
        int start,
        int end,
        float x,
        int top,
        int y,
        int bottom,
        @NonNull Paint paint
    ) {
        int iconWidth = drawable.getIntrinsicWidth();
        int iconHeight = drawable.getIntrinsicHeight();
        float radius = Math.max(iconWidth, iconHeight) / 2.0f + paddingPx;
        float diameter = radius * 2.0f;
        int descent = paint.getFontMetricsInt().descent;
        float iconBottom = y + descent;
        float iconTop = iconBottom - iconHeight;
        float centerY = iconTop + iconHeight / 2.0f;
        float centerX = x + diameter / 2.0f;
        if (currentCircleColor != Color.BLACK) {
            circlePaint.setColor(currentCircleColor);
            canvas.drawCircle(centerX, centerY, radius, circlePaint);
        }
        float iconLeft = centerX - iconWidth / 2.0f;
        drawable.setBounds(0, 0, iconWidth, iconHeight);
        canvas.save();
        canvas.translate(iconLeft, iconTop);
        drawable.draw(canvas);
        canvas.restore();
    }
}
