package net.bancer.sparkdict.domain.core;

import android.util.Log;

import androidx.annotation.NonNull;

import net.bancer.sparkdict.domain.parsers.IParser;
import net.bancer.sparkdict.domain.parsers.ParsingStrategyFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * LexicalEntry is the entry in a dictionary of information about a word
 * (syn: dictionary entry).
 */
public class LexicalEntry {

    private static final String TAG = "LexicalEntry";

    private static final ParsingStrategyFactory parsersFactory = ParsingStrategyFactory.getInstance();

    private final String lemma;

    private final String dictTitle;

    private final BookInfo bookInfo;

    private String definitions;

    /**
     * ZIP archive containing dictionary resources such as audio files and images.
     */
    private ResourcesZipFile resZipFile = null;

    /**
     * Constructor.
     *
     * @param lemma      lemma of the lexical entry.
     * @param dataBlocks bytes array of data from <dictionary name>.dict file.
     * @param bookInfo   BookInfo object.
     */
    public LexicalEntry(String lemma, byte[] dataBlocks, BookInfo bookInfo) {
        this.bookInfo = bookInfo;
        this.lemma = lemma;
        this.dictTitle = bookInfo.getBookName();
        String dataTypes = bookInfo.getSameTypeSequence();
        if (dataTypes == null) {
            dataTypes = "";
        }
        char[] dataType = dataTypes.toCharArray();
        if (dataType.length == 1) {
            //sametypesequence is one character
            setDefinitions(dataBlocks, dataType[0]);
        } else if (dataType.length > 1) {
            //sametypesequence is more than one character
            setDefinitions(dataBlocks, dataType);
        } else {
            //sametypesequence is not set
            setDefinitions(dataBlocks);
        }
    }

    /**
     * Creates a lexical entry from the specified dictionary data.
     *
     * @param lemma      lemma of the lexical entry.
     * @param buffer     dictionary data containing the lexical entry.
     * @param bookInfo   information about the dictionary containing the entry.
     * @param resZipFile ZIP archive containing the dictionary resources, or {@code null}
     *                   *               if resources are stored as individual files.
     */
    public LexicalEntry(String lemma, byte[] buffer, BookInfo bookInfo, ResourcesZipFile resZipFile) {
        this(lemma, buffer, bookInfo);
        this.resZipFile = resZipFile;
    }

    /**
     * Parses the data blocks using the parsers associated with their data types
     * and combines the parsed definitions into a single string.
     *
     * <p>Data blocks are separated by {@link StarDictIndex#SEPARATOR}. The parsed
     * definitions are separated by newline characters in the resulting string.</p>
     *
     * @param dataBlocks raw data containing one or more separated data blocks
     * @param dataTypes data types corresponding to the data blocks
     */
    private void setDefinitions(byte[] dataBlocks, char[] dataTypes) {
        definitions = "";
        StringBuilder definitionsBuilder = new StringBuilder();
        int dataBlockStart = 0;
        int dataBlockLength = 0;
        int dataBlockIdx = 0;
        for (int i = 0; i <= dataBlocks.length; i++) {
            if (i == dataBlocks.length || dataBlocks[i] == StarDictIndex.SEPARATOR) {
                byte[] data = new byte[dataBlockLength];
                System.arraycopy(dataBlocks, dataBlockStart, data, 0, dataBlockLength);
                IParser parser = parsersFactory.getParser(dataTypes[dataBlockIdx]);
                if (definitionsBuilder.length() > 0) {
                    definitionsBuilder.append("\n");
                }
                definitionsBuilder.append(parser.parse(data));
                dataBlockStart = i + 1;
                dataBlockLength = 0;
                dataBlockIdx++;
            } else {
                dataBlockLength++;
            }
        }
        definitions = definitionsBuilder.toString();
    }

    /**
     * Parses the data block using the parser associated with the specified data type
     * and stores the resulting definition.
     *
     * @param dataBlock raw data block to parse
     * @param dataType data type used to select the parser
     */
    private void setDefinitions(byte[] dataBlock, char dataType) {
        IParser parser = parsersFactory.getParser(dataType);
        definitions = parser.parse(dataBlock);
    }

