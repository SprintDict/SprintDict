package net.bancer.sparkdict.adapters;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.bancer.sparkdict.DictManagerActivity;
import net.bancer.sparkdict.R;
import net.bancer.sparkdict.domain.IndexBuilderListener;
import net.bancer.sparkdict.domain.utils.DomainException;

import java.lang.ref.WeakReference;

/**
 * Receives index-building progress notifications and updates the
 * {@link DictManagerActivity} UI accordingly.
 *
 * <p>Holds a weak reference to the activity so that the background
 * index-building process does not prevent the activity from being
 * garbage-collected.</p>
 *
 * <p>The application context is held strongly for operations that do not
 * require an activity context, such as sending notifications.</p>
 */
public class DictManagerActivityIndexBuilderListener implements IndexBuilderListener {

    private static final String TAG = "DictManagerActivityIndexBuilderListener";

    private final WeakReference<DictManagerActivity> activityRef;
    private final Context appContext; // safe to hold strongly — lives with the process

    /**
     * Creates a progress UI updater for the specified activity.
     *
     * @param activity activity whose indexing progress UI should be updated
     */
    public DictManagerActivityIndexBuilderListener(DictManagerActivity activity) {
        activityRef = new WeakReference<>(activity);
        appContext = activity.getApplicationContext();
    }

    /**
     * Updates the indexing progress UI on the activity's UI thread.
     *
     * <p>If the activity is no longer available, the progress update is skipped.
     * The progress layout is hidden when indexing is complete.</p>
     *
     * @param indexed number of articles indexed so far
     * @param total   total number of articles to be indexed
     */
    @Override
    public void onProgress(int indexed, int total) {
        DictManagerActivity activity = activityRef.get();
        if (activity == null) {
            return; // no screen to update — fine to skip
        }
        activity.runOnUiThread(() -> activity.updateIndexRebuildProgressBar(indexed, total));
    }

    /**
     * Logs an indexing error and sends a notification to the user.
     *
     * <p>The notification is sent regardless of whether the activity is still available.</p>
     *
     * @param dictionaryName name of the dictionary that could not be indexed
     * @param e              exception that caused the indexing failure
     */
    @Override
    public void onIndexingError(String dictionaryName, DomainException e) {
        String message = appContext.getString(R.string.dict_cannot_be_indexed, dictionaryName);
        Log.e(TAG, message, e);
        sendNotification(message); // always fires, regardless of Activity state
    }

    /**
     * Sends a notification indicating that dictionary indexing has completed successfully.
     */
    @Override
    public void onIndexingComplete() {
        sendNotification(appContext.getString(R.string.rebuilding_index_success));
    }

    /**
     * Sends a notification about the dictionary index building process.
     *
     * @param message the message to display in the notification
     */
    private void sendNotification(String message) {
        NotificationManager mgr = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(appContext, DictManagerActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(appContext, 0, notificationIntent, flags);
        Notification note = new Notification.Builder(appContext, DictManagerActivity.INDEX_BUILD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(appContext.getString(R.string.sprint_dict_indexing_notification))
            .setContentText(message)
            .setContentIntent(pi)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .build();
        mgr.notify(message.hashCode(), note);
    }
}
