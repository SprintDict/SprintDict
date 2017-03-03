package net.bancer.sparkdict;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;

/**
 * DictPreferencesActivity displays a list of editable preferences.
 * 
 * @author Valerij Bancer
 *
 */
public class DictPreferencesActivity extends PreferenceActivity 
		implements OnSharedPreferenceChangeListener {
	
	private String DICT_TITLE_SIZE_STR;
    private String ARTICLE_TITLE_SIZE_STR;
    private String DEFINITIONS_SIZE_STR;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		DICT_TITLE_SIZE_STR 	= getString(R.string.pref_dict_title_font_size);
	    ARTICLE_TITLE_SIZE_STR 	= getString(R.string.pref_article_title_font_size);
	    DEFINITIONS_SIZE_STR 	= getString(R.string.pref_definitions_font_size);
		
		SharedPreferences settings = getSharedPreferences(BaseActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String path = settings.getString(getString(R.string.menu_dict_path), null);
        float dictTitleSize = settings.getFloat(DICT_TITLE_SIZE_STR, 22.0f);
		float articleTitleSize = settings.getFloat(ARTICLE_TITLE_SIZE_STR, 18.0f);
		float definitionsSize = settings.getFloat(DEFINITIONS_SIZE_STR, 14.0f);
		
		Preference pathPref = (Preference) findPreference(getString(R.string.pref_dict_path));
		pathPref.setSummary(path);
		
		EditTextPreference dictTitlePref = (EditTextPreference) findPreference(DICT_TITLE_SIZE_STR);
		dictTitlePref.setSummary("" + dictTitleSize);
		dictTitlePref.setText("" + dictTitleSize);
		dictTitlePref.getEditText().setKeyListener(DigitsKeyListener.getInstance());
		
		EditTextPreference articleTitlePref = (EditTextPreference) findPreference(ARTICLE_TITLE_SIZE_STR);
		articleTitlePref.setSummary("" + articleTitleSize);
		articleTitlePref.setText("" + articleTitleSize);
		articleTitlePref.getEditText().setKeyListener(DigitsKeyListener.getInstance());
		
		EditTextPreference definitionsPref = (EditTextPreference) findPreference(DEFINITIONS_SIZE_STR);
		definitionsPref.setSummary("" + definitionsSize);
		definitionsPref.setText("" + definitionsSize);
		definitionsPref.getEditText().setKeyListener(DigitsKeyListener.getInstance());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		updatePreference(DICT_TITLE_SIZE_STR);
		updatePreference(ARTICLE_TITLE_SIZE_STR);
		updatePreference(DEFINITIONS_SIZE_STR);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreference, String key) {
		updatePreference(key);
	}

	private void updatePreference(String key) {
		if(		key.equals(DICT_TITLE_SIZE_STR) || 
				key.equals(ARTICLE_TITLE_SIZE_STR) || 
				key.equals(DEFINITIONS_SIZE_STR)) {
			
			EditTextPreference pref = (EditTextPreference) findPreference(key);
			if(pref.getText().trim().length() > 0) {
				pref.setSummary("" + pref.getText());
				SharedPreferences settings = getSharedPreferences(BaseActivity.PREFS_NAME, Context.MODE_PRIVATE);
				Editor editor = settings.edit();
				editor.putFloat(key, Float.parseFloat(pref.getText()));
				editor.commit();
			}
		} 
	}
}
