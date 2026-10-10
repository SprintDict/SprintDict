package net.bancer.sparkdict.storage;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Preference key for custom dictionary display names map.
     */
    public static final String PREF_CUSTOM_DICT_NAMES = "custom_dict_names";

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

    /**
     * Gets map of custom dictionary display names from shared preferences.
     *
     * @return map of original book names to custom display names.
     */
    public Map<String, String> getCustomDictNames() {
        Map<String, String> result = new HashMap<>();
        String raw = getString(PREF_CUSTOM_DICT_NAMES);
        if (raw.isEmpty()) {
            return result;
        }
        String[] pairs = raw.split("\\|\\|");
        for (String pair : pairs) {
            String[] parts = pair.split("=>", 2);
            if (parts.length == 2 && !parts[0].isEmpty()) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }

    /**
     * Saves map of custom dictionary display names to shared preferences.
     *
     * @param customNames map of original book names to custom display names.
     * @return `true` if saved successfully, else `false`.
     */
    public boolean saveCustomDictNames(Map<String, String> customNames) {
        StringBuilder sb = new StringBuilder();
        if (customNames != null) {
            for (Map.Entry<String, String> entry : customNames.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("||");
                    }
                    sb.append(entry.getKey())
                        .append("=>")
                        .append(entry.getValue().trim());
                }
            }
        }
        return save(PREF_CUSTOM_DICT_NAMES, sb.toString());
    }

    /**
     * Saves or removes custom display name for a specific dictionary in shared preferences.
     *
     * @param bookName   original book name.
     * @param customName custom display name to save, or null/empty to clear.
     * @return `true` if saved successfully, else `false`.
     */
    public boolean saveCustomDictName(String bookName, String customName) {
        Map<String, String> map = getCustomDictNames();
        if (customName == null || customName.trim().isEmpty()) {
            map.remove(bookName);
        } else {
            map.put(bookName, customName.trim());
        }
        return saveCustomDictNames(map);
    }
}
