package net.bancer.sparkdict.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

/**
 * SearchInputField is a form field where the user can type a word to be found
 * in the dictionaries.
 */
@SuppressLint("AppCompatCustomView")
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

    /**
     * Performs the click action for this view and displays the soft keyboard.
     *
     * @return {@code true} indicating the click was handled.
     */
    @Override
    public boolean performClick() {
        super.performClick();
        showKeyboard();
        return true;
    }

    /**
     * Handles text context menu items such as paste, ensuring cursor positioning
     * at the end of pasted text and displaying the soft keyboard.
     *
     * @param id identifier of the context menu item.
     * @return {@code true} if the menu item was consumed.
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
            ensureValidSelection();
            showKeyboard();
        }
        return consumed;
    }

    /**
     * Called when the focus state of this view changes; places the selection
     * cursor at a valid position when focus is gained.
     *
     * @param gainFocus             {@code true} if the view gained focus.
     * @param direction             direction of focus movement.
     * @param previouslyFocusedRect rectangle of previously focused view.
     */
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            ensureValidSelection();
        }
    }

    /**
     * Ensures that the selection cursor is placed at a valid position (the end of the text)
     * if no selection exists or if the cursor is at the start of non-empty text.
     */
    private void ensureValidSelection() {
        if (getText() != null) {
            int len = getText().length();
            if (getSelectionStart() < 0 || (getSelectionStart() == 0 && getSelectionEnd() == 0)) {
                setSelection(len);
            }
        }
    }

    /**
     * Displays the soft keyboard for this input field.
     */
    public void showKeyboard() {
        requestFocus();
        ensureValidSelection();
        WindowInsetsController controller = getWindowInsetsController();
        if (controller != null) {
            controller.show(WindowInsets.Type.ime());
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(this, 0);
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (isPopupShowing()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    InputMethodManager imm = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    //hide keyboard
                    boolean keyboardHided = imm.hideSoftInputFromWindow(getWindowToken(), 0);
                    //if there was no keyboard to hide then hide the drop down list
                    if (!keyboardHided) {
                        dismissDropDown();
                    }
                }
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
