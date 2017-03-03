package net.bancer.sparkdict.mocks;

import net.bancer.sparkdict.domain.core.IndexEntry;


public class Mocks {

	public static final String IFO_EXT = ".ifo";
	public static final String IDX_EXT = ".idx";
	public static final String DICT_EXT = ".dict.dz";
	
	public static final String ROOT_PATH = "/mnt/sdcard/dictionaries";
	
	public static final String MUELLER_DICT_PATH = ROOT_PATH + "/mueller";
	public static final String MUELLER_BASE_PATH = MUELLER_DICT_PATH + "/Mueller7GPL";
	public static final String MUELLER_IFO_PATH = MUELLER_BASE_PATH + IFO_EXT;
	
	public static final String BSE_DICT_PATH = ROOT_PATH + "/bse";
	public static final String BSE_BASE_PATH = BSE_DICT_PATH + "/rus_bse";
	public static final String BSE_IFO_PATH = BSE_BASE_PATH + IFO_EXT;

	public static final String WORDNET_IFO_PATH = "/mnt/sdcard/dictionaries/wordnet/wordnet" + IFO_EXT;
	
	public static final String CAMBRIDGE_DICT_PATH = ROOT_PATH + "/Cambridge Advanced Learners Dictionary 3th Ed";
	public static final String CAMBRIDGE_BASE_PATH = CAMBRIDGE_DICT_PATH + "/Cambridge Advanced Learners Dictionary 3th Ed";
	public static final String CAMBRIDGE_IFO_PATH = CAMBRIDGE_BASE_PATH + IFO_EXT;
	
	public static final String MUELLER_DICT_NAME = "Mueller7GPL";
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
