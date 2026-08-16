package net.bancer.sparkdict;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.SparkDictIndex;

import java.io.IOException;

public final class Fixtures {

    public static final String TEST_DATA_PATH = System.getProperty("user.dir") + "/../test-data/dictionaries/";
    public static final String GCIDE_DICT_FOLDER = "gcide";
    public static final String GCIDE_IFO_FILE = TEST_DATA_PATH + GCIDE_DICT_FOLDER + "/stardict.ifo";
    public static final String GCIDE_IDX_FILE = TEST_DATA_PATH + GCIDE_DICT_FOLDER + "/stardict.idx";
    public static final String GCIDE_DICT_DZ_FILE = TEST_DATA_PATH + GCIDE_DICT_FOLDER + "/stardict.dict.dz";
    public static final String ALL_FIELDS_IFO_FILE = TEST_DATA_PATH + "all-fields-ifo/stardict.ifo";

    public static void buildSparkDictIndex() throws IOException {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
    }

    public static void deleteSparkDictIndex() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
    }
}
