package net.bancer.sparkdict.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

/**
 * SearchInputField is a form field where the user can type a word to be found
 * in the dictionaries.
 * 
 * @author Valerij Bancer
 *
 */
public class SearchInputField extends AutoCompleteTextView {

	public SearchInputField(Context context) {
		super(context);
	}

	public SearchInputField(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SearchInputField(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (isPopupShowing()) {
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				if(event.getAction() == KeyEvent.ACTION_UP) {
					InputMethodManager imm = (InputMethodManager) 
							getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					//hide keyboard
					boolean keyboardHided = imm.hideSoftInputFromWindow(getWindowToken(), 0);
					//if there was no keyboard to hide then hide the drop down list
					if(!keyboardHided) {
						dismissDropDown();
					}
				}
				return true;
			}
		}
		return super.onKeyPreIme(keyCode, event);
	}
}
