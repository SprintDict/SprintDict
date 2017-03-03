package net.bancer.sparkdict.views;

import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.LexicalEntry;
import net.bancer.sparkdict.views.helpers.DictResourceImageGetter;
import net.bancer.sparkdict.views.helpers.UnrecognizedTagsHandler;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

/**
 * DefinitionsView displays definitions of the lexical entry and performs
 * different transformations of them.
 * 
 * @author Valerij Bancer
 *
 */
public class DefinitionsView extends EditText {

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
	 * @param context	application context.
	 */
	public DefinitionsView(Context context) {
		super(context);
	}

	/**
	 * Constructor.
	 * 
	 * @param context	application context.
	 * @param attrs		view attributes.
	 */
	public DefinitionsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor.
	 * 
	 * @param context	application context.
	 * @param attrs		view attributes.
	 * @param defStyle	default style to apply to this view.
	 */
	public DefinitionsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Retrieve the background colour of the focused word.
	 * 
	 * @return	background colour object of the focused word.
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
	 * @param start		selection start position.
	 * @param end		selection end position.
	 */
	public void requestFocusAt(int start, int end) {
		setSelection(start, end);
		getText().setSpan(
				getFocusedWordBackground(),
				start,
				end,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	/**
	 * Highlights all occurrences of the word in the lexical entry view.
	 * 
	 * @param word	word to be highlighted.
	 * @return		`true` if at least one word was highlighted, else `false`.
	 */
	public boolean highlightAllInstancesOfWord(String word) {
		boolean atLeastOneHighlighted = false;
		int wordLength = word.length();
		String lowerCaseWord = word.toLowerCase();
		String def = getText().toString().toLowerCase();
		int start = def.indexOf(lowerCaseWord);
		int end = 0;
		while(start != -1) {
			atLeastOneHighlighted = true;
			end = start + wordLength;
			BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(Color.YELLOW);
			TextAppearanceSpan txtAppearanceSpan = 
				new TextAppearanceSpan(null, Typeface.NORMAL, (int) getTextSize(), getColorStateList(), null);		
			getText().setSpan(bgColorSpan,       start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			getText().setSpan(txtAppearanceSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			start = def.indexOf(lowerCaseWord, end);
		}
		return atLeastOneHighlighted;
	}

	/**
	 * Colour state list getter. 
	 * 
	 * @return	colour state list object.
	 */
	private ColorStateList getColorStateList() {
		if(colorStateList == null) {
			colorStateList = getResources().getColorStateList(R.color.highlighted_text_color_list);
		}
		return colorStateList;
	}

	/**
	 * Parses the HTML string provided as the parameter and sets it as the text
	 * for the current view.
	 * 
	 * @param lexicalEntry
	 *            HTML string of the definitions to be set as text.
	 */
	public void parseHtmlAndSetText(final LexicalEntry lexicalEntry) {
		setText(Html.fromHtml(lexicalEntry.getDefinitions(),
				new DictResourceImageGetter(lexicalEntry),
				new UnrecognizedTagsHandler(lexicalEntry, getContext())),
				TextView.BufferType.SPANNABLE);
	}

}
