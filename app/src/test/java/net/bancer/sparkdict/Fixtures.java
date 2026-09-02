package net.bancer.sparkdict;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.DictionaryFiles;
import net.bancer.sparkdict.domain.core.FileDictionaryFiles;
import net.bancer.sparkdict.domain.core.SparkDictIndex;

import java.io.IOException;

public final class Fixtures {

    public static final String TEST_DATA_PATH = System.getProperty("user.dir") + "/../test-data/dictionaries/";
    public static final String GCIDE_DICT_FOLDER = "gcide";
    public static final String GCIDE_IFO_FILE_RELATIVE = GCIDE_DICT_FOLDER + "/stardict.ifo";
    public static final String GCIDE_IFO_FILE = TEST_DATA_PATH + GCIDE_IFO_FILE_RELATIVE;
    public static final String GCIDE_IDX_FILE = TEST_DATA_PATH + GCIDE_DICT_FOLDER + "/stardict.idx";
    public static final String GCIDE_DICT_DZ_FILE_RELATIVE = GCIDE_DICT_FOLDER + "/stardict.dict.dz";
    public static final String GCIDE_DICT_DZ_FILE = TEST_DATA_PATH + GCIDE_DICT_DZ_FILE_RELATIVE;
    public static final String ALL_FIELDS_IFO_FILE = TEST_DATA_PATH + "all-fields-ifo/stardict.ifo";

    public static final String DUMMY_TM_DICT_FOLDER = "dummy-dict-tm-type";

    public static final String DUMMY_TM_IFO_FILE_RELATIVE = DUMMY_TM_DICT_FOLDER + "/stardict.ifo";
    public static final String DUMMY_TM_IFO_FILE = TEST_DATA_PATH + DUMMY_TM_IFO_FILE_RELATIVE;

    public static final String DUMMY_TM_IDX_FILE = TEST_DATA_PATH + DUMMY_TM_DICT_FOLDER + "/stardict.idx";

    public static final String DUMMY_TM_DICT_DZ_FILE = TEST_DATA_PATH + DUMMY_TM_DICT_FOLDER + "/stardict.dict.dz";

    public static final String DUMMY_MULTI_DICT_FOLDER = "dymmy-dict-multi-type";

    public static final String DUMMY_MULTI_IFO_FILE_RELATIVE = DUMMY_MULTI_DICT_FOLDER + "/stardict.ifo";
    public static final String DUMMY_MULTI_IFO_FILE = TEST_DATA_PATH + DUMMY_MULTI_IFO_FILE_RELATIVE;

    public static final String DUMMY_MULTI_IDX_FILE = TEST_DATA_PATH + DUMMY_MULTI_DICT_FOLDER + "/stardict.idx";

    public static final String DUMMY_MULTI_DICT_DZ_FILE = TEST_DATA_PATH + DUMMY_MULTI_DICT_FOLDER + "/stardict.dict.dz";

    public static final String DUMMY_MULTI_RES_ZIP_FILE = TEST_DATA_PATH + DUMMY_MULTI_DICT_FOLDER + "/res.zip";

    public static void buildSparkDictIndex() throws IOException {
        DictionaryFiles dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
    }

    public static void deleteSparkDictIndex() {
        DictionaryFiles dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
    }

    public static void buildDummyTmDictIndex() throws IOException {
        DictionaryFiles dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.DUMMY_TM_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
    }

    public static void deleteDummyTmDictIndex() {
        DictionaryFiles dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.DUMMY_TM_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
    }

    public static void buildDummyMultiDictIndex() throws IOException {
        DictionaryFiles dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.DUMMY_MULTI_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.buildIndex();
    }

    public static void deleteDummyMultiDictIndex() {
        DictionaryFiles dictionaryFiles = new FileDictionaryFiles(Fixtures.TEST_DATA_PATH);
        BookInfo bookInfo = new BookInfo(Fixtures.DUMMY_MULTI_IFO_FILE_RELATIVE, dictionaryFiles);
        SparkDictIndex index = new SparkDictIndex(bookInfo);
        index.delete();
    }
}
