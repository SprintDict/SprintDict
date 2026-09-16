package net.bancer.sparkdict.logging;

import android.util.Log;

public class AndroidLogger implements Logger {

    @Override
    public void error(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
    }

    @Override
    public void debug(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable throwable) {
        Log.d(tag, message, throwable);
    }
}
