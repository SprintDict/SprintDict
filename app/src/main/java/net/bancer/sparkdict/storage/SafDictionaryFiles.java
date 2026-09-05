package net.bancer.sparkdict.storage;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.documentfile.provider.DocumentFile;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.DictionaryFiles;

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
 * <p>{@code DocumentFile.findFile()} runs a fresh content-provider query
 * every time it's called. That was directly responsible for the multi-minute
 * res.zip regression found earlier in this migration. To avoid repeating
 * that mistake, this class caches each resolved folder and file the first
 * time it's looked up. The cache is built fresh for the lifetime of this
 * object and does not watch for external changes to the tree -- if
 * dictionary files change on disk while this object is alive, a new
 * instance should be constructed.</p>
 *
 * <p><b>Not yet exercised by any real caller</b> -- see {@code AppConfig}.
 * As a result this class has not been verified on-device, and two SAF
 * behaviours specifically need checking once it is wired up: whether
 * {@code DocumentFile.createFile()} always honours the exact display name
 * requested (some providers adjust names based on the guessed MIME type),
 * and how {@code openOutputStream(uri, "wt")} behaves across the range of
 * providers this app is likely to see (SD card vs. internal storage).</p>
 */
public class SafDictionaryFiles implements DictionaryFiles {

    private static final String GENERIC_MIME_TYPE = "application/octet-stream";

    private final Context context;

    private final Uri treeUri;

    /** Caches folder name -> DocumentFile, to avoid repeat root scans. */
    private final Map<String, DocumentFile> folderCache = new HashMap<>();

    /** Caches "folder/file" -> DocumentFile, to avoid repeat folder scans. */
    private final Map<String, DocumentFile> fileCache = new HashMap<>();

    public SafDictionaryFiles(Context context, Uri treeUri) {
        this.context = context.getApplicationContext();
        this.treeUri = treeUri;
    }

    @Override
    public List<String> findDictionaryMetaFilePaths() {
        List<String> result = new ArrayList<>();
        if (treeUri == null || treeUri.toString().isEmpty()) {
            return result;
        }
        DocumentFile root = DocumentFile.fromTreeUri(context, treeUri);
        if (root == null) {
            return result;
        }
        for (DocumentFile folder : root.listFiles()) {
            if (!folder.isDirectory() || folder.getName() == null) {
                continue;
            }
            folderCache.put(folder.getName(), folder);
            for (DocumentFile file : folder.listFiles()) {
                String name = file.getName();
                if (name != null && name.endsWith(BookInfo.INFO_FILE_EXTENTION)) {
                    String relativePath = folder.getName() + "/" + name;
                    fileCache.put(relativePath, file);
                    result.add(relativePath);
                }
            }
        }
        return result;
    }

    @Override
    public SeekableByteChannel openForRead(String path) throws IOException {
        DocumentFile file = resolve(path, false);
        if (file == null) {
            throw new FileNotFoundException(path);
        }
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(file.getUri(), "r");
        if (pfd == null) {
            throw new FileNotFoundException(path);
        }
        FileInputStream stream = new FileInputStream(pfd.getFileDescriptor());
        return new ParcelFileDescriptorChannel(pfd, stream, stream.getChannel());
    }

    @Override
    public OutputStream createForWrite(String path) throws IOException {
        DocumentFile file = resolve(path, true);
        if (file == null) {
            throw new IOException("Cannot create or find document: " + path);
        }
        OutputStream out = context.getContentResolver().openOutputStream(file.getUri(), "wt");
        if (out == null) {
            throw new IOException("Cannot open document for writing: " + path);
        }
        return out;
    }

    @Override
    public boolean delete(String path) {
        DocumentFile file;
        try {
            file = resolve(path, false);
        } catch (IOException e) {
            return false;
        }
        return file != null && file.delete();
    }

    /**
     * Resolves a "folder/name" path to its DocumentFile, using and
     * populating the caches. If {@code create} is true, both the folder and
     * the file are created if they do not already exist.
     */
    private DocumentFile resolve(String path, boolean create) throws IOException {
        DocumentFile cached = fileCache.get(path);
        if (cached != null) {
            return cached;
        }
        int slash = path.indexOf('/');
        if (slash < 0) {
            throw new IOException("Expected a 'folder/name' path, got: " + path);
        }
        String folderName = path.substring(0, slash);
        String fileName = path.substring(slash + 1);
        DocumentFile folder = folderCache.get(folderName);
        if (folder == null) {
            DocumentFile root = DocumentFile.fromTreeUri(context, treeUri);
            if (root == null) {
                throw new IOException("Cannot resolve dictionary root");
            }
            folder = root.findFile(folderName);
            if (folder == null) {
                if (!create) {
                    return null;
                }
                folder = root.createDirectory(folderName);
                if (folder == null) {
                    throw new IOException("Cannot create directory: " + folderName);
                }
            }
            folderCache.put(folderName, folder);
        }
        DocumentFile file = folder.findFile(fileName);
        if (file == null && create) {
            file = folder.createFile(GENERIC_MIME_TYPE, fileName);
            if (file == null) {
                throw new IOException("Cannot create document: " + fileName);
            }
        }
        if (file != null) {
            fileCache.put(path, file);
        }
        return file;
    }
}
