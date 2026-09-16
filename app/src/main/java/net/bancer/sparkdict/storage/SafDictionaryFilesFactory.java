package net.bancer.sparkdict.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import net.bancer.sparkdict.logging.ConsoleLogger;
import net.bancer.sparkdict.logging.Logger;

public class SafDictionaryFilesFactory {

    private SafDictionaryFilesFactory() {
    }

    public static SafDictionaryFiles create(Context context) {
        return create(context, new ConsoleLogger());
    }

    public static SafDictionaryFiles create(Context context, Logger logger) {
        SharedPreferences preferences = context.getSharedPreferences(SparkDictPreferences.PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = preferences.getString(SparkDictPreferences.PREF_DICT_ROOT_URI_NAME, "");
        return new SafDictionaryFiles(context, Uri.parse(uriString), logger);
    }
}
