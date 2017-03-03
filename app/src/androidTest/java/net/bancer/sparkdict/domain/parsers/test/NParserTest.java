/**
 * 
 */
package net.bancer.sparkdict.domain.parsers.test;

import net.bancer.sparkdict.domain.parsers.IParser;
import net.bancer.sparkdict.domain.parsers.NParser;
import junit.framework.TestCase;

/**
 * @author valera
 *
 */
public class NParserTest extends TestCase {
	
	private IParser parser;

	/**
	 * @param name
	 */
	public NParserTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		parser = new NParser();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		parser = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.parsers.NParser#parse(byte[])}.
	 */
	public void testParse() {
		String rawEntry = "<type>a</type>" +
				"<wordgroup>" +
				"<word>.22 caliber</word>" +
				"<word>.22-caliber</word>" +
				"<word>.22 calibre</word>" +
				"<word>.22-calibre</word>" +
				"</wordgroup>" +
				"<gloss>of or relating to the bore of a gun (or its " +
				"ammunition) that measures twenty-two hundredths of an inch " +
				"in diameter; &quot;a .22 caliber pistol&quot;</gloss>";
		String parsedEntry = "<i><font color=\"#006600\">a</font></i><br>" +
				"" +
				"<b>&#8226; .22 caliber</b><br>" +
				"<b>&#8226; .22-caliber</b><br>" +
				"<b>&#8226; .22 calibre</b><br>" +
				"<b>&#8226; .22-calibre</b><br>" +
				"" +
				"<gloss>of or relating to the bore of a gun (or its " +
				"ammunition) that measures twenty-two hundredths of an inch " +
				"in diameter; &quot;a .22 caliber pistol&quot;</gloss>";
		assertEquals(parsedEntry, parser.parse(rawEntry.getBytes()));
	}

}
