package net.bancer.sparkdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

/**
 * Displays and manages dictionary-related preferences.
 */
public class DictPreferencesFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener,
    EditTextPreference.OnBindEditTextListener {

    private String dictTitleSizeKey;
    private String articleTitleSizeKey;
    private String definitionsSizeKey;

    /**
     * Returns the application's shared preferences.
     *
     * @return application shared preferences.
     */
    private SharedPreferences getSharedPreferences() {
        return requireContext()
            .getSharedPreferences(BaseActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Finds a preference and ensures that it exists.
     *
     * @param name preference key.
     * @param <T> preference type.
     * @return the preference.
     * @throws NullPointerException if the preference does not exist.
     */
    private <T extends Preference> T findNonNullPreference(String name) {
        return Objects.requireNonNull(findPreference(name));
    }

    /**
     * Adds horizontal dividers between preferences.
     *
     * @param view the fragment's root view.
     * @param savedInstanceState previously saved fragment state, or {@code null}.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView.ItemDecoration horizontalLine = new DividerItemDecoration(
            requireContext(),
            DividerItemDecoration.VERTICAL
        );
        RecyclerView recyclerView = getListView();
        recyclerView.addItemDecoration(horizontalLine);
    }

    /**
     * Initialises the preference screen and its dictionary preferences.
     *
     * @param savedInstanceState previously saved fragment state, or {@code null}.
     * @param rootKey preference hierarchy root key, or {@code null}.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        dictTitleSizeKey = getString(R.string.pref_dict_title_font_size);
        articleTitleSizeKey = getString(R.string.pref_article_title_font_size);
        definitionsSizeKey = getString(R.string.pref_definitions_font_size);

        initPathPreference();
        initFontSizePreference(dictTitleSizeKey, 22.0f);
        initFontSizePreference(articleTitleSizeKey, 18.0f);
        initFontSizePreference(definitionsSizeKey, 14.0f);
    }

    /**
     * Initialises the dictionary path preference.
     */
    private void initPathPreference() {
        SharedPreferences settings = getSharedPreferences();
        String path = settings.getString(getString(R.string.menu_dict_path), null);
        Preference pathPref = findNonNullPreference(getString(R.string.pref_dict_path));
        pathPref.setSummary(path);
    }

    /**
     * Initialises a font-size preference.
     *
     * @param name preference key.
     * @param defaultSize default font size.
     */
    private void initFontSizePreference(String name, float defaultSize) {
        SharedPreferences settings = getSharedPreferences();
        float size = settings.getFloat(name, defaultSize);
        EditTextPreference definitionsPref = findNonNullPreference(name);
        definitionsPref.setSummary("" + size);
        definitionsPref.setText("" + size);
        definitionsPref.setOnBindEditTextListener(this);
    }

    /**
     * Registers the preference change listener and updates font-size preferences.
     */
    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
            .registerOnSharedPreferenceChangeListener(this);
        updateFontSizePreference(dictTitleSizeKey);
        updateFontSizePreference(articleTitleSizeKey);
        updateFontSizePreference(definitionsSizeKey);
    }

    /**
     * Updates the corresponding application preference when a preference changes.
     *
     * @param sharedPreferences shared preferences containing the changed preference.
     * @param key key of the changed preference.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateFontSizePreference(key);
    }

    /**
     * Updates the summary and application preference for a font-size preference.
     *
     * @param key preference key.
     */
    private void updateFontSizePreference(String key) {
        if (
            !key.equals(dictTitleSizeKey) &&
            !key.equals(articleTitleSizeKey) &&
            !key.equals(definitionsSizeKey)
        ) {
            return;
        }
        EditTextPreference pref = findNonNullPreference(key);
        String text = pref.getText();
        if (text != null && !text.trim().isEmpty()) {
            pref.setSummary(text);
            SharedPreferences settings = getSharedPreferences();
            Editor editor = settings.edit();
            editor.putFloat(key, Float.parseFloat(text));
            editor.apply();
        }
    }

    /**
     * Unregisters the preference change listener.
     */
    @Override
    public void onPause() {
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences())
            .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * Configures the edit text used by a font-size preference.
     *
     * @param editText edit text used to enter the preference value.
     */
    @Override
    public void onBindEditText(EditText editText) {
        int decimalType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        editText.setInputType(decimalType);
    }
}
