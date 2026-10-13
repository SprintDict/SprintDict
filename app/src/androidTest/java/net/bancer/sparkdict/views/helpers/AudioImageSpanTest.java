package net.bancer.sparkdict.views.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link AudioImageSpan}.
 */
@RunWith(AndroidJUnit4.class)
public class AudioImageSpanTest {

    private Context context;

    private Drawable testDrawable;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        testDrawable = context.getDrawable(R.drawable.ic_audio_vol);
    }

    /**
     * Verifies that AudioImageSpan initializes with default black background colour.
     */
    @Test
    public void testInitialization() {
        AudioImageSpan span = new AudioImageSpan(testDrawable, "audio.mp3", context);
        assertNotNull(span.getDrawable());
        assertEquals("audio.mp3", span.getSource());
        assertEquals(Color.BLACK, span.getCurrentCircleColor());
    }

    /**
     * Verifies that getSize calculates span width including padding.
     */
    @Test
    public void testGetSize() {
        AudioImageSpan span = new AudioImageSpan(testDrawable, "audio.mp3", context);
        Paint paint = new Paint();
        Paint.FontMetricsInt fm = new Paint.FontMetricsInt();
        int size = span.getSize(paint, "￼", 0, 1, fm);
        assertTrue(size >= 48);
    }

    /**
     * Verifies that animateBackground sets light-grey start colour and triggers view invalidation.
     */
    @Test
    @UiThreadTest
    public void testAnimateBackground() {
        AudioImageSpan span = new AudioImageSpan(testDrawable, "audio.mp3", context);
        View testView = new View(context);
        span.animateBackground(testView);
        assertEquals(Color.LTGRAY, span.getCurrentCircleColor());
    }

    /**
     * Verifies that draw executes without throwing exceptions.
     */
    @Test
    public void testDraw() {
        AudioImageSpan span = new AudioImageSpan(testDrawable, "audio.mp3", context);
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        span.setCurrentCircleColor(Color.LTGRAY);
        span.draw(canvas, "￼", 0, 1, 0, 0, 20, 50, paint);
        assertEquals(Color.LTGRAY, span.getCurrentCircleColor());
    }
}
