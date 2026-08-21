package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IndexEntryTest {

    private static final String LEMMA = "Example";
    private static final int WORD_DATA_OFFSET = 123;
    private static final int WORD_DATA_SIZE = 456;
    private static final int LENGTH_IN_BYTES = 7;

    private final IndexEntry entry = new IndexEntry(
        LEMMA,
        WORD_DATA_OFFSET,
        WORD_DATA_SIZE,
        LENGTH_IN_BYTES
    );

    @Test
    public void constructorStoresValues() {
        assertEquals(LEMMA, entry.getLemma());
        assertEquals(WORD_DATA_OFFSET, entry.getWordDataOffset());
        assertEquals(WORD_DATA_SIZE, entry.getWordDataSize());
        assertEquals(LENGTH_IN_BYTES, entry.getLengthInBytes());
    }

    @Test
    public void compareToReturnsZeroForEqualLemmas() {
        IndexEntry other = new IndexEntry(LEMMA, 999, 888, 777);
        assertEquals(0, entry.compareTo(other));
    }

    @Test
    public void compareToUsesCaseSensitiveComparisonWhenAsciiCaseInsensitiveComparisonMatches() {
        IndexEntry upperCase = new IndexEntry("Example", 0, 0, 0);
        IndexEntry lowerCase = new IndexEntry("example", 0, 0, 0);
        assertEquals(
            "Example".compareTo("example"),
            upperCase.compareTo(lowerCase)
        );
        assertEquals(
            "example".compareTo("Example"),
            lowerCase.compareTo(upperCase)
        );
    }

    @Test
    public void compareToOrdersDifferentLemmas() {
        IndexEntry shorter = new IndexEntry("Examplex", 0, 0, 0);
        assertTrue(entry.compareTo(shorter) < 0);
        IndexEntry longer = new IndexEntry("Exampl", 0, 0, 0);
        assertTrue(entry.compareTo(longer) > 0);
    }

    @Test
    public void compareToIsCaseSensitiveAfterAsciiCaseInsensitiveComparison() {
        IndexEntry upperCase = new IndexEntry("Example", 0, 0, 0);
        IndexEntry lowerCase = new IndexEntry("example", 0, 0, 0);
        assertEquals(
            "Example".compareTo("example"),
            upperCase.compareTo(lowerCase)
        );
    }

    @Test
    public void compareToTreatsNonAsciiCharactersAsCaseSensitive() {
        IndexEntry lowerCase = new IndexEntry("ä", 0, 0, 0);
        IndexEntry upperCase = new IndexEntry("Ä", 0, 0, 0);
        assertEquals(
            'ä' - 'Ä',
            lowerCase.compareTo(upperCase)
        );
    }

    @Test
    public void compareToStringWithWordMatchReturnsZeroForSameWord() {
        assertEquals(0, entry.compareTo("Example", IndexEntry.WORD_MATCH));
    }

    @Test
    public void compareToStringWithWordMatchIsCaseSensitiveWhenCaseDiffers() {
        assertEquals(
            LEMMA.compareTo("example"),
            entry.compareTo("example", IndexEntry.WORD_MATCH)
        );
    }

    @Test
    public void compareToStringWithWordMatchOrdersDifferentWords() {
        assertEquals(
            LEMMA.compareTo("Examplex"),
            entry.compareTo("Examplex", IndexEntry.WORD_MATCH)
        );
        assertEquals(
            LEMMA.compareTo("Exampl"),
            entry.compareTo("Exampl", IndexEntry.WORD_MATCH)
        );
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsZeroForMatchingPrefix() {
        assertEquals(0, entry.compareTo("exam", IndexEntry.PREFIX_MATCH));
    }

    @Test
    public void compareToStringWithPrefixMatchIsCaseInsensitive() {
        assertEquals(0, entry.compareTo("EXAM", IndexEntry.PREFIX_MATCH));
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsZeroWhenPrefixIsWholeLemma() {
        assertEquals(0, entry.compareTo("example", IndexEntry.PREFIX_MATCH));
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsNegativeWhenPrefixIsLongerThanLemma() {
        IndexEntry shortEntry = new IndexEntry("exam", 0, 0, 0);
        assertTrue(shortEntry.compareTo("example", IndexEntry.PREFIX_MATCH) < 0);
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsNegativeWhenLemmaPrefixIsLess() {
        IndexEntry abcEntry = new IndexEntry("abcdef", 0, 0, 0);
        assertTrue(abcEntry.compareTo("abd", IndexEntry.PREFIX_MATCH) < 0);
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsZeroWhenStringIsPrefixOfLemma() {
        IndexEntry entry = new IndexEntry("abcdef", 0, 0, 0);
        assertEquals(0, entry.compareTo("abc", IndexEntry.PREFIX_MATCH));
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsNegativeWhenLemmaIsLessThanPrefix() {
        IndexEntry entry = new IndexEntry("abcdef", 0, 0, 0);
        assertTrue(entry.compareTo("abd", IndexEntry.PREFIX_MATCH) < 0);
    }

    @Test
    public void compareToStringWithPrefixMatchReturnsPositiveWhenLemmaIsGreaterThanPrefix() {
        IndexEntry entry = new IndexEntry("abddef", 0, 0, 0);
        assertTrue(entry.compareTo("abc", IndexEntry.PREFIX_MATCH) > 0);
    }

    @Test
    public void compareToStringWithPrefixMatchHandlesEmptyPrefix() {
        assertEquals(0, entry.compareTo("", IndexEntry.PREFIX_MATCH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void compareToStringThrowsExceptionForInvalidMode() {
        entry.compareTo("example", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compareToStringThrowsExceptionForUnknownMode() {
        entry.compareTo("example", 3);
    }

    @Test
    public void toStringReturnsLemma() {
        assertEquals(LEMMA, entry.toString());
    }
}
