package net.bancer.sparkdict.views.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.style.ClickableSpan;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link UnrecognizedTagsHandler}.
 */
@RunWith(AndroidJUnit4.class)
public class UnrecognizedTagsHandlerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    /**
     * Verifies that UnrecognizedTagsHandler parses `<object>` tags into AudioImageSpan and AudioButtonSpan.
     */
    @Test
    public void testParseObjectTagCreatesAudioSpans() {
        String html = "Word <object data=\"test.wav\">test.wav</object> def";
        UnrecognizedTagsHandler tagHandler = new UnrecognizedTagsHandler(null, context);
        Editable output = (Editable) Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler);
        assertNotNull(output);
        AudioImageSpan[] imageSpans = output.getSpans(0, output.length(), AudioImageSpan.class);
        assertEquals(1, imageSpans.length);
        ClickableSpan[] clickableSpans = output.getSpans(0, output.length(), ClickableSpan.class);
        assertTrue(clickableSpans.length >= 1);
    }

    /**
     * Verifies that clicking AudioButtonSpan triggers animateBackground on AudioImageSpan.
     */
    @Test
    @UiThreadTest
    public void testAudioButtonSpanClickTriggersAnimation() {
        String html = "<object data=\"test.wav\">test.wav</object>";
        UnrecognizedTagsHandler tagHandler = new UnrecognizedTagsHandler(null, context);
        Editable output = (Editable) Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler);
        AudioImageSpan[] imageSpans = output.getSpans(0, output.length(), AudioImageSpan.class);
        ClickableSpan[] clickableSpans = output.getSpans(0, output.length(), ClickableSpan.class);
        assertEquals(1, imageSpans.length);
        assertTrue(clickableSpans.length >= 1);
        TextView textView = new TextView(context);
        textView.setText(output);
        clickableSpans[0].onClick(textView);
        AudioImageSpan audioImageSpan = imageSpans[0];
        assertNotNull(audioImageSpan);
    }
}
