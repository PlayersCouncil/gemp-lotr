package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ScenarioLibraryTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File canon;
    private File users;
    private long now = 1_000_000;

    @Before
    public void folders() throws Exception {
        canon = temp.newFolder("canon");
        users = temp.newFolder("scenarios-user");
    }

    private ScenarioLibrary library() {
        return new ScenarioLibrary(canon, users, ScenarioTesting.library(), ScenarioLibraryTest::format, null, 5000, () -> now);
    }

    private static LotroFormat format(String code) {
        try {
            return ScenarioTesting.format(code);
        } catch (CardNotFoundException exp) {
            return null;
        }
    }

    /** A scenario that plays through: one step, which ends it. */
    private static String working(String id) {
        return ScenarioTesting.scenario("""
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: pass
                			end: complete
                		}
                	]
                """).replace("id: test", "id: " + id);
    }

    /** A scenario that is well-formed and can be started, but whose script does not fit the game. */
    private static String broken(String id) {
        return working(id).replace("phase(fellowship)", "phase(regroup)");
    }

    private static void write(File folder, String path, String text) throws Exception {
        File file = new File(folder, path);
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
    }

    private static List<String> ids(List<ScenarioLibrary.Entry> entries) {
        return entries.stream().map(ScenarioLibrary.Entry::id).toList();
    }

    @Test
    public void ReadsBothFoldersAndNamesAUsersScenarioAfterItsOwner() throws Exception {
        write(canon, "basics/first.hjson", working("first"));
        write(canon, "campaigns/basics.hjson", "{ id: basics }");
        write(canon, "README.md", "not a scenario");
        write(users, "users/ketura/drafts/mine.hjson", working("mine"));
        write(users, "submitted/ketura/mine.hjson", working("mine"));
        ScenarioLibrary library = library();

        assertEquals(List.of("first", "ketura/mine"), ids(library.getAll()));
        ScenarioLibrary.Entry first = library.get("first");
        assertEquals("basics/first.hjson", first.path());
        assertTrue(first.canonical());
        assertTrue(first.playable());
        assertTrue(first.report().toString(), first.verified());

        ScenarioLibrary.Entry mine = library.get("ketura/mine");
        assertEquals("ketura", mine.owner());
        assertEquals("drafts/mine.hjson", mine.path());
        assertEquals("ketura/mine", mine.def().id);
        assertNull(library.get("mine"));
    }

    @Test
    public void OnlyWorkingCanonicalScenariosAreOfferedToEverybody() throws Exception {
        write(canon, "first.hjson", working("first"));
        write(canon, "second.hjson", broken("second"));
        write(users, "users/ketura/mine.hjson", broken("mine"));
        write(users, "users/someone/theirs.hjson", working("theirs"));
        ScenarioLibrary library = library();

        ScenarioLibrary.Entry second = library.get("second");
        assertTrue("It can be started", second.playable());
        assertFalse("But it does not play through", second.verified());

        assertEquals(List.of("first"), ids(library.getAvailableTo("anybody", false)));
        //An author gets their own, working or not
        assertEquals(List.of("first", "ketura/mine"), ids(library.getAvailableTo("ketura", false)));
        assertEquals(List.of("first", "second", "ketura/mine", "someone/theirs"), ids(library.getAvailableTo("admin", true)));
    }

    @Test
    public void ABadFileIsKeptWithItsErrorsAndTheRestStillLoad() throws Exception {
        write(canon, "a-bad.hjson", "{ id: bad\n title: Bad\n steps: [ ] \n nonsense: 1 }");
        write(canon, "b-nocard.hjson", working("nocard").replace("aragorn: 4_109", "aragorn: 4_9999"));
        write(canon, "c-noformat.hjson", working("noformat").replace("format: scenario", "format: nosuchformat"));
        write(canon, "d-fine.hjson", working("fine"));
        ScenarioLibrary library = library();

        assertEquals(4, library.getAll().size());
        ScenarioLibrary.Entry bad = library.getAll().get(0);
        assertNull(bad.def());
        assertFalse(bad.playable());
        assertEquals(1, bad.errors().size());

        ScenarioLibrary.Entry noCard = library.get("nocard");
        assertFalse(noCard.playable());
        assertTrue(noCard.errors().toString(), noCard.errors().getFirst().contains("4_9999"));
        assertNull("A scenario that cannot be laid out is not played", noCard.report());

        assertTrue(library.get("noformat").errors().getFirst().contains("nosuchformat"));
        assertEquals(List.of("fine"), ids(library.getAvailableTo("anybody", false)));
        assertEquals("Not even an admin can start what cannot be laid out", List.of("fine"), ids(library.getAvailableTo("admin", true)));
    }

    @Test
    public void TwoFilesWithTheSameIdTheFirstByPathWins() throws Exception {
        write(canon, "a.hjson", working("same"));
        write(canon, "b.hjson", working("same"));
        ScenarioLibrary library = library();

        assertEquals("a.hjson", library.get("same").path());
        ScenarioLibrary.Entry loser = library.getAll().get(1);
        assertEquals("b.hjson", loser.path());
        assertNull(loser.id());
        assertFalse(loser.playable());
        assertTrue(loser.errors().toString(), loser.errors().getFirst().contains("already the id of a.hjson"));
    }

    @Test
    public void AChangedFileIsPickedUpAndAnUnchangedOneIsNotReadAgain() throws Exception {
        write(canon, "first.hjson", working("first"));
        write(canon, "second.hjson", broken("second"));
        ScenarioLibrary library = library();
        ScenarioLibrary.Entry first = library.get("first");
        assertFalse(library.get("second").verified());

        //Fixed in place, and a new file added
        write(canon, "second.hjson", working("second") + "\n");
        write(canon, "third.hjson", working("third"));
        assertFalse("Nothing is looked at again until the interval has passed", library.get("second").verified());
        assertNull(library.get("third"));

        now += 6000;
        assertTrue(library.get("second").verified());
        assertNotNull(library.get("third"));
        assertSame("A file that did not change keeps what was read from it", first, library.get("first"));

        assertTrue(new File(canon, "third.hjson").delete());
        now += 6000;
        assertNull(library.get("third"));
        assertEquals(2, library.getItemCount());

        library.clearCache();
        assertNotNull(library.get("first"));
        assertTrue("Clearing the cache reads everything again", first != library.get("first"));
    }

    @Test
    public void AMissingUserFolderIsNoUserScenarios() throws Exception {
        write(canon, "first.hjson", working("first"));
        ScenarioLibrary library = new ScenarioLibrary(canon, new File(users, "nowhere"), ScenarioTesting.library(),
                ScenarioLibraryTest::format, null);
        assertEquals(List.of("first"), ids(library.getAll()));
        library = new ScenarioLibrary(new File(canon, "nowhere"), null, ScenarioTesting.library(), ScenarioLibraryTest::format, null);
        assertEquals(List.of(), ids(library.getAll()));
    }

    @Test
    public void CardLinksThatNameNothingAreWarnings() throws Exception {
        write(canon, "first.hjson", working("first").replace("allow: pass",
                "messages: \"[[Frodo]] and [[Nobody At All]] and [[aragorn|the ranger]].\"\n\t\t\tallow: pass"));
        ScenarioLibrary library = library();
        ScenarioLibrary.Entry first = library.get("first");
        assertTrue(first.report().toString(), first.verified());
        assertEquals(1, first.warnings().size());
        assertTrue(first.warnings().getFirst(), first.warnings().getFirst().contains("[[Nobody At All]]"));

        String html = library.textFor(first).render("[[Frodo]] and [[aragorn|the ranger]]");
        assertTrue(html, html.contains("value=\"10_121\">Frodo</span>"));
        assertTrue(html, html.contains("value=\"4_109\">the ranger</span>"));
    }
}
