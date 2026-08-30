package net.bancer.sparkdict.domain.core;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Scanner;

/**
 * BookInfo is an abstraction of <dictionary name>.ifo file.
 */
public class BookInfo {

    public static String INFO_FILE_EXTENTION = ".ifo";

    private final String filePath;

    /**
     * Path to the folder containing current dictionary files.
     *
     * <p>When this BookInfo was constructed from a real {@code java.io.File}
     * (see {@link #BookInfo(File)}), this is an absolute filesystem path.
     * When constructed via {@link #BookInfo(String, DictionaryFiles)}, this
     * is a root-relative folder name instead -- e.g.
     * {@code "LingvoUniversal (En-Ru)"} -- meant only to be passed back into
     * a {@link DictionaryFiles}, never into {@code new File(...)}.</p>
     */
    private final String dirPath;

    private String version;

    /**
     * required
     */
    private String bookName;

    /**
     * wordCount is the count of word entries in .idx file
     * <p>
     * required
     */
    private int wordCount;

    /**
     * required if ".syn" file exists
     */
    private int synWordCount;

    /**
     * idxFileSize is the size(in bytes) of the .idx file,
     * even the .idx is compressed to a .idx.gz file,
     * this entry must record the original .idx file's size,
     * and it must be right too.
     * <p>
     * required
     */
    private int idxFileSize;

    /**
     * idxOffsetBits can be 64 or 32.
     * If "idxOffsetBits=64", the offset field of the .idx file will be 64 bits.
     * <p>
     * New in 3.0.0
     */
    private int idxOffsetBits = 32;

    private String author;

    private String email;

    private String website;

    /**
     * `&lt;br&gt;` can be used for new line.
     */
    private String description;

    private String date;

    /**
     * If the sametypesequence option is set, it tells StarDict that each
     * word's data in the .dict file will have the same sequence of datatypes.
     * In this case, we expect a .dict file that's been optimised in two
     * ways: the type identifiers should be omitted, and the size marker for
     * the last data entry of each word should be omitted.
     * <p>
     * x - <a href="http://xdxf.revdanica.com/drafts/visual/latest/XDXF-draft-028.txt">...</a>, <a href="http://kenai.com/projects/xdxf-parser">...</a>
     * <p>
     * very important
     */
    private String sameTypeSequence;

    private String dictType;

    private DictionaryFiles dictionaryFiles;

    /**
     * Constructor.
     *
     * @param path Full path to .ifo file including extension itself.
     */
    public BookInfo(String path) {
        this(new File(path));
    }

    /**
     * Constructor for callers that resolve dictionary files through a
     * {@link DictionaryFiles} implementation rather than a real
     * {@code java.io.File} -- most importantly
     * {@link net.bancer.sparkdict.storage.SafDictionaryFiles}. There is no
     * filesystem File backing this BookInfo at all: {@code relativeIfoPath}
     * and everything derived from it (see {@link #getFileBaseName()},
     * {@link #getDirPath()}) are root-relative identifiers, meant only to be
     * passed back into {@code dictionaryFiles}, never into {@code new File(...)}.
     *
     * @param relativeIfoPath root-relative path to the .ifo document, e.g.
     *                        {@code "LingvoUniversal (En-Ru)/LingvoUniversal.ifo"}.
     * @param dictionaryFiles the DictionaryFiles used to both read the .ifo
     *                        content and resolve this book's sibling files.
     * @throws IllegalArgumentException if relativeIfoPath is null or does
     *                                  not end with .ifo.
     */
    public BookInfo(String relativeIfoPath, DictionaryFiles dictionaryFiles) {
        if (relativeIfoPath == null || !relativeIfoPath.toLowerCase().endsWith(INFO_FILE_EXTENTION)) {
            throw new IllegalArgumentException("relativeIfoPath must have .ifo extension");
        }
        filePath = relativeIfoPath;
        int slash = relativeIfoPath.lastIndexOf('/');
        dirPath = slash >= 0 ? relativeIfoPath.substring(0, slash) : "";
        this.dictionaryFiles = dictionaryFiles;
        try (SeekableByteChannel channel = dictionaryFiles.openForRead(relativeIfoPath)) {
            Scanner input = new Scanner(Channels.newInputStream(channel), "UTF-8");
            parseIfoContent(input);
        } catch (IOException e) {
            //TODO: "Cannot read info file: " + relativeIfoPath
        }
    }

    /**
     * Constructor.
     *
     * @param infoFile java.io.File object of <dictionary name>.ifo file.
     */
    public BookInfo(File infoFile) {
        if (infoFile == null) {
            throw new IllegalArgumentException("infoFile must not be null or empty");
        }
        if (!infoFile.getName().toLowerCase().endsWith(INFO_FILE_EXTENTION)) {
            throw new IllegalArgumentException("infoFile must have .ifo extension");
        }
        filePath = infoFile.toString();
        dirPath = infoFile.getParent();
        // NOTE: this default roots FileDictionaryFiles at this book's own
        // folder, not at the overall dictionaries root -- which is the
        // wrong root per FileDictionaryFiles' root-relative contract. It's
        // harmless today because nothing calls getDictionaryFiles() for real
        // I/O on a bare BookInfo(File) -- Book keeps its own, correctly
        // rooted DictionaryFiles field instead (see Book.defaultDictionaryFilesFor).
        // Worth fixing or removing if this default is ever relied upon directly.
        dictionaryFiles = new FileDictionaryFiles(dirPath);
        try {
            Scanner input = new Scanner(infoFile, "UTF-8");
            parseIfoContent(input);
        } catch (FileNotFoundException e) {
            //TODO: "Cannot read info file: " + infoFile
        }
    }

