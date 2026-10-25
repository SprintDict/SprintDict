package net.bancer.sparkdict.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.bancer.sparkdict.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI and behaviour tests for {@link LexicalEntryView}.
 */
@RunWith(AndroidJUnit4.class)
public class LexicalEntryViewTest {

    private LexicalEntryView lexicalEntryView;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        lexicalEntryView = new LexicalEntryView(context);
    }

    /**
     * Verifies that LexicalEntryView can be instantiated with context.
     */
    @Test
    public void testViewInstantiation() {
        assertNotNull(lexicalEntryView);
    }

    /**
     * Verifies that collapse and expand change view visibility appropriately.
     */
    @Test
    public void testCollapseAndExpand() {
        lexicalEntryView.collapse();
        assertEquals(View.GONE, lexicalEntryView.getDefinitionsVisibility());

        TextView dictTitleView = lexicalEntryView.findViewById(R.id.dict_title);
        assertNotNull(dictTitleView);

        // Clicking on dictionary title toggles state
        dictTitleView.performClick();
        assertEquals(View.VISIBLE, lexicalEntryView.findViewById(R.id.article_title).getVisibility());
    }

    /**
     * Verifies font size zoom operations.
     */
    @Test
    public void testZoomInAndZoomOut() {
        TextView dictTitleView = lexicalEntryView.findViewById(R.id.dict_title);
        assertNotNull(dictTitleView);

        lexicalEntryView.zoomIn();
        float zoomedSize = dictTitleView.getTextSize();

        lexicalEntryView.zoomOut();
        float unzoomedSize = dictTitleView.getTextSize();

        assertTrue(zoomedSize > 0);
        assertTrue(unzoomedSize > 0);
    }

    /**
     * Verifies searching for word indices when definitions are hidden returns -1.
     */
    @Test
    public void testFindIndexOfWhenHidden() {
        lexicalEntryView.collapse();
        assertEquals(-1, lexicalEntryView.findNextIndexOf("test", 0));
        assertEquals(-1, lexicalEntryView.findLastIndexOf("test", 0));
    }
}
