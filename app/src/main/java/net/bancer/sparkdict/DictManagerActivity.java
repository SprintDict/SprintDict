package net.bancer.sparkdict;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.bancer.sparkdict.adapters.DictManagerItemsAdapter;
import net.bancer.sparkdict.domain.IndexBuilder;
import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.utils.DomainException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * DictManagerActivity displays a list of all dictionaries, allows the user
 * to enable/disable the dictionaries, to change their order, to set the path to
 * the dictionaries, to build the additional index files for all dictionaries.
 */
public class DictManagerActivity extends BaseActivity {

    public static final int FILE_PERMISSIONS_ACCESS_REQUEST = 34232131;
    public static final int PICK_DIRECTORY = 43522432;
    /**
     * Tag (key) used in Bundle extras to indicate that it contains a value
     * indicating that another activity that is a child of DictManagerActivity
     * must be started. This another activity could be started by selecting menu
     * item from DictManagerActivity activity.
     */
    public static final String SUB_ACTIVITY = "SubActivityToStart";
    /**
     * Flag used in Bundle extras to indicate that DirectoryPicker subactivity
     * must be started.
     *
     * @see net.bancer.sparkdict.DictManagerActivity#SUB_ACTIVITY
     */
    public static final int START_DIR_PICKER = 1;
    /**
     * Flag used in Bundle extras to indicate that no subactivity to be started.
     *
     * @see net.bancer.sparkdict.DictManagerActivity#SUB_ACTIVITY
     */
    protected static final int DO_NOT_START_SUB_ACTIVITY = 0;
    private static final String INDEX_BUILD_CHANNEL_ID = "index_build";
    IndexBuilder progressThread;

    private LinearLayout rebuildProgressLayout;

    private TextView rebuildProgressText;

    private ProgressBar rebuildProgress;