    /**
     * Lemma getter.
     *
     * @return lemma of the lexical entry.
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * Definitions getter.
     *
     * @return the definitions of the lexical entry.
     */
    public String getDefinitions() {
        return definitions;
    }

    /**
     * Parses StarDict data blocks and appends their parsed content to the definitions.
     *
     * <p>Each data block starts with a one-byte type identifier. Lowercase type
     * identifiers are followed by NUL-terminated data, while uppercase type
     * identifiers are followed by a four-byte big-endian length and then the
     * specified number of bytes.</p>
     * <p>The format is effectively:
     * [type][data]\0
     * for lowercase types, and:
     * [type][4-byte length][data]
     * for uppercase types.</p>
     *
     * @param dataBlocks the raw StarDict data blocks
     */
    private void setDefinitions(byte[] dataBlocks) {
        StringBuilder definitionsBuilder = new StringBuilder();
        int currentByte = 0;
        while (currentByte < dataBlocks.length) {
            char type = (char)dataBlocks[currentByte++];
            byte[] data;
            if (Character.isUpperCase(type)) {
                // 4-byte big-endian length prefix, then raw payload, no terminator.
                int len = ((dataBlocks[currentByte] & 0xFF) << 24)
                    | ((dataBlocks[currentByte + 1] & 0xFF) << 16)
                    | ((dataBlocks[currentByte + 2] & 0xFF) << 8)
                    | (dataBlocks[currentByte + 3] & 0xFF);
                currentByte += 4;
                data = new byte[len];
                System.arraycopy(dataBlocks, currentByte, data, 0, len);
                currentByte += len;
            } else {
                // Lowercase type: NUL-terminated.
                int start = currentByte;
                while (dataBlocks[currentByte] != 0) {
                    currentByte++;
                }
                data = new byte[currentByte - start];
                System.arraycopy(dataBlocks, start, data, 0, data.length);
                currentByte++; // Skip the terminating NUL.
            }
            IParser parser = parsersFactory.getParser(type);
            if (definitionsBuilder.length() > 0) {
                definitionsBuilder.append("\n");
            }
            definitionsBuilder.append(parser.parse(data));
        }
        definitions = definitionsBuilder.toString();
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    /**
     * Dictionary title getter.
     *
     * @return dictionary title.
     */
    public CharSequence getDictTitle() {
        return dictTitle;
    }

    /**
     * LexicalEntry as a string (for debug).
     */
    @Override
    @NonNull
    public String toString() {
        return "[" + dictTitle + "," + lemma + "," + definitions + "]";
    }

    /**
     * Retrieves the resource identified by the specified resource name.
     *
     * <p>If a resource ZIP archive is available, the resource is extracted from
     * the archive. Otherwise, the resource is read from the {@code res}
     * directory.</p>
     *
     * @param resourceName name of the resource to retrieve.
     * @return resource contents as a byte array, or an empty byte array if the resource cannot be found or read.
     */
    public byte[] getResource(String resourceName) {
        if (resZipFile != null) {
            return resZipFile.getResourceFromZip(resourceName);
        }
        return getResourceFromResFolder(resourceName);
    }

    private byte[] getResourceFromResFolder(String resourceName) {
        String path = bookInfo.getDirPath() + File.separator + "res" + File.separator + resourceName;
        File file = new File(path);
        if (file.exists()) {
            return readResourceFile(path);
        } else {
            Log.e(TAG, file + " does not exist");
        }
        return new byte[0];
    }

    /**
     * Reads the contents of the specified resource file.
     *
     * @param path path to the resource file.
     * @return resource contents as a byte array, or an empty byte array if the file cannot be read.
     */
    private byte[] readResourceFile(String path) {
        byte[] result = new byte[0];
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            result = new byte[(int) raf.length()];
            raf.read(result);
        } catch (IOException e) {
            Log.e(TAG, "Cannot read resource file: " + path);
        }
        return result;
    }
}
