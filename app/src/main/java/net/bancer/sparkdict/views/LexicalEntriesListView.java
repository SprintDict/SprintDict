package net.bancer.sparkdict.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import net.bancer.sparkdict.BaseActivity;
import net.bancer.sparkdict.R;
import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.core.LexicalEntry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * LexicalEntriesListView is a container for lexical entry views. It displays a
 * list of lexical entries after the user searched a word in dictionaries.
 */
public class LexicalEntriesListView extends LinearLayout {

    /**
     * Word that is focused when user searches a specific word on the screen.
     */
    private WordFocused wordFocused;

    /**
     * Detects pinch gestures used to change the font size of all lexical
     * entries displayed by this view.
     */
    private ScaleGestureDetector scaleGestureDetector;

    /**
     * Constructor.
     *
     * @param context application context.
     */
    public LexicalEntriesListView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor.
     *
     * @param context application context.
     * @param attrs   view attributes.
     */
    public LexicalEntriesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialises the view's state and pinch gesture detector.
     *
     * <p>The gesture detector accumulates scale changes during a pinch and
     * changes the font size when the accumulated scale exceeds the configured
     * threshold.</p>
     */
    private void init() {
        wordFocused = new WordFocused();
        scaleGestureDetector = new ScaleGestureDetector(
            getContext(),
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                private float accumulatedScale = 1.0f;

                @Override
                public boolean onScale(@NonNull ScaleGestureDetector detector) {
                    accumulatedScale *= detector.getScaleFactor();
                    if (accumulatedScale > 1.10f) {
                        zoomIn();
                        accumulatedScale = 1.0f;
                    } else if (accumulatedScale < 0.90f) {
                        zoomOut();
                        accumulatedScale = 1.0f;
                    }
                    return true;
                }
            }
        );
    }

    /**
     * Handles touch events and pinch-to-zoom gestures.
     *
     * <p>Single-finger clicks are delegated to {@link #performClick()}, while
     * multi-finger gestures are handled by the scale gesture detector.</p>
     *
     * @param event touch event to process.
     * @return {@code true} to indicate that the event has been handled.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        if (
            event.getActionMasked() == MotionEvent.ACTION_UP &&
            !scaleGestureDetector.isInProgress()
        ) {
            performClick();
        }
        return true;
    }

    /**
     * Performs the click action for this view.
     *
     * <p>This method is overridden to comply with Android's custom view
     * touch-handling contract when {@link #onTouchEvent(MotionEvent)} is
     * overridden.</p>
     *
     * @return {@code true} if the click was handled.
     */
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Intercepts touch events when a pinch-to-zoom gesture is detected.
     *
     * <p>When a second pointer is placed on the screen, interception by the
     * parent view is disabled so that vertical pinch gestures are not
     * intercepted by the surrounding {@code ScrollView}. Once the scale
     * gesture is in progress, subsequent events are intercepted by this
     * view.</p>
     *
     * @param event touch event to process.
     * @return {@code true} if the event should be intercepted by this view,
     * otherwise the result of the superclass implementation.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        if (scaleGestureDetector.isInProgress()) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * Adds a list of lexical entries into this view.
     *
     * @param lexicalEntries Lexical entries to be displayed.
     */
    public void addAll(ArrayList<LexicalEntry> lexicalEntries) {
        for (LexicalEntry lexicalEntry : lexicalEntries) {
            addView(new LexicalEntryView(getContext(), lexicalEntry));
        }
    }

    public void add(LexicalEntry lexicalEntry) {
        LexicalEntryView lexicalEntryView = new LexicalEntryView(getContext(), lexicalEntry);
        if (getChildCount() == 0) {
            lexicalEntryView.expand();
        }
        addView(lexicalEntryView);
    }

    /**
     * Increases the font size of all lexical entries displayed by this view.
     */
    private void zoomIn() {
        for (int i = 0; i < getChildCount(); i++) {
            ((LexicalEntryView) getChildAt(i)).zoomIn();
        }
    }

    /**
     * Decreases the font size of all lexical entries displayed by this view.
     */
    private void zoomOut() {
        for (int i = 0; i < getChildCount(); i++) {
            ((LexicalEntryView) getChildAt(i)).zoomOut();
        }
    }

    /**
     * Restores the highlighting and focus after the application was paused
     * while searching a word on the screen.
     *
     * @param word           word that was searched on the screen.
     * @param lexicalEntryNr sequence number of the lexical entry.
     * @param start          start position of the focused word.
     * @param end            end position of the focused word.
     */
    public void restore(String word, int lexicalEntryNr, int start, int end) {
        findOnScreenAndHighlightAllMatches(word);

        wordFocused.lexicalEntryNr = lexicalEntryNr;
        wordFocused.startPosition = start;
        wordFocused.endPosition = end;
        wordFocused.word = word;

        ((LexicalEntryView) getChildAt(lexicalEntryNr)).requestFocusAtWord(start, end);
    }

    /**
     * Clears fields participating in "find on page" functionality and removes
     * highlighting.
     */
    public void clearSearchOnScreenResults() {
        wordFocused.clear();
        removeHighlighting();
    }

    /**
     * Finds the next occurrence of a word or phrase on the screen.
     *
     * @param word word or phrase to be found.
     */
    public void findNextOnScreen(String word) {
        if (!word.isEmpty()) {
            boolean wordIsFound;
            if (word.equals(wordFocused.word)) { //not the first time the find next button was pressed
                wordIsFound = focusOnNextFoundWord(word);
            } else { // the first time find button was pressed
                wordIsFound = focusOnFirstFoundWord(word);
            }
            if (!wordIsFound) {
                ((BaseActivity) getContext()).showLongToast(getContext().getString(R.string.no_more_matches_found));
            }
        }
    }

    /**
     * Finds the previous occurrence of a word or phrase on the screen.
     *
     * @param word word or phrase to be found.
     */
    public void findPreviousOnScreen(String word) {
        if (!word.isEmpty()) {
            boolean nextIsFound;
            if (word.equals(wordFocused.word)) { //not the first time the find next button was pressed
                nextIsFound = focusOnPreviousFoundWord(word);
            } else { // the first time the find button was pressed
                nextIsFound = focusOnFirstFoundWord(word);
            }
            if (!nextIsFound) {
                ((BaseActivity) getContext()).showLongToast(getContext().getString(R.string.no_more_matches_found));
            }
        }
    }

    /**
     * Finds all occurrences of the word on the screen, highlights them and
     * focuses on the first occurrence highlighting it differently.
     *
     * @param word word to be found.
     * @return `true` when the word was found and took focus, else `false`.
     */
    private boolean focusOnFirstFoundWord(String word) {
        removeHighlighting();
        boolean nextIsFound = false;
        if (findOnScreenAndHighlightAllMatches(word)) { // at least one word is found on page
            nextIsFound = focusOnNextFoundWord(word);
            ((SparkDictActivity) getContext()).hideKeyboard(this);
        } else { // no matches found
            ((BaseActivity) getContext()).showLongToast(getContext().getString(R.string.no_matches_found));
        }
        return nextIsFound;
    }

    /**
     * Focuses and highlights the next occurrence of the searched word on the screen.
     *
     * @param word word to be found.
     * @return `true` if the next word was found and highlighted, else `false`.
     */
    private boolean focusOnNextFoundWord(String word) {
        for (int i = wordFocused.lexicalEntryNr; i < getChildCount(); i++) {
            int startOffset;
            if (i == wordFocused.lexicalEntryNr) {
                startOffset = wordFocused.endPosition;
            } else {
                startOffset = 0;
            }
            LexicalEntryView lexicalEntryView = (LexicalEntryView) getChildAt(i);
            int position = lexicalEntryView.findNextIndexOf(word, startOffset);
            if (position > -1) {
                return moveFocus(word, i, position);
            }
        }
        return false;
    }

    /**
     * Focuses and highlights the previous occurrence of the searched word on the screen.
     *
     * @param word word to be found.
     * @return `true` if the next word was found and highlighted, else `false`.
     */
    private boolean focusOnPreviousFoundWord(String word) {
        for (int i = wordFocused.lexicalEntryNr; i >= 0; i--) {
            LexicalEntryView lexicalEntryView = (LexicalEntryView) getChildAt(i);
            int startOffset;
            if (i == wordFocused.lexicalEntryNr) {
                startOffset = wordFocused.startPosition - 1;
            } else {
                startOffset = lexicalEntryView.getDefinitionsLength();
            }
            int position = lexicalEntryView.findLastIndexOf(word, startOffset);
            if (position > -1) {
                return moveFocus(word, i, position);
            }
        }
        return false;
    }

    /**
     * Moves highlighted focus of the "Find on screen" functionality.
     *
     * @param word           word or phrase to be focused.
     * @param lexicalEntryNr lexical entry number to be focused.
     * @param focusPosition  focus position in the lexical entry number position.
     * @return `true` when the view took focus, else `false`.
     */
    private boolean moveFocus(String word, int lexicalEntryNr, int focusPosition) {
        ((LexicalEntryView) getChildAt(wordFocused.lexicalEntryNr)).removeFocusedWordBackground();
        wordFocused.lexicalEntryNr = lexicalEntryNr;
        wordFocused.startPosition = focusPosition;
        wordFocused.endPosition = focusPosition + word.length();
        wordFocused.word = word;
        ((SparkDictActivity) getContext()).hideKeyboard(this);
        return ((LexicalEntryView) getChildAt(lexicalEntryNr)).requestFocusAtWord(wordFocused.startPosition, wordFocused.endPosition);
    }

    /**
     * Highlights all occurrences of the searched word or phrase on the screen.
     *
     * @param word word to be found.
     * @return `true` if at least one match was found and highlighted, else `false`.
     */
    private boolean findOnScreenAndHighlightAllMatches(String word) {
        boolean wordIsFoundOnPage = false;
        for (int i = 0; i < getChildCount(); i++) {
            LexicalEntryView articleLayout = (LexicalEntryView) getChildAt(i);
            if (articleLayout.highlightWord(word)) {
                wordIsFoundOnPage = true;
            }
        }
        return wordIsFoundOnPage;
    }

    /**
     * Removes highlighting of all found matches from the screen.
     */
    public void removeHighlighting() {
        for (int i = 0; i < getChildCount(); i++) {
            ((LexicalEntryView) getChildAt(i)).removeHighlighting();
        }
    }

    /**
     * Expands definitions of all lexical entries.
     */
    public void expandAll() {
        for (int i = 0; i < getChildCount(); i++) {
            ((LexicalEntryView) getChildAt(i)).expand();
        }
    }

    /**
     * Collapses definitions of all lexical entries.
     */
    public void collapseAll() {
        for (int i = 0; i < getChildCount(); i++) {
            ((LexicalEntryView) getChildAt(i)).collapse();
        }
    }

    /**
     * Retrieves focused word on the screen.
     *
     * @return focused word.
     */
    public String getFocusedWord() {
        return wordFocused.word;
    }

    /**
     * Determines the focused article number and focused word start position.
     * <p>
     * The method is used in SparkDictActivity onSaveInstanceState method to
     * save the coordinates of the focused word.
     *
     * @return array of integers where the first element is article number
     * and the second element is word start position
     */
    public int[] getFocusedWordPosition() {
        int[] result = new int[3];
        result[0] = wordFocused.lexicalEntryNr;
        result[1] = wordFocused.startPosition;
        result[2] = wordFocused.endPosition;
        return result;
    }

    /**
     * Retrieves definitions visibility field from all lexical entry views.
     *
     * @return array of definitions visibility fields for each lexical entry view.
     */
    public int[] getExpandedLexicalEntryViewsVisibility() {
        int[] result = new int[getChildCount()];
        for (int i = 0; i < getChildCount(); i++) {
            result[i] = ((LexicalEntryView) getChildAt(i)).getDefinitionsVisibility();
        }
        return result;
    }

    /**
     * Restores each lexical entry view's definitions visibility.
     *
     * @param visibilities array of definitions visibility values.
     */
    public void restoreDefinitionsVisibility(int[] visibilities) {
        for (int i = 0; i < visibilities.length; i++) {
            if (visibilities[i] == View.VISIBLE) {
                ((LexicalEntryView) getChildAt(i)).expand();
            } else if (visibilities[i] == View.GONE) {
                ((LexicalEntryView) getChildAt(i)).collapse();
            }
        }
    }

    /**
     * Container class to hold focused word parameters for "Find on screen"
     * functionality.
     */
    private static class WordFocused {

        /**
         * Sequence number of the lexical entry which contains the focused word.
         */
        private int lexicalEntryNr;

        /**
         * Start position of the focused word.
         */
        private int startPosition;

        /**
         * End position of the focused word.
         */
        private int endPosition;

        /**
         * Focused word.
         */
        private String word;

        /**
         * Constructor.
         */
        public WordFocused() {
            clear();
        }

        /**
         * Resets FocusedWord fields to default values.
         */
        private void clear() {
            lexicalEntryNr = 0;
            startPosition = 0;
            endPosition = 0;
            word = "";
        }

        /**
         * Returns FocusedWord object as a string. Used for debug only.
         */
        @NotNull
        public String toString() {
            return "[" + lexicalEntryNr + "," + startPosition + "," + endPosition + "," + word + "]";
        }
    }
}
