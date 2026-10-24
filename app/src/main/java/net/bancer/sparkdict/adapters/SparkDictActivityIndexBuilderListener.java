package net.bancer.sparkdict.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.bancer.sparkdict.SparkDictActivity;
import net.bancer.sparkdict.domain.IndexBuilderListener;
import net.bancer.sparkdict.domain.utils.DomainException;

import java.lang.ref.WeakReference;

/**
 * Updates search index building progress on the main thread and logs indexing errors
 * for {@link SparkDictActivity}.
 */
public class SparkDictActivityIndexBuilderListener implements IndexBuilderListener {

    private static final String TAG = "SparkDictActivityIndexBuilderListener";

    private final WeakReference<Context> contextRef;
    private final Handler mainHandler;

    /**
     * Creates a progress UI updater for index building during search suggestion retrieval.
     *
     * @param context caller context.
     */
    public SparkDictActivityIndexBuilderListener(Context context) {
        this.contextRef = new WeakReference<>(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Updates the indexing progress UI on the main application thread.
     *
     * @param indexed number of articles indexed so far.
     * @param total   total number of articles to be indexed.
     */
    @Override
    public void onProgress(int indexed, int total) {
        mainHandler.post(() -> {
            Context context = contextRef.get();
            if (context instanceof SparkDictActivity) {
                ((SparkDictActivity) context).updateIndexRebuildProgressBar(indexed, total);
            }
        });
    }

    /**
     * Logs indexing errors.
     *
     * @param dictionaryName name of the dictionary that could not be indexed.
     * @param e              exception that caused the indexing failure.
     */
    @Override
    public void onIndexingError(String dictionaryName, DomainException e) {
        Log.e(TAG, "Failed to index " + dictionaryName, e);
    }

    /**
     * Called when indexing completes.
     */
    @Override
    public void onIndexingComplete() {
    }
}
