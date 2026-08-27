package net.bancer.sparkdict.views;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.bancer.sparkdict.BaseActivity;
import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.LexicalEntry;

/**
 * LexicalEntryView displays all parts of the lexical entry and handles
 * different transformations of them.
 */
public class LexicalEntryView extends LinearLayout implements
    View.OnFocusChangeListener, View.OnTouchListener {

    /**
     * Factor by which the font size is changed after zoom in/out operation.
     */
    private static final float TEXT_SCALE_FACTOR = 1.1f;

    private static final int MIN_TEXT_SIZE = 12;

    private static final int MAX_TEXT_SIZE = 60;

    /**
     * Lexical entry.
     */
    private LexicalEntry lexicalEntry;

    /**
     * Dictionary title view.
     */
    private TextView dictTitleView;

    /**
     * Lemma view.
     */
    private TextView lemmaView;

    /**
     * Definitions view.
     */
    private DefinitionsView definitionsView;

    /**
     * An image visually indicating whether the definitions of the lexical entry
     * are expanded or collapsed.
     */
    private ImageView expanderView;

    /**
     * Flag to indicate if the procedure of highlighting a word that was
     * searched on the screen was performed.
     */
    private boolean highlightingDone;

    /**
     * Constructor.
     *
     * @param context application context.
     */
    public LexicalEntryView(Context context) {
        super(context);
    }

    /**
     * Constructor.
     *
     * @param context application context.
     * @param attrs   view attributes.
     */
    public LexicalEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Default constructor.
     *
     * @param context      application context.
     * @param lexicalEntry lexical entry domain object.
     */
    public LexicalEntryView(Context context, LexicalEntry lexicalEntry) {
        super(context);
        init();
        this.lexicalEntry = lexicalEntry;
        dictTitleView.setText(lexicalEntry.getDictTitle());
        lemmaView.setText(lexicalEntry.getLemma());
        definitionsView.parseHtmlAndSetText(lexicalEntry);
    }

    /**
     * Initialisation of the view.
     */
    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.lexical_entry, this, true);

        expanderView = findViewById(R.id.expander_icon);
        expanderView.setOnTouchListener(this);

        dictTitleView = findViewById(R.id.dict_title);
        dictTitleView.setOnTouchListener(this);

        lemmaView = findViewById(R.id.article_title);
        lemmaView.setOnTouchListener(this);

        definitionsView = findViewById(R.id.definitions_body);
        definitionsView.setOnClickListener((OnClickListener) getContext());
        definitionsView.setOnFocusChangeListener(this);
        definitionsView.setOnTouchListener(this);
        // make links clickable
        definitionsView.setMovementMethod(LinkMovementMethod.getInstance());//Causes problems with action bar for editing/copying/pasting

        restoreTextSizeFromPreferences();
    }

    /**
     * Retrieves text sizes of the dictionary's title, article's title and
     * article's from shared preferences and applies them to the current view.
     */
    private void restoreTextSizeFromPreferences() {
        BaseActivity baseActivity = (BaseActivity) getContext();
        int dictTitleSize = (int) baseActivity.getFloatSharedPreference(getContext().getString(R.string.pref_dict_title_font_size));
        int articleTitleSize = (int) baseActivity.getFloatSharedPreference(getContext().getString(R.string.pref_article_title_font_size));
        int definitionsSize = (int) baseActivity.getFloatSharedPreference(getContext().getString(R.string.pref_definitions_font_size));
        setTextSizes(dictTitleSize, articleTitleSize, definitionsSize);
    }

    /**
     * Saves text sizes of the dictionary's title, article's title and article's
     * body into shared preferences.
     */
    private void saveFontSizesToSharedPreferences() {
        BaseActivity baseActivity = (BaseActivity) getContext();
        TextView dictTitleView = findViewById(R.id.dict_title);
        if (dictTitleView != null) {
            baseActivity.saveSharedPreference(getContext().getString(R.string.pref_dict_title_font_size), dictTitleView.getTextSize());
        }
        TextView articleTitleView = findViewById(R.id.article_title);
        if (articleTitleView != null) {
            baseActivity.saveSharedPreference(getContext().getString(R.string.pref_article_title_font_size), articleTitleView.getTextSize());
        }
        TextView definitionsView = findViewById(R.id.definitions_body);
        if (definitionsView != null) {
            baseActivity.saveSharedPreference(getContext().getString(R.string.pref_definitions_font_size), definitionsView.getTextSize());
        }
    }

    /**
     * Increases article's text size.
     */
    public void zoomIn() {
        transformTextSizes(TEXT_SCALE_FACTOR);
    }

    /**
     * Decreases article's text size.
     */
    public void zoomOut() {
        transformTextSizes(1 / TEXT_SCALE_FACTOR);
    }

    /**
     * Transforms article's text size. If `scaleFactor` is more than 1 the text
     * size increases, if it is less than 1 then decreases.
     *
     * @param scaleFactor Float value multiplying by which the text size must be change.
     */
    private void transformTextSizes(float scaleFactor) {
        int dictionaryTitleSize = (int) (dictTitleView.getTextSize() * scaleFactor);
        int articleTitleSize = (int) (lemmaView.getTextSize() * scaleFactor);
        int bodyTextSize = (int) (definitionsView.getTextSize() * scaleFactor);
        setTextSizes(dictionaryTitleSize, articleTitleSize, bodyTextSize);
        saveFontSizesToSharedPreferences();
    }

    /**
     * Sets the text size of dictionary title, article title and article body.
     * All parameters values are interpreted to be raw pixels.
     *
     * @param dictionaryTitleSize font size of the dictionary title.
     * @param articleTitleSize    font size of the article title.
     * @param bodyTextSize        font size of the article body.
     */
    private void setTextSizes(int dictionaryTitleSize, int articleTitleSize, int bodyTextSize) {
        if (MIN_TEXT_SIZE <= dictionaryTitleSize && dictionaryTitleSize <= MAX_TEXT_SIZE) {
            dictTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dictionaryTitleSize);
        }
        if (MIN_TEXT_SIZE <= articleTitleSize && articleTitleSize <= MAX_TEXT_SIZE) {
            lemmaView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTitleSize);
        }
        if (MIN_TEXT_SIZE <= bodyTextSize && bodyTextSize <= MAX_TEXT_SIZE) {
            definitionsView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize);
        }
    }

    /**
     * Sets the provided view to be focusable in all modes including touch mode
     * and requests focus on that view.
     *
     * @param view view to be focused.
     * @return        `true` when the view took focus, else `false`.
     */
    private boolean requestFocusAt(EditText view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return view.requestFocus();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            ((EditText) v).setCursorVisible(false);
        }
    }

    /**
     * Expands/collapses definitions of this lexical entry after a touch on the
     * dictionary title.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();
        if (v.getId() == R.id.dict_title || v.getId() == R.id.expander_icon) {
            if (action == MotionEvent.ACTION_UP) {
                if (definitionsView.getVisibility() == View.VISIBLE) {
                    collapse();
                } else if (definitionsView.getVisibility() == View.GONE) {
                    expand();
                }
            }
        }
        return false;
    }

    /**
     * Collapses definitions of this lexical entry.
     */
    void collapse() {
        expanderView.setImageResource(R.drawable.expander_ic_minimized);
        lemmaView.setVisibility(View.GONE);
        definitionsView.setVisibility(View.GONE);
    }

    /**
     * Expands definitions of this lexical entry.
     */
    void expand() {
        expanderView.setImageResource(R.drawable.expander_ic_maximized);
        lemmaView.setVisibility(View.VISIBLE);
        definitionsView.setVisibility(View.VISIBLE);
    }

    /**
     * Highlights all occurrences of the word in this lexical entry.
     *
     * @param word word to be found and highlighted.
     * @return        `true` if at least one word occurrence was found.
     */
    public boolean highlightWord(String word) {
        if (definitionsView.getVisibility() == View.VISIBLE) {
            highlightingDone = true;
            return definitionsView.highlightAllInstancesOfWord(word);
        } else {
            return false;
        }
    }

    /**
     * Removes highlighting from all highlighted words in this lexical entry.
     */
    public void removeHighlighting() {
        highlightingDone = false;
        definitionsView.parseHtmlAndSetText(lexicalEntry);
    }

    /**
     * Focuses on the specific position of this view and highlights the focused
     * string.
     *
     * @param start start position of the focus.
     * @param end   end position of the focus.
     * @return            `true` when the view took focus, else `false`.
     */
    public boolean requestFocusAtWord(int start, int end) {
        if (!highlightingDone) {
            String word = definitionsView.getText().toString().substring(start, end);
            highlightWord(word);
        }
        definitionsView.requestFocusAt(start, end);
        return requestFocusAt(definitionsView);
    }

    /**
     * Removes highlighting of the focused word found in this lexical entry.
     */
    public void removeFocusedWordBackground() {
        definitionsView.removeFocusedWordBackground();
    }

    /**
     * Searches in the definitions view text for the index of a word or phrase
     * provided as the `word` parameter. The search of the string starts from
     * the specified offset and moves towards the end of this string..
     *
     * @param word  word or phrase to find.
     * @param start the starting offset.
     * @return the index of the first character of the specified string
     * in this string, -1 if the specified string is not a substring.
     */
    public int findNextIndexOf(String word, int start) {
        if (definitionsView.getVisibility() == View.VISIBLE) {
            return definitionsView.getText().toString().indexOf(word, start);
        } else {
            return -1;
        }
    }

    /**
     * Searches in the definitions view text for the last index of a word or phrase
     * provided as the `word` parameter. The search of the string starts from
     * the specified offset and moves towards the beginning of this string..
     *
     * @param word  word or phrase to find.
     * @param start the starting offset.
     * @return            the index of the first character of the specified string
     * in this string, -1 if the specified string is not a substring.
     */
    public int findLastIndexOf(String word, int start) {
        if (definitionsView.getVisibility() == View.VISIBLE) {
            return definitionsView.getText().toString().lastIndexOf(word, start);
        } else {
            return -1;
        }
    }

    /**
     * Retrieves the length of the definitions text.
     *
     * @return    the length of the definitions text.
     */
    public int getDefinitionsLength() {
        return definitionsView.getText().toString().length();
    }

    /**
     * Retrieves definitions visibility field.
     *
     * @return    definitions visibility field.
     */
    public int getDefinitionsVisibility() {
        return definitionsView.getVisibility();
    }
}
