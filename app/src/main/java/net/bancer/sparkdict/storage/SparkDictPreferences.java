package net.bancer.sparkdict.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SparkDictPreferences {

    /**
     * The name of SparkDict shared preferences.
     */
    public static final String PREFS_NAME = "SparkDict";

    /**
     * The preferences name that stores the root dictionaries' path selected by the user
     * in the format content://com.android.externalstorage.documents/tree/primary%3Adictionaries
     * where "dictionaries" is the name of the selected folder.
     */
    public static final String PREF_DICT_ROOT_URI_NAME = "dict_root_uri";

    private final SharedPreferences preferences;

    public SparkDictPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get string value from shared preferences identified by key.
     *
     * @param key shared preference key.
     * @return string value of the shared preference.
     */
    public String getString(String key) {
        return preferences.getString(key, "");
    }

    /**
     * Get float value from shared preferences identified by key.
     *
     * @param key shared preference key.
     * @return float value of the shared preference.
     */
    public float getFloat(String key) {
        return preferences.getFloat(key, 20.0f);
    }

    /**
     * Saves a string value to shared preferences identifying it by the key.
     *
     * @param key   key of the shared preference to be saved.
     * @param value string value to be saved.
     * @return `true` if the value was saved, else `false`.
     */
    public boolean save(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Saves a float value to shared preferences identifying it by the key.
     *
     * @param key   key of the shared preference to be saved.
     * @param value float value to be saved.
     * @return `true` if the value was saved, else `false`.
     */
    public boolean save(String key, float value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }
}
