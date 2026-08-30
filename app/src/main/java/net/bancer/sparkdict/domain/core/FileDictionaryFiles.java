package net.bancer.sparkdict.domain.core;

import net.bancer.sparkdict.storage.SafDictionaryFiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link DictionaryFiles} implementation backed by ordinary
 * {@code java.io.File} access to a root directory on the filesystem.
 *
 * <p>All paths accepted and returned by this class are root-relative, using
 * {@code /} as a separator -- e.g. {@code "LingvoUniversal (En-Ru)/LingvoUniversal.dict.dz"}
 * -- matching the two-level "dictionary-folder/file-name" structure Shelf
 * has always assumed. This mirrors {@link SafDictionaryFiles}'s addressing
 * scheme, so callers never need to know which backend they're talking to.</p>
 *
 * <p>{@link #openForRead} deliberately uses {@link FileChannel#open} rather
 * than wrapping a {@link java.io.RandomAccessFile}: discarding the
 * {@code RandomAccessFile} after taking only its channel is exactly the
 * finalizer-driven {@code ClosedChannelException} bug already found and
 * fixed in {@link DictZipFile} and {@link StarDictIndex}.</p>
 */
public class FileDictionaryFiles implements DictionaryFiles {

    private final String rootPath;

    /**
     * Creates a DictionaryFiles instance rooted at the specified directory.
     *
     * @param rootPath path to the directory containing dictionary folders.
     */
    public FileDictionaryFiles(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public List<String> findDictionaryMetaFilePaths() {
        List<String> result = new ArrayList<>();
        File root = new File(rootPath);
        File[] dictFolders = root.listFiles();
        if (dictFolders != null) {
            for (File dictFolder : dictFolders) {
                if (dictFolder.isDirectory()) {
                    File[] dictFiles = dictFolder.listFiles();
                    if (dictFiles != null) {
                        for (File file : dictFiles) {
                            if (file.toString().endsWith(BookInfo.INFO_FILE_EXTENTION)) {
                                result.add(root.toPath().relativize(file.toPath()).toString());
                            }
                        }
                    }
                }
            }
        } //else {
        //TODO: log "Failed to list files in " + rootPath
        //}
        return result;
    }

    @Override
    public SeekableByteChannel openForRead(String path) throws IOException {
        return FileChannel.open(resolve(path), StandardOpenOption.READ);
    }

    @Override
    public OutputStream createForWrite(String path) throws IOException {
        return new FileOutputStream(resolve(path).toFile());
    }

    @Override
    public boolean delete(String path) {
        return resolve(path).toFile().delete();
    }

    private Path resolve(String path) {
        return Paths.get(rootPath).resolve(path);
    }
}
