/**
 * 
 */
package net.bancer.sparkdict.domain.parsers.test;

import net.bancer.sparkdict.domain.parsers.IParser;
import net.bancer.sparkdict.domain.parsers.NParser;
import net.bancer.sparkdict.domain.parsers.XParser;
import junit.framework.TestCase;

/**
 * @author valera
 *
 */
public class XParserTest extends TestCase {
	
	private IParser parser;

	/**
	 * @param name
	 */
	public XParserTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		parser = new XParser();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		parser = null;
	}

	/**
	 * Test method for {@link net.bancer.sparkdict.domain.parsers.XParser#parse(byte[])}.
	 */
	public void testParse() {
		String raw = "<k>abacus</k>" +
				"<b>abacus</b> " +
				"<c c=\"gray\"> </c>" +
				"<abr>UK</abr> " +
				"<rref>z_uka____012.wav</rref> " +
				"<abr>US</abr> " +
				"<rref>z_abacus.wav</rref> " +
				"<c c=\"darkcyan\">[</c>" +
				"<c c=\"darkcyan\">ˈæb.ə.kəs</c>" +
				"<c c=\"darkcyan\">]</c> " +
				"<c c=\"orange\"> noun </c> " +
				"<c c=\"orangered\">countable</c> " +
				"<c c=\"rosybrown\">[</c>" +
				"<c c=\"darkslategray\"><b>abacuses</b></c>" +
				"<c c=\"rosybrown\">]</c>" +
				"<blockquote>" +
				"<rref>x_abacus.jpg</rref> a square or rectangular frame " +
				"holding an arrangement of small balls on metal rods or wires, " +
				"which is used for counting, adding and subtracting </blockquote>" +
				"<blockquote><blockquote><blockquote>" +
				"<c c=\"darkslategray\"><c>Thesaurus</c><sup>+</sup>: </c>" +
				"[Weighing, measuring and counting devices]" +
				"</blockquote></blockquote></blockquote>";
		String parsed = "<big>abacus</big><br>" +
				"<b>abacus</b> " +
				"<font color=\"#808080\"> </font>" +
				"<font color=\"#006600\">UK</font> " +
				"<object data=\"z_uka____012.wav\">z_uka____012.wav</object> " +
				"<font color=\"#006600\">US</font> " +
				"<object data=\"z_abacus.wav\">z_abacus.wav</object> " +
				"<font color=\"#008B8B\">[</font>" +
				"<font color=\"#008B8B\">ˈæb.ə.kəs</font>" +
				"<font color=\"#008B8B\">]</font> " +
				"<font color=\"#FFA500\"> noun </font> " +
				"<font color=\"#FF4500\">countable</font> " +
				"<font color=\"#BC8F8F\">[</font>" +
				"<font color=\"#2F4F4F\"><b>abacuses</b></font>" +
				"<font color=\"#BC8F8F\">]</font>" +
				"<blockquote>" +
				"<img src=\"x_abacus.jpg\"><br> a square or rectangular frame " +
				"holding an arrangement of small balls on metal rods or wires, " +
				"which is used for counting, adding and subtracting </blockquote>" +
				"<blockquote><blockquote><blockquote>" +
				"<font color=\"#2F4F4F\"><font color=\"%s\">Thesaurus</font><sup>+</sup>: </font>" +
				"[Weighing, measuring and counting devices]" +
				"</blockquote></blockquote></blockquote>";
		assertEquals(parsed, parser.parse(raw.getBytes()));
	}

}