    private DictManagerItemsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Environment.isExternalStorageManager()) {
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
            startActivityForResult(intent, FILE_PERMISSIONS_ACCESS_REQUEST);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getInt(SUB_ACTIVITY) == START_DIR_PICKER) {
            startDirPicker();
        }
        initLayout();
        createNotificationChannel();
    }

    /**
     * Creates the notification channel used for dictionary index building notifications.
     *
     * <p>The channel is configured with the default notification sound and
     * notification importance.</p>
     */
    private void createNotificationChannel() {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build();
        NotificationChannel channel = new NotificationChannel(
            INDEX_BUILD_CHANNEL_ID,
            getString(R.string.sprint_dict_indexing_notification),
            NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setSound(sound, audioAttributes);
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.createNotificationChannel(channel);
    }

    @Override
    public void onResume() {
        super.onResume();
        //initLayout();
    }

    private void initLayout() {
        setContentView(R.layout.activity_dict_manager);

        ArrayList<Book> books = getBooks();
        adapter = new DictManagerItemsAdapter(this, books);

        ListView listView = findViewById(R.id.dict_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);

        rebuildProgressLayout = findViewById(R.id.rebuild_progress_layout);
        rebuildProgressText = findViewById(R.id.rebuild_progress_text);
        rebuildProgress = findViewById(R.id.rebuild_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.activity_dict_manager, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_set_dict_path) {
            startDirPicker();
            return true;
        }
        if (itemId == R.id.menu_rebuild_index) {
            startIndexRebuild();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void startDirPicker() {
        //Log.d(TAG, "Set dict path!");
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        String uriToLoad = "/";
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
        startActivityForResult(intent, PICK_DIRECTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Save the path if DirectoryPicker successfully finished its job.
        if (requestCode == PICK_DIRECTORY && resultCode == RESULT_OK) {
            if (intent != null) {
                saveDictPath(intent);
            }
        }
    }

    /**
     * Extracts the filesystem path of the folder selected by the user.
     *
     * <p>The selected folder is obtained from the tree URI returned in the
     * {@link Intent} data. The URI's document ID is split into the storage
     * volume type and the relative path, which are then combined into the
     * corresponding filesystem path.</p>
     *
     * @param intent the intent returned by the directory picker
     * @return the filesystem path of the selected folder
     */
    private String extractSelectedFolder(Intent intent) {
        // The result data contains a URI for the document or directory that the user selected.
        Uri uri = intent.getData();
        final String docId = DocumentsContract.getTreeDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];
        // Get the path that was picked from intent returned by DirectoryPicker
        String path = split.length > 1 ? split[1] : "";
        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + path;
        }
        return "/storage/" + type + "/" + path;
    }

    private void saveDictPath(Intent intent) {
        String value = this.extractSelectedFolder(intent);
        // Get the key that identifies the path in SharedPreferences
        String key = getString(R.string.menu_dict_path);
        // Save the path to SharedPreferences
        boolean isSaved = saveSharedPreference(key, value);
        String msg;
        if (isSaved) { // Create message string for a toast
            msg = getString(R.string.dict_path_saved_msg, value);
        } else {
            msg = getString(R.string.dict_path_not_saved_msg, value);
        }
        refreshShelf();
        initLayout(); // Generate DictManagerActivity screen
        // Display a toast informing that path was saved/not saved
        showLongToast(msg);
    }

    /**
     * Saves the ordered list of enabled dictionaries into shared preferences
     * and refreshes Shelf object.
     */
    @Override
    protected void onPause() {
        super.onPause();
        StringBuilder enabledDicts = new StringBuilder();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            Book book = (Book) adapter.getItem(i);
            if (book.isEnabled()) {
                if (enabledDicts.length() > 0) {
                    enabledDicts.append("||");
                }
                enabledDicts.append(book.getBookName());
            }
        }
        boolean isSaved = saveSharedPreference(
            getString(R.string.enabled_dicts),
            enabledDicts.toString()
        );
        if (isSaved) {
            refreshShelf();
        }
    }

    /**
     * Selects all dictionaries in the dictionary list.
     *
     * @param v the view that triggered the action
     */
    public void onSelectAllDicts(View v) {
        adapter.setCheckedAll(true);
    }

    /**
     * "Unselect All" button handler. Un-checks all dictionaries.
     *
     * @param v the view that triggered the action
     */
    public void onUnselectAllDicts(View v) {
        adapter.setCheckedAll(false);
    }

    /**
     * "Move Up" arrow image button handler. Moves the dictionary up the list.
     *
     * @param v the view that triggered the action
     */
    public void onMoveUp(View v) {
        int position = Integer.parseInt((String) v.getTag());
        if (position > 0) {
            ArrayList<Book> books = getBooks();
            Book book = books.get(position);
            books.remove(position);
            books.add(position - 1, book);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Starts rebuilding the dictionary indexes and displays the rebuild progress UI.
     *
     * <p>The progress indicators are reset before starting the index-building
     * thread.</p>
     */
    private void startIndexRebuild() {
        rebuildProgressLayout.setVisibility(View.VISIBLE);
        rebuildProgress.setProgress(0);
        rebuildProgressText.setText(
            getString(R.string.rebuilding_index_progress, 0, 0)
        );
        progressThread = new IndexBuilder(new IndexBuilderProgressUIUpdater(this), getShelf());
        progressThread.start();
    }

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
    private static class IndexBuilderProgressUIUpdater implements IndexBuilder.Listener {

        private final WeakReference<DictManagerActivity> activityRef;
        private final Context appContext; // safe to hold strongly — lives with the process

        /**
         * Creates a progress UI updater for the specified activity.
         *
         * @param activity activity whose indexing progress UI should be updated
         */
        IndexBuilderProgressUIUpdater(DictManagerActivity activity) {
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
            activity.runOnUiThread(() -> {
                activity.rebuildProgress.setMax(total);
                activity.rebuildProgress.setProgress(indexed);
                activity.rebuildProgressText.setText(
                    activity.getString(R.string.rebuilding_index_progress, indexed, total)
                );
                if (indexed >= total) {
                    activity.rebuildProgressLayout.setVisibility(View.GONE);
                }
            });
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
            NotificationManager mgr = (NotificationManager) appContext.getSystemService(NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(appContext, DictManagerActivity.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pi = PendingIntent.getActivity(appContext, 0, notificationIntent, flags);
            Notification note = new Notification.Builder(appContext, INDEX_BUILD_CHANNEL_ID)
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
}
