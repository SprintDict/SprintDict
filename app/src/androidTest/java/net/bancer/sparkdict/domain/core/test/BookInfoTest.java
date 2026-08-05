/**
 * 
 */
package net.bancer.sparkdict.domain.core.test;

import java.io.File;

import net.bancer.sparkdict.domain.core.BookInfo;
import net.bancer.sparkdict.mocks.Mocks;
import junit.framework.TestCase;

/**
 * @author valera
 *
 */
public class BookInfoTest extends TestCase {

	private BookInfo muellerBookInfo;
	private BookInfo bseBookInfo;

	/**
	 * @param name
	 */
	public BookInfoTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		muellerBookInfo = new BookInfo(new File(Mocks.MUELLER_IFO_PATH));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPreConditions() {
		File dir = new File(Mocks.ROOT_PATH);
		assertTrue(Mocks.ROOT_PATH + " does not exists", dir.exists());
		assertTrue(Mocks.ROOT_PATH + " is not a directory", dir.isDirectory());
		assertTrue(Mocks.ROOT_PATH + " is not readable", dir.canRead());

		File muellerDir = new File(Mocks.MUELLER_DICT_PATH);
		assertTrue(muellerDir + " does not exists", 	muellerDir.exists());
		assertTrue(muellerDir + " is not a directory", 	muellerDir.isDirectory());
		assertTrue(muellerDir + " is not readable", 	muellerDir.canRead());
		
		File muellerIfo = new File(Mocks.MUELLER_IFO_PATH);
		assertTrue(muellerIfo + " does not exists", muellerIfo.exists());
		assertTrue(muellerIfo + " is not a file", 	muellerIfo.isFile());
		assertTrue(muellerIfo + " is not readable", muellerIfo.canRead());
		
		File bseDir = new File(Mocks.BSE_DICT_PATH);
		assertTrue(bseDir + " does not exists", 	bseDir.exists());
		assertTrue(bseDir + " is not a directory", 	bseDir.isDirectory());
		assertTrue(bseDir + " is not readable", 	bseDir.canRead());
		
		File bseIfo = new File(Mocks.BSE_IFO_PATH);
		assertTrue(bseIfo + " does not exists", bseIfo.exists());
		assertTrue(bseIfo + " is not a file",   bseIfo.isFile());
		assertTrue(bseIfo + " is not readable", bseIfo.canRead());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.BookInfo#BookInfo(java.lang.String)}.
	 */
	public void testBookInfoString() {
		bseBookInfo = new BookInfo(Mocks.BSE_IFO_PATH);
		assertNotNull(bseBookInfo);
		assertEquals("2.4.2", bseBookInfo.getVersion());
		assertEquals(95058, bseBookInfo.getWordCount());
		assertEquals(3861800, bseBookInfo.getIdxFileSize());
		assertEquals("Большая Советская Энциклопедия", bseBookInfo.getBookName());
		assertEquals("2009.01.30", bseBookInfo.getDate());
		assertEquals("x", bseBookInfo.getSameTypeSequence());
		assertEquals("Copyright: Converted by swaj under GNU Public License; Version: 1.0", bseBookInfo.getDescription());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.BookInfo#BookInfo(java.io.File)}.
	 */
	public void testBookInfoFile() {
		assertNotNull(muellerBookInfo);
		assertEquals("2.4.2", muellerBookInfo.getVersion());
		assertEquals(46198, muellerBookInfo.getWordCount());
		assertEquals(806372, muellerBookInfo.getIdxFileSize());
		assertEquals("Mueller7GPL", muellerBookInfo.getBookName());
		assertEquals("2004.03.09", muellerBookInfo.getDate());
		assertEquals("tm", muellerBookInfo.getSameTypeSequence());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.BookInfo#toString()}.
	 */
	public void testToString() {
		assertTrue(muellerBookInfo.toString().contains("Dictionary name: Mueller7GPL"));
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.BookInfo#getFileBaseName()}.
	 */
	public void testGetFileBaseName() {
		assertEquals(Mocks.MUELLER_BASE_PATH, muellerBookInfo.getFileBaseName());
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.core.BookInfo#getPathToDictFile()}.
	 */
	public void testGetPathToDictFile() {
		assertEquals(Mocks.MUELLER_BASE_PATH + Mocks.DICT_EXT, muellerBookInfo.getPathToDictFile());
	}

}
