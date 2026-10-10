package net.bancer.sparkdict.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.views.helpers.DictResourceImageGetter;
import net.bancer.sparkdict.views.helpers.UnrecognizedTagsHandler;

/**
 * DefinitionsView displays definitions of the lexical entry and performs
 * different transformations of them.
 */
@SuppressLint("AppCompatCustomView")
public class DefinitionsView extends TextView {

    private static final int BLOCKQUOTE_INDENT = 10;

    /**
     * Context menu item identifier for searching selected text in the app.
     */
    public static final int ID_SEARCH = 1;

    /**
     * Context menu item identifier for finding selected text on the current page.
     */
    public static final int ID_FIND_ON_PAGE = 2;

    /**
     * Context menu item identifier for copying selected text to clipboard.
     */
    public static final int ID_COPY = 3;

    /**
     * Context menu item identifier for sharing selected text with other apps.
     */
    public static final int ID_SHARE = 4;

    /**
     * Focused word background colour.
     */
    private BackgroundColorSpan focusedWordBackground;

    /**
     * Colour state list used for highlighting.
     */
    private ColorStateList colorStateList;

    /**
     * Original definitions text without any highlighting.
     */
    private Spanned originalText;

    /**
     * Interface for handling selection context menu actions.
     */
    public interface SelectionActionListener {

        /**
         * Triggered when the user selects the "Search" context menu item for selected text.
         *
         * @param selectedText text selected by the user.
         */
        void onSearchSelected(String selectedText);

        /**
         * Triggered when the user selects the "Find on page" context menu item for selected text.
         *
         * @param selectedText text selected by the user.
         */
        void onFindOnPageSelected(String selectedText);

        /**
         * Triggered when the user selects the "Copy" context menu item for selected text.
         *
         * @param selectedText text selected by the user.
         */
        void onCopySelected(String selectedText);

        /**
         * Triggered when the user selects the "Share" context menu item for selected text.
         *
         * @param selectedText text selected by the user.
         */
        void onShareSelected(String selectedText);
    }

    /**
     * Constructor.
     *
     * @param context application context.
     */
    public DefinitionsView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor.
     *
     * @param context application context.
     * @param attrs   view attributes.
     */
    public DefinitionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor.
     *
     * @param context  application context.
     * @param attrs    view attributes.
     * @param defStyle default style to apply to this view.
     */
    public DefinitionsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialises view state, enables text selection, and registers the custom action mode callback.
     */
    private void init() {
        setTextIsSelectable(true);
        setCustomSelectionActionModeCallback(new CustomSelectionActionModeCallback());
    }

    /**
     * Prevents the soft keyboard from displaying when this view receives focus or is tapped,
     * as definitions text is read-only and non-editable.
     *
     * @return {@code false} to indicate this view is not an editable text editor.
     */
    @Override
    public boolean onCheckIsTextEditor() {
        return false;
    }

    /**
     * Retrieve the background colour of the focused word.
     *
     * @return    background colour object of the focused word.
     */
    public BackgroundColorSpan getFocusedWordBackground() {
        if (focusedWordBackground == null) {
            focusedWordBackground = new BackgroundColorSpan(Color.WHITE);
        }
        return focusedWordBackground;
    }

    /**
     * Removes highlighting of the focused word.
     */
    public void removeFocusedWordBackground() {
        if (getText() instanceof Spannable) {
            ((Spannable) getText()).removeSpan(getFocusedWordBackground());
        }
    }

