package net.bancer.sparkdict.domain.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link DictionaryFiles} implementation backed by ordinary
 * {@code java.io.File} access to a root directory on the filesystem.
 *
 * <p>{@link #findDictionaryMetaFilePaths()} reproduces exactly the
 * directory-scanning behaviour {@link Shelf} used before
 * {@link DictionaryFiles} was introduced: dictionaries are found up to two
 * directory levels below the root path. {@link #openForRead} deliberately
 * uses {@link FileChannel#open} rather than wrapping a
 * {@link java.io.RandomAccessFile}, since discarding the
 * {@code RandomAccessFile} after taking only its channel is exactly the
 * finaliser-driven {@code ClosedChannelException} bug already found and
 * fixed in {@link DictZipFile} and {@link StarDictIndex} -- there is no
 * reason to reintroduce that risk in new code.</p>
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
        File[] dictFolders = new File(rootPath).listFiles();
        if (dictFolders != null) {
            for (File dictFolder : dictFolders) {
                if (dictFolder.isDirectory()) {
                    File[] dictFiles = dictFolder.listFiles();
                    if (dictFiles != null) {
                        for (File file : dictFiles) {
                            if (file.toString().endsWith(BookInfo.INFO_FILE_EXTENTION)) {
                                result.add(file.toString());
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
        return FileChannel.open(Paths.get(path), StandardOpenOption.READ);
    }

    @Override
    public OutputStream createForWrite(String path) throws IOException {
        return Files.newOutputStream(Paths.get(path));
    }

    @Override
    public boolean delete(String path) {
        return new File(path).delete();
    }
}
