package net.bancer.sparkdict.domain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import net.bancer.sparkdict.Fixtures;
import net.bancer.sparkdict.domain.utils.DomainException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LexicalEntryTest {

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        Fixtures.buildSparkDictIndex();
        Fixtures.buildDummyTmDictIndex();
        Fixtures.buildDummyMultiDictIndex();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Fixtures.deleteSparkDictIndex();
        Fixtures.deleteDummyTmDictIndex();
        Fixtures.deleteDummyMultiDictIndex();
    }

    private LexicalEntry getGcideLexicalEntry(String lemma) throws Exception {
        String ifoFile = Fixtures.GCIDE_IFO_FILE;
        BookInfo bookInfo = new BookInfo(ifoFile);
        byte[] buffer = getLexicalEntry(lemma, bookInfo);
        return new LexicalEntry(lemma, buffer, bookInfo);
    }

    private LexicalEntry getDummTmLexicalEntry(String lemma) throws Exception {
        String ifoFile = Fixtures.DUMMY_TM_IFO_FILE;
        BookInfo bookInfo = new BookInfo(ifoFile);
        byte[] buffer = getLexicalEntry(lemma, bookInfo);
        return new LexicalEntry(lemma, buffer, bookInfo);
    }

    private LexicalEntry getDummMultiLexicalEntry(String lemma) throws Exception {
        String ifoFile = Fixtures.DUMMY_MULTI_IFO_FILE;
        BookInfo bookInfo = new BookInfo(ifoFile);
        byte[] buffer = getLexicalEntry(lemma, bookInfo);
        ResourcesZipFile resZip = new ResourcesZipFile(new File(Fixtures.DUMMY_MULTI_RES_ZIP_FILE));
        return new LexicalEntry(lemma, buffer, bookInfo, resZip);
    }

    @NonNull
    private static byte[] getLexicalEntry(String lemma, BookInfo bookInfo) throws DomainException, IOException {
        IndexEntriesIterator iterator = new IndexEntriesIterator(bookInfo);
        IndexEntry indexEntry = iterator.findIndexEntry(lemma);
        assertEquals(lemma, indexEntry.getLemma());
        DictZipFile dictZipFile = new DictZipFile(bookInfo.getPathToDictFile());
        byte[] buffer;
        try {
            int offset = indexEntry.getWordDataOffset();
            int size = indexEntry.getWordDataSize();
            buffer = dictZipFile.read(offset, size);
        } finally {
            dictZipFile.close();
        }
        return buffer;
    }

    @Test
    public void constructorParsesAbacus() throws Exception {
        LexicalEntry entry = getGcideLexicalEntry("abacus");
        assertEquals("abacus", entry.getLemma());
        String expectedTitle = "GNU Collaborative International Dictionary of English";
        assertEquals(expectedTitle, entry.getDictTitle());
        String expectedDefinitions = "<p><b style=\"color: #00b\">Abacus</b>" +
            " <i>(ăbȧkŭs)</i>, <i style=\"color: #a00\">n.</i>;" +
            " E. <i>pl.</i> <span style=\"color: #00b\">Abacuses</span> ;" +
            " L. pl. <span style=\"color: #00b\">Abaci</span> <i>(-sī)</i>." +
            " [L. <span style=\"color: #8B4513\">abacus</span>," +
            " <span style=\"color: #8B4513\">abax</span>, Gr. ἄβαξ]" +
            " <b>1.</b> A table or tray strewn with sand, anciently used for drawing, calculating," +
            " etc. <span style=\"color: #00b\">[Obs.]</span></p>" +
            "<p><b>2.</b> A calculating table or frame; an instrument for performing arithmetical" +
            " calculations by balls sliding on wires, or counters in grooves, the lowest line" +
            " representing units, the second line, tens, etc. It is still employed in China.</p>" +
            "<p><b>3.</b> <span style=\"color: #00b\">(Arch.)</span> <b>(a)</b> The uppermost" +
            " member or division of the capital of a column, immediately under the architrave." +
            " See <a href=\"bword://Column\">Column</a>.&nbsp;&nbsp;<b>(b)</b> A tablet, panel," +
            " or compartment in ornamented or mosaic work.</p><p>" +
            "<b>4.</b> A board, tray, or table, divided into perforated compartments, for holding" +
            " cups, bottles, or the like; a kind of cupboard, buffet, or sideboard.</p><p><b>Abacus" +
            " harmonicus</b> <span style=\"color: #00b\">(Mus.)</span>, an ancient diagram showing" +
            " the structure and disposition of the keys of an instrument." +
            "&nbsp;&nbsp;<small>Crabb.</small></p>";
        assertEquals(expectedDefinitions, entry.getDefinitions());
    }

    @Test
    public void constructorParsesAbandon() throws Exception {
        LexicalEntry entry = getGcideLexicalEntry("abandon");
        assertEquals("abandon", entry.getLemma());
        String expectedDefinitions = "<p>Ø<b style=\"color: #00b\">Abandon</b> <i>(ȧbäNdôN)</i>," +
            " <i style=\"color: #a00\">n.</i> [F. See <a href=\"bword://Abandon\">Abandon</a>.]" +
            " A complete giving up to natural impulses; freedom from artificial constraint;" +
            " careless freedom or ease.</p>" +
            "<p><b style=\"color: #00b\">Abandon</b> <i>(ȧbăndŭn)</i>," +
            " <i style=\"color: #a00\">v. t.</i> [<i style=\"color: #a00\">imp. &amp; p. p.</i>" +
            " <span style=\"color: #00b\">Abandoned</span> <i>(-dŭnd)</i>;" +
            " <i style=\"color: #a00\">p. pr. &amp; vb. n.</i>" +
            " <span style=\"color: #00b\">Abandoning</span>.]" +
            " [OF. <span style=\"color: #8B4513\">abandoner</span>," +
            " F. <span style=\"color: #8B4513\">abandonner</span>;" +
            " <span style=\"color: #8B4513\">a</span>" +
            " (L. <span style=\"color: #8B4513\">ad</span>)" +
            " + <span style=\"color: #8B4513\">bandon</span> permission, authority," +
            " LL. <span style=\"color: #8B4513\">bandum</span>," +
            " <span style=\"color: #8B4513\">bannum</span>, public proclamation, interdiction," +
            " <span style=\"color: #8B4513\">bannire</span> to proclaim, summon: of Germanic origin;" +
            " cf. Goth. <span style=\"color: #8B4513\">bandwjan</span> to show by signs, to designate OHG." +
            " <span style=\"color: #8B4513\">ban</span> proclamation. The word meant to proclaim," +
            " put under a ban, put under control; hence, as in OE., to compel, subject, or to" +
            " leave in the control of another, and hence, to give up." +
            " See <a href=\"bword://Ban\">Ban</a>.] " +
            "<b>1.</b> To cast or drive out; to banish; to expel; to reject." +
            " <span style=\"color: #00b\">[Obs.]</span></p>" +
            "<p><i style=\"color: #33f\">That he might . . . <b>abandon</b> them from him.</i>" +
            " <small>Udall.</small></p><p><i style=\"color: #33f\">Being all this time" +
            " <b>abandoned</b> from your bed.</i> <small>Shak.</small></p>" +
            "<p><b>2.</b> To give up absolutely; to forsake entirely ; to renounce utterly; to" +
            " relinquish all connection with or concern on; to desert, as a person to whom one" +
            " owes allegiance or fidelity; to quit; to surrender.</p>" +
            "<p><i style=\"color: #33f\">Hope was overthrown, yet could not be <b>abandoned</b>.</i>" +
            " <small>I. Taylor.</small></p>" +
            "<p><b>3.</b> Reflexively: To give (one's self) up without attempt at self-control;" +
            " to yield (one's self) unrestrainedly; -- often in a bad sense.</p>" +
            "<p><i style=\"color: #33f\">He <b>abandoned</b> himself . . . to his favorite" +
            " vice.</i> <small>Macaulay.</small></p>" +
            "<p><b>4.</b> <span style=\"color: #00b\">(Mar. Law)</span> To relinquish all claim to;" +
            " -- used when an insured person gives up to underwriters all claim to the property" +
            " covered by a policy, which may remain after loss or damage by a peril insured against.</p>" +
            "<p><b>Syn.</b> -- To give up; yield; forego; cede; surrender; resign; abdicate;" +
            " quit; relinquish; renounce; desert; forsake; leave; retire; withdraw from." +
            " -- <a href=\"bword://To\">To Abandon</a>, <a href=\"bword://Desert\">Desert</a>," +
            " <a href=\"bword://Forsake\">Forsake</a>. These words agree in representing a person" +
            " as <i>giving up</i> or <i>leaving</i> some object, but differ as to the mode of" +
            " doing it. The distinctive sense of abandon is that of giving up a thing absolutely" +
            " and finally; as, to abandon one's friends, places, opinions, good or evil habits," +
            " a hopeless enterprise, a shipwrecked vessel. <i>Abandon</i> is more widely applicable" +
            " than <i>forsake</i> or <i>desert</i>. The Latin original of <i>desert</i> appears" +
            " to have been originally applied to the case of deserters from military service." +
            " Hence, the verb, when used of <i>persons</i> in the active voice, has usually or" +
            " always a bad sense, implying some breach of fidelity, honor, etc., the leaving of" +
            " something which the person should rightfully stand by and support; as, to" +
            " <i>desert</i> one's colors, to <i>desert</i> one's post, to <i>desert</i> one's" +
            " principles or duty. When used in the passive, the sense is not necessarily bad; as," +
            " the fields were <i>deserted</i>, a <i>deserted</i> village, <i>deserted</i> halls." +
            " <i>Forsake</i> implies the breaking off of previous habit, association, personal" +
            " connection, or that the thing left had been familiar or frequented; as, to forsake" +
            " old friends, to <i>forsake</i> the paths of rectitude, the blood <i>forsook</i> his" +
            " cheeks. It may be used either in a good or in a bad sense.</p>" +
            "<p><b style=\"color: #00b\">Abandon</b>, <i style=\"color: #a00\">n.</i>" +
            " [F. <span style=\"color: #8B4513\">abandon</span>. fr." +
            " <span style=\"color: #8B4513\">abandonner</span>." +
            " See <a href=\"bword://Abandon\">Abandon</a>," +
            " <i style=\"color: #a00\">v.</i>] Abandonment; relinquishment." +
            " <span style=\"color: #00b\">[Obs.]</span></p>";
        assertEquals(expectedDefinitions, entry.getDefinitions());
    }

    @Test
    public void constructorParsesAbility() throws Exception {
        LexicalEntry entry = getGcideLexicalEntry("ability");
        assertEquals("ability", entry.getLemma());
        String expectedDefinitions = "<p><b style=\"color: #00b\">Ability</b> <i>(ȧbĭlĭt)</i>," +
            " <i style=\"color: #a00\">n.</i>; <i>pl.</i>" +
            " <span style=\"color: #00b\">Abilities</span> <i>(ȧbĭlĭtĭz)</i>." +
            " [F. <span style=\"color: #8B4513\">habileté</span>, earlier spelling" +
            " <span style=\"color: #8B4513\">habilité</span> (with silent <i>h</i>)," +
            " L. <span style=\"color: #8B4513\">habilitas</span> aptitude, ability," +
            " fr. <span style=\"color: #8B4513\">habilis</span> apt." +
            " See <a href=\"bword://Able\">Able</a>.] The quality or state of being able; power" +
            " to perform, whether physical, moral, intellectual, conventional, or legal; capacity;" +
            " skill or competence in doing; sufficiency of strength, skill, resources, etc.;" +
            " -- in the <i>plural</i>, faculty, talent.</p>" +
            "<p><i style=\"color: #33f\">Then the disciples, every man according to his" +
            " <b>ability</b>, determined to send relief unto the brethren.</i> <small>Acts xi. 29.</small></p>" +
            "<p><i style=\"color: #33f\">Natural <b>abilities</b> are like natural plants, that" +
            " need pruning by study.</i> <small>Bacon.</small></p>" +
            "<p><i style=\"color: #33f\">The public men of England, with much of a peculiar kind" +
            " of <b>ability</b>.</i> <small>Macaulay.</small></p>" +
            "<p><b>Syn.</b> -- Capacity; talent; cleverness; faculty; capability; efficiency;" +
            " aptitude; aptness; address; dexterity; skill." +
            "&nbsp;&nbsp;<a href=\"bword://Ability\">Ability</a>," +
            " <a href=\"bword://Capacity\">Capacity</a>.&nbsp;&nbsp;These words come into" +
            " comparison when applied to the higher intellectual powers." +
            "&nbsp;&nbsp;<i>Ability</i> has reference to the <i>active</i> exercise of our" +
            " faculties.&nbsp;&nbsp;It implies not only native vigor of mind, but that ease and" +
            " promptitude of execution which arise from mental training." +
            "&nbsp;&nbsp;Thus, we speak of the <i>ability</i> with which a book is written, an" +
            " argument maintained, a negotiation carried on, etc." +
            "&nbsp;&nbsp;It always something to be <i>done</i>, and the power of <i>doing</i> it." +
            "&nbsp;&nbsp;<i>Capacity</i> has reference to the <i>receptive</i> powers." +
            "&nbsp;&nbsp;In its higher exercises it supposes great quickness of apprehension and" +
            " breadth of intellect, with an uncommon aptitude for acquiring and retaining knowledge." +
            "&nbsp;&nbsp;Hence it carries with it the idea of <i>resources</i> and undeveloped power." +
            "&nbsp;&nbsp;Thus we speak of the extraordinary <i>capacity</i> of such men as Lord" +
            " Bacon, Blaise Pascal, and Edmund Burke." +
            "&nbsp;&nbsp;“<i>Capacity</i>,” says H. Taylor, “is requisite to devise, and" +
            " <i>ability</i> to execute, a great enterprise.”" +
            "&nbsp;&nbsp;The word <i>abilities</i>, in the plural, embraces both these qualities," +
            " and denotes high mental endowments.</p>";
        assertEquals(expectedDefinitions, entry.getDefinitions());
    }

    @Test
    public void constructorWithDefinitionsSetsFields() {
        BookInfo bookInfo = new BookInfo(Fixtures.GCIDE_IFO_FILE);
        byte[] dataBlocks = "definition".getBytes(StandardCharsets.UTF_8);
        LexicalEntry entry = new LexicalEntry("abacus", dataBlocks, bookInfo);
        assertEquals("abacus", entry.getLemma());
        assertEquals("definition", entry.getDefinitions());
        String expectedTitle = "GNU Collaborative International Dictionary of English";
        assertEquals(expectedTitle, entry.getDictTitle());
    }

    @Test
    public void setDefinitionsReplacesDefinitions() throws Exception {
        LexicalEntry entry = getGcideLexicalEntry("abacus");
        entry.setDefinitions("new definition");
        assertEquals("new definition", entry.getDefinitions());
    }

    @Test
    public void toStringReturnsEntryInformation() throws Exception {
        LexicalEntry entry = getGcideLexicalEntry("abacus");
        entry.setDefinitions("definition");
        String expected = "[GNU Collaborative International Dictionary of English,abacus,definition]";
        assertEquals(expected, entry.toString());
    }

    @Test
    public void constructorParsesTmTypeLemma() throws Exception {
        LexicalEntry entry = getDummTmLexicalEntry("blorptastic");
        assertEquals("blorptastic", entry.getLemma());
        String expectedTitle = "Test Dummy Dictionary";
        assertEquals(expectedTitle, entry.getDictTitle());
        String expectedDefinitions = "blɔːrpˈtæstɪk\n" +
            "Feeling absurdly proud of a trivial accomplishment.";
        assertEquals(expectedDefinitions, entry.getDefinitions());
    }

    @Test
    public void constructorParsesMultiTypeLemma() throws Exception {
        LexicalEntry entry = getDummMultiLexicalEntry("emberloop");
        assertEquals("emberloop", entry.getLemma());
        String expectedTitle = "Test Multitype Dictionary";
        assertEquals(expectedTitle, entry.getDictTitle());
        String expectedDefinitions =
            /* sametypesequence */ /* parsed data */
            /* m */ "The dummy meaning of \"emberloop\": a plain-text definition used purely for StarDict format testing." +
            /* t */ "\n/emb-tɛst-oop/" +
            /* g */ "\n<span foreground=\"blue\">emberloop</span>: a <b>Pango-markup</b> test definition." +
            /* x */ "\n<ar><big>emberloop</big><br><def>An <b>xdxf</b>-markup test definition for emberloop.</def></ar>" +
            /* y */ "\nemberloopヤ・emピョウ (dummy YinBiao/Kana test string)" +
            /* k */ "\n<POWERWORD><HEAD>emberloop</HEAD><BODY>KingSoft-style XML test data for emberloop.</BODY></POWERWORD>" +
            /* w */ "\n'''emberloop''' is a ''MediaWiki''-markup test entry. See also [[emberloops]]." +
            /* h */ "\n<p><strong>emberloop</strong> &mdash; an <em>HTML</em> test definition.</p>" +
            /* n */ "\nWordNet-style test gloss for 'emberloop': (n) emberloop (a fabricated concept used only for testing)" +
            /* l */ "\nemberloop locale-caf\ufffd-test" +
            /* r */ "\nimg:pic/emberloop.jpg<br>snd:emberloop.wav<br>vdo:emberloop.avi<br>att:emberloop.bin" +
            /* W */ "\nRIFFD\u0000\u0000\u0000WAVEfmt \u0010\u0000\u0000\u0000\u0001\u0000\u0001\u0000@\u001f\u0000\u0000@\u001f\u0000\u0000\u0001\u0000\u0008\u0000data \u0000\u0000\u0000\ufffd\ufffd&\u034e\ufffd\u0557\u0016\ufffd^\ufffdi\u0014f'M\ufffd\u00031V6&\u001d\ufffdl\ufffd\ufffd5\ufffd\u0017" +
            /* P */ "\n\ufffdPNG\r<br>\u001a<br>FAKEIHDR\ufffd\ufffdT\ufffd\ufffd\ufffd\ufffdR\ufffdnX\ufffd\ufffd\u0011Tn\ufffd\ufffd\ufffd\u0014F\ufffd~\ufffd:R\ufffd\ufffd\u0001\u0016\ufffd\ufffd" +
            /* X */ "\nW\ufffdn\u0012瑶\ufffd\ufffd\ufffdVY[Y\u0008\ufffd\ufffd5o\ufffd\ufffd\ufffd";
        assertEquals(expectedDefinitions, entry.getDefinitions());
    }

    //TODO: Run just that test, take the printed line (it'll have every non-printable/non-ASCII
    // character as a \\uXXXX escape, so nothing gets silently dropped or replaced when you paste it),
    // and drop it straight in as the new expectedDefinitions in constructorParsesMultiTypeLemma.
    /*@Test
    public void dumpActualDefinitions() throws Exception {
        LexicalEntry entry = getDummMultiLexicalEntry("emberloop");
        String actual = entry.getDefinitions();
        // Print as an escaped Java string literal - safe to copy/paste, no lossy terminal rendering
        StringBuilder sb = new StringBuilder();
        for (char c : actual.toCharArray()) {
            switch (c) {
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        System.out.println("\"" + sb + "\"");
    }*/

    @Test
    public void getResourceDummyMultiEntry() throws Exception {
        LexicalEntry entry = getDummMultiLexicalEntry("aplander");
        assertEquals("aplander", entry.getLemma());
        String expectedTitle = "Test Multitype Dictionary";
        assertEquals(expectedTitle, entry.getDictTitle());
        assertTrue(entry.getDefinitions().contains("pic/aplander.jpg"));
        byte[] image = entry.getResource("pic/aplander.jpg");
        assertNotNull(image);
        assertTrue(image.length > 0);
    }
}