    /**
     * Focuses on the specific portion of the definitions, highlights it,
     * and scrolls the line containing the word into view.
     *
     * @param start selection start position.
     * @param end   selection end position.
     */
    public void requestFocusAt(int start, int end) {
        if (getText() instanceof Spannable) {
            Spannable spannable = (Spannable) getText();
            Selection.setSelection(spannable, start, end);
            spannable.setSpan(
                getFocusedWordBackground(),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            if (getLayout() != null) {
                int line = getLayout().getLineForOffset(start);
                Rect rect = new Rect();
                getLayout().getLineBounds(line, rect);
                scrollToLine(rect.top);
            }
        }
    }

    /**
     * Scrolls the parent ScrollView to bring the specified vertical line position into view.
     *
     * @param lineTop top Y coordinate of the target line relative to this view.
     */
    private void scrollToLine(int lineTop) {
        ViewParent parent = getParent();
        int y = lineTop;
        while (parent != null && !(parent instanceof ScrollView)) {
            if (parent instanceof View) {
                y += ((View) parent).getTop();
                parent = parent.getParent();
            } else {
                break;
            }
        }
        if (parent instanceof ScrollView) {
            final ScrollView scrollView = (ScrollView) parent;
            final int targetY = Math.max(0, y - 200);
            scrollView.post(() -> scrollView.smoothScrollTo(0, targetY));
        }
    }

    /**
     * Highlights all occurrences of the word in the lexical entry view.
     *
     * @param word word to be highlighted.
     * @return        `true` if at least one word was highlighted, else `false`.
     */
    public boolean highlightAllInstancesOfWord(String word) {
        boolean atLeastOneHighlighted = false;
        int wordLength = word.length();
        String lowerCaseWord = word.toLowerCase();
        String def = getText().toString().toLowerCase();
        int start = def.indexOf(lowerCaseWord);
        int end;
        while (start != -1) {
            atLeastOneHighlighted = true;
            end = start + wordLength;
            BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(Color.YELLOW);
            TextAppearanceSpan txtAppearanceSpan = new TextAppearanceSpan(
                null,
                Typeface.NORMAL,
                (int) getTextSize(),
                getColorStateList(),
                null
            );
            if (getText() instanceof Spannable) {
                Spannable spannable = (Spannable) getText();
                spannable.setSpan(bgColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(txtAppearanceSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            start = def.indexOf(lowerCaseWord, end);
        }
        return atLeastOneHighlighted;
    }

    /**
     * Colour state list getter.
     *
     * @return    colour state list object.
     */
    private ColorStateList getColorStateList() {
        if (colorStateList == null) {
            colorStateList = getContext().getColorStateList(R.color.highlighted_text_color_list);
        }
        return colorStateList;
    }

    /**
     * Parses the HTML definitions of the specified lexical entry.
     *
     * <p>The HTML content is converted into a {@link Spanned} object using
     * {@link Html#fromHtml(String, int, Html.ImageGetter, Html.TagHandler)}.
     * Dictionary images are resolved by {@link DictResourceImageGetter}, custom
     * tags are handled by {@link UnrecognizedTagsHandler}, and blockquotes are
     * adjusted to use indentation instead of Android's default quote styling.</p>
     *
     * @param lexicalEntry lexical entry containing the HTML definitions to display.
     * @return parsed definition.
     */
    public Spanned parseHtml(final LexicalEntry lexicalEntry) {
        String html = lexicalEntry.getDefinitions();
        int maxImageWidth = computeMaxImageWidth();
        Resources resources = getContext().getResources();
        Html.ImageGetter imageGetter = new DictResourceImageGetter(lexicalEntry, maxImageWidth, resources);
        Html.TagHandler tagHandler = new UnrecognizedTagsHandler(lexicalEntry, getContext());
        Spanned parsedHtml = Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler);
        return indentBlockquotes(parsedHtml);
    }

    /**
     * Sets a previously parsed definition as the content of this view.
     *
     * @param parsedHtml parsed definition.
     */
    public void setParsedHtml(Spanned parsedHtml) {
        originalText = parsedHtml;
        setText(parsedHtml, BufferType.SPANNABLE);
    }

    /**
     * Restores original definitions text without any highlighting.
     */
    public void restoreOriginalText() {
        if (originalText != null) {
            setText(originalText, BufferType.SPANNABLE);
        }
    }

    /**
     * Computes the maximum width available for dictionary images.
     *
     * <p>The width is limited by both the available screen width and the
     * remaining vertical space below the search bar. Additional horizontal
     * space is reserved for blockquote indentation to prevent images inside
     * nested blockquotes from overflowing.</p>
     *
     * @return maximum image width in pixels.
     */
    private int computeMaxImageWidth() {
        int approxSearchBarHeight = 400;
        int height = getResources().getDisplayMetrics().heightPixels - approxSearchBarHeight;
        // Reserve space for up to four nested blockquotes.
        int width = getResources().getDisplayMetrics().widthPixels - BLOCKQUOTE_INDENT * 4;
        return Math.min(height, width);
    }

    /**
     * Replaces Android's default blockquote rendering with an indented layout.
     *
     * <p>{@link Html#fromHtml(String, int, Html.ImageGetter, Html.TagHandler)}
     * converts {@code <blockquote>} tags into {@link QuoteSpan} instances, which
     * render as a vertical line. This method removes those spans and replaces
     * them with {@link LeadingMarginSpan.Standard} instances to display
     * blockquotes with a left indentation instead.</p>
     *
     * @param parsedHtml parsed HTML content containing possible blockquote spans.
     * @return a modifiable spannable text with blockquotes indented.
     */
    private SpannableStringBuilder indentBlockquotes(Spanned parsedHtml) {
        SpannableStringBuilder builder = new SpannableStringBuilder(parsedHtml);
        QuoteSpan[] quotes = builder.getSpans(0, builder.length(), QuoteSpan.class);
        for (QuoteSpan quote : quotes) {
            int start = builder.getSpanStart(quote);
            int end = builder.getSpanEnd(quote);
            int flags = builder.getSpanFlags(quote);
            builder.removeSpan(quote);
            LeadingMarginSpan.Standard blockquoteMargin = new LeadingMarginSpan.Standard(BLOCKQUOTE_INDENT);
            builder.setSpan(blockquoteMargin, start, end, flags);
        }
        return builder;
    }

    /**
     * Retrieves the active selection action listener from the host context if implemented.
     *
     * @return the selection action listener instance or null.
     */
    private SelectionActionListener getSelectionActionListener() {
        if (getContext() instanceof SelectionActionListener) {
            return (SelectionActionListener) getContext();
        }
        return null;
    }

    /**
     * Dispatches a selection context menu action to the registered selection action listener.
     *
     * @param itemId       identifier of the selected menu item.
     * @param selectedText text selected by the user.
     */
    private void handleSelectionAction(int itemId, String selectedText) {
        SelectionActionListener listener = getSelectionActionListener();
        if (listener != null) {
            if (itemId == ID_SEARCH) {
                listener.onSearchSelected(selectedText);
            } else if (itemId == ID_FIND_ON_PAGE) {
                listener.onFindOnPageSelected(selectedText);
            } else if (itemId == ID_COPY) {
                listener.onCopySelected(selectedText);
            } else if (itemId == ID_SHARE) {
                listener.onShareSelected(selectedText);
            }
        }
    }

    /**
     * Custom {@link ActionMode.Callback} handling text selection context menu creation and item invocation.
     */
    private class CustomSelectionActionModeCallback implements ActionMode.Callback {

        /**
         * Called when the action mode is created; populates the custom context menu.
         *
         * @param mode action mode being created.
         * @param menu menu to populate.
         * @return {@code true} to indicate the menu was created.
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            populateMenu(menu);
            return true;
        }

        /**
         * Called when the action mode is prepared; populates the custom context menu.
         *
         * @param mode action mode being prepared.
         * @param menu menu to populate.
         * @return {@code true} to indicate the menu was prepared.
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            populateMenu(menu);
            return true;
        }

        /**
         * Clears default selection action items and adds custom items:
         * Search, Find on page, Copy, Share.
         *
         * @param menu menu to populate.
         */
        private void populateMenu(Menu menu) {
            menu.clear();
            menu.add(Menu.NONE, ID_SEARCH, 1, getContext().getString(R.string.search));
            menu.add(Menu.NONE, ID_FIND_ON_PAGE, 2, getContext().getString(R.string.menu_find_on_page));
            menu.add(Menu.NONE, ID_COPY, 3, getContext().getString(R.string.context_menu_copy));
            menu.add(Menu.NONE, ID_SHARE, 4, getContext().getString(R.string.context_menu_share));
        }

        /**
         * Triggered when a context menu item is clicked.
         * Retrieves the selected text and executes the corresponding action.
         *
         * @param mode action mode containing the menu item.
         * @param item menu item clicked.
         * @return {@code true} if the action was handled, otherwise {@code false}.
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int start = Math.min(getSelectionStart(), getSelectionEnd());
            int end = Math.max(getSelectionStart(), getSelectionEnd());
            String selectedText = "";
            if (start >= 0 && end > start && getText() != null) {
                selectedText = getText().subSequence(start, end).toString().trim();
            }
            mode.finish();
            if (!selectedText.isEmpty()) {
                handleSelectionAction(item.getItemId(), selectedText);
                return true;
            }
            return false;
        }

        /**
         * Called when the action mode is destroyed.
         *
         * @param mode action mode being destroyed.
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
