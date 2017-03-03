package net.bancer.sparkdict.views;

import net.bancer.sparkdict.BaseActivity;
import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.core.LexicalEntry;
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

/**
 * LexicalEntryView displays all parts of the lexical entry and handles
 * different transformations of them.
 * 
 * @author Valerij Bancer
 *
 */
public class LexicalEntryView extends LinearLayout implements 
		/*View.OnLongClickListener,*/ View.OnFocusChangeListener,
		View.OnTouchListener/*, OnEditorActionListener*/ {

	/**
	 * Factor by which the font size is changed after zoom in/out operation.
	 */
	private static final float TEXT_SCALE_FACTOR = 1.1f;

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
	
	//private InputMethodManager inputMethodManager;
	
	//private int touchPosition;

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
	 * @param context	application context.
	 */
	public LexicalEntryView(Context context) {
		super(context);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param context	application context.
	 * @param attrs		view attributes.
	 */
	public LexicalEntryView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Default constructor.
	 * 
	 * @param context		application context.
	 * @param lexicalEntry	lexical entry domain object.
	 */
	public LexicalEntryView(Context context, LexicalEntry lexicalEntry) {
		super(context);
		init();
		this.lexicalEntry = lexicalEntry;
		dictTitleView.setText(lexicalEntry.getDictTitle());
		lemmaView.setText(lexicalEntry.getLemma());
		definitionsView.parseHtmlAndSetText(lexicalEntry);
		//setTag(lexicalEntry.getDictTitle());
	}

	/**
	 * Initialisation of the view.
	 */
	private void init() {
//		inputMethodManager = (InputMethodManager) 
//				getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		LayoutInflater inflater = (LayoutInflater) 
				getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.lexical_entry, this, true);
		
		expanderView = (ImageView) findViewById(R.id.expander_icon);
		expanderView.setOnTouchListener(this);
		
		dictTitleView = (TextView) findViewById(R.id.dict_title);
		dictTitleView.setOnTouchListener(this);
		
		lemmaView = (TextView) findViewById(R.id.article_title);
		lemmaView.setOnTouchListener(this);
		
		definitionsView = (DefinitionsView) findViewById(R.id.definitions_body);
		definitionsView.setOnClickListener((OnClickListener) getContext());
		//definitionsView.setOnLongClickListener(this);
		definitionsView.setOnFocusChangeListener(this);
		definitionsView.setOnTouchListener(this);
		//definitionsView.setOnEditorActionListener(this);
		// make links clickable
		definitionsView.setMovementMethod(LinkMovementMethod.getInstance());//Causes problems with action bar for editing/copying/pasting
		
		restoreTextSizeFromPreferences();
	}

	/**
	 * Retrieves text sizes of the dictionary's title, article's title and
	 * article's from shared preferences and applies them to the current view.
	 */
	private void restoreTextSizeFromPreferences() {
		BaseActivity baseActivity = (BaseActivity)getContext();
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
		BaseActivity baseActivity = (BaseActivity)getContext();
		TextView dictTitleView = (TextView) findViewById(R.id.dict_title);
		if (dictTitleView != null) {
			baseActivity.saveSharedPreference(getContext().getString(R.string.pref_dict_title_font_size), dictTitleView.getTextSize());
		}
		TextView articleTitleView = (TextView) findViewById(R.id.article_title);
		if (articleTitleView != null) {
			baseActivity.saveSharedPreference(getContext().getString(R.string.pref_article_title_font_size), articleTitleView.getTextSize());
		}
		TextView definitionsView = (TextView) findViewById(R.id.definitions_body);
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
		transformTextSizes(1/TEXT_SCALE_FACTOR);
	}

	/**
	 * Transforms article's text size. If `scaleFactor` is more than 1 the text
	 * size increases, if it is less than 1 then decreases.
	 * 
	 * @param scaleFactor Float value multiplying by which the text size must be change.
	 */
	private void transformTextSizes(float scaleFactor) {
		int dictionaryTitleSize = (int)(dictTitleView.getTextSize() * scaleFactor);
		int articleTitleSize = (int)(lemmaView.getTextSize() * scaleFactor);
		int bodyTextSize = (int)(definitionsView.getTextSize() * scaleFactor);
		setTextSizes(dictionaryTitleSize, articleTitleSize, bodyTextSize);
		saveFontSizesToSharedPreferences();
	}

	/**
	 * Sets the text size of dictionary title, article title and article body.
	 * All parameters values are interpreted to be raw pixels.
	 * 
	 * @param dictionaryTitleSize	font size of the dictionary title.
	 * @param articleTitleSize		font size of the article title.
	 * @param bodyTextSize			font size of the article body.
	 */
	private void setTextSizes(int dictionaryTitleSize, int articleTitleSize,
			int bodyTextSize) {
		dictTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dictionaryTitleSize);
		lemmaView.setTextSize(TypedValue.COMPLEX_UNIT_PX, articleTitleSize);
		definitionsView.setTextSize(TypedValue.COMPLEX_UNIT_PX, bodyTextSize);
	}

	/**
	 * Sets the provided view to be focusable in all modes including touch mode
	 * and requests focus on that view.
	 * 
	 * @param view	view to be focused.
	 * @return		`true` when the view took focus, else `false`.
	 */
	private boolean requestFocusAt(EditText view) {
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		return view.requestFocus();
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus) {
			((EditText)v).setFocusable(false);
			((EditText)v).setFocusableInTouchMode(false);
			((EditText)v).setCursorVisible(false);
		}
	}

	/**
	 * Expands/collapses definitions of this lexical entry after a touch on the
	 * dictionary title.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//System.out.println("view class: " + v.getClass().getName());
		//System.out.println("onTouch view: " + v.getId());
		//System.out.println("onTouch event action: " + event.getAction());
		//inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
		final int action = event.getAction();
//		switch (action) {
//			case MotionEvent.ACTION_DOWN:
//				int touchX = (int)event.getX();
//				int touchY = (int)event.getY();
//				touchX -= v.getPaddingLeft();
//				touchY -= v.getPaddingTop();
//				touchX += v.getScrollX();
//				touchY += v.getScrollY();
//				Layout layout = ((TextView)v).getLayout();
//				if(layout != null) {
//					int line = layout.getLineForVertical(touchY);
//					touchPosition = layout.getOffsetForHorizontal(line, touchX);
//				}
//				break;
//			default:
//				break;
//		}
//		return false;
		if(v.getId() == R.id.dict_title || v.getId() == R.id.expander_icon) {
			if(action == MotionEvent.ACTION_UP) {
				if(definitionsView.getVisibility() == View.VISIBLE) {
					collapse();
				} else if(definitionsView.getVisibility() == View.GONE) {
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

//	@Override
//	public boolean onLongClick(View view) {
//		if(view instanceof EditText) {
//			//Log.d("long click", "line: " + line + ", off: " + off + ", pos: " + pos);
//			Log.d("long click", "selection start: " + definitionsView.getSelectionStart());
//			requestFocusAt((EditText)view);
//			//Log.d("long click", "selection start: " + mDefinitionsBody.getSelectionStart());
//			//mDefinitionsBody.setSelection(pos, pos + 10);
//			((EditText)view).setCursorVisible(true);
//			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
//			
//			definitionsView.setSelection(touchPosition, touchPosition + 10);
//		}
//		return false;
//	}

//	@Override
//	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//		if(actionId == EditorInfo.IME_ACTION_DONE) {
//			System.out.println("IME_ACTION_DONE");
//		} else {
//			System.out.println("Editor action: " + actionId);
//		}
//		return false;
//	}

	/**
	 * Highlights all occurrences of the word in this lexical entry.
	 * 
	 * @param word	word to be found and highlighted.
	 * @return		`true` if at least one word occurrence was found.
	 */
	public boolean highlightWord(String word) {
		if(definitionsView.getVisibility() == View.VISIBLE) {
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
	 * @param start		start position of the focus.
	 * @param end		end position of the focus.
	 * @return			`true` when the view took focus, else `false`.
	 */
	public boolean requestFocusAtWord(int start, int end) {
		if(!highlightingDone) {
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
	 * @param word		word or phrase to find.
	 * @param start		the starting offset.
	 * @return 			the index of the first character of the specified string
	 * 					in this string, -1 if the specified string is not a substring.
	 */
	public int findNextIndexOf(String word, int start) {
		if(definitionsView.getVisibility() == View.VISIBLE) {
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
	 * @param word		word or phrase to find.
	 * @param start		the starting offset.
	 * @return			the index of the first character of the specified string
	 * 					in this string, -1 if the specified string is not a substring.
	 */
	public int findLastIndexOf(String word, int start) {
		if(definitionsView.getVisibility() == View.VISIBLE) {
			return definitionsView.getText().toString().lastIndexOf(word, start);
		} else {
			return -1;
		}	
	}

	/**
	 * Retrieves the length of the definitions text.
	 * 
	 * @return	the length of the definitions text.
	 */
	public int getDefinitionsLength() {
		return definitionsView.getText().toString().length();
	}

	/**
	 * Retrieves definitions visibility field.
	 * 
	 * @return	definitions visibility field.
	 */
	public int getDefinitionsVisibility() {
		return definitionsView.getVisibility();
	}
}