    /**
     * Constructor for callers that want to supply the {@link DictionaryFiles}
     * used for this dictionary explicitly, rather than the default
     * {@link FileDictionaryFiles} this class would otherwise construct.
     *
     * @param infoFile java.io.File object of <dictionary name>.ifo file.
     * @param dictionaryFiles the DictionaryFiles to associate with this book.
     */
    public BookInfo(File infoFile, DictionaryFiles dictionaryFiles) {
        this(infoFile);
        this.dictionaryFiles = dictionaryFiles;
    }

    /**
     * Parses .ifo content, line by line, into this object's fields.
     * Shared by every constructor regardless of where the content came from.
     *
     * @param input a Scanner positioned at the start of the .ifo content.
     */
    private void parseIfoContent(Scanner input) {
        input.useDelimiter("\n");
        while (input.hasNextLine()) {
            String line = input.nextLine();
            if (line.contains("version=")) {
                version = line.substring(8);
            } else if (line.contains("bookname=")) {
                bookName = line.substring(9);
            } else if (line.contains("synwordcount=")) {
                synWordCount = Integer.parseInt(line.substring(13));
            } else if (line.contains("wordcount=")) {
                wordCount = Integer.parseInt(line.substring(10));
            } else if (line.contains("idxfilesize=")) {
                idxFileSize = Integer.parseInt(line.substring(12));
            } else if (line.contains("idxoffsetbits=")) {
                idxOffsetBits = Integer.parseInt(line.substring(14));
            } else if (line.contains("author=")) {
                author = line.substring(7);
            } else if (line.contains("email=")) {
                email = line.substring(6);
            } else if (line.contains("website=")) {
                website = line.substring(8);
            } else if (line.contains("description=")) {
                description = line.substring(12);
            } else if (line.contains("date=")) {
                date = line.substring(5);
            } else if (line.contains("sametypesequence=")) {
                sameTypeSequence = line.substring(17);
            } else if (line.contains("dicttype=")) {
                dictType = line.substring(9);
            }
        }
    }

    /**
     * DictionaryFiles getter.
     *
     * @return the DictionaryFiles associated with this book.
     */
    public DictionaryFiles getDictionaryFiles() {
        return dictionaryFiles;
    }

    /**
     * String representation of the BookInfo object (for debug  only).
     */
    @Override
    @NonNull
    public String toString() {
        String result = "";
        result += "\nVersion: " + version + "\n";
        result += "Dictionary name: " + bookName + "\n";
        result += "Words: " + wordCount + "\n";
        result += "Synonyms: " + synWordCount + "\n";
        result += "Index file size: " + idxFileSize + "\n";
        result += "Index offset bits: " + idxOffsetBits + "\n";
        result += "Author: " + author + "\n";
        result += "Email: " + email + "\n";
        result += "Website: " + website + "\n";
        result += "Description: " + description + "\n";
        result += "Date: " + date + "\n";
        result += "Same type sequence: " + sameTypeSequence + "\n";
        result += "Dictionary type: " + dictType + "\n";
        result += "Path: " + filePath + "\n";
        return result;
    }

    /**
     * Version getter.
     *
     * @return dictionary version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Book name getter.
     *
     * @return the title of the dictionary.
     */
    public String getBookName() {
        return bookName;
    }

    /**
     * Words quantity getter.
     *
     * @return quantity of dictionary entries.
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Index file size getter.
     *
     * @return the size of index file.
     */
    public int getIdxFileSize() {
        return idxFileSize;
    }

    /**
     * Index offset bits getter.
     *
     * @return index offset bits size.
     */
    public int getIdxOffsetBits() {
        return idxOffsetBits;
    }

    /**
     * Description getter.
     *
     * @return description of the dictionary.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Date getter.
     *
     * @return the date of the dictionary file.
     */
    public String getDate() {
        return date;
    }

    /**
     * Same data type sequence getter.
     *
     * @return same data type sequence.
     */
    public String getSameTypeSequence() {
        return sameTypeSequence;
    }

    /**
     * File base name getter.
     *
     * @return path to the <dictionary name>.ifo file excluding ".ifo" part.
     * Root-relative if this BookInfo was constructed via
     * {@link #BookInfo(String, DictionaryFiles)}, absolute otherwise.
     */
    public String getFileBaseName() {
        int end = filePath.length() - 4;
        return filePath.substring(0, end);
    }

    /**
     * Compressed dictionary file path getter.
     *
     * @return path to the dictionary data file including ".dict.dz" at the
     * end. Root-relative or absolute, matching {@link #getFileBaseName()}.
     */
    public String getPathToDictFile() {
        return getFileBaseName() + ".dict.dz";
    }

    public String getDirPath() {
        return dirPath;
    }
}
