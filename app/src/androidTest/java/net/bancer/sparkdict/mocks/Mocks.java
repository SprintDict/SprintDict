package net.bancer.sparkdict.mocks;

import android.os.Environment;

import net.bancer.sparkdict.domain.core.Book;
import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.IndexEntry;


public class Mocks {

    public static final String IDX_EXT = ".idx";
    public static final String DICT_EXT = ".dict.dz";
    public static final String ROOT_FOLDER = "dictionaries";
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ROOT_FOLDER;

    public static final String MUELLER_FOLDER = "mueller";
    public static final String MUELLER_FILE_BASE = "Mueller7GPL";
    public static final String MUELLER_IFO_PATH_RELATIVE = MUELLER_FOLDER + "/" + MUELLER_FILE_BASE + BookInfo.INFO_FILE_EXTENTION;
    public static final String MUELLER_IDX_PATH_RELATIVE = MUELLER_FOLDER + "/" + MUELLER_FILE_BASE + IDX_EXT;
    public static final String MUELLER_DICT_PATH = ROOT_PATH + "/" + MUELLER_FOLDER;
    public static final String MUELLER_BASE_PATH = MUELLER_DICT_PATH + "/" + MUELLER_FILE_BASE;
    public static final String MUELLER_IFO_PATH = ROOT_PATH + "/" + MUELLER_IFO_PATH_RELATIVE;

    public static final String BSE_FOLDER = "bse";
    public static final String BSE_FILE_BASE = "rus_bse";
    public static final String BSE_IFO_PATH_RELATIVE = BSE_FOLDER + "/" + BSE_FILE_BASE + BookInfo.INFO_FILE_EXTENTION;
    public static final String BSE_IDX_PATH_RELATIVE = BSE_FOLDER + "/" + BSE_FILE_BASE + IDX_EXT;
    public static final String BSE_DICT_PATH = ROOT_PATH + "/" + BSE_FOLDER;
    public static final String BSE_BASE_PATH = BSE_DICT_PATH + "/" + BSE_FILE_BASE;
    public static final String BSE_IFO_PATH = ROOT_PATH + "/" + BSE_IFO_PATH_RELATIVE;

    public static final String WORDNET_FOLDER = "wordnet";
    public static final String WORDNET_FILE_BASE = "wordnet";
    public static final String WORDNET_IFO_PATH_RELATIVE = WORDNET_FOLDER + "/" + WORDNET_FILE_BASE + BookInfo.INFO_FILE_EXTENTION;
    public static final String WORDNET_IFO_PATH = ROOT_PATH + "/" + WORDNET_IFO_PATH_RELATIVE;

    public static final String CAMBRIDGE_FOLDER = "Cambridge Advanced Learners Dictionary 3th Ed";
    public static final String CAMBRIDGE_FILE_BASE = "Cambridge Advanced Learners Dictionary 3th Ed";
    public static final String CAMBRIDGE_IFO_PATH_RELATIVE = CAMBRIDGE_FOLDER + "/" + CAMBRIDGE_FILE_BASE + BookInfo.INFO_FILE_EXTENTION;
    public static final String CAMBRIDGE_DICT_DZ_PATH_RELATIVE = CAMBRIDGE_FOLDER + "/" + CAMBRIDGE_FILE_BASE + Book.DICT_FILE_EXTENSION;
    public static final String CAMBRIDGE_RES_ZIP_PATH_RELATIVE = CAMBRIDGE_FOLDER + "/" + Book.RES_ZIP_NAME;

    public static final String MUELLER_DICT_NAME = "Mueller7GPL";
    public static final String WORDNET_DICT_NAME = "WordNet";
    public static final String CAMBRIDGE_DICT_NAME = "Cambridge Advanced Learners Dictionary 3th Ed. (En-En)";
    public static final String BSE_DICT_NAME = "Большая Советская Энциклопедия";

    public static final int MUELLER_DICT_SIZE = 46198;
    public static final int BSE_DICT_SIZE = 95058;

    public static final String PREFIX_ABA = "aba";
    public static final IndexEntry MUELLER_INDEX_ENTRY_ABACUS = new IndexEntry("abacus", 25975, 133, 15);
    public static final IndexEntry MUELLER_INDEX_ENTRY_ABADDON = new IndexEntry("Abaddon", 26108, 110, 16);
    public static final IndexEntry MUELLER_INDEX_ENTRY_ABAFT = new IndexEntry("abaft", 26218, 170, 14);

    public static final IndexEntry MUELLER_INDEX_ENTRY_A = new IndexEntry("A", 22712, 1867, 10);
    public static final IndexEntry MUELLER_INDEX_ENTRY_a = new IndexEntry("a", 24579, 381, 10);
    public static final IndexEntry MUELLER_INDEX_ENTRY_FIRST = new IndexEntry("'cause", 0, 23, 15);
    public static final IndexEntry MUELLER_INDEX_ENTRY_LAST = new IndexEntry("усил.", 8028778, 48, 18);

    public static final IndexEntry BSE_INDEX_ENTRY_0 = new IndexEntry("ЮНЕСКО", 280781038, 440, 21);
    public static final IndexEntry BSE_INDEX_ENTRY_1 = new IndexEntry("Юань (монг. династия)", 280788620, 3297, 46);
    public static final IndexEntry BSE_INDEX_ENTRY_2 = new IndexEntry("Юань (совет)", 280791917, 303, 30);
    public static final IndexEntry BSE_INDEX_ENTRY_3 = new IndexEntry("Юань Мэй", 280792220, 1829, 24);
    public static final IndexEntry BSE_INDEX_ENTRY_SOBAT = new IndexEntry("Собат", 219190614, 987, 19);
    public static final IndexEntry BSE_INDEX_ENTRY_FIRST = new IndexEntry("...Биоз", 0, 289, 20);
    public static final IndexEntry BSE_INDEX_ENTRY_LAST = new IndexEntry("Яёи культура", 284502504, 2574, 32);

    public static final long MUELLER_INDEX_ENTRY_START_1 = 4792;//abacus
    public static final long MUELLER_INDEX_ENTRY_START_2 = 4807;//Abaddon
    public static final long MUELLER_INDEX_ENTRY_START_3 = 4823;//abaft
    public static final long MUELLER_INDEX_ENTRY_START_4 = 4599;//A
    public static final long MUELLER_INDEX_ENTRY_START_5 = 4609;//a
    public static final long MUELLER_INDEX_ENTRY_START_FIRST = 0;//'cause
    public static final long MUELLER_INDEX_ENTRY_START_LAST = 806354;//усил.

    public static final long BSE_INDEX_ENTRY_START_1 = 3820646; //Юань (монг. династия)
    public static final long BSE_INDEX_ENTRY_START_2 = 3820692; //Юань (совет)
    public static final long BSE_INDEX_ENTRY_START_3 = 3820722; //Юань Мэй
    public static final long BSE_INDEX_ENTRY_START_4 = 2976568; //Собат
    public static final long BSE_INDEX_ENTRY_START_FIRST = 0; //...Биоз
    public static final long BSE_INDEX_ENTRY_START_LAST = 3861768;//Яёи культура
}
