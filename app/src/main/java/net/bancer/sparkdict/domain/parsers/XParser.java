/**
 * A utf-8 string which is marked up with the xdxf language.
 * See http://xdxf.sourceforge.net
 * StarDict have these extention:
 * <rref> can have "type" attribute, it can be "image", "sound", "video" 
 * and "attach".
 * <kref> can have "k" attribute.
 * 
 * @author Valera
 *
 */
package net.bancer.sparkdict.domain.parsers;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * Parser for 'x' data type - the data is in XDXF format.
 * 
 * @author Valerij Bancer
 *
 */
public class XParser extends MParser {

	private static final String A_TAG_OPEN = "<a href=\"net.bancer.sparkdict://%s\">";
	private static final String A_TAG_CLOSE = "</a>";
	private static final String ABR_OPEN_TAG = "<abr>";
	private static final String ABR_CLOSE_TAG = "</abr>";
	private static final String EX_OPEN_TAG = "<ex>";
	private static final String EX_CLOSE_TAG = "</ex>";
	private static final String KREF_OPEN_TAG = "<kref>";
	private static final String KREF_CLOSE_TAG = "</kref>";
	private static final String RREF_OPEN_TAG = "<rref>";
	private static final String RREF_CLOSE_TAG = "</rref>";
	private static final String SQUARE_BRACKET_OPEN = "[";
	private static final String SQUARE_BRACKET_CLOSE = "]";
	private static final String TR_OPEN_TAG = "<tr>";
	private static final String TR_CLOSE_TAG = "</tr>";
	private static final String TT_TAG_OPEN = "<tt>";
	private static final String TT_TAG_CLOSE = "</tt>";
	private static final String C_TAG_OPEN = "<c>";
	private static final String C_TAG_OPEN_INCOMPLETE = "<c c=";
	private static final String C_TAG_CLOSE = "</c>";
	private static final String FONT_TAG_OPEN_INCOMPLETE = "<font color=";
	//private static final String FONT_TAG_CLOSE = "</font>";
	private static final String IMG_TAG = "<img src=\"%s\">";
	private static final String OBJECT_TAG = "<object data=\"%s\">%s</object>";
	private static final String K_OPEN_TAG = "<k>";
	private static final String K_CLOSE_TAG = "</k>";
	private static final String BIG_OPEN_TAG = "<big>";
	private static final String BIG_CLOSE_TAG = "</big>";

	private static final String TRANSCRIPTION_OPEN = TT_TAG_OPEN + SQUARE_BRACKET_OPEN;
	private static final String TRANSCRIPTION_CLOSE = SQUARE_BRACKET_CLOSE + TT_TAG_CLOSE;

	private static final String COLOR_GREY 	= "#808080";
	
	private static final Map<String, String> HTML_COLORS = new HashMap<String, String>();
	static {
		HTML_COLORS.put("darkcyan", 		"#008B8B");
		HTML_COLORS.put("darkslategray", 	"#2F4F4F");
		HTML_COLORS.put("gray", 			"#808080");
		HTML_COLORS.put("orange", 			"#FFA500");
		HTML_COLORS.put("orangered", 		"#FF4500");
		HTML_COLORS.put("rosybrown", 		"#BC8F8F");
	}

