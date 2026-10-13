package net.bancer.sparkdict.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;

import androidx.core.text.HtmlCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.views.helpers.AudioImageSpan;
import net.bancer.sparkdict.views.helpers.UnrecognizedTagsHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration and UI behaviour tests for {@link DefinitionsView}.
 */
@RunWith(AndroidJUnit4.class)
public class DefinitionsViewTest {

    private Context context;

    private DefinitionsView definitionsView;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        definitionsView = new DefinitionsView(context);
    }

    /**
     * Verifies that DefinitionsView is created with text selection enabled.
     */
    @Test
    public void testDefinitionsViewSelectionEnabled() {
        assertNotNull(definitionsView);
        assertTrue(definitionsView.isTextSelectable());
    }

    /**
     * Verifies that requesting focus at a specific range sets text selection on DefinitionsView.
     */
    @Test
    public void testRequestFocusAtSetsSelection() {
        String html = "Hello <object data=\"audio.mp3\">audio.mp3</object> World";
        UnrecognizedTagsHandler tagHandler = new UnrecognizedTagsHandler(null, context);
        Editable parsed = (Editable) Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler);
        definitionsView.setText(parsed, android.widget.TextView.BufferType.SPANNABLE);
        definitionsView.requestFocusAt(0, 5);
        if (definitionsView.getText() instanceof Spannable) {
            Spannable spannable = (Spannable) definitionsView.getText();
            int start = Selection.getSelectionStart(spannable);
            int end = Selection.getSelectionEnd(spannable);
            assertTrue(start >= 0);
            assertTrue(end > start);
        }
    }

    /**
     * Verifies that audio icon spans are rendered inside DefinitionsView without throwing errors.
     */
    @Test
    public void testParsedHtmlWithAudioSpanSetOnDefinitionsView() {
        String html = "bat <object data=\"bat_uk.mp3\">bat_uk.mp3</object> definition";
        UnrecognizedTagsHandler tagHandler = new UnrecognizedTagsHandler(null, context);
        Editable parsed = (Editable) Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler);
        definitionsView.setText(parsed, android.widget.TextView.BufferType.SPANNABLE);
        if (definitionsView.getText() instanceof Spannable) {
            Spannable spannable = (Spannable) definitionsView.getText();
            AudioImageSpan[] spans = spannable.getSpans(0, spannable.length(), AudioImageSpan.class);
            assertTrue(spans.length > 0);
        }
    }
}
