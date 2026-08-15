package net.bancer.sparkdict.domain.parsers.test;

import static org.junit.Assert.assertEquals;

import net.bancer.sparkdict.domain.parsers.IParser;
import net.bancer.sparkdict.domain.parsers.NParser;

import org.junit.Before;
import org.junit.Test;

public class NParserTest {

	private IParser parser;

	@Before
	public void setUp() {
		parser = new NParser();
	}

	@Test
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
