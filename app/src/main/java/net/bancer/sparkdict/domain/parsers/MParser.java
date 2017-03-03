/**
 * Word's pure text meaning.
 * The data should be a utf-8 string ending with '\0'.
 * 
 * @author Valera
 *
 */
package net.bancer.sparkdict.domain.parsers;

import java.io.UnsupportedEncodingException;

import android.util.Log;

/**
 * Parser for 'm' data type - word's pure text meaning.
 * The data is a utf-8 string ending with '\0'.
 * 
 * @author Valerij Bancer
 *
 */
public class MParser implements IParser {

	private static final String QUADRUPLE_NBSP = "&nbsp;&nbsp;&nbsp;&nbsp;";
	private static final String QUADRUPLE_SPACE = "    ";
	private static final String TRIPLE_NBSP = "&nbsp;&nbsp;&nbsp;";
	private static final String TRIPLE_SPACE = "   ";
	private static final String DOUBLE_NBSP = "&nbsp;&nbsp;";
	private static final String DOUBLE_SPACE = "  ";
	private static final String LINE_BREAK = "\n";
	
	/**
	 * HTML `br` tag.
	 */
	protected static final String BR_TAG = "<br>";
	
	/**
	 * Abbreviation text colour.
	 */
	protected static final String ABBREVIATION_COLOR = "#006600";
	
	/**
	 * Opening HTML `font` tag for a coloured text.
	 */
	protected static final String FONT_TAG_OPEN = "<font color=\"%s\">";
	
	/**
	 * Closing HTML `font` tag for a coloured text.
	 */
	protected static final String FONT_TAG_CLOSE = "</font>";

	@Override
	public String parse(byte[] data) {
		try {
			StringBuffer result = new StringBuffer(new String(data, "UTF8"));
			parseLineBreaks(result);
			parseQuadrupleSpaces(result);
			parseTripleSpaces(result);
			parseDoubleSpaces(result);
			return result.toString();
		} catch (UnsupportedEncodingException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}
		return null;
	}

	/**
	 * Converts four consecutive spaces into HTML entities.
	 * 
	 * @param strBuffer data block.
	 */
	protected void parseQuadrupleSpaces(StringBuffer strBuffer) {
		int quadrupleSpacePos = strBuffer.indexOf(QUADRUPLE_SPACE);
		while(quadrupleSpacePos >= 0) {
			strBuffer.replace(quadrupleSpacePos, quadrupleSpacePos + 4, QUADRUPLE_NBSP);
			quadrupleSpacePos = strBuffer.indexOf(QUADRUPLE_SPACE);
		}
	}

	/**
	 * Converts tree consecutive spaces into HTML entities.
	 * 
	 * @param strBuffer data block.
	 */
	protected void parseTripleSpaces(StringBuffer strBuffer) {
		int tripleSpacePos = strBuffer.indexOf(TRIPLE_SPACE);
		while(tripleSpacePos >= 0) {
			strBuffer.replace(tripleSpacePos, tripleSpacePos + 3, TRIPLE_NBSP);
			tripleSpacePos = strBuffer.indexOf(TRIPLE_SPACE);
		}
	}

	/**
	 * Converts two consecutive spaces into HTML entities.
	 * 
	 * @param strBuffer data block.
	 */
	protected void parseDoubleSpaces(StringBuffer strBuffer) {
		int doubleSpacePos = strBuffer.indexOf(DOUBLE_SPACE);
		while(doubleSpacePos >= 0) {
			strBuffer.replace(doubleSpacePos, doubleSpacePos + 2, DOUBLE_NBSP);
			doubleSpacePos = strBuffer.indexOf(DOUBLE_SPACE);
		}
	}

	/**
	 * Converts "\n" to "&lt;br&gt;".
	 * 
	 * @param strBuffer data block.
	 */
	protected void parseLineBreaks(StringBuffer strBuffer) {
		int lineBreakPos = strBuffer.indexOf(LINE_BREAK);
		while(lineBreakPos >= 0) {
			strBuffer.replace(lineBreakPos, lineBreakPos + 1, BR_TAG);
			lineBreakPos = strBuffer.indexOf(LINE_BREAK);
		}
	}

	/**
	 * Scans the provided buffer and replaces all occurrences of tag pair with a
	 * replacement tag pair.
	 * 
	 * @param buffer
	 *            string buffer to be scanned.
	 * @param openTag
	 *            HTML opening tag to be replaced.
	 * @param closeTag
	 *            HTML closing tag to be replaced.
	 * @param openTagReplacement
	 *            replacement for HTML opening tag.
	 * @param closeTagReplacement
	 *            replacement for HTML closing tag.
	 */
	protected void parseHTMLTagPair(StringBuffer buffer, String openTag,
			String closeTag, String openTagReplacement,
			String closeTagReplacement) {
		int openTagLength = openTag.length();
		int closeTagLength = closeTag.length();
		int openTagPosition = buffer.indexOf(openTag);
		int closeTagPosition = buffer.indexOf(closeTag);
		while (openTagPosition >= 0 && closeTagPosition > openTagPosition) {
			buffer.replace(closeTagPosition, closeTagPosition + closeTagLength, closeTagReplacement);
			buffer.replace(openTagPosition, openTagPosition + openTagLength, openTagReplacement);
			openTagPosition = buffer.indexOf(openTag);
			closeTagPosition = buffer.indexOf(closeTag);
		}
	}
	
	protected void parseHTMLTag(StringBuffer buffer, String tag,
			String replacement) {
		int length = tag.length();
		int position = buffer.indexOf(tag);
		while (position > -1) {
			buffer.replace(position, position + length, replacement);
			position = buffer.indexOf(tag);
		}
	}

}
