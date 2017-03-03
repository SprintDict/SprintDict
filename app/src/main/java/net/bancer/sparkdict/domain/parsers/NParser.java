package net.bancer.sparkdict.domain.parsers;

import java.io.UnsupportedEncodingException;

import android.util.Log;

/**
 * Parser for 'n' data type.
 * 
 * @author Valerij Bancer
 *
 */
public class NParser extends MParser {

	private static final String TYPE_OPEN_TAG = "<type>";
	
	private static final String TYPE_CLOSE_TAG = "</type>";
	
	private static final String WORDGROUP_OPEN_TAG = "<wordgroup>";
	
	private static final String WORDGROUP_CLOSE_TAG = "</wordgroup>";
	
	private static final String WORD_OPEN_TAG = "<word>";
	
	private static final String WORD_CLOSE_TAG = "</word>";
	
	private static final String I_OPEN_TAG = "<i>";
	
	private static final String I_CLOSE_TAG = "</i>";
	
	private static final String B_OPEN_TAG = "<b>";
	
	private static final String B_CLOSE_TAG = "</b>";
	
	/**
	 * HTML entity for bullet.
	 */
	private static final String BULLET = "&#8226; ";

	@Override
	public String parse(byte[] data) {
		try {
			StringBuffer strBuffer = new StringBuffer(new String(data, "UTF8"));
			parseTypeTags(strBuffer);
			parseWordgroupTags(strBuffer);
			parseWordTags(strBuffer);
			
			parseLineBreaks(strBuffer);
			parseQuadrupleSpaces(strBuffer);
			parseTripleSpaces(strBuffer);
			parseDoubleSpaces(strBuffer);
			return strBuffer.toString();
		} catch (UnsupportedEncodingException e) {
			Log.e(this.getClass().getName(), e.getMessage());
		}
		return null;
	}

	/**
	 * Parses the string buffer and replaces all occurrences of `type` tag with
	 * coloured italics tags.
	 * 
	 * @param buffer
	 *            buffer with article's text.
	 */
	private void parseTypeTags(StringBuffer buffer) {
		String openTagReplacement = I_OPEN_TAG
				+ String.format(FONT_TAG_OPEN, ABBREVIATION_COLOR);
		String closeTagReplacement = FONT_TAG_CLOSE + I_CLOSE_TAG + BR_TAG;
		parseHTMLTagPair(buffer, TYPE_OPEN_TAG, TYPE_CLOSE_TAG,
				openTagReplacement, closeTagReplacement);
	}

	/**
	 * Parses the string buffer and removes all occurrences of `wordgroup` tag.
	 * 
	 * @param buffer
	 *            buffer with article's text.
	 */
	private void parseWordgroupTags(StringBuffer buffer) {
		parseHTMLTagPair(buffer, WORDGROUP_OPEN_TAG, WORDGROUP_CLOSE_TAG, "", "");
	}

	/**
	 * Parses the string buffer and replaces each pair of `word` tag with a pair
	 * of `b` tags and a line break tag.
	 * 
	 * @param strBuffer
	 *            buffer with article's text.
	 */
	private void parseWordTags(StringBuffer strBuffer) {
		String openTagReplacement = B_OPEN_TAG + BULLET;
		String closeTagReplacement = B_CLOSE_TAG + BR_TAG;
		parseHTMLTagPair(strBuffer, WORD_OPEN_TAG, WORD_CLOSE_TAG,
				openTagReplacement, closeTagReplacement);
	}
}