	/*
	 * Extract
	 * 
	 * Replace 	<xdxf tag> by 	<html tag>
	 * 			\n				<br>
	 * 			<k>				<big>
	 * 			</k>			</big>
	 * 			<def>			<blockquote>
	 * 			</def>			</blockquote>
	 * 			<tr>			[
	 * 			</tr>			]
	 * 			<c>				<font>
	 * 			</c>			</font>
	 * 			<c ...>			<font ...>
	 * 			<co>			<font color="#E0E0E0"> (gray)
	 * 			</co>			</font>
	 * 			<abr>			<font color="#FFCCFF"> (pink)
	 * 			</abr>			</font>
	 * 			<kref>			<a href="contents of kref tag">
	 * 			</kref>			</a>
	 * 			<rref>...</rref>	<img src="..." />			
	 */
	@Override
	public String parse(byte[] data) {
		try {
			StringBuffer result = new StringBuffer(new String(data, "UTF8"));
			parseKrefTags(result);
			parseTrTags(result);
			parseKTags(result);
			parseExTags(result);
			parseAbrTags(result);
			parseCTags(result);
			parseRrefTags(result);
			
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
	private void parseAbrTags(StringBuffer strBuffer) {
		String openTagReplacement = String.format(FONT_TAG_OPEN, ABBREVIATION_COLOR);
		parseHTMLTagPair(strBuffer, ABR_OPEN_TAG, ABR_CLOSE_TAG,
				openTagReplacement, FONT_TAG_CLOSE);
	}

	private void parseExTags(StringBuffer strBuffer) {
		String openTagReplacement = String.format(FONT_TAG_OPEN, COLOR_GREY);
		parseHTMLTagPair(strBuffer, EX_OPEN_TAG, EX_CLOSE_TAG,
				openTagReplacement, FONT_TAG_CLOSE);
	}

	private void parseKTags(StringBuffer strBuffer) {
		parseHTMLTagPair(strBuffer, K_OPEN_TAG, K_CLOSE_TAG, BIG_OPEN_TAG,
				BIG_CLOSE_TAG + BR_TAG);
	}

	private void parseTrTags(StringBuffer strBuffer) {
		parseHTMLTagPair(strBuffer, TR_OPEN_TAG, TR_CLOSE_TAG,
				TRANSCRIPTION_OPEN, TRANSCRIPTION_CLOSE);
	}

	private void parseKrefTags(StringBuffer strBuffer) {
		int openTagPos = strBuffer.indexOf(KREF_OPEN_TAG);
		int closeTagPos = strBuffer.indexOf(KREF_CLOSE_TAG);
		while(openTagPos >= 0 && closeTagPos > openTagPos) {
			String innerString = strBuffer.substring(openTagPos + KREF_OPEN_TAG.length(), closeTagPos);
			String link = String.format(A_TAG_OPEN, innerString);
			strBuffer.replace(closeTagPos, closeTagPos + KREF_CLOSE_TAG.length(), A_TAG_CLOSE);
			strBuffer.replace(openTagPos, openTagPos + KREF_OPEN_TAG.length(), link);
			openTagPos = strBuffer.indexOf(KREF_OPEN_TAG);
			closeTagPos = strBuffer.indexOf(KREF_CLOSE_TAG);
		}
	}

	private void parseCTags(StringBuffer strBuffer) {
		int tagPos = strBuffer.indexOf(C_TAG_OPEN_INCOMPLETE);
		while (tagPos > -1) {
			String html = FONT_TAG_OPEN_INCOMPLETE;
			// double quote position indicates the start of color attribute value
			int start = strBuffer.indexOf("\"", tagPos) + 1;
			if (start > -1) {
				// double quote position indicates the end of color attribute value
				int end = strBuffer.indexOf("\"", start + 1);
				// get color attribute value
				String color = strBuffer.substring(start, end);
				if (end > -1 && HTML_COLORS.containsKey(color)) {
					// replace the color attribute value to HEX value
					strBuffer.replace(start, end, HTML_COLORS.get(color));
				}
			}
			strBuffer.replace(tagPos, tagPos + C_TAG_OPEN_INCOMPLETE.length(),
					html);
			tagPos = strBuffer.indexOf(C_TAG_OPEN_INCOMPLETE);
		}
		parseHTMLTag(strBuffer, C_TAG_OPEN, FONT_TAG_OPEN);
		parseHTMLTag(strBuffer, C_TAG_CLOSE, FONT_TAG_CLOSE);
	}

	private void parseRrefTags(StringBuffer strBuffer) {
		int openTagPos = strBuffer.indexOf(RREF_OPEN_TAG);
		int closeTagPos = strBuffer.indexOf(RREF_CLOSE_TAG, openTagPos);
		while(openTagPos > -1 && closeTagPos > openTagPos) {
			String innerString = strBuffer.substring(openTagPos + RREF_OPEN_TAG.length(), closeTagPos);
			String data;
			if(innerString.endsWith(".wav")) {
				data = String.format(OBJECT_TAG, innerString, innerString);
			} else {
				data = String.format(IMG_TAG, innerString) + BR_TAG;
			}
			strBuffer.replace(openTagPos, closeTagPos + RREF_CLOSE_TAG.length(), data);
			openTagPos = strBuffer.indexOf(RREF_OPEN_TAG);
			closeTagPos = strBuffer.indexOf(RREF_CLOSE_TAG, openTagPos);
		}
	}

}
