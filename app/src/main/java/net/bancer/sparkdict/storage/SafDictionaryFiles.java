package net.bancer.sparkdict.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.logging.ConsoleLogger;
import net.bancer.sparkdict.logging.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link DictionaryFiles} implementation backed by a Storage Access
 * Framework tree, identified by a persisted tree {@link Uri}. Paths are
 * root-relative, {@code /}-separated -- e.g.
 * {@code "LingvoUniversal (En-Ru)/LingvoUniversal.dict.dz"} -- matching
 * {@link net.bancer.sparkdict.domain.core.FileDictionaryFiles}'s scheme.
 *
 * <p>Uses {@link DocumentsContract} directly rather than
 * {@link androidx.documentfile.provider.DocumentFile}, for performance --
 * see the earlier revision's notes. One behaviour DocumentFile provided
 * "for free" that this class must handle explicitly: any provider call can
 * throw {@link SecurityException} if the tree Uri's permission was never
 * granted, or has since been revoked (e.g. the user revoked storage access
 * via system Settings while this object is still alive). Every provider
 * call is wrapped accordingly, treating a denied permission the same as
 * "not found" rather than letting an unchecked exception escape methods
 * whose contract otherwise looks like ordinary I/O.</p>
 *
 * <p>Folder/file lookups cache the resolved document ID for this object's
 * lifetime. The cache does not watch for external changes to the tree --
 * if dictionary files change on disk, or permission is revoked, while
 * this object is alive, a new instance should be constructed.</p>
 *
 * <p>{@link #createForWrite} does not create a missing parent folder --
 * every dictionary's folder is expected to already exist, since it was
 * found via {@link #findDictionaryMetaFilePaths()} in the first place.</p>
 */
public class SafDictionaryFiles implements DictionaryFiles {

    private static final String TAG = "SafDictionaryFiles";
    private static final String GENERIC_MIME_TYPE = "application/octet-stream";

    private final Context context;
    private final Uri treeUri;
    private final Logger logger;

    /** Caches folder name -> its document ID, to avoid repeat root scans. */
    private final Map<String, String> folderCache = new HashMap<>();

    /** Caches "folder/file" -> its document ID, to avoid repeat folder scans. */
    private final Map<String, String> fileCache = new HashMap<>();

    public SafDictionaryFiles(Context context, Uri treeUri) {
        this(context, treeUri, new ConsoleLogger());
    }

    public SafDictionaryFiles(Context context, Uri treeUri, Logger logger) {
        this.context = context.getApplicationContext();
        this.treeUri = treeUri;
        this.logger = logger;
    }

    @Override
    public List<String> findDictionaryMetaFilePaths() {
        List<String> result = new ArrayList<>();
        if (treeUri == null || treeUri.toString().isEmpty()) {
            return result;
        }
        String rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
        for (ChildDocument folder : queryChildren(rootDocumentId)) {
            if (!folder.isDirectory) {
                continue;
            }
            folderCache.put(folder.name, folder.documentId);
            for (ChildDocument file : queryChildren(folder.documentId)) {
                String relativePath = folder.name + "/" + file.name;
                fileCache.put(relativePath, file.documentId);
                if (file.name.endsWith(BookInfo.INFO_FILE_EXTENTION)) {
                    result.add(relativePath);
                }
            }
        }
        return result;
    }

    @Override
    public boolean exists(String path) {
        return resolveFile(path, false) != null;
    }

    @Override
    public SeekableByteChannel openForRead(String path) throws IOException {
        String documentId = resolveFile(path, false);
        if (documentId == null) {
            throw new FileNotFoundException(path);
        }
        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(fileUri, "r");
            if (pfd == null) {
                throw new FileNotFoundException(path);
            }
            FileInputStream stream = new FileInputStream(pfd.getFileDescriptor());
            return new ParcelFileDescriptorChannel(pfd, stream, stream.getChannel());
        } catch (SecurityException e) {
            throw new IOException("No permission to open document for reading: " + path, e);
        }
    }

    @Override
    public OutputStream createForWrite(String path) throws IOException {
        String documentId = resolveFile(path, true);
        if (documentId == null) {
            throw new IOException("Cannot create or find document: " + path);
        }
        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
        try {
            OutputStream out = context.getContentResolver().openOutputStream(fileUri, "wt");
            if (out == null) {
                throw new IOException("Cannot open document for writing: " + path);
            }
            return out;
        } catch (SecurityException e) {
            throw new IOException("No permission to open document for writing: " + path, e);
        }
    }

    @Override
    public boolean delete(String path) {
        String documentId = resolveFile(path, false);
        if (documentId == null) {
            return false;
        }
        fileCache.remove(path);
        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
        try {
            return DocumentsContract.deleteDocument(context.getContentResolver(), fileUri);
        } catch (SecurityException | FileNotFoundException e) {
            logger.error(TAG, "No permission to delete document: " + path, e);
            return false;
        }
    }

    /**
     * Resolves a "folder/name" path to the file's document ID, using and
     * populating the caches. If {@code create} is true and the file does
     * not already exist, it is created. The parent folder is expected to
     * already exist and is never created here.
     *
     * @return the file's document ID, or {@code null} if it does not
     * exist and {@code create} is {@code false}, its folder does not
     * exist, or access is denied.
     */
    private String resolveFile(String path, boolean create) {
        String cached = fileCache.get(path);
        if (cached != null) {
            return cached;
        }
        int slash = path.indexOf('/');
        if (slash < 0) {
            return null;
        }
        String folderName = path.substring(0, slash);
        String fileName = path.substring(slash + 1);
        String folderDocumentId = resolveFolder(folderName);
        if (folderDocumentId == null) {
            return null;
        }
        for (ChildDocument child : queryChildren(folderDocumentId)) {
            if (child.name.equals(fileName)) {
                fileCache.put(path, child.documentId);
                return child.documentId;
            }
        }
        if (!create) {
            return null;
        }
        Uri folderUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, folderDocumentId);
        try {
            Uri newFileUri = DocumentsContract.createDocument(
                context.getContentResolver(),
                folderUri,
                GENERIC_MIME_TYPE,
                fileName
            );
            if (newFileUri == null) {
                return null;
            }
            String newDocumentId = DocumentsContract.getDocumentId(newFileUri);
            fileCache.put(path, newDocumentId);
            return newDocumentId;
        } catch (SecurityException | IOException e) {
            logger.error(TAG, "No permission to create document: " + path, e);
            return null;
        }
    }

    /**
     * Resolves a folder name directly under the tree root to its document
     * ID, using and populating the folder cache. Never creates a folder.
     */
    private String resolveFolder(String folderName) {
        String cached = folderCache.get(folderName);
        if (cached != null) {
            return cached;
        }
        String rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
        for (ChildDocument child : queryChildren(rootDocumentId)) {
            if (child.isDirectory && child.name.equals(folderName)) {
                folderCache.put(folderName, child.documentId);
                return child.documentId;
            }
        }
        return null;
    }

    /**
     * Lists the immediate children of the specified parent document,
     * fetching document ID, display name, and MIME type for all of them in
     * a single query. Treats a permission denial the same as "no
     * children" -- see class documentation -- rather than propagating an
     * unchecked exception, matching what DocumentFile did implicitly.
     */
    private List<ChildDocument> queryChildren(String parentDocumentId) {
        List<ChildDocument> result = new ArrayList<>();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId);
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        };
        try (Cursor cursor = resolver.query(childrenUri, projection, null, null, null)) {
            if (cursor == null) {
                return result;
            }
            int documentIdIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int displayNameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeTypeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(documentIdIndex);
                String name = cursor.getString(displayNameIndex);
                String mimeType = cursor.getString(mimeTypeIndex);
                boolean isDirectory = DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);
                result.add(new ChildDocument(documentId, name, isDirectory));
            }
        } catch (SecurityException e) {
            logger.error(TAG, "No permission to list children of document: " + parentDocumentId, e);
        }
        return result;
    }

    private static final class ChildDocument {
        private final String documentId;
        private final String name;
        private final boolean isDirectory;

        private ChildDocument(String documentId, String name, boolean isDirectory) {
            this.documentId = documentId;
            this.name = name;
            this.isDirectory = isDirectory;
        }
    }
}
