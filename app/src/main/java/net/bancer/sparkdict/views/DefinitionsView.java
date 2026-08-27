package net.bancer.sparkdict.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.widget.EditText;
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
public class DefinitionsView extends EditText {

    private static final int BLOCKQUOTE_INDENT = 40;

    /**
     * Focused word background colour.
     */
    private BackgroundColorSpan focusedWordBackground;

    /**
     * Colour state list used for highlighting.
     */
    private ColorStateList colorStateList;

    /**
     * Constructor.
     *
     * @param context application context.
     */
    public DefinitionsView(Context context) {
        super(context);
    }

    /**
     * Constructor.
     *
     * @param context application context.
     * @param attrs   view attributes.
     */
    public DefinitionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        getText().removeSpan(getFocusedWordBackground());
    }

    /**
     * Focuses on the specific portion of the definitions and highlights it.
     *
     * @param start selection start position.
     * @param end   selection end position.
     */
    public void requestFocusAt(int start, int end) {
        setSelection(start, end);
        getText().setSpan(
            getFocusedWordBackground(),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
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
            getText().setSpan(bgColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            getText().setSpan(txtAppearanceSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
     * Parses the HTML definitions of the specified lexical entry and sets the
     * resulting formatted text as the content of this view.
     *
     * <p>The HTML content is converted into a {@link Spanned} object using
     * {@link Html#fromHtml(String, int, Html.ImageGetter, Html.TagHandler)}.
     * Dictionary images are resolved by {@link DictResourceImageGetter}, custom
     * tags are handled by {@link UnrecognizedTagsHandler}, and blockquotes are
     * adjusted to use indentation instead of Android's default quote styling.</p>
     *
     * @param lexicalEntry lexical entry containing the HTML definitions to display.
     */
    public void parseHtmlAndSetText(final LexicalEntry lexicalEntry) {
        String html = lexicalEntry.getDefinitions();
        int maxImageWidth = computeMaxImageWidth();
        Resources resources = getContext().getResources();
        Html.ImageGetter imageGetter = new DictResourceImageGetter(lexicalEntry, maxImageWidth, resources);
        Html.TagHandler tagHandler = new UnrecognizedTagsHandler(lexicalEntry, getContext());
        Spanned parsedHtml = Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler);
        SpannableStringBuilder finalizedHtml = indentBlockquotes(parsedHtml);
        setText(finalizedHtml, TextView.BufferType.SPANNABLE);
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
}
