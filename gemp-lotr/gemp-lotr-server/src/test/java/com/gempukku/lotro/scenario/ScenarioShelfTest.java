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
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The public folder kept in step with what the repository ships (ScenarioLibrary), the archive, and what players and
 * admins do with scenario files (ScenarioShelf): copying, submitting, approving, rejecting, disabling, restoring.
 */
public class ScenarioShelfTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File shipped;
    private File data;
    private File pub;
    private long now = 1_000_000;
    private ScenarioLibrary library;
    private ScenarioShelf shelf;

    @Before
    public void folders() throws Exception {
        shipped = temp.newFolder("shipped");
        data = temp.newFolder("data");
        pub = new File(data, "public");
        library = new ScenarioLibrary(shipped, data, ScenarioTesting.library(), ScenarioShelfTest::format, null, 0, () -> now);
        shelf = new ScenarioShelf(library, ScenarioTesting.library(), () -> now);
    }

    private static LotroFormat format(String code) {
        try {
            return ScenarioTesting.format(code);
        } catch (CardNotFoundException exp) {
            return null;
        }
    }

    /** A scenario that plays through: one step, which ends it. */
    private static String working(String id, String title) {
        return ScenarioTesting.scenario("""
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: pass
                			end: complete
                		}
                	]
                """).replace("id: test", "id: " + id).replace("title: Test", "title: " + title);
    }

    /** One that can be started but does not play through. */
    private static String broken(String id) {
        return working(id, "Broken").replace("phase(fellowship)", "phase(regroup)");
    }

    private static void write(File folder, String path, String text) throws Exception {
        File file = new File(folder, path);
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
    }

    private static String read(File folder, String path) throws Exception {
        return Files.readString(new File(folder, path).toPath(), StandardCharsets.UTF_8);
    }

    private File own(String player) {
        return new File(new File(data, "users"), player);
    }

    private Map<String, Object> mine(String player, String path) {
        for (Map<String, Object> json : shelf.mine(player))
            if (path.equals(json.get("path")))
                return json;
        return null;
    }

    private static void refused(ThrowingRunnable action, String saying) {
        try {
            action.run();
            fail("Should have been refused");
        } catch (Exception expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains(saying));
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /* ------------------------------------------------------------------ the repository's scenarios */

    @Test
    public void TheShippedScenariosAreCopiedIntoThePublicFolderAndKeptUpToDate() throws Exception {
        write(shipped, "tutorials/one.hjson", working("one", "One"));
        write(shipped, "campaigns/basics.hjson", "{\n\tid: basics\n\ttitle: Basics\n\tscenarios: [\n\t\tone\n\t]\n}");
        assertEquals("tutorials/one.hjson", library.get("one").path());
        assertEquals(working("one", "One"), read(pub, "tutorials/one.hjson"));
        assertTrue("Campaigns are brought across too", new File(pub, "campaigns/basics.hjson").isFile());
        assertEquals("basics", library.campaignOf("one").id());

        //Changed in the repository, and not on the server: the server takes it, and keeps what it had in the archive
        write(shipped, "tutorials/one.hjson", working("one", "One, revised"));
        assertEquals("One, revised", library.get("one").def().title);
        assertEquals(1, library.archive().list().size());
        assertEquals("updated from the repository", library.archive().list().get(0).reason());
        assertTrue(library.conflicts().isEmpty());

        //A file that the repository no longer has is not taken away
        assertTrue(new File(shipped, "tutorials/one.hjson").delete());
        assertNotNull(library.get("one"));
    }

    @Test
    public void ALineEndingIsNotAChange() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        library.get("one");
        write(shipped, "one.hjson", working("one", "One").replace("\n", "\r\n"));
        library.getAll();
        assertTrue(library.conflicts().isEmpty());
        assertEquals("Nothing was replaced", 0, library.archive().list().size());
    }

    @Test
    public void WhatWasChangedOnTheServerIsNotOverwrittenButListedToSettle() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        write(shipped, "two.hjson", working("two", "Two"));
        library.getAll();
        //Changed here (approved, say), and then in the repository as well
        write(pub, "one.hjson", working("one", "Ours"));
        write(shipped, "one.hjson", working("one", "Theirs"));
        //Deleted here, and then changed in the repository
        library.getAll();
        library.delete(null, "two.hjson", "deleted", "admin");
        write(shipped, "two.hjson", working("two", "Two, revised"));

        assertEquals("Ours", library.get("one").def().title);
        assertNull("What was deleted here stays deleted", library.get("two"));
        assertEquals(List.of(new ScenarioLibrary.Conflict("one.hjson", "changed"), new ScenarioLibrary.Conflict("two.hjson", "deleted")),
                library.conflicts());

        library.settle("one.hjson", false, "admin");
        assertEquals("Ours", library.get("one").def().title);
        library.settle("two.hjson", true, "admin");
        assertEquals("Two, revised", library.get("two").def().title);
        assertTrue(library.conflicts().isEmpty());

        //Kept ours until the repository changes it again
        write(shipped, "one.hjson", working("one", "Theirs again"));
        assertEquals(List.of(new ScenarioLibrary.Conflict("one.hjson", "changed")), library.conflicts());
        library.settle("one.hjson", true, "admin");
        assertEquals("Theirs again", library.get("one").def().title);
        assertTrue("Ours went to the archive", library.archive().list().stream().anyMatch(record -> "one.hjson".equals(record.path())
                && record.reason().contains("repository")));
    }

    @Test
    public void AFileTheServerHadBeforeItKeptTrackIsAConflictIfItDiffers() throws Exception {
        write(pub, "one.hjson", working("one", "Server's"));
        write(pub, "same.hjson", working("same", "Same"));
        write(shipped, "one.hjson", working("one", "Repository's"));
        write(shipped, "same.hjson", working("same", "Same"));
        assertEquals("Server's", library.get("one").def().title);
        assertEquals(List.of(new ScenarioLibrary.Conflict("one.hjson", "different")), library.conflicts());
    }

    /* ------------------------------------------------------------------ My Scenarios */

    @Test
    public void ACopyHasATitleAnIdAndAFileOfItsOwn() throws Exception {
        write(own("ketura"), "tutorials/mine.hjson", "# a comment that is kept\n" + working("mine", "Mine"));
        ScenarioLibrary.Entry copy = shelf.copy("ketura", "tutorials/mine.hjson");
        assertEquals("ketura/mine-copy", copy.id());
        assertEquals("tutorials/mine-copy.hjson", copy.path());
        assertEquals("Copy of Mine", copy.def().title);
        assertTrue("The rest of the file is as it was", read(own("ketura"), "tutorials/mine-copy.hjson").startsWith("# a comment that is kept\n"));
        //and again: another id
        assertEquals("ketura/mine-copy-2", shelf.copy("ketura", "tutorials/mine.hjson").id());
        assertEquals("Copy of Copy of Mine", shelf.copy("ketura", "tutorials/mine-copy.hjson").def().title);
    }

    @Test
    public void ACopyOfAPublicScenarioIsLinkedToIt() throws Exception {
        write(shipped, "tutorials/one.hjson", working("one", "One"));
        ScenarioLibrary.Entry copy = shelf.copyPublic("ketura", "one", false);
        assertEquals("ketura/one", copy.id());
        assertEquals("tutorials/one.hjson", copy.path());
        assertEquals("One", copy.def().title);
        assertEquals("one", library.meta("ketura", copy.path()).origin);
        @SuppressWarnings("unchecked")
        Map<String, Object> origin = (Map<String, Object>) mine("ketura", "tutorials/one.hjson").get("origin");
        assertEquals("one", origin.get("id"));
        assertEquals(true, origin.get("exists"));

        ScenarioLibrary.Entry second = shelf.copyPublic("ketura", "one", false);
        assertEquals("ketura/one-2", second.id());
        assertEquals("one", library.meta("ketura", second.path()).origin);
        //and a copy of a copy is linked to it too
        assertEquals("one", library.meta("ketura", shelf.copy("ketura", second.path()).path()).origin);

        refused(() -> shelf.copyPublic("ketura", "nothing", false), "no public scenario");
    }

    @Test
    public void ASubmittedScenarioIsLockedUntilItIsReviewed() throws Exception {
        write(own("ketura"), "mine.hjson", working("mine", "Mine"));
        write(own("ketura"), "bad.hjson", broken("bad"));
        refused(() -> shelf.submit("ketura", "bad.hjson"), "does not play through");

        String[] closed = new String[1];
        shelf.onLocked((owner, path) -> closed[0] = owner + "/" + path);
        shelf.submit("ketura", "mine.hjson");
        assertEquals("A draft of it is closed", "ketura/mine.hjson", closed[0]);
        assertTrue(shelf.locked("ketura", "mine.hjson"));
        assertEquals(true, mine("ketura", "mine.hjson").get("locked"));
        refused(() -> shelf.submit("ketura", "mine.hjson"), "already");
        refused(() -> shelf.delete("ketura", "mine.hjson"), "in review");
        //It can still be copied, to go on working on it
        assertFalse(shelf.locked("ketura", shelf.copy("ketura", "mine.hjson").path()));

        shelf.reject("ketura", "mine.hjson", "  Say more about the Ring.  ", "admin");
        Map<String, Object> json = mine("ketura", "mine.hjson");
        assertEquals("rejected", json.get("status"));
        assertEquals("Say more about the Ring.", json.get("feedback"));
        assertEquals("admin", json.get("reviewedBy"));
        assertFalse(shelf.locked("ketura", "mine.hjson"));
        shelf.delete("ketura", "mine.hjson");
        assertNull(library.at("ketura", "mine.hjson"));
    }

    @Test
    public void AnApprovedSubmissionBecomesPublicAndItsAuthorsCopyIsMarked() throws Exception {
        write(own("ketura"), "puzzles/riddle.hjson", working("riddle", "Riddle").replace("title: Riddle", "title: Riddle\n\tkind: puzzle"));
        shelf.submit("ketura", "puzzles/riddle.hjson");
        assertEquals(1, ((List<?>) shelf.adminState(10).get("submissions")).size());

        ScenarioLibrary.Entry published = shelf.approve("ketura", "puzzles/riddle.hjson", "admin");
        assertEquals("riddle", published.id());
        assertEquals("puzzles/riddle.hjson", published.path());
        assertTrue(published.canonical());
        assertTrue(library.listedFor(published, "anybody", false));
        ScenarioLibrary.Meta meta = library.meta(null, "puzzles/riddle.hjson");
        assertEquals("ketura/puzzles/riddle.hjson", meta.approvedFrom);
        assertEquals("admin", meta.approvedBy);

        Map<String, Object> json = mine("ketura", "puzzles/riddle.hjson");
        assertEquals("approved", json.get("status"));
        assertEquals(false, json.get("locked"));
        assertEquals("current", json.get("published"));
        assertEquals(0, ((List<?>) shelf.adminState(10).get("submissions")).size());

        //Changed since: still linked, and submitting it again replaces the public one
        write(own("ketura"), "puzzles/riddle.hjson", working("riddle", "Riddle, better").replace("title: Riddle, better", "title: Riddle, better\n\tkind: puzzle"));
        assertEquals("changed", mine("ketura", "puzzles/riddle.hjson").get("published"));
        shelf.submit("ketura", "puzzles/riddle.hjson");
        @SuppressWarnings("unchecked")
        Map<String, Object> submission = ((List<Map<String, Object>>) shelf.adminState(10).get("submissions")).get(0);
        assertNotNull("It says what it replaces", submission.get("replaces"));
        shelf.approve("ketura", "puzzles/riddle.hjson", "admin");
        assertEquals("Riddle, better", library.get("riddle").def().title);
        assertEquals(1, library.archive().list().size());
    }

    @Test
    public void ApprovingAVersionOfAPublicScenarioReplacesItKeepingItsIdAndPlace() throws Exception {
        write(shipped, "tutorials/basics/one.hjson", working("one", "One"));
        write(shipped, "campaigns/basics.hjson", "{\n\tid: basics\n\ttitle: Basics\n\tscenarios: [\n\t\tone\n\t]\n}");
        library.getAll();
        shelf.copyPublic("ketura", "one", false);
        //(a second copy, which has an id of its own in the player's folder)
        ScenarioLibrary.Entry copy = shelf.copyPublic("ketura", "one", false);
        write(own("ketura"), copy.path(), ScenarioShelf.retitle(read(own("ketura"), copy.path()), "one-2", "One, improved"));
        shelf.submit("ketura", copy.path());

        ScenarioLibrary.Entry published = shelf.approve("ketura", copy.path(), "admin");
        assertEquals("one", published.id());
        assertEquals("tutorials/basics/one.hjson", published.path());
        assertEquals("One, improved", published.def().title);
        assertEquals("It is still in its campaign", "basics", library.campaignOf("one").id());
        assertTrue(read(pub, "tutorials/basics/one.hjson").contains("id: \"one\"\n"));
        ScenarioArchive.Record replaced = library.archive().list().get(0);
        assertEquals("tutorials/basics/one.hjson", replaced.path());
        assertTrue(replaced.reason(), replaced.reason().startsWith("replaced by approving ketura/"));
        assertEquals("current", mine("ketura", copy.path()).get("published"));

        //and the repository's changing it afterwards is something to settle, not to take
        write(shipped, "tutorials/basics/one.hjson", working("one", "One, from the repository"));
        assertEquals("One, improved", library.get("one").def().title);
        assertEquals(List.of(new ScenarioLibrary.Conflict("tutorials/basics/one.hjson", "changed")), library.conflicts());
    }

    @Test
    public void ASubmissionWhoseIdIsTakenIsNotApproved() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        write(own("ketura"), "one.hjson", working("one", "Not a copy"));
        shelf.submit("ketura", "one.hjson");
        @SuppressWarnings("unchecked")
        Map<String, Object> submission = ((List<Map<String, Object>>) shelf.adminState(10).get("submissions")).get(0);
        assertNull(submission.get("replaces"));
        assertNotNull(submission.get("clash"));
        refused(() -> shelf.approve("ketura", "one.hjson", "admin"), "already");
        assertEquals("One", library.get("one").def().title);
    }

    /* ------------------------------------------------------------------ Scenario Admin */

    @Test
    public void ADisabledScenarioIsNotOfferedNorStartedButByAnAdmin() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        shelf.disable("one", true);
        ScenarioLibrary.Entry one = library.get("one");
        assertFalse(library.listedFor(one, "anybody", false));
        assertFalse(library.startableBy(one, "anybody", false));
        assertFalse(library.listedFor(one, "admin", true));
        assertTrue(library.startableBy(one, "admin", true));
        assertEquals(List.of(), library.getAvailableTo("anybody", false));
        shelf.disable("one", false);
        assertTrue(library.listedFor(library.get("one"), "anybody", false));
    }

    @Test
    public void WhatIsDeletedCanBePutBack() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        write(own("ketura"), "mine.hjson", working("mine", "Mine"));
        library.getAll();
        shelf.disable("one", true);
        shelf.deletePublic("one.hjson", "admin");
        shelf.delete("ketura", "mine.hjson");
        assertNull(library.get("one"));
        assertNull(library.get("ketura/mine"));

        List<ScenarioArchive.Record> records = library.archive().list();
        assertEquals(2, records.size());
        ScenarioArchive.Record theirs = records.get(0);
        assertEquals("ketura", theirs.owner());
        assertEquals("deleted by its author", theirs.reason());
        assertEquals("mine", theirs.id());
        assertEquals("Mine", theirs.title());

        shelf.restore(theirs.key(), "admin");
        assertNotNull(library.get("ketura/mine"));
        ScenarioLibrary.Entry back = shelf.restore(records.get(1).key(), "admin");
        assertEquals("one", back.id());
        assertTrue("What was kept about it comes back with it", library.disabled(back));
        assertNotNull(library.archive().get(records.get(1).key()).restoredAt());

        //Putting back over what is there archives that first
        write(pub, "one.hjson", working("one", "Changed since"));
        shelf.restore(records.get(1).key(), "admin");
        assertEquals("One", library.get("one").def().title);
        assertEquals("Changed since", library.archive().list().get(0).title());
    }

    @Test
    public void AFileThatCannotBeReadIsDeletedAllTheSame() throws Exception {
        write(own("ketura"), "bad.hjson", "{ not a scenario");
        write(pub, "bad.hjson", "{ nor this");
        shelf.delete("ketura", "bad.hjson");
        shelf.deletePublic("bad.hjson", "admin");
        assertEquals(List.of(), library.getAll());
        assertEquals(2, library.archive().list().size());
    }

    @Test
    public void WithoutADataFolderTheRepositorysFilesAreNotChangedFromHere() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        ScenarioLibrary repository = new ScenarioLibrary(shipped, null, ScenarioTesting.library(), ScenarioShelfTest::format, null, 0, () -> now);
        ScenarioShelf admin = new ScenarioShelf(repository, ScenarioTesting.library(), () -> now);
        refused(() -> admin.disable("one", true), "no scenario data folder");
        refused(() -> admin.deletePublic("one.hjson", "admin"), "no scenario data folder");
        assertTrue(new File(shipped, "one.hjson").isFile());
        assertFalse(new File(shipped, ".meta.json").exists());
    }

    @Test
    public void ItIsNoLongerPublishedOnceThePublicOneIsReplacedAnotherWay() throws Exception {
        write(shipped, "one.hjson", working("one", "One"));
        ScenarioLibrary.Entry copy = shelf.copyPublic("ketura", "one", false);
        write(own("ketura"), copy.path(), working("one", "Better"));
        shelf.submit("ketura", copy.path());
        shelf.approve("ketura", copy.path(), "admin");
        assertEquals("current", mine("ketura", copy.path()).get("published"));
        //The repository changes it too, and the admins take the repository's
        write(shipped, "one.hjson", working("one", "Repository's"));
        library.settle("one.hjson", true, "admin");
        assertNull(mine("ketura", copy.path()).get("published"));
        shelf.submit("ketura", copy.path());
    }

    @Test
    public void WhatNoLongerPlaysThroughIsNotApproved() throws Exception {
        write(own("ketura"), "mine.hjson", working("mine", "Mine"));
        shelf.submit("ketura", "mine.hjson");
        //(as if the cards had changed under it since)
        write(own("ketura"), "mine.hjson", broken("mine"));
        refused(() -> shelf.approve("ketura", "mine.hjson", "admin"), "does not play through");
        assertNull(library.get("mine"));
        //and its author cannot change it from the authoring tool's side either
        refused(() -> library.save("ketura", "mine.hjson", working("mine", "Sneaky"), "x", "ketura", true), "submitted for review");
    }

    @Test
    public void APlayerKeepsNoMoreThanSoManyScenarios() throws Exception {
        for (int i = 0; i < ScenarioLibrary.MAX_OWN; i++)
            write(own("ketura"), "s" + i + ".hjson", working("s" + i, "S"));
        refused(() -> shelf.copy("ketura", "s0.hjson"), "as many as a player can keep");
    }

    /* ------------------------------------------------------------------ retitling */

    @Test
    public void RetitlingChangesTheTopLevelLinesAndNothingElse() throws Exception {
        String text = """
                # The first tutorial
                {
                	id: one
                	// the title is what the hall shows
                	title: "The: first"
                	options: { gap: 20, fast: true }
                	cards: {
                		freeps: { frodo: 10_121
                		}
                	}
                	messages: '''
                		title: not this one
                		id: nor this
                		'''
                	steps: [
                		{
                			id: inner
                			title: nested
                			text: braces { in a quoteless string
                		}
                	]
                }
                """;
        String retitled = ScenarioShelf.retitle(text, "two", "Copy of The: first");
        assertEquals(text.replace("\tid: one", "\tid: \"two\"").replace("\ttitle: \"The: first\"", "\ttitle: \"Copy of The: first\""), retitled);
        assertEquals("Windows line endings stay so",
                retitled.replace("\n", "\r\n"), ScenarioShelf.retitle(text.replace("\n", "\r\n"), "two", "Copy of The: first"));
        //A file with no title gets one, under its id
        assertEquals("{\n\tid: \"two\"\n\ttitle: \"T\"\n}", ScenarioShelf.retitle("{\n\tid: one\n}", "two", "T"));
        //And a file written without the root's braces
        assertEquals("id: \"two\"\ntitle: \"T\"", ScenarioShelf.retitle("id: one\ntitle: S", "two", "T"));
        assertNull(ScenarioShelf.retitle("{ }", "two", "T"));
    }
}
