package net.bancer.sparkdict.domain.core;

import net.bancer.sparkdict.domain.utils.DomainException;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;
import java.util.Vector;

/**
 * Book is an abstraction of a dictionary containing lexical entries and index
 * entries.
 */
public class Book implements Iterable<IndexEntry>, Closeable {

    /**
     * Extension of the compressed dictionary file: <dictionary name>.dict.dz
     */
    public static final String DICT_FILE_EXTENSION = ".dict.dz";

    /**
     * ZIP archive containing dictionary resources such as audio files and pictures.
     */
    public static final String RES_ZIP_NAME = "res.zip";

    /**
     * BookInfo object.
     */
    private final BookInfo bookInfo;

    /**
     * Flag indicating whether the dictionary is enabled (searchable) or not.
     */
    private boolean enabled = false;

    /**
     * DictZipFile object.
     */
    private DictZipFile dzFile = null;

    private SeekableByteChannel dictZipChannel = null;

    /**
     * ZIP archive containing the resource.
     */
    private ResourcesZipFile resZipFile = null;

    /**
     * Index entries iterator.
     */
    private Iterator<IndexEntry> indexEntriesIterator;

    private final DictionaryFiles dictionaryFiles;

    /**
     * Constructor.
     *
     * @param relativeIfoPath root-relative path to the .ifo document.
     * @param dictionaryFiles the DictionaryFiles to associate with this book.
     */
    public Book(String relativeIfoPath, DictionaryFiles dictionaryFiles) {
        this.dictionaryFiles = dictionaryFiles;
        bookInfo = new BookInfo(relativeIfoPath, dictionaryFiles);
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
     * Figures out what different strings could match the provided string
     * if some of the letters would be in upper case.
     *
     * @param prefix string to be matched.
     * @return strings array of different variations of the prefix.
     */
    private static String[] getPrefixVariations(String prefix) {
        String[] result = new String[4];
        result[0] = prefix;
        result[1] = prefix.toLowerCase();
        result[2] = prefix.toUpperCase();
        result[3] = capitalizeString(prefix);
        for (int i = 1; i < result.length; i++) {
            if (prefix.equals(result[i])) {
                result[i] = null;
            }
        }
        return result;
    }

    /**
     * Converts the first letter of every word in the provided string to upper case.
     *
     * @param prefix string to be capitalised.
     * @return capitalized string.
     */
    private static String capitalizeString(String prefix) {
        char[] chars = prefix.toLowerCase().toCharArray();
        boolean previousCharIsLetter = false;
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (!previousCharIsLetter) {
                    chars[i] = Character.toUpperCase(chars[i]);
                }
                previousCharIsLetter = true;
            } else {
                previousCharIsLetter = false;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * Checks if the string contains only ASCII characters.
     *
     * @param str string to be verified.
     * @return `true` if the string contains only ASCII characters, else `false`.
     */
    private static boolean isAsciiString(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);
            if ((int) c > 127) { // if contains non-ASCII character
                return false;
            }
        }
        return true;
    }

    /**
     * BookInfo getter.
     *
     * @return bookInfo object.
     */
    public BookInfo getInfo() {
        return bookInfo;
    }

    /**
     * Enabled flag field getter.
     *
     * @return `true` if the book is enabled, else `false`.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enabled flag field mutator.
     *
     * @param enabled `true` to enable, `false` to disable.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Lexical entry retriever. Retrieves and constructs LexicalEntry
     * corresponding to the index entry provided as the parameter.
     *
     * @param idxEntry index entry for what the lexical entry is requested.
     * @return LexicalEntry object or `null`.
     */
    private LexicalEntry getLexicalEntry(IndexEntry idxEntry) {
        try {
            if (dzFile == null) {
                dictZipChannel = dictionaryFiles.openForRead(bookInfo.getFileBaseName() + DICT_FILE_EXTENSION);
                dzFile = new DictZipFile(dictZipChannel);
            }
            if (resZipFile == null) {
                String resZipPath = bookInfo.getDirPath() + "/" + RES_ZIP_NAME;
                resZipFile = new ResourcesZipFile(resZipPath, dictionaryFiles);
            }
            byte[] buffer = dzFile.read(idxEntry.getWordDataOffset(), idxEntry.getWordDataSize());
            String lemma = idxEntry.getLemma();
            return new LexicalEntry(lemma, buffer, bookInfo, resZipFile);
        } catch (IOException e) {
            // TODO: log error
            return null;
        }
    }

