package com.gempukku.lotro.patchnotes;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

/** [[card links]] against the real card library. */
public class LibraryCardLookupTest {
    private static LotroCardBlueprintLibrary _library;
    private static LibraryCardLookup _lookup;

    @BeforeClass
    public static void loadCards() {
        _library = new LotroCardBlueprintLibrary();
        _lookup = new LibraryCardLookup(_library);
    }

    private static String id(String reference) {
        CardLookup.Result result = _lookup.resolve(reference);
        assertTrue(reference + ": " + result.error(), result.isFound());
        return result.blueprintId();
    }

    private static String error(String reference) {
        CardLookup.Result result = _lookup.resolve(reference);
        assertFalse(reference + " resolved to " + result.blueprintId(), result.isFound());
        return result.error();
    }

    @Test
    public void aBlueprintIdIsThatCard() {
        assertEquals("1_5", id("1_5"));
        assertEquals("Cleaving Blow", _lookup.resolve("1_5").name());
        assertEquals("51_5", id("51_5"));                  // an errata id shows the errata
        assertEquals("1_89", id(" 1_89 "));
        assertEquals("Aragorn, Ranger of the North", _lookup.resolve("1_89").name());
        assertTrue(error("99_999"), error("99_999").contains("no card with the id 99_999"));
    }

    @Test
    public void aNameIsThePrintedCardEvenWhenAnErrataSharesIt() {
        assertEquals("1_5", id("Cleaving Blow"));           // 51_5 is its errata
        assertEquals("1_5", id("cleaving blow"));
        assertEquals("1_5", id("  Cleaving   Blow "));
        assertEquals("1_25", id("Still Draws Breath"));     // 51_25
        assertEquals("1_89", id("Aragorn, Ranger of the North"));
        assertEquals("1_89", id("Aragorn Ranger of the North"));
        assertEquals("1_158", id("Uruk-hai Raiding Party"));  // 4_207 is a reprint, 51_158 the errata
        assertEquals("1_2", id("The One Ring, The Ruling Ring"));
    }

    @Test
    public void accentsAndPunctuationDoNotMatter() {
        String erethon = id("Erethón, Naith Lieutenant");
        assertEquals(erethon, id("Erethon, Naith Lieutenant"));
        assertEquals(erethon, id("erethon naith lieutenant"));
        assertEquals("11_249", id("Neekerbreekers' Bog"));   // 61_249 is its errata
        assertEquals("11_249", id("Neekerbreekers\u2019 Bog"));
        assertEquals("11_249", id("neekerbreekers bog"));
    }

    @Test
    public void aCollectorsInfoIsThePrintedCard() {
        assertEquals("1_5", id("1C5"));
        assertEquals("1_5", id("1c5"));
        assertEquals("1_89", id("1R89"));
        assertEquals("101_1", id("V1C1"));
    }

    @Test
    public void aNameSeveralCardsShareIsAnErrorThatListsThem() {
        String aragorn = error("Aragorn");
        assertTrue(aragorn, aragorn.startsWith("'Aragorn' matches "));
        assertTrue(aragorn, aragorn.contains("1_89 Aragorn, Ranger of the North 1R89"));
        assertTrue(aragorn, aragorn.contains("[[1_"));
        assertTrue(error("Gandalf").startsWith("'Gandalf' matches "));
        // a name that is one card's whole name is that card, though it is also other cards' title
        assertEquals("30_48", id("The One Ring"));
        assertEquals("1_2", id("The One Ring, The Ruling Ring"));
        assertEquals("1_299", id("Hobbit Sword"));        // its reprints are mapped to it
    }

    @Test
    public void unknownNamesAreErrors() {
        assertEquals("no card is named or numbered 'Cleaving Blows'", error("Cleaving Blows"));
        assertEquals("the card link is empty", error("  "));
        assertEquals("no card is named or numbered '1Z5'", error("1Z5"));
    }

    @Test
    public void placeholdersAreNotCards() {
        assertTrue(error(LotroCardBlueprintLibrary.PLACEHOLDER_TITLE).startsWith("no card"));
    }

    @Test
    public void invalidateRebuildsTheIndex() {
        assertEquals("1_5", id("Cleaving Blow"));
        _lookup.invalidate();
        assertEquals("1_5", id("Cleaving Blow"));
    }

    @Test
    public void everyCardLinkInTheShippedNotesResolves() throws IOException {
        File shipped = new File("../gemp-lotr-async/src/main/web/patchnotes");
        if (!shipped.isDirectory())
            return;
        PatchNoteRenderer renderer = new PatchNoteRenderer(_lookup);
        List<String> problems = new ArrayList<>();
        for (File file : shipped.listFiles((dir, name) -> PatchNotesLibrary.FILE_NAME.matcher(name).matches())) {
            PatchNote note = PatchNotesLibrary.readNote(file);
            Matcher links = PatchNoteRenderer.CARD_LINK.matcher(note.getMarkdown());
            if (!links.find())
                continue;
            renderer.render(note.getMarkdown(), problem -> problems.add(file.getName() + ": " + problem));
        }
        assertEquals(List.of(), problems);
    }

    @Test
    public void theReadmeExamplesResolve() throws IOException {
        File readme = new File("../gemp-lotr-async/src/main/web/patchnotes/README.md");
        if (!readme.isFile())
            return;
        // (inside the README's table a | is written \\|)
        String text = java.nio.file.Files.readString(readme.toPath()).replace("\\|", "|");
        // the examples of working links (the README also shows an ambiguous one, [[Aragorn]], on purpose)
        for (String example : new String[]{"[[Cleaving Blow]]", "[[1C5]]", "[[1_5]]", "[[51_5|the errata]]",
                "[[Aragorn, Ranger of the North]]"}) {
            assertTrue("README shows " + example, text.contains(example));
            Matcher m = PatchNoteRenderer.CARD_LINK.matcher(example);
            assertTrue(m.find());
            id(m.group(1));
        }
    }
}
