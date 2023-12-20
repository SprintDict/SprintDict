/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.domain.core.SparkDictIndex;
import net.bancer.sparkdict.mocks.Mocks;
import junit.framework.TestCase;

/**
 * @author valera
 *
 */
public class SparkDictIndexTest extends TestCase {
	
	private SparkDictIndex indexMueller;
	private SparkDictIndex indexBSE;

	/**
	 * @param name
	 */
	public SparkDictIndexTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		indexMueller = new SparkDictIndex(new BookInfo(Mocks.MUELLER_IFO_PATH));
		indexBSE = new SparkDictIndex(new BookInfo(Mocks.BSE_IFO_PATH));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		indexMueller = null;
		indexBSE = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.SparkDictIndex#SparkDictIndex(net.bancer.sparkdict.domain.core.BookInfo)}.
	 */
	public void testSparkDictIndex() {
		assertNotNull(indexMueller);
		assertNotNull(indexBSE);
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.SparkDictIndex#getSize()}.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void testGetSize() throws FileNotFoundException, IOException {
		assertEquals(Mocks.MUELLER_DICT_SIZE, indexMueller.getSize());
		assertEquals(Mocks.BSE_DICT_SIZE, indexBSE.getSize());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.SparkDictIndex#getIndexEntry(long)}.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void testGetIndexEntry() throws FileNotFoundException, IOException {
		assertEquals(Mocks.MUELLER_INDEX_ENTRY_FIRST.getLemma(), indexMueller.getIndexEntry(0).getLemma());
		assertEquals(Mocks.MUELLER_INDEX_ENTRY_LAST.getLemma(), indexMueller.getIndexEntry(Mocks.MUELLER_DICT_SIZE-1).getLemma());
		assertEquals(null, indexMueller.getIndexEntry(Mocks.MUELLER_DICT_SIZE));
		try {
			indexMueller.getIndexEntry(-1);
			fail("IOException expected");
		} catch (IOException e) {
		}
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.SparkDictIndex#getBookName()}.
	 */
	public void testGetBookName() {
		assertEquals(Mocks.MUELLER_DICT_NAME, indexMueller.getBookName());
		assertEquals(Mocks.BSE_DICT_NAME, indexBSE.getBookName());
	}

}
