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

    /** The public scenarios: public/ in the data folder.  (What the repository ships is in 'shipped': see ShippedScenarios.) */
    private File canon;
    private File users;
    private File shipped;
    private long now = 1_000_000;

    @Before
    public void folders() throws Exception {
        users = temp.newFolder("scenarios-user");
        canon = new File(users, "public");
        assertTrue(canon.mkdirs());
        shipped = temp.newFolder("shipped");
    }

    private ScenarioLibrary library() {
        return new ScenarioLibrary(shipped, users, ScenarioTesting.library(), ScenarioLibraryTest::format, null, 5000, () -> now);
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
        //An admin is offered the public ones that do not play through as well, to see why; other players' scenarios
        // are for them to review when they are submitted, and are not in the list (though an admin can start them)
        assertEquals(List.of("first", "second"), ids(library.getAvailableTo("admin", true)));
        assertTrue(library.startableBy(library.get("someone/theirs"), "admin", true));
        assertFalse(library.startableBy(library.get("someone/theirs"), "ketura", false));
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
        assertFalse(bad.openable());
        //(what can be left out is, to see what else is wrong; here that leaves a file with no format)
        assertEquals(bad.errors().toString(), 2, bad.errors().size());
        assertTrue(bad.errors().getFirst(), bad.errors().getFirst().contains("unrecognized field 'nonsense'"));
        assertTrue(bad.errors().getLast(), bad.errors().getLast().contains("format: is missing"));

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

    /** A card renamed by hand under 'cards', and still called by its old name ('the') in three other places. */
    private static String renamed(String id) {
        return working(id)
                .replace("allow: pass", "do: attach(the, frodo), exert(the)\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Put the Ring on.\n\t\t\t\t}\n\t\t\t]\n\t\t\tallow: pass")
                .replace("steps: [", "# the old name was 'the'\n\tsetup: {\n\t\tfreeps: {\n\t\t\thand: aragorn, the, boromir\n\t\t}\n\t\tshadow: {\n\t\t\thand: the\n\t\t}\n\t}\n\tsteps: [");
    }

    @Test
    public void AFileWithMistakesIsReadWithThemLeftOutForItsAuthorOnly() throws Exception {
        write(canon, "first.hjson", renamed("first"));
        ScenarioLibrary library = library();
        ScenarioLibrary.Entry broken = library.get("first");
        assertNotNull("It is known by its id, so that it can be opened", broken);
        assertFalse(broken.playable());
        assertFalse(broken.verified());
        assertFalse("Nobody can start it, an admin included", library.startableBy(broken, "anybody", true));
        assertEquals(List.of(), library.getAvailableTo("anybody", true));
        assertTrue(broken.openable());
        assertNull(broken.report());

        //Every mistake is found, not only the first
        assertEquals(broken.errors().toString(), 3, broken.errors().size());
        assertTrue(broken.errors().get(0), broken.errors().get(0).startsWith("setup.freeps.hand: there is no card called 'the'"));
        assertTrue(broken.errors().get(1), broken.errors().get(1).startsWith("setup.shadow.hand: there is no card called 'the'"));
        assertTrue(broken.errors().get(2), broken.errors().get(2).startsWith("step 1.do: there is no card called 'the'"));

        //Where the card is named is said once, and neither the comment nor the message's words are among the lines
        String[] lines = Files.readString(new File(canon, "first.hjson").toPath(), StandardCharsets.UTF_8).split("\n", -1);
        List<Integer> named = new java.util.ArrayList<>();
        for (int i = 0; i < lines.length; i++)
            if (lines[i].strip().startsWith("hand: ") && lines[i].contains("the") || lines[i].strip().startsWith("do: attach(the"))
                named.add(i + 1);
        assertEquals(3, named.size());
        assertTrue(broken.errors().get(0), broken.errors().get(0).endsWith(
                "('the' is named on lines " + named.get(0) + ", " + named.get(1) + ", " + named.get(2) + " of the file.)"));
        assertFalse(broken.errors().get(1).contains("is named on"));

        //What is left: the cards that are declared stay in a list, and what names only the other goes whole
        assertEquals(3, broken.leftOut().size());
        ScenarioSalvage.LeftOut hand = broken.leftOut().get(0);
        assertEquals("setup.freeps.hand", hand.where());
        assertEquals("aragorn, the, boromir", hand.was());
        assertEquals(0, hand.stepInFile());
        assertEquals(List.of("aragorn", "boromir"), broken.def().setup.zones.get(Seat.FREEPS).get(ScenarioDefinition.SetupZone.HAND));
        assertEquals("the", broken.leftOut().get(1).was());
        ScenarioSalvage.LeftOut does = broken.leftOut().get(2);
        assertEquals("step 1.do", does.where());
        assertEquals("attach(the, frodo), exert(the)", does.was());
        assertTrue(does.message(), does.message().startsWith("there is no card called 'the'"));
        assertEquals(1, does.stepInFile());
        assertEquals(1, does.step());
        assertTrue(broken.def().steps.getFirst().doOps.isEmpty());
        assertEquals("Put the Ring on.", broken.def().steps.getFirst().messages.getFirst().text);

        assertEquals("", ScenarioLibrary.elsewhere("steps: []", "step 1.when: something else is wrong"));
        assertEquals("", ScenarioLibrary.elsewhere(null, "there is no card called 'the'"));
    }

    @Test
    public void AStepThatCannotBeMendedIsLeftOutWholeAndTheRestKeepTheFilesNumbers() throws Exception {
        write(canon, "first.hjson", working("first").replace("steps: [", """
                setup: {
                		startAt: {
                			turn: 1
                			site: 3
                		}
                	}
                	steps: [
                		{
                			pause: true
                			when: freeps, action, phase(fellowship)
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			allow: play(nobody)
                		}"""));
        ScenarioLibrary.Entry broken = library().get("first");
        assertTrue(broken.errors().toString(), broken.openable());
        assertEquals(broken.errors().toString(), 3, broken.leftOut().size());

        assertEquals("setup.startAt", broken.leftOut().get(0).where());
        assertEquals("site: 3", broken.leftOut().get(0).was());

        ScenarioSalvage.LeftOut pause = broken.leftOut().get(1);
        assertEquals("step 1", pause.where());
        assertTrue(pause.message(), pause.message().contains("a pause with no messages"));
        assertTrue(pause.was(), pause.was().contains("pause: true"));
        assertEquals(1, pause.stepInFile());
        assertEquals("The whole step went", 0, pause.step());

        ScenarioSalvage.LeftOut allow = broken.leftOut().get(2);
        assertEquals("It is still step 2 to whoever has the file in front of them", "step 2.allow", allow.where());
        assertTrue(broken.errors().get(2), broken.errors().get(2).startsWith("step 2.allow: there is no card called 'nobody'"));
        assertEquals(2, allow.stepInFile());
        assertEquals(1, allow.step());
        assertEquals(2, broken.def().steps.size());
    }

    @Test
    public void SomeMistakesCannotBeLeftOut() throws Exception {
        write(canon, "a-no-title.hjson", working("first").replace("title: Test", ""));
        write(canon, "b-not-hjson.hjson", working("second") + "\n}");
        write(canon, "c-no-such-format.hjson", working("third").replace("format: scenario", "format: nonesuch"));
        //A card that is no card goes, and everything that named it after it; a Ring-bearer cannot go
        write(canon, "d-no-such-card.hjson", working("fourth").replace("aragorn: 4_109", "aragorn: 99_999")
                .replace("steps: [", "setup: {\n\t\tfreeps: {\n\t\t\thand: aragorn, boromir\n\t\t}\n\t}\n\tsteps: ["));
        write(canon, "e-no-ring-bearer.hjson", working("fifth").replace("oppFrodo: 10_121", "oppFrodo: 99_999"));
        ScenarioLibrary library = library();
        List<ScenarioLibrary.Entry> all = library.getAll();
        assertEquals(5, all.size());

        assertFalse(all.get(0).openable());
        assertTrue(all.get(0).errors().toString(), all.get(0).errors().getLast().contains("title: is missing"));
        assertFalse(all.get(1).openable());
        assertTrue(all.get(1).errors().toString(), all.get(1).errors().getFirst().startsWith("Not valid HJSON"));
        assertFalse(all.get(2).openable());
        assertNotNull("It reads; it is its format there is none of", all.get(2).def());
        assertTrue(all.get(2).errors().toString(), all.get(2).errors().getFirst().startsWith("format: there is no format"));

        ScenarioLibrary.Entry card = all.get(3);
        assertTrue(card.errors().toString(), card.openable());
        assertEquals(2, card.leftOut().size());
        assertEquals("aragorn: 99_999", card.leftOut().get(0).was());
        assertEquals("aragorn, boromir", card.leftOut().get(1).was());
        assertNull(card.def().cards.get("aragorn"));

        assertFalse(all.get(4).errors().toString(), all.get(4).openable());
        assertTrue(all.get(4).errors().toString(), all.get(4).errors().getLast().startsWith("ringBearer"));
    }

    @Test
    public void AFileThatCannotBePlayedDoesNotTakeAnIdFromOneThatCan() throws Exception {
        write(canon, "a-broken.hjson", renamed("first"));
        write(canon, "b-working.hjson", working("first"));
        ScenarioLibrary library = library();
        assertTrue(library.get("first").playable());
        assertEquals("b-working.hjson", library.get("first").path());
        ScenarioLibrary.Entry loser = library.getAll().getFirst();
        assertNull(loser.id());
        assertFalse("With no id of its own there is nothing to open it by", loser.openable());
        assertTrue(loser.errors().getLast(), loser.errors().getLast().contains("is already the id of b-working.hjson"));
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
