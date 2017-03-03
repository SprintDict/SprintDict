package net.bancer.sparkdict.domain.core;

/**
 * IndexEntry is a single entry in <dictionary name>.idx file.
 * 
 * @author  Valerij Bancer
 */
public class IndexEntry implements Comparable<IndexEntry>{
	
	/**
	 * Flag indicating that the whole word should be compared during the search.
	 */
	public static final int WORD_MATCH = 1;
	
	/**
	 * Flag indicating that only the beginning of the word should be compared
	 * during the search.
	 */
	public static final int PREFIX_MATCH = 2;

	/**
	 * Lemma gives the string representing this word.
	 * It's the string that is "looked up" by the StarDict.
	 */
	protected String lemma;

	/**
	 * If the dictionary file version is "3.0.0" and "idxoffsetbits=64",
	 * wordDataOffset is be 64-bits unsigned number in network byte order.
	 * Otherwise it is 32-bits.
	 */
	private int wordDataOffset;

	/**
	 * wordDataSize should be 32-bits unsigned number in network byte order.
	 */
	private int wordDataSize;

	private int lengthInBytes;

	/**
	 * Constructor.
	 * 
	 * @param lemma				lemma as a string.
	 * @param wordDataOffset	data offset in <dictionary name>.dict file.
	 * @param wordDataSize		data size in <dictionary name>.dict file.
	 * @param length			data length in bytes.
	 */
	public IndexEntry(String lemma, int wordDataOffset, int wordDataSize, int length) {
		this.lemma = lemma;
		this.wordDataOffset = wordDataOffset;
		this.wordDataSize = wordDataSize;
		this.lengthInBytes = length;
	}

	/**
	 * Compares this IndexEntry to another IndexEntry ignoring the case of ASCII
	 * characters. It treats non-ASCII characters taking in account case differences.
	 * 
	 * @param other IndexEntry to compare.
	 * 
	 * @return If the value of the word from the argument lexicographically
	 * greater than the value of the word of this IndexEntry <b>ignoring</b> case 
	 * differences the method returns a value less than 0.
	 * If the value of the word from the argument lexicographically
	 * less than the value of the word of this IndexEntry <b>ignoring</b> case 
	 * differences the method returns a value greater than 0.
	 * If the value of the word from the argument lexicographically
	 * equals to the  value of the word of this IndexEntry <b>taking in account</b> case
	 * differences the method returns 0.
	 * 
	 * Developer note: if `compareToIgnoreCaseASCIIOnly` does not discover
	 * differences only then `compareTo` is applied.
	 */
	@Override
	public int compareTo(IndexEntry other) {
		return compareWordStringTo(other.lemma);
	}
	
	/**
	 * Compares given string with the lemma of this IndexEntry.
	 * 
	 * Second parameter indicates the comparison mode. Possible values are:
	 * {@link #WORD_MATCH} and {@link #PREFIX_MATCH}. When {@link #WORD_MATCH}
	 * is provided the comparison is done the same way as in {@link #compareTo(IndexEntry)}.
	 * When {@link #PREFIX_MATCH} the lemma of the current IndexEntry is
	 * truncated to be not longer than the first parameter and comparison is
	 * done ignoring case differences. The important difference between these
	 * two modes is that when the strings are equal comparing them in case
	 * insensitive manner in {@link #WORD_MATCH} mode then the further comparison
	 * is done in case sensitive manner but when they are the same in {@link #PREFIX_MATCH}
	 * mode no further comparison is done.
	 * 
	 * @see #compareTo(IndexEntry)
	 * 
	 * @param str	string to compare.
	 * @param mode	{@link #WORD_MATCH} or {@link #PREFIX_MATCH}.
	 * @return		0 if the match is found, else greater or smaller number.
	 * @throws		IllegalArgumentException if the second parameter is wrong.
	 */
	public int compareTo(String str, int mode) {
		switch (mode) {
			case WORD_MATCH:
				return compareWordStringTo(str);
			case PREFIX_MATCH:
				return compareToPrefix(str);
			default:
				throw new IllegalArgumentException("invalid mode argument");
		}
	}

	/**
	 * Compares given string with the lemma of this IndexEntry.
	 * 
	 * @see #compareTo(IndexEntry)
	 * 
	 * @param str	string to compare to.
	 * @return
	 */
	private int compareWordStringTo(String str) {
		int res = compareToIgnoreCaseASCIIOnly(lemma, str);
		if(res == 0) {
			res = lemma.compareTo(str);
		}
		return res;
	}

	/**
	 * Compares two strings, ignoring the case of ASCII characters. It treats
	 * non-ASCII characters taking in account case differences.
	 * This is an attempt to mimic glib's string utility function 
	 * <a href="http://developer.gnome.org/glib/2.28/glib-String-Utility-Functions.html#g-ascii-strcasecmp">g_ascii_strcasecmp ()</a>
	 * which source can be find <a href="http://git.gnome.org/browse/glib/tree/glib/gstrfuncs.c">here</a>.
	 *
	 * This is a slightly modified version of java.lang.String.CASE_INSENSITIVE_ORDER.compare(String s1, String s2) method.
	 * 
	 * @param str1	string to compare with str2
	 * @param str2	string to compare with str1
	 * @return		0 if the strings match, a negative value if str1 < str2, or a positive value if str1 > str2
	 */
	private static int compareToIgnoreCaseASCIIOnly(String str1, String str2) {
		int n1 = str1.length();
		int n2 = str2.length();
		int min = Math.min(n1, n2);
		for (int i = 0; i < min; i++) {
			char c1 = str1.charAt(i);
			char c2 = str2.charAt(i);
			if (c1 != c2) {
				if ((int) c1 > 127 || (int) c2 > 127) { //if non-ASCII char
					return c1 - c2;
				} else {
					c1 = Character.toUpperCase(c1);
					c2 = Character.toUpperCase(c2);
					if(c1 != c2) {
						c1 = Character.toLowerCase(c1);
						c2 = Character.toLowerCase(c2);
						if(c1 != c2) {
							return c1 - c2;
						}
					}
				}
			}
		}
		return n1 - n2;
	}
	
	/**
	 * Compares the given prefix with the lemma of this index entry. If the word
	 * is longer than prefix the truncated part is used for comparison ignoring
	 * case differences.
	 * 
	 * @param prefix
	 *            prefix to be compared
	 * @return 0 if the prefix equals to the truncated lemma, -1 if the prefix
	 *         is greater than the truncated lemma, 1 if the prefix is smaller
	 *         than the truncated lemma.
	 */
	private int compareToPrefix(String prefix) {
		String trancatedWord;
		if(lemma.length() > prefix.length()) {
			trancatedWord = lemma.substring(0, prefix.length());
		} else {
			trancatedWord = lemma;
		}
		return IndexEntry.compareToIgnoreCaseASCIIOnly(trancatedWord, prefix);
	}

	/**
	 * Getter for lemma.
	 * 
	 * @return lemma as a string.
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * Data offset getter.
	 * 
	 * @return data offset in <dictionary name>.dict file.
	 */
	public int getWordDataOffset() {
		return wordDataOffset;
	}

	/**
	 * Data size getter.
	 * 
	 * @return data size in <dictionary name>.dict file.
	 */
	public int getWordDataSize() {
		return wordDataSize;
	}

	/**
	 * IndexEntry as a string (for debug).
	 */
	@Override
	public String toString() {
		return lemma;
	}

	/**
	 * Getter for length in bytes.
	 * 
	 * @return	data length in bytes.
	 */
	public int getLengthInBytes() {
		return lengthInBytes;
	}
}
