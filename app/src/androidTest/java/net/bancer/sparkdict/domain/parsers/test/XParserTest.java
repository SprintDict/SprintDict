package net.bancer.sparkdict.domain.parsers.test;

import static org.junit.Assert.assertEquals;

import net.bancer.sparkdict.domain.parsers.IParser;
import net.bancer.sparkdict.domain.parsers.XParser;

import org.junit.Before;
import org.junit.Test;

/**
 * Android integration tests for {@link XParser}.
 */
public class XParserTest {

    /**
     * Parser instance under test.
     */
    private IParser parser;

    /**
     * Sets up test fixtures before each test execution.
     */
    @Before
    public void setUp() {
        parser = new XParser();
    }

    /**
     * Tests parsing of various XDXF tags including &lt;k&gt;, &lt;c&gt;, &lt;abr&gt;, &lt;rref&gt;.
     */
    @Test
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

    /**
     * Tests parsing of &lt;co&gt; tags into HTML font tags with coral color (#FF7F50).
     */
    @Test
    public void testParseCoTags() {
        String raw = "<co>commentary text</co>";
        String expected = "<font color=\"#FF7F50\">commentary text</font>";
        assertEquals(expected, parser.parse(raw.getBytes()));
    }

    /**
     * Tests parsing of multiple &lt;co&gt; tags in a single string.
     */
    @Test
    public void testParseMultipleCoTags() {
        String raw = "First <co>note 1</co> second <co>note 2</co> end";
        String expected = "First <font color=\"#FF7F50\">note 1</font> second <font color=\"#FF7F50\">note 2</font> end";
        assertEquals(expected, parser.parse(raw.getBytes()));
    }
}