    /**
     * Book name getter.
     *
     * @return the book title.
     */
    public String getBookName() {
        return bookInfo.getBookName();
    }

    /**
     * String representation of the book.
     */
    @Override
    @NotNull
    public String toString() {
        return bookInfo + "Enabled: " + enabled + "\n";
    }

    /**
     * Getter of the quantity of lexical entries.
     *
     * @return the quantity of lexical entries in this dictionary.
     */
    public int getLexicalEntriesQuantity() {
        return bookInfo.getWordCount();
    }

    /**
     * Lexical entry retriever. Retrieves and constructs LexicalEntry
     * that matches the string provided as the parameter.
     *
     * @param lemma lemma of the lexical entry to be retrieved.
     * @return LexicalEntry if the match is found, else `null`.
     * @throws DomainException when errored while retrieving the data.
     */
    public LexicalEntry getLexicalEntry(String lemma) throws DomainException {
        LexicalEntry result = null;
        IndexEntriesIterator iterator = (IndexEntriesIterator) iterator();
        if (!iterator.hasNext()) {
            //TODO: send notification - probably index file is missing.
            return null;
        }
        IndexEntry indexEntry = iterator.findIndexEntry(lemma);
        while (indexEntry != null && indexEntry.getLemma().equals(lemma)) {
            LexicalEntry lexicalEntry = getLexicalEntry(indexEntry);
            if (result == null) {
                result = lexicalEntry;
            } else {
                String definitions = result.getDefinitions();
                if (lexicalEntry != null) {
                    definitions += "<br><br>" + lexicalEntry.getDefinitions();
                }
                result.setDefinitions(definitions);
            }
            indexEntry = iterator.next();
        }
        return result;
    }

    /**
     * Constructs SparkDictIndex.
     *
     * @param observer observer to be attached to SparkDictIndex object.
     * @throws DomainException when failed to build an index.
     */
    public void buildSparkDictIndex(IObserver observer) throws DomainException {
        SparkDictIndex sparkDictIndex = new SparkDictIndex(bookInfo);
        sparkDictIndex.registerObserver(observer);
        try {
            sparkDictIndex.buildIndex();
        } catch (IOException e) {
            String message = "Cannot build index for `" + bookInfo.getBookName() + "` dictionary.";
            throw new DomainException(message, e);
        }
    }

    /**
     * Returns an Iterator for index entries.
     */
    @Override
    @NotNull
    public Iterator<IndexEntry> iterator() {
        if (indexEntriesIterator == null) {
            try {
                indexEntriesIterator = new IndexEntriesIterator(bookInfo);
            } catch (DomainException e) {
                // TODO log error
            }
        }
        return indexEntriesIterator;
    }

    /**
     * Retrieves a list of index entries which lemmas starts with provided prefix.
     *
     * @param prefix prefix to be matched.
     * @return a collection of index entries.
     */
    public Vector<IndexEntry> getSuggestions(String prefix) {
        Vector<IndexEntry> result = new Vector<>(IndexEntriesIterator.MAX);
        IndexEntriesIterator iterator = (IndexEntriesIterator) iterator();
        if (!iterator.hasNext()) {
            //TODO: send notification - probably index file is missing.
            return result;
        }
        String[] prefixVariations;
        if (isAsciiString(prefix)) {
            prefixVariations = new String[1];
            prefixVariations[0] = prefix;
        } else { // get suggestions for all variations of the prefix (aspROvided, lowercase, UPPERCASE, Capitalized)
            prefixVariations = getPrefixVariations(prefix);
        }
        for (String prefixVariation : prefixVariations) {
            if (prefixVariation != null) {
                try {
                    IndexEntry entry = iterator.nextSuggestion(prefixVariation);
                    while (entry != null) {
                        result.add(entry);
                        entry = iterator.nextSuggestion(prefixVariation);
                    }
                } catch (DomainException e) {
                    // TODO: log error
                }
            }
        }
        return result;
    }

    /**
     * Closes the dictionary and resource files and releases their resources.
     *
     * <p>If either resource is not currently open, it is ignored. The resources
     * can be reopened automatically when they are needed again.</p>
     */
    public void close() {
        if (dzFile != null) {
            dzFile = null;
        }
        if (dictZipChannel != null) {
            try {
                dictZipChannel.close();
            } catch (IOException e) {
                //TODO: log "Cannot close .dict.dz channel"
            } finally {
                dictZipChannel = null;
            }
        }
        if (resZipFile != null) {
            resZipFile.close();
            resZipFile = null;
        }
    }
}
