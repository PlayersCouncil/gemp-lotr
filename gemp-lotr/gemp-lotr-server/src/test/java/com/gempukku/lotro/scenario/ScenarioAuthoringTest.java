package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Writing a scenario by playing it: the author's requests on one side, a client that connects to the game and
 * answers its decisions on the other.
 */
public class ScenarioAuthoringTest {
    private static final String AUTHOR = "Tester";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File canon;
    private File users;
    private ScenarioLibrary library;
    private ScenarioAuthoring authoring;
    private final Player player = new Player(1, AUTHOR, "", "u", null, null, null, null, false);
    private final List<LotroGameMediator> games = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        users = temp.newFolder("scenarios-user");
        //(the public scenarios are public/ in the data folder; nothing ships here)
        canon = new File(users, "public");
        canon.mkdirs();
        library = new ScenarioLibrary(temp.newFolder("shipped"), users, ScenarioTesting.library(), ScenarioAuthoringTest::format, null, 0, System::currentTimeMillis);
        authoring = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioAuthoringTest::format, null, (who, live) -> {
            LotroGameMediator mediator = new LotroGameMediator(String.valueOf(games.size() + 1), live, LotroServer.AUTHORING_TIMER, false);
            mediator.startGame();
            games.add(mediator);
            return mediator;
        });
    }

    private static LotroFormat format(String code) {
        try {
            return ScenarioTesting.format(code);
        } catch (CardNotFoundException exp) {
            return null;
        }
    }

    /** The author's browser, as far as the game can tell. */
    private class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        int channel;
        GameEvent decision;
        final List<JSONObject> steps = new ArrayList<>();
        JSONObject info;

        Client(LotroGameMediator mediator) throws Exception {
            this.mediator = mediator;
            mediator.signupUserForGame(player, this);
        }

        void poll() throws Exception {
            mediator.processVisitor(mediator.getCommunicationChannel(player, channel), channel, AUTHOR, this);
        }

        @Override
        public void visitChannelNumber(int channelNumber) {
            channel = channelNumber;
        }

        @Override
        public void visitClock(Map<String, Integer> secondsLeft) { }

        @Override
        public void visitGameEvent(GameEvent event) {
            if (event.getType() == GameEvent.Type.DECISION) {
                decision = event;
            } else if (event.getType() == GameEvent.Type.SCENARIO) {
                JSONObject json = JSON.parseObject(event.getScenario());
                if (json.getString("type").equals("info"))
                    info = json;
                else if (json.getString("type").equals("step"))
                    steps.add(json);
            }
        }

        JSONObject lastStep() {
            return steps.isEmpty() ? null : steps.getLast();
        }

        void answer(String answer) throws Exception {
            assertNotNull("The game is not asking the author anything", decision);
            GameEvent answering = decision;
            decision = null;
            mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
            poll();
        }
    }

    private LotroGameMediator game() {
        return games.getLast();
    }

    private static JSONObject json(Map<String, Object> result) {
        return JSON.parseObject(JSON.toJSONString(result));
    }

    private JSONObject state() {
        return json(authoring.state(player));
    }

    private static JSONArray steps(JSONObject state) {
        JSONArray steps = state.getJSONObject("draft").getJSONArray("steps");
        return steps == null ? new JSONArray() : steps;
    }

    /** The scenario the tests write on: something for each seat to play, and nothing scripted. */
    private void openBlank() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/blank.hjson").toPath(), ScenarioTesting.scenario("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			hand: aragorn
                		}
                		shadow: {
                			hand: runner1, runner2
                		}
                	}
                """).replace("id: test", "id: blank"), StandardCharsets.UTF_8);
        authoring.open(player, "blank", true);
    }

    @Test
    public void AnAuthorIsToldWhatSavingOverAHandWrittenFileLoses() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        File file = new File(canon, "tutorials/commented.hjson");
        Files.writeString(file.toPath(), ScenarioTesting.scenario("""
                	# Why the opponent passes
                	opponent: {
                		fallback: pass
                	}
                	// and another way of writing a comment
                	note: This is kept.
                """).replace("id: test", "id: commented"), StandardCharsets.UTF_8);
        JSONObject state = json(authoring.open(player, "commented", true)).getJSONObject("state");
        assertEquals(2, state.getIntValue("commentsLost"));
        authoring.save(player, null, false, true);
        assertEquals(0, state().getIntValue("commentsLost"));
        String saved = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertFalse(saved.contains("Why the opponent passes"));
        assertTrue(saved.contains("note: This is kept."));
        //What the writer says for itself is not counted as something to lose
        assertEquals(0, json(authoring.open(player, "commented", true)).getJSONObject("state").getIntValue("commentsLost"));
    }

    @Test
    public void WhatTheAuthorDoesWhereTheScriptIsSilentBecomesScript() throws Exception {
        openBlank();
        JSONObject state = state();
        assertTrue(state.getBooleanValue("session"));
        assertEquals("author", state.getString("mode"));
        assertEquals(0, steps(state).size());
        assertTrue("With no steps, the script has nothing more to say from the start",
                state.getJSONObject("position").getBooleanValue("frontier"));
        assertEquals("action", state.getJSONObject("waiting").getJSONObject("human").getString("type"));

        Client client = new Client(game());
        assertTrue("The client is told it is an author's game", client.info.getBooleanValue("author"));
        assertNotNull(client.decision);

        //Aragorn is played by picking his action; the script gets it by his name
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        client.answer(actionIds[0]);
        state = state();
        assertEquals(1, steps(state).size());
        JSONObject first = steps(state).getJSONObject(0);
        assertEquals("freeps, action, phase(fellowship), turn(1)", first.getString("when"));
        assertEquals("play(aragorn)", first.getString("allow"));
        assertTrue(state.getBooleanValue("dirty"));
        assertEquals("matched", state.getJSONArray("statuses").getString(0));

        //Passing out of the phase is a step too.  (Whatever the game asks next is not what this test is about.)
        client.answer("");
        state = state();
        assertEquals("pass", steps(state).getJSONObject(1).getString("allow"));
        assertFalse(game().isFinished());
        assertNull("Leaving the script is not the end of an author's game", game().getScenario().Result());
    }

    @Test
    public void TheOpponentWaitsForTheAuthorAndItsAnswerIsWrittenDown() throws Exception {
        openBlank();
        Client client = new Client(game());
        //Out of the Fellowship phase and on to site 2, where the Shadow has twilight to spend
        for (int i = 0; i < 10 && state().getJSONObject("waiting").getJSONObject("opponent") == null; i++) {
            client.poll();
            client.answer("");
        }
        JSONObject held = state().getJSONObject("waiting").getJSONObject("opponent");
        assertNotNull("The opponent's decision should be waiting for the author", held);
        assertTrue(held.getBooleanValue("held"));
        assertEquals("shadow", held.getString("seat"));
        List<String> terms = new ArrayList<>();
        for (Object option : held.getJSONArray("options"))
            terms.add(((JSONObject) option).getString("term"));
        assertTrue("The runners should be on offer by name: " + terms, terms.contains("play(runner1)"));

        int before = steps(state()).size();
        try {
            authoring.answer(player, "play(aragorn)", true);
            fail("The opponent cannot play the other seat's card");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("play(aragorn)"));
        }
        authoring.answer(player, "play(runner1)", true);
        JSONObject state = state();
        assertEquals(before + 1, steps(state).size());
        JSONObject written = steps(state).getJSONObject(before);
        assertEquals("play(runner1)", written.getString("answer"));
        assertTrue(written.getString("when"), written.getString("when").startsWith("shadow, action, phase(shadow)"));

        //Played again from the top, the script answers for the Shadow by itself
        state = json(authoring.goTo(player, 99)).getJSONObject("state");
        assertNull(state.getString("notice"), state.getString("notice"));
        assertEquals(before + 1, steps(state).size());
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("alias").equals("runner1"))
                assertEquals("minions", ((JSONObject) card).getString("zone"));
    }

    @Test
    public void TheOpponentCanBeLeftToDoTheLeastUnwritten() throws Exception {
        openBlank();
        Client client = new Client(game());
        for (int i = 0; i < 10 && state().getJSONObject("waiting").getJSONObject("opponent") == null; i++) {
            client.poll();
            client.answer("");
        }
        int before = steps(state()).size();
        authoring.answer(player, null, false);
        JSONObject state = state();
        assertEquals("Nothing is written for it", before, steps(state).size());
        assertNull("The Shadow passed and the game went on", state.getJSONObject("waiting").getJSONObject("opponent"));

        //With the author not wanting to be asked, the opponent falls back as it would in a player's game
        authoring.settings(player, null, false, null);
        state = json(authoring.goTo(player, 99)).getJSONObject("state");
        assertFalse(state.getBooleanValue("askOpponent"));
        //(after the moment it takes over everything it does, as in a player's game)
        long deadline = System.currentTimeMillis() + 5000;
        while (state().getJSONObject("waiting").getJSONObject("opponent") != null && System.currentTimeMillis() < deadline)
            Thread.sleep(20);
        assertNull(state().getJSONObject("waiting").getJSONObject("opponent"));
        assertEquals(before, steps(state()).size());
    }

    @Test
    public void GoingToAStepPlaysTheScriptThere() throws Exception {
        openBlank();
        Client client = new Client(game());
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        client.answer(actionIds[0]);
        client.answer("");
        for (int i = 0; i < 10 && state().getJSONObject("waiting").getJSONObject("opponent") == null; i++) {
            client.poll();
            client.answer("");
        }
        authoring.answer(player, "play(runner1)", true);
        int written = steps(state()).size();
        assertTrue(written >= 3);

        String gameBefore = game().getGameId();
        JSONObject result = json(authoring.goTo(player, 2));
        assertTrue(result.getBooleanValue("rebuilt"));
        assertNotEquals(gameBefore, result.getString("gameId"));
        JSONObject state = result.getJSONObject("state");
        assertEquals("The script is as it was", written, steps(state).size());
        assertEquals(2, state.getJSONObject("position").getIntValue("step"));
        assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));
        assertEquals("matched", state.getJSONArray("statuses").getString(0));
        assertNull(state.getString("notice"));
        //Aragorn was played on the way
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("alias").equals("aragorn"))
                assertEquals("characters", ((JSONObject) card).getString("zone"));

        //Past the end is the whole script, and then the author's again
        state = json(authoring.goTo(player, 99)).getJSONObject("state");
        assertEquals(written + 1, state.getJSONObject("position").getIntValue("step"));
        assertTrue(state.getJSONObject("position").getBooleanValue("frontier"));
        assertEquals(written, steps(state).size());
    }

    @Test
    public void AChangeToTheStepInHandIsOnTheScreenAtOnce() throws Exception {
        openBlank();
        Client client = new Client(game());
        JSONObject result = json(authoring.stepHere(player));
        assertFalse("Writing a step for the decision in hand needs no fresh game", result.getBooleanValue("rebuilt"));
        assertTrue(result.getBooleanValue("events"));
        JSONObject state = result.getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));
        client.poll();
        assertNotNull("The step should have reached the screen", client.lastStep());
        assertEquals(1, client.lastStep().getIntValue("step"));

        //A message for it
        JSONObject draft = state.getJSONObject("draft");
        JSONObject step = draft.getJSONArray("steps").getJSONObject(0);
        JSONObject message = new JSONObject();
        message.put("text", "This is [[Aragorn]]. Play him.");
        message.put("anchor", "card:aragorn");
        JSONArray messages = new JSONArray();
        messages.add(message);
        step.put("messages", messages);
        result = json(authoring.draft(player, draft.toString(), null));
        assertFalse("A message on the step in hand needs no fresh game", result.getBooleanValue("rebuilt"));
        client.poll();
        JSONObject shown = client.lastStep().getJSONArray("messages").getJSONObject(0);
        assertTrue(shown.getString("html"), shown.getString("html").contains("Play him."));
        assertEquals("card:aragorn", shown.getString("anchor"));

        //The answer the author then gives is what the step allows
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        client.answer(actionIds[0]);
        state = state();
        assertEquals(2 - 1, steps(state).size());
        assertEquals("play(aragorn)", steps(state).getJSONObject(0).getString("allow"));
        assertEquals(1, steps(state).getJSONObject(0).getJSONArray("messages").size());
    }

    @Test
    public void AStepPutInBeforeWhereTheGameIsStartsItAgain() throws Exception {
        openBlank();
        Client client = new Client(game());
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        client.answer(actionIds[0]);
        JSONObject draft = state().getJSONObject("draft");
        JSONObject pause = new JSONObject();
        pause.put("pause", true);
        pause.put("messages", JSON.parseArray("[\"Welcome.\"]"));
        draft.getJSONArray("steps").add(0, pause);

        String gameBefore = game().getGameId();
        JSONObject result = json(authoring.draft(player, draft.toString(), 1));
        assertTrue(result.getBooleanValue("rebuilt"));
        assertNotEquals(gameBefore, result.getString("gameId"));
        JSONObject state = result.getJSONObject("state");
        assertEquals(2, steps(state).size());
        assertEquals(1, state.getJSONObject("position").getIntValue("step"));
        Client fresh = new Client(game());
        assertNotNull(fresh.lastStep());
        assertTrue(fresh.lastStep().getBooleanValue("pause"));
        assertNull("A pause holds its decision back", fresh.decision);

        //A draft that is not a scenario is refused, and the game in hand is left alone
        try {
            authoring.draft(player, "{id: blank}", null);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertEquals(game().getGameId(), state().getString("gameId"));
        }
    }

    @Test
    public void TheBoardIsChangedNowOrFromTheStart() throws Exception {
        openBlank();
        Client client = new Client(game());

        //Now: the change is made, and the next step to be written carries it
        JSONObject result = json(authoring.apply(player, "wound(frodo, 2)", false));
        assertFalse(result.getBooleanValue("rebuilt"));
        assertEquals(1, result.getJSONObject("state").getJSONArray("pending").size());
        assertEquals(2, wounds(result.getJSONObject("state"), "frodo"));
        client.answer("");
        JSONObject state = state();
        assertEquals("wound(frodo, 2)", steps(state).getJSONObject(0).getString("do"));
        assertEquals(0, state.getJSONArray("pending").size());

        //From the start: the setup is changed, and a fresh game is brought to the same place
        result = json(authoring.apply(player, "move(boromir, freeps.hand)", true));
        assertTrue(result.getBooleanValue("rebuilt"));
        state = result.getJSONObject("state");
        assertEquals("aragorn, boromir", state.getJSONObject("draft").getJSONObject("setup").getJSONObject("freeps").getString("hand"));
        assertEquals(2, state.getJSONObject("position").getIntValue("step"));
        assertEquals("The step that wounds Frodo was played on the way", 2, wounds(state, "frodo"));

        try {
            authoring.apply(player, "move(nobody, freeps.hand)", false);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("nobody"));
        }
    }

    private static JSONObject cardNamed(JSONObject state, String alias) {
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("alias").equals(alias))
                return (JSONObject) card;
        throw new AssertionError("No card called " + alias);
    }

    private static List<String> inZone(JSONObject state, String owner, String zone) {
        List<String> found = new ArrayList<>();
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("owner").equals(owner) && ((JSONObject) card).getString("zone").equals(zone))
                found.add(((JSONObject) card).getString("alias"));
        return found;
    }

    private static int wounds(JSONObject state, String alias) {
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("alias").equals(alias))
                return ((JSONObject) card).getIntValue("wounds");
        throw new AssertionError("No card called " + alias);
    }

    /** The action in the decision the client has whose text names a card ("Play Aragorn"). */
    private static String playing(Client client, String name) {
        Map<String, String[]> parameters = client.decision.getAwaitingDecision().getDecisionParameters();
        String[] texts = parameters.get("actionText");
        for (int i = 0; i < texts.length; i++)
            if (texts[i].contains(name))
                return parameters.get("actionId")[i];
        throw new AssertionError("Nothing offers " + name + ": " + String.join(", ", texts));
    }

    @Test
    public void AStepIsPutInFrontOfTheBlockStepThatHasTheDecisionAndTakesIt() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.copy(new File("src/test/resources/scenarios/two-skirmishes.hjson").toPath(), new File(canon, "tutorials/two-skirmishes.hjson").toPath());
        authoring.open(player, "two-skirmishes", true);
        //The first decision the block's catch-all step takes: in Gandalf's skirmish, which is fought first
        JSONObject state = json(authoring.goTo(player, 1, 4)).getJSONObject("state");
        assertEquals(4, state.getJSONObject("position").getIntValue("sub"));
        state = json(authoring.stepBefore(player)).getJSONObject("state");
        JSONArray kids = steps(state).getJSONObject(0).getJSONArray("anyOrder");
        assertEquals(5, kids.size());
        JSONObject made = kids.getJSONObject(3);
        assertEquals("freeps, action, phase(skirmish), fighting(gandalf)", made.getString("when"));
        assertTrue(made.getBooleanValue("once"));
        assertEquals("It has the decision now", 4, state.getJSONObject("position").getIntValue("sub"));
        assertEquals("freeps, action, phase(skirmish)", kids.getJSONObject(4).getString("when"));

        //What is done at it is what it allows
        Client client = new Client(game());
        client.answer("");
        kids = steps(state()).getJSONObject(0).getJSONArray("anyOrder");
        assertEquals("pass", kids.getJSONObject(3).getString("allow"));

        //Not for a step of the script itself: that step can be given the message
        authoring.goTo(player, 2);
        try {
            authoring.stepBefore(player);
            fail("Step 2 is not in a block");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("give it the message itself"));
        }
    }

    @Test
    public void WhatIsDoneAtARepeatingStepIsWrittenIntoItsAnswersInOrder() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/both.hjson").toPath(), ScenarioTesting.scenario("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			hand: aragorn, boromir
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship), turn(1)
                			until: phase(shadow)
                			allow: play(aragorn)
                		}
                	]
                """).replace("id: test", "id: both"), StandardCharsets.UTF_8);
        authoring.open(player, "both", true);
        Client client = new Client(game());
        client.answer(playing(client, "Aragorn"));
        client.answer(playing(client, "Boromir"));
        client.answer("");
        JSONObject step = steps(state()).getJSONObject(0);
        assertEquals("play(aragorn), play(boromir), pass", step.getString("answer"));
        assertEquals("What was done and was not allowed is allowed now", "play(aragorn), play(boromir), pass", step.getString("allow"));

        //Played to again, and done the same way: nothing changes, and nothing is written twice
        authoring.goTo(player, 1);
        client = new Client(game());
        client.answer(playing(client, "Aragorn"));
        client.answer(playing(client, "Boromir"));
        client.answer("");
        step = steps(state()).getJSONObject(0);
        assertEquals("play(aragorn), play(boromir), pass", step.getString("answer"));

        //Done another way: from where it differs, the answers are what was done this time
        authoring.goTo(player, 1);
        client = new Client(game());
        client.answer(playing(client, "Boromir"));
        step = steps(state()).getJSONObject(0);
        assertEquals("play(boromir)", step.getString("answer"));
    }

    @Test
    public void ARepeatingStepWithPassWhenIsNotGivenPassToAllow() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/both.hjson").toPath(), ScenarioTesting.scenario("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			hand: aragorn, boromir
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship), turn(1)
                			until: phase(shadow)
                			allow: {
                				answers: play(aragorn), play(boromir)
                				passWhen: [
                					{
                						type: canSpot
                						filter: alias(aragorn)
                					}
                				]
                			}
                		}
                	]
                """).replace("id: test", "id: both"), StandardCharsets.UTF_8);
        authoring.open(player, "both", true);
        Client client = new Client(game());
        client.answer(playing(client, "Aragorn"));
        client.answer("");
        JSONObject step = steps(state()).getJSONObject(0);
        assertEquals("play(aragorn), pass", step.getString("answer"));
        assertEquals("play(aragorn), play(boromir)", step.getJSONObject("allow").getString("answers"));
    }

    @Test
    public void AScenarioInReviewIsNotChangedByItsAuthor() throws Exception {
        authoring.create(player, "unsaved", "Unsaved");
        try {
            authoring.submitting(player);
            fail("Not saved yet");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Save it first"));
        }
        //One that plays through, in the author's folder
        File file = new File(users, "users/" + AUTHOR + "/tutorials/my-first.hjson");
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), ScenarioTesting.scenario("""
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: pass
                			end: complete
                		}
                	]
                """).replace("id: test", "id: my-first"), StandardCharsets.UTF_8);
        authoring.open(player, AUTHOR + "/my-first", false);
        assertEquals("tutorials/my-first.hjson", authoring.submitting(player));
        assertTrue(state().getBooleanValue("own"));

        ScenarioShelf shelf = new ScenarioShelf(library, ScenarioTesting.library());
        shelf.onLocked(authoring::closeOn);
        shelf.submit(AUTHOR, "tutorials/my-first.hjson");
        assertNull("Submitting it closed the draft", authoring.current(player));
        try {
            authoring.open(player, AUTHOR + "/my-first", false);
            fail("It is in review");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("submitted for review"));
        }
        //An admin may open it, and save it
        Player admin = new Player(2, "Boss", "", "a", null, null, null, null, false);
        authoring.open(admin, AUTHOR + "/my-first", true);
        authoring.edit(admin, "{top: {title: \"Better\"}}", null);
        authoring.save(admin, null, false, true);
        assertEquals("Better", library.get(AUTHOR + "/my-first").def().title);
        assertTrue("and it is still in review", shelf.locked(AUTHOR, "tutorials/my-first.hjson"));
        authoring.close(admin);
    }

    @Test
    public void ANewScenarioIsSavedWhereTheLibraryFindsIt() throws Exception {
        JSONObject result = json(authoring.create(player, "my-first", "My First"));
        assertTrue(result.getBooleanValue("rebuilt"));
        JSONObject state = result.getJSONObject("state");
        //It has a place from the start, named after it, and a file there once it has been saved
        assertEquals("users/" + AUTHOR + "/tutorials/my-first.hjson", state.getString("path"));
        assertEquals("A tutorial, so it is filed with the tutorials", "tutorials/my-first.hjson", state.getString("file"));
        assertEquals("tutorials", state.getString("kindFolder"));
        assertFalse(state.getBooleanValue("onDisk"));
        assertFalse(new File(users, "users/" + AUTHOR + "/tutorials/my-first.hjson").exists());
        assertEquals("My First", state.getJSONObject("draft").getString("title"));
        //Both Ring-bearers have a Ring
        assertEquals("oppRing", state.getJSONObject("draft").getJSONObject("ring").getString("shadow"));
        assertEquals("attached", cardNamed(state, "oppRing").getString("zone"));
        assertEquals("oppFrodo", cardNamed(state, "oppRing").getString("on"));

        Client client = new Client(game());
        client.answer("");

        result = json(authoring.save(player, null, false, true));
        JSONObject saved = result.getJSONObject("saved");
        assertEquals("users/" + AUTHOR + "/tutorials/my-first.hjson", saved.getString("path"));
        assertEquals(AUTHOR + "/my-first", saved.getString("id"));
        assertTrue(saved.toString(), saved.getBooleanValue("playable"));
        assertFalse(result.getJSONObject("state").getBooleanValue("dirty"));
        assertTrue(result.getJSONObject("state").getBooleanValue("onDisk"));
        File file = new File(users, "users/" + AUTHOR + "/tutorials/my-first.hjson");
        assertTrue(file.isFile());
        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(text, text.contains("id: my-first"));
        assertTrue(text, text.contains("allow: pass"));

        //Opened again, it is the same draft, and saving writes the same file
        state = json(authoring.open(player, AUTHOR + "/my-first", false)).getJSONObject("state");
        assertEquals("my-first", state.getJSONObject("draft").getString("id"));
        assertEquals(1, steps(state).size());
        authoring.save(player, null, false, false);
        assertEquals(text, Files.readString(file.toPath(), StandardCharsets.UTF_8));

        //Somebody else's is not theirs to open
        try {
            authoring.open(new Player(2, "Other", "", "u", null, null, null, null, false), AUTHOR + "/my-first", false);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            //(as it should be)
        }
    }

    @Test
    public void APlayersGameOfTheDraftHasNoAuthorInIt() throws Exception {
        openBlank();
        Client client = new Client(game());
        client.answer("");
        JSONObject result = json(authoring.test(player, 1));
        assertEquals("test", result.getJSONObject("state").getString("mode"));
        Client tester = new Client(game());
        assertFalse(tester.info.getBooleanValue("author"));
        assertEquals(1, tester.lastStep().getIntValue("step"));
        try {
            authoring.stepHere(player);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            //(as it should be)
        }
        result = json(authoring.resume(player));
        assertEquals("author", result.getJSONObject("state").getString("mode"));
        assertEquals(1, result.getJSONObject("state").getJSONObject("position").getIntValue("step"));

        JSONObject check = json(authoring.check(player));
        assertNotNull(check.getString("report"));
    }

    @Test
    public void AStepInHandCanBeAnsweredAgain() throws Exception {
        openBlank();
        Client client = new Client(game());
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        client.answer(actionIds[0]);
        assertEquals("play(aragorn)", steps(state()).getJSONObject(0).getString("allow"));

        authoring.goTo(player, 1);
        Client again = new Client(game());
        assertEquals(1, again.lastStep().getJSONObject("allow").getJSONArray("answers").size());
        JSONObject result = json(authoring.retake(player));
        assertFalse(result.getBooleanValue("rebuilt"));
        assertNull(steps(result.getJSONObject("state")).getJSONObject(0).getString("allow"));
        again.poll();
        assertNull("The step no longer limits the answer", again.lastStep().get("allow"));
        again.answer("");
        JSONObject state = state();
        assertEquals(1, steps(state).size());
        assertEquals("pass", steps(state).getJSONObject(0).getString("allow"));
    }

    @Test
    public void AnAssignmentIsWrittenAsWhoFightsWhom() throws Exception {
        openBlank();
        Client client = new Client(game());
        //To site 2, where the author has the Shadow play a runner, and on to the assignment
        for (int i = 0; i < 10 && state().getJSONObject("waiting").getJSONObject("opponent") == null; i++) {
            client.poll();
            client.answer("");
        }
        authoring.answer(player, "play(runner1)", true);
        for (int i = 0; i < 10; i++) {
            client.poll();
            if (client.decision != null && client.decision.getAwaitingDecision().getDecisionType().name().equals("ASSIGN_MINIONS"))
                break;
            if (client.decision != null)
                client.answer("");
            else if (state().getJSONObject("waiting").getJSONObject("opponent") != null)
                authoring.answer(player, null, false);
        }
        assertNotNull(client.decision);
        assertEquals("ASSIGN_MINIONS", client.decision.getAwaitingDecision().getDecisionType().name());
        Map<String, String[]> params = client.decision.getAwaitingDecision().getDecisionParameters();
        String frodo = params.get("freeCharacters")[0];
        String runner = params.get("minions")[0];
        //The game screen lists every character, with or without minions
        StringBuilder answer = new StringBuilder();
        for (String character : params.get("freeCharacters"))
            answer.append(answer.length() == 0 ? "" : ",").append(character).append(character.equals(frodo) ? " " + runner : "");
        client.answer(answer.toString());
        JSONObject state = state();
        JSONObject written = steps(state).getJSONObject(steps(state).size() - 1);
        assertEquals("assign(frodo: runner1)", written.getString("allow"));
    }

    @Test
    public void AStepIsKnownByItsNameHoweverTheScriptMoves() throws Exception {
        openBlank();
        Client client = new Client(game());
        authoring.stepHere(player);
        JSONObject state = state();
        String name = state.getJSONArray("uids").getString(0);

        //Its message goes in by name, and only what is said to have changed is changed
        JSONObject result = json(authoring.edit(player, "{\"steps\": [{\"op\": \"merge\", \"uid\": \"" + name
                + "\", \"set\": {\"messages\": [\"Play him.\"]}}]}", null));
        assertFalse(result.getBooleanValue("rebuilt"));
        assertTrue(result.getBooleanValue("events"));
        client.answer(client.decision.getAwaitingDecision().getDecisionParameters().get("actionId")[0]);
        assertEquals("play(aragorn)", steps(state()).getJSONObject(0).getString("allow"));

        //A page that has not heard of the answer changes the message again: the answer stays
        result = json(authoring.edit(player, "{\"steps\": [{\"op\": \"merge\", \"uid\": \"" + name
                + "\", \"set\": {\"messages\": [\"Play Aragorn.\"]}, \"unset\": [\"note\"]}]}", null));
        assertFalse(result.getBooleanValue("rebuilt"));
        JSONObject step = steps(result.getJSONObject("state")).getJSONObject(0);
        assertEquals("play(aragorn)", step.getString("allow"));
        assertEquals("Play Aragorn.", step.getJSONArray("messages").getString(0));

        //The name outlives a fresh game, and a step that has gone is said to have gone
        state = json(authoring.goTo(player, 1)).getJSONObject("state");
        assertEquals(name, state.getJSONArray("uids").getString(0));
        authoring.edit(player, "{\"steps\": [{\"op\": \"insert\", \"before\": \"" + name + "\", \"step\": {\"pause\": true, \"messages\": [\"Hello.\"]}}]}", 1);
        state = state();
        assertEquals(name, state.getJSONArray("uids").getString(1));
        authoring.edit(player, "{\"steps\": [{\"op\": \"delete\", \"uid\": \"" + name + "\"}]}", 1);
        try {
            authoring.edit(player, "{\"steps\": [{\"op\": \"merge\", \"uid\": \"" + name + "\", \"set\": {\"note\": \"too late\"}}]}", null);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("no longer in the script"));
        }
    }

    @Test
    public void AStepWaitingForItsAnswerStillWaitsInAFreshGame() throws Exception {
        openBlank();
        new Client(game());
        authoring.stepHere(player);
        //(a change that needs a fresh game)
        JSONObject result = json(authoring.apply(player, "move(boromir, freeps.hand)", true));
        assertTrue(result.getBooleanValue("rebuilt"));
        assertNotNull("The step has no answer, and the script stops at it", result.getJSONObject("state").getJSONObject("position"));
        Client fresh = new Client(game());
        assertEquals(1, fresh.lastStep().getIntValue("step"));
        fresh.answer("");
        assertEquals("pass", steps(state()).getJSONObject(0).getString("allow"));
    }

    @Test
    public void AnOpponentThatWouldFailIsNotLeftHanging() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/strictopp.hjson").toPath(), ScenarioTesting.scenario("""
                	setup: {
                		shadow: {
                			hand: runner1
                		}
                	}
                """).replace("id: test", "id: strictopp"), StandardCharsets.UTF_8);
        authoring.open(player, "strictopp", true);
        authoring.settings(player, null, false, null);
        Client client = new Client(game());
        for (int i = 0; i < 6; i++) {
            client.poll();
            if (client.decision != null)
                client.answer("");
            Thread.sleep(150);
        }
        //The Shadow had a runner it could play, nobody to say so, and a fallback of 'fail': it passed, and said why
        JSONObject state = state();
        assertFalse(game().isFinished());
        assertNull(state.getJSONObject("waiting").getJSONObject("opponent"));
        assertTrue(state.getJSONArray("problems").toString(), state.getJSONArray("problems").toString().contains("fallback is 'fail'"));
    }

    @Test
    public void ANewDraftIsNotSavedOverAnotherScenario() throws Exception {
        openBlank();
        authoring.create(player, "brand-new", "Brand New");
        try {
            authoring.save(player, "tutorials/blank.hjson", true, true);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("already a scenario"));
        }
        for (String bad : List.of("../escape.hjson", "/etc/passwd.hjson", "a/../../b.hjson", ".hidden.hjson", "x.txt", "a\\b.hjson")) {
            try {
                authoring.save(player, bad, true, true);
                fail("Should not have been saved to " + bad);
            } catch (ScenarioAuthoring.AuthoringException expected) {
                //(as it should be)
            }
        }
        //Whatever folders it is given, they are folders among the tutorials: it is one
        JSONObject saved = json(authoring.save(player, "my drafts/brand new v1.2.hjson", true, true)).getJSONObject("saved");
        assertEquals("tutorials/my drafts/brand new v1.2.hjson", saved.getString("path"));
        assertTrue(new File(canon, "tutorials/my drafts/brand new v1.2.hjson").isFile());
    }

    @Test
    public void AScenarioIsFiledUnderItsKind() throws Exception {
        assertEquals("puzzles/a/b.hjson", ScenarioAuthoring.filed("a/b.hjson", ScenarioDefinition.Kind.PUZZLE));
        assertEquals("puzzles/a/b.hjson", ScenarioAuthoring.filed("tutorials/a/b.hjson", ScenarioDefinition.Kind.PUZZLE));
        assertEquals("sandboxes/b.hjson", ScenarioAuthoring.filed("Puzzles/b.hjson", ScenarioDefinition.Kind.SANDBOX));
        assertEquals("tutorials/tutorials.hjson", ScenarioAuthoring.filed("tutorials.hjson", ScenarioDefinition.Kind.TUTORIAL));

        //Its kind is changed: saving moves its file to where that kind is kept
        openBlank();
        JSONObject state = json(authoring.edit(player, "{top: {kind: \"puzzle\"}}", null)).getJSONObject("state");
        assertEquals("tutorials/blank.hjson", state.getString("file"));
        assertEquals("puzzles", state.getString("kindFolder"));
        assertEquals("puzzles/blank.hjson", state.getString("fileOnSave"));
        JSONObject result = json(authoring.save(player, null, false, true));
        assertEquals("puzzles/blank.hjson", result.getJSONObject("saved").getString("path"));
        assertEquals("tutorials/blank.hjson", result.getJSONObject("saved").getString("movedFrom"));
        assertEquals("puzzles/blank.hjson", result.getJSONObject("state").getString("file"));
        assertFalse(new File(canon, "tutorials/blank.hjson").exists());
        assertTrue(new File(canon, "puzzles/blank.hjson").isFile());
        assertEquals("puzzles/blank.hjson", library.get("blank").path());
        assertEquals(ScenarioDefinition.Kind.PUZZLE, library.get("blank").def().kind);
        //(and saving again leaves it where it is)
        result = json(authoring.save(player, null, false, true));
        assertNull(result.getJSONObject("saved").get("movedFrom"));

        //A file that was put somewhere else by hand is filed as it is saved; not over another, though
        File loose = new File(users, "users/" + AUTHOR + "/loose.hjson");
        loose.getParentFile().mkdirs();
        Files.writeString(loose.toPath(), ScenarioTesting.scenario("").replace("id: test", "id: loose"), StandardCharsets.UTF_8);
        authoring.open(player, AUTHOR + "/loose", true);
        assertEquals("loose.hjson", state().getString("file"));
        File inTheWay = new File(users, "users/" + AUTHOR + "/tutorials/loose.hjson");
        inTheWay.getParentFile().mkdirs();
        Files.writeString(inTheWay.toPath(), "not a scenario", StandardCharsets.UTF_8);
        try {
            authoring.save(player, null, false, true);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("already a scenario"));
        }
        assertTrue(inTheWay.delete());
        result = json(authoring.save(player, null, false, true));
        assertEquals("users/" + AUTHOR + "/tutorials/loose.hjson", result.getJSONObject("saved").getString("path"));
        assertFalse(loose.exists());
        assertTrue(inTheWay.isFile());
    }

    @Test
    public void AHandOrAPileIsPutInOrder() throws Exception {
        openBlank();
        new Client(game());
        JSONObject state = state();
        assertEquals(0, cardNamed(state, "runner1").getIntValue("at"));
        assertEquals(1, cardNamed(state, "runner2").getIntValue("at"));
        assertNull("Where a card on the table comes is not a matter of order", cardNamed(state, "frodo").get("at"));

        //Now: done at once, and one ordering however many times it is changed
        state = json(authoring.apply(player, "order(shadow.hand, runner2, runner1)", false)).getJSONObject("state");
        assertEquals(0, cardNamed(state, "runner2").getIntValue("at"));
        assertEquals(1, cardNamed(state, "runner1").getIntValue("at"));
        assertEquals(List.of("order(shadow.hand, runner2, runner1)"), state.getJSONArray("pending").toJavaList(String.class));
        state = json(authoring.apply(player, "order(shadow.hand, runner1)", false)).getJSONObject("state");
        assertEquals("The one named comes first, and the rest after it as they were", 0, cardNamed(state, "runner1").getIntValue("at"));
        assertEquals(List.of("order(shadow.hand, runner1)"), state.getJSONArray("pending").toJavaList(String.class));

        //From the start: the deck is what the setup places nowhere, and listing it is what orders it
        List<String> deck = new ArrayList<>(inZone(state, "freeps", "deck"));
        deck.sort(java.util.Comparator.comparingInt(alias -> cardNamed(state(), alias).getIntValue("at")));
        assertEquals("As they are declared", List.of("gandalf", "servant1", "servant2", "swordarm", "boromir"), deck);
        JSONObject result = json(authoring.apply(player, "order(freeps.deck, boromir, aragorn, gandalf)", true));
        assertTrue(result.getBooleanValue("rebuilt"));
        state = result.getJSONObject("state");
        assertEquals("Aragorn is in the hand, and stays there", "boromir, gandalf",
                state.getJSONObject("draft").getJSONObject("setup").getJSONObject("freeps").getString("deck"));
        assertEquals(0, cardNamed(state, "boromir").getIntValue("at"));
        assertEquals(1, cardNamed(state, "gandalf").getIntValue("at"));
        assertEquals(2, cardNamed(state, "servant1").getIntValue("at"));
        state = json(authoring.apply(player, "order(shadow.hand, runner2, runner1)", true)).getJSONObject("state");
        assertEquals("runner2, runner1", state.getJSONObject("draft").getJSONObject("setup").getJSONObject("shadow").getString("hand"));
        assertEquals(0, cardNamed(state, "runner2").getIntValue("at"));

        for (String bad : List.of("order(freeps.characters, frodo)", "order(freeps.hand, runner1)", "order(freeps.deck.top, boromir)",
                "order(freeps.hand)", "order(freeps.hand, aragorn, aragorn)")) {
            try {
                authoring.apply(player, bad, false);
                fail("Should not have been taken: " + bad);
            } catch (ScenarioAuthoring.AuthoringException expected) {
                //(as it should be)
            }
        }
        try {
            authoring.apply(player, "order(freeps.discard, aragorn)", true);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("None of those cards"));
        }
    }

    @Test
    public void ADraftWhoseGameIsOverCanBeGoneBackTo() throws Exception {
        openBlank();
        Client client = new Client(game());
        client.answer(client.decision.getAwaitingDecision().getDecisionParameters().get("actionId")[0]);
        client.answer("");
        JSONObject info = json(authoring.current(player));
        assertFalse(info.getBooleanValue("over"));
        assertEquals("blank", info.getString("id"));
        //Still going: nothing to do
        assertFalse(json(authoring.resume(player)).getBooleanValue("rebuilt"));

        game().concede(player);
        assertTrue(json(authoring.current(player)).getBooleanValue("over"));
        JSONObject result = json(authoring.resume(player));
        assertTrue(result.getBooleanValue("rebuilt"));
        assertEquals("...at the step the game had got to", 3, result.getJSONObject("state").getJSONObject("position").getIntValue("step"));
        assertEquals(2, steps(result.getJSONObject("state")).size());

        //A player's game of it says what it is, so that any page showing it can offer the way back
        authoring.test(player, 1);
        assertTrue(new Client(game()).info.getBooleanValue("draftTest"));
        assertTrue(json(authoring.current(player)).getBooleanValue("test"));
    }

    @Test
    public void TheClientSendsWhatItChanged() throws Exception {
        openBlank();
        Client client = new Client(game());
        client.answer(client.decision.getAwaitingDecision().getDecisionParameters().get("actionId")[0]);
        client.answer("");
        assertEquals(2, steps(state()).size());

        //Something about the scenario that has nothing to do with how its game goes
        JSONObject result = json(authoring.edit(player, "{\"top\": {\"title\": \"Renamed\", \"summary\": null}}", null));
        assertFalse(result.getBooleanValue("rebuilt"));
        assertEquals("Renamed", result.getJSONObject("state").getJSONObject("draft").getString("title"));

        //Something that has: the game is started again
        result = json(authoring.edit(player, "{\"top\": {\"coverage\": \"guided\"}}", null));
        assertTrue(result.getBooleanValue("rebuilt"));
        assertEquals("guided", result.getJSONObject("state").getJSONObject("draft").getString("coverage"));
        assertEquals("...at the same place", 3, result.getJSONObject("state").getJSONObject("position").getIntValue("step"));

        //Steps: put in, changed, moved, taken out
        result = json(authoring.edit(player, "{\"steps\": [{\"op\": \"insert\", \"index\": 0, \"step\": {\"pause\": true, \"messages\": [\"Hello.\"]}}]}", 1));
        JSONObject state = result.getJSONObject("state");
        assertEquals(3, steps(state).size());
        assertTrue(steps(state).getJSONObject(0).getBooleanValue("pause"));
        assertEquals(1, state.getJSONObject("position").getIntValue("step"));

        result = json(authoring.edit(player, "{\"steps\": [{\"op\": \"set\", \"index\": 0, \"step\": {\"pause\": true, \"messages\": [\"Hello again.\"]}}]}", null));
        assertFalse("What a step in hand says can be changed where it stands", result.getBooleanValue("rebuilt"));
        assertEquals("Hello again.", steps(result.getJSONObject("state")).getJSONObject(0).getJSONArray("messages").getString(0));

        result = json(authoring.edit(player, "{\"steps\": [{\"op\": \"delete\", \"index\": 2}, {\"op\": \"delete\", \"index\": 0}]}", 1));
        state = result.getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertEquals("play(aragorn)", steps(state).getJSONObject(0).getString("allow"));

        for (String bad : List.of("{\"steps\": [{\"op\": \"delete\", \"index\": 7}]}", "{\"steps\": [{\"op\": \"explode\", \"index\": 0}]}",
                "{\"top\": {\"steps\": []}}", "nonsense", "{\"top\": {\"cards\": {\"freeps\": {}, \"shadow\": {}}}}")) {
            try {
                authoring.edit(player, bad, null);
                fail("Should have been refused: " + bad);
            } catch (ScenarioAuthoring.AuthoringException expected) {
                assertEquals("A change that is refused leaves the draft alone", 1, steps(state()).size());
            }
        }
    }

    /** The scenarios that ship with the game are what the tool is for: each has to open, and play to any of its steps. */
    @Ignore("Tied to the shipped tutorials, which are changing too much to test against yet")
    @Test
    public void TheFirstTutorialOpensAndPlaysToAnyStep() throws Exception {
        File shipped = new File("../gemp-lotr-cards/src/main/resources/scenarios");
        ScenarioLibrary real = new ScenarioLibrary(shipped, null, ScenarioTesting.library(), ScenarioAuthoringTest::format, null);
        ScenarioAuthoring tool = new ScenarioAuthoring(real, ScenarioTesting.library(), ScenarioAuthoringTest::format, null, (who, live) -> {
            LotroGameMediator mediator = new LotroGameMediator(String.valueOf(games.size() + 1), live, LotroServer.AUTHORING_TIMER, false);
            mediator.startGame();
            games.add(mediator);
            return mediator;
        });
        JSONObject state = json(tool.open(player, "basics-01-into-the-wild", true)).getJSONObject("state");
        int count = steps(state).size();
        assertTrue(count > 40);
        assertEquals(1, state.getJSONObject("position").getIntValue("step"));
        assertEquals("tutorials/basics-01-into-the-wild.hjson", state.getString("path"));
        assertTrue(state.getBooleanValue("canonical"));
        assertFalse(state.getBooleanValue("dirty"));
        assertEquals(List.of("plan", "help"), state.getJSONArray("flags").toList(String.class));

        for (List<String> way : List.of(List.<String>of(), List.of("help"))) {
            tool.settings(player, null, null, way);
            for (int target = 2; target <= count; target += 7) {
                state = json(tool.goTo(player, target)).getJSONObject("state");
                assertNull("Going to step " + target + " by way of " + way + ": " + state.getString("notice"), state.getString("notice"));
                int reached = state.getJSONObject("position").getIntValue("step");
                assertTrue("Asked for step " + target + ", got to " + reached, reached >= target);
                assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));
                assertEquals("Playing the script writes nothing into it", count, steps(state).size());
                assertFalse(state.getBooleanValue("dirty"));
                assertEquals(List.of(), state.getJSONArray("problems").toList(String.class));
            }
        }

        //What comes back out is the file the library read
        ScenarioDefinition written = ScenarioParser.parse(tool.source(player));
        assertEquals(count, written.steps.size());
    }

    /** The first tutorial, opened for writing, with the rule that switches Gandalf's text off taken out. */
    private ScenarioAuthoring tutorialWithGandalfsTextOn() throws Exception {
        File shipped = new File("../gemp-lotr-cards/src/main/resources/scenarios");
        ScenarioLibrary real = new ScenarioLibrary(shipped, null, ScenarioTesting.library(), ScenarioAuthoringTest::format, null);
        ScenarioAuthoring tool = new ScenarioAuthoring(real, ScenarioTesting.library(), ScenarioAuthoringTest::format, null, (who, live) -> {
            LotroGameMediator mediator = new LotroGameMediator(String.valueOf(games.size() + 1), live, LotroServer.AUTHORING_TIMER, false);
            mediator.startGame();
            games.add(mediator);
            return mediator;
        });
        JSONObject state = json(tool.open(player, "basics-01-into-the-wild", true)).getJSONObject("state");
        JSONObject rules = state.getJSONObject("draft").getJSONObject("rules");
        JSONArray kept = new JSONArray();
        for (Object rule : rules.getJSONArray("freeps"))
            if (!rule.toString().contains("Gandalf"))
                kept.add(rule);
        assertEquals(rules.getJSONArray("freeps").size() - 1, kept.size());
        rules.put("freeps", kept);
        tool.edit(player, new JSONObject(Map.of("top", new JSONObject(Map.of("rules", rules)))).toJSONString(), null);
        return tool;
    }

    private static int stepWaitingFor(JSONObject state, String when) {
        JSONArray steps = steps(state);
        for (int i = 0; i < steps.size(); i++)
            if (when.equals(steps.getJSONObject(i).getString("when")))
                return i;
        fail("No step waits for " + when);
        return -1;
    }

    /**
     * A card's text is switched on, and at the start of turn 3 its trigger is offered: a decision that arrives while
     * the step before (sanctuary healing, which repeats) is still under way, and that it neither applies to nor ends
     * at.  One request writes a step for it, in the right place, and has the repeating step give way.
     */
    @Ignore("Tied to the shipped tutorials, which are changing too much to test against yet")
    @Test
    public void AStepIsAddedForADecisionThatArrivesWhileARepeatingStepIsUnderWay() throws Exception {
        ScenarioAuthoring tool = tutorialWithGandalfsTextOn();
        JSONObject state = json(tool.goTo(player, 99)).getJSONObject("state");
        int count = steps(state).size();
        int healing = stepWaitingFor(state, "freeps, cards, text(Sanctuary healing)");
        assertEquals("freeps, action, phase(fellowship)", steps(state).getJSONObject(healing).getString("until"));

        //The script stopped at the trigger, and the state says what a step for it would take
        assertEquals("uncovered", state.getString("stopped"));
        assertTrue("The game is at the step that repeats, between its decisions", state.getJSONObject("position").getBooleanValue("underWay"));
        assertEquals(healing + 1, state.getJSONObject("position").getIntValue("step"));
        JSONObject uncovered = state.getJSONObject("uncovered");
        assertNotNull(uncovered);
        assertEquals("human", uncovered.getString("seat"));
        assertEquals(healing + 1, uncovered.getIntValue("repeating"));
        assertEquals("It goes in after the step that repeats", healing + 1, uncovered.getIntValue("after"));
        assertEquals("freeps, action", uncovered.getString("becomes"));
        assertEquals("freeps, action, phase(between_turns), turn(3), text(Optional responses)", uncovered.getString("when"));
        assertNull(uncovered.getString("problem"));

        state = json(tool.stepHere(player)).getJSONObject("state");
        assertEquals(count + 1, steps(state).size());
        JSONObject added = steps(state).getJSONObject(healing + 1);
        assertEquals("freeps, action, phase(between_turns), turn(3), text(Optional responses)", added.getString("when"));
        assertEquals("freeps, action", steps(state).getJSONObject(healing).getString("until"));
        assertEquals(healing + 2, state.getJSONObject("position").getIntValue("step"));
        assertTrue("The decision that was waiting is the new step's", state.getJSONObject("position").getBooleanValue("inHand"));
        assertNull(state.getJSONObject("uncovered"));
        assertEquals(List.of(state.getJSONArray("uids").getString(healing + 1)), state.getJSONArray("awaiting").toList(String.class));
        assertTrue(state.getString("notice"), state.getString("notice").contains("is now \"freeps, action\""));
        assertNull("Where the script stopped is no longer news", state.getString("stopped"));

        //What is done at it is what it allows
        Client client = new Client(game());
        client.answer(client.decision.getAwaitingDecision().getDecisionParameters().get("actionId")[0]);
        state = json(tool.state(player));
        assertEquals("use(gandalf)", steps(state).getJSONObject(healing + 1).getString("allow"));
        assertEquals(List.of(), state.getJSONArray("awaiting").toList(String.class));

        //And the script plays itself through the trigger from now on
        state = json(tool.goTo(player, healing + 5)).getJSONObject("state");
        assertNull(state.getString("notice"), state.getString("notice"));
        assertNull(state.getString("stopped"));
        assertTrue(state.getJSONObject("position").getIntValue("step") >= healing + 5);
        assertEquals("matched", state.getJSONArray("statuses").getString(healing));
        assertEquals("matched", state.getJSONArray("statuses").getString(healing + 1));
    }

    /** The same, with nothing asked for: the author just answers the decision in the game. */
    @Ignore("Tied to the shipped tutorials, which are changing too much to test against yet")
    @Test
    public void AnsweringItInTheGameWritesTheSameStep() throws Exception {
        ScenarioAuthoring tool = tutorialWithGandalfsTextOn();
        JSONObject state = json(tool.goTo(player, 99)).getJSONObject("state");
        int count = steps(state).size();
        int healing = stepWaitingFor(state, "freeps, cards, text(Sanctuary healing)");

        Client client = new Client(game());
        client.answer("");
        state = json(tool.state(player));
        assertEquals(count + 1, steps(state).size());
        JSONObject added = steps(state).getJSONObject(healing + 1);
        assertEquals("freeps, action, phase(between_turns), turn(3), text(Optional responses)", added.getString("when"));
        assertEquals("Declining the trigger is passing", "pass", added.getString("allow"));
        assertEquals("freeps, action", steps(state).getJSONObject(healing).getString("until"));
        assertEquals("The game has gone on to the step after", healing + 3, state.getJSONObject("position").getIntValue("step"));

        state = json(tool.goTo(player, 99)).getJSONObject("state");
        assertNull("The whole script plays: " + state.getString("notice"), state.getString("notice"));
        assertTrue(json(tool.check(player)).getBooleanValue("passed"));
    }

    /** A step put in by hand says nothing yet: it takes its answer from what is done when the game comes to it. */
    @Test
    public void AStepPutInByHandTakesItsAnswerFromWhatIsDone() throws Exception {
        openBlank();
        new Client(game());
        JSONObject insert = new JSONObject(Map.of("op", "insert", "awaiting", true, "step", new JSONObject(Map.of("when", "freeps, action"))));
        JSONObject state = json(authoring.edit(player, new JSONObject(Map.of("steps", List.of(insert))).toJSONString(), 1)).getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertEquals(state.getJSONArray("uids").toList(String.class), state.getJSONArray("awaiting").toList(String.class));
        assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));

        Client client = new Client(game());
        client.answer(client.decision.getAwaitingDecision().getDecisionParameters().get("actionId")[0]);
        state = state();
        assertEquals("play(aragorn)", steps(state).getJSONObject(0).getString("allow"));
        assertEquals(List.of(), state.getJSONArray("awaiting").toList(String.class));
    }

    /** A step put in by hand for a decision, whose 'when' does not fit it, is made to fit: a second is not written. */
    @Test
    public void AnEmptyStepThatDoesNotFitIsMadeToFit() throws Exception {
        openBlank();
        new Client(game());
        JSONObject insert = new JSONObject(Map.of("op", "insert", "awaiting", true, "step", new JSONObject(Map.of("when", "freeps, cards"))));
        JSONObject state = json(authoring.edit(player, new JSONObject(Map.of("steps", List.of(insert))).toJSONString(), 1)).getJSONObject("state");
        assertFalse("It is the wrong kind of decision for the step", state.getJSONObject("position").getBooleanValue("inHand"));
        assertEquals(1, state.getJSONObject("uncovered").getIntValue("reuses"));

        new Client(game());
        state = json(authoring.stepHere(player)).getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertEquals("freeps, action, phase(fellowship), turn(1)", steps(state).getJSONObject(0).getString("when"));
        assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));
    }

    /**
     * A scenario's first step can wait for the opening: the board as it is laid out, before the first turn.  It is
     * read with nothing drawn; what it does, it does once it has been read; and the first turn's decision is worked
     * out after that, so that what was drawn can be played.
     */
    @Test
    public void TheOpeningIsReadBeforeTheFirstTurnAndBeforeItsOwnChanges() throws Exception {
        openBlank();
        new Client(game());
        JSONObject opening = new JSONObject(Map.of("when", "opening", "do", "draw(freeps, 2), twilight(3)",
                "messages", List.of(new JSONObject(Map.of("text", "Before anything is drawn.")))));
        JSONObject insert = new JSONObject(Map.of("op", "insert", "index", 0, "step", opening));
        JSONObject state = json(authoring.edit(player, new JSONObject(Map.of("steps", List.of(insert))).toJSONString(), 1)).getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertEquals("opening", steps(state).getJSONObject(0).getString("when"));
        assertTrue("The opening is in hand", state.getJSONObject("position").getBooleanValue("inHand"));
        assertEquals(1, state.getJSONObject("position").getIntValue("step"));
        assertEquals("Nothing has been drawn while it is being read", List.of("aragorn"), inZone(state, "freeps", "hand"));
        assertNull("The person is asked nothing yet", state.getJSONObject("waiting").getJSONObject("human"));
        assertNull(state.getJSONObject("uncovered"));

        Client client = new Client(game());
        assertNull(client.decision);
        assertEquals(1, client.lastStep().getIntValue("step"));
        assertTrue("It is read, and the game waits to be told it has been", client.lastStep().getBooleanValue("acknowledge"));
        game().scenarioAcknowledged(player, 1);
        for (int i = 0; i < 100 && client.decision == null; i++) {
            Thread.sleep(50);
            client.poll();
        }
        assertNotNull("The first turn has begun", client.decision);
        state = state();
        assertEquals("The top two of the deck, in the order the scenario declares them",
                List.of("aragorn", "gandalf", "servant1"), inZone(state, "freeps", "hand"));
        assertEquals(1, steps(state).size());
        assertEquals("matched", state.getJSONArray("statuses").getString(0));
        String offered = state.getJSONObject("waiting").getJSONObject("human").getJSONArray("options").toString();
        assertTrue("What was drawn is in the first decision: " + offered, offered.contains("play(gandalf)"));
        assertEquals("(the first turn starts the pool at nothing, whatever was put in it before)", 0, state.getJSONObject("board").getIntValue("twilight"));

        //The script plays itself through it, and the file says it as it was written
        state = json(authoring.goTo(player, 5)).getJSONObject("state");
        assertNull(state.getString("notice"), state.getString("notice"));
        assertEquals(List.of("aragorn", "gandalf", "servant1"), inZone(state, "freeps", "hand"));
        assertTrue(authoring.source(player).contains("when: opening"));
        assertTrue(authoring.source(player).contains("do: draw(freeps, 2), twilight(3)"));

        //Only the first step can wait for it
        JSONObject second = new JSONObject(Map.of("op", "insert", "step", new JSONObject(Map.of("when", "opening", "do", "draw(1)"))));
        try {
            authoring.edit(player, new JSONObject(Map.of("steps", List.of(second))).toJSONString(), null);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("only the first step"));
        }
    }

    @Test
    public void ADraftIsSavedAsAnotherScenarioOrItsFileMoved() throws Exception {
        openBlank();
        assertEquals("tutorials/blank.hjson", state().getString("file"));

        //Moved: the same scenario, somewhere else
        JSONObject result = json(authoring.rename(player, "tutorials/first steps/blank-moved.hjson", true, true));
        assertEquals("tutorials/first steps/blank-moved.hjson", result.getString("renamed"));
        assertEquals("tutorials/first steps/blank-moved.hjson", result.getJSONObject("state").getString("path"));
        assertFalse(new File(canon, "tutorials/blank.hjson").exists());
        assertTrue(new File(canon, "tutorials/first steps/blank-moved.hjson").isFile());
        assertEquals("blank", result.getJSONObject("state").getJSONObject("draft").getString("id"));
        assertNotNull("The library knows it where it now is", library.get("blank"));
        assertEquals("tutorials/first steps/blank-moved.hjson", library.get("blank").path());

        //To the author's own folder
        result = json(authoring.rename(player, "blank.hjson", false, true));
        assertEquals("users/" + AUTHOR + "/tutorials/blank.hjson", result.getString("renamed"));
        assertTrue(new File(users, "users/" + AUTHOR + "/tutorials/blank.hjson").isFile());
        assertFalse(new File(canon, "tutorials/first steps/blank-moved.hjson").exists());
        authoring.rename(player, "tutorials/blank.hjson", true, true);

        //Saved as another: a file and an id of its own, and the first one left as it was
        Client client = new Client(game());
        client.answer("");
        result = json(authoring.saveAs(player, "tutorials/Blank Two.hjson", true, true));
        assertEquals("tutorials/Blank Two.hjson", result.getJSONObject("saved").getString("path"));
        assertEquals("blank-two", result.getJSONObject("saved").getString("id"));
        assertEquals("blank-two", result.getJSONObject("state").getJSONObject("draft").getString("id"));
        assertFalse(result.getJSONObject("state").getBooleanValue("dirty"));
        assertEquals(1, library.get("blank-two").def().steps.size());
        assertEquals("The one it was copied from has not been written to", 0, library.get("blank").def().steps.size());
        assertEquals(List.of(), library.get("blank-two").errors());
        assertEquals(List.of(), library.get("blank").errors());

        //Not over another scenario, not under another's name, not somewhere a scenario cannot be
        for (String bad : List.of("tutorials/blank.hjson", "elsewhere/blank.hjson", "../out.hjson", "x".repeat(130) + ".hjson", "tutorials/!.hjson")) {
            try {
                authoring.saveAs(player, bad, true, true);
                fail("Should not have been saved as " + bad);
            } catch (ScenarioAuthoring.AuthoringException expected) {
                //(as it should be)
            }
        }
        try {
            authoring.rename(player, "tutorials/blank.hjson", true, true);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("already a scenario"));
        }
    }

    /** The parts of the file are given to the panel as the file has them, and taken back the same way. */
    @Test
    public void ThePartsOfTheFileAreReadAndWrittenAsTheFileHasThem() throws Exception {
        openBlank();
        JSONObject parts = state().getJSONObject("parts");
        String cards = parts.getString("cards");
        assertTrue(cards, cards.startsWith("{\n\tfreeps: {\n\t\t# Frodo, Resolute Hobbit\n\t\tfrodo: 10_121\n"));
        assertEquals("{\n\tfreeps: {\n\t\thand: aragorn\n\t}\n\tshadow: {\n\t\thand: runner1, runner2\n\t}\n}", parts.getString("setup"));
        assertFalse(parts.containsKey("steps"));

        //As it is in the file, name and comments and all
        String pasted = "# what each seat starts with\nsetup: {\n\tfreeps: {\n\t\thand: aragorn, boromir\n\t}\n}\n";
        JSONObject state = json(authoring.edit(player, new JSONObject(Map.of("topText", new JSONObject(Map.of("setup", pasted)))).toJSONString(), null))
                .getJSONObject("state");
        assertEquals("aragorn, boromir", state.getJSONObject("draft").getJSONObject("setup").getJSONObject("freeps").getString("hand"));
        assertNull(state.getJSONObject("draft").getJSONObject("setup").getJSONObject("shadow"));
        //Or only what the name is given; and JSON is HJSON too
        state = json(authoring.edit(player, new JSONObject(Map.of("topText", new JSONObject(Map.of("setup", "{ \"freeps\": { \"hand\": \"boromir\" } }")))).toJSONString(), null))
                .getJSONObject("state");
        assertEquals(List.of("boromir"), inZone(state, "freeps", "hand"));
        //Emptied, the part is gone
        state = json(authoring.edit(player, new JSONObject(Map.of("topText", new JSONObject(Map.of("setup", "  ")))).toJSONString(), null)).getJSONObject("state");
        assertNull(state.getJSONObject("draft").getJSONObject("setup"));
        try {
            authoring.edit(player, new JSONObject(Map.of("topText", new JSONObject(Map.of("setup", "{ freeps: { hand: [ }")))).toJSONString(), null);
            fail();
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("setup: that is not HJSON"));
        }
    }

    /** A draft with nothing unsaved in it, whose page has gone, is not something the Game Hall asks about. */
    @Test
    public void ASavedDraftWhosePageIsClosedIsDoneWith() throws Exception {
        openBlank();
        assertFalse(authoring.currentForHall(player) == null);
        authoring.left(player);
        //(a page that was only reloaded is heard from again)
        authoring.state(player);
        assertNotNull(authoring.currentForHall(player));

        new Client(game()).answer("");
        authoring.left(player);
        JSONObject current = json(authoring.currentForHall(player));
        assertTrue("With something unsaved in it, it is still there to go back to", current.getBooleanValue("dirty"));

        authoring.save(player, null, false, true);
        authoring.left(player);
        assertNull(authoring.currentForHall(player));
        assertFalse(state().getBooleanValue("session"));
    }

    @Test
    public void AFileWithMistakesIsOpenedWithoutThemAndTheAuthorIsTold() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        File file = new File(canon, "tutorials/mended.hjson");
        Files.writeString(file.toPath(), ScenarioTesting.scenario("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			hand: aragorn, the
                		}
                	}
                	steps: [
                		{
                			pause: true
                			when: freeps, action, phase(fellowship)
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			do: exert(the)
                			allow: play(aragorn)
                		}
                	]
                """).replace("id: test", "id: mended"), StandardCharsets.UTF_8);
        assertFalse(library.get("mended").playable());

        JSONObject state = json(authoring.open(player, "mended", true)).getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertNull(steps(state).getJSONObject(0).get("do"));
        assertTrue("The draft is not what the file says, so there is something to save", state.getBooleanValue("dirty"));
        JSONArray leftOut = state.getJSONArray("leftOut");
        assertEquals(3, leftOut.size());
        assertEquals("setup.freeps.hand", leftOut.getJSONObject(0).getString("where"));
        assertEquals("aragorn, the", leftOut.getJSONObject(0).getString("was"));
        assertNull(leftOut.getJSONObject(0).get("uid"));
        assertEquals("step 1", leftOut.getJSONObject(1).getString("where"));
        assertNull("The whole step went", leftOut.getJSONObject(1).get("uid"));
        assertEquals("step 2.do", leftOut.getJSONObject(2).getString("where"));
        assertEquals("exert(the)", leftOut.getJSONObject(2).getString("was"));
        assertEquals(2, leftOut.getJSONObject(2).getIntValue("step"));
        assertEquals("It is the draft's first step now", state.getJSONArray("uids").getString(0), leftOut.getJSONObject(2).getString("uid"));

        //It plays like any other draft
        Client client = new Client(game());
        client.poll();
        assertNotNull(client.decision);

        //Saving it writes the file without its mistakes; what they were is still there to be read until it is put away
        authoring.save(player, null, false, true);
        assertTrue(library.get("mended").errors().toString(), library.get("mended").playable());
        assertTrue(library.get("mended").leftOut().isEmpty());
        assertFalse(Files.readString(file.toPath(), StandardCharsets.UTF_8).contains("the)"));
        assertEquals(3, state().getJSONArray("leftOut").size());
        authoring.settings(player, null, null, null, true);
        assertEquals(0, state().getJSONArray("leftOut").size());

        //A file that cannot be read even so is not opened, and the author is told what is wrong with it
        Files.writeString(new File(canon, "tutorials/hopeless.hjson").toPath(),
                ScenarioTesting.scenario("").replace("id: test", "id: hopeless").replace("format: scenario", "format: nonesuch"), StandardCharsets.UTF_8);
        try {
            authoring.open(player, "hopeless", true);
            fail("It has no format to be played in");
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("format: there is no format called 'nonesuch'"));
        }
    }

    @Test
    public void PicturesThatAreNotCardsAreListed() throws Exception {
        File images = temp.newFolder("images");
        Files.writeString(new File(images, "placeholder.png").toPath(), "x");
        Files.writeString(new File(images, "A Map.jpg").toPath(), "x");
        Files.writeString(new File(images, "notes.txt").toPath(), "x");
        ScenarioAuthoring tool = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioAuthoringTest::format, null, null, images);
        assertEquals("[{\"name\":\"A Map.jpg\",\"url\":\"/gemp-lotr/images/scenarios/A%20Map.jpg\"},"
                + "{\"name\":\"placeholder.png\",\"url\":\"/gemp-lotr/images/scenarios/placeholder.png\"}]", JSON.toJSONString(tool.images()));
        assertEquals(List.of(), authoring.images());
    }

    @Test
    public void CardsAreFoundByName() {
        List<Map<String, Object>> found = authoring.findCards("aragorn heir", false);
        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(card -> card.get("id").equals("4_109")));
        found = authoring.findCards("rivendell", true);
        assertTrue(found.stream().allMatch(card -> card.get("type").equals("site")));
        assertFalse(found.isEmpty());
        assertTrue(authoring.findCards("a", false).isEmpty());
    }


    private static JSONObject edit(Object... ops) {
        return new JSONObject(Map.of("steps", List.of(ops)));
    }

    private static JSONArray under(JSONObject state, int index) {
        JSONArray under = steps(state).getJSONObject(index).getJSONArray("anyOrder");
        return under == null ? new JSONArray() : under;
    }

    /**
     * A block is written the way any step is: by playing.  With the script at a block, what is done where none of
     * its steps applies becomes one more of its steps, until the decision the block ends at arrives.
     */
    @Test
    public void ABlockIsWrittenByPlayingThroughIt() throws Exception {
        openBlank();
        JSONObject block = new JSONObject(Map.of("when", "turn(1)", "until", "phase(regroup)", "anyOrder", new JSONArray()));
        JSONObject state = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "insert", "step", block))).toJSONString(), 1)).getJSONObject("state");
        assertEquals(1, steps(state).size());
        assertEquals(1, state.getJSONObject("position").getIntValue("step"));
        JSONObject uncovered = state.getJSONObject("uncovered");
        assertNotNull("The decision in front of the author is one the block is for, and none of its steps covers", uncovered);
        assertEquals(1, uncovered.getIntValue("block"));
        assertEquals(1, uncovered.getIntValue("sub"));
        assertEquals("What the block says for all its steps is not said again", "freeps, action, phase(fellowship)", uncovered.getString("when"));

        //Answered in the game, it is a step of the block, done once
        Client client = new Client(game());
        client.answer(client.decision.getAwaitingDecision().getDecisionParameters().get("actionId")[0]);
        state = state();
        assertEquals("The script has one step: the block", 1, steps(state).size());
        assertEquals(1, under(state, 0).size());
        JSONObject first = under(state, 0).getJSONObject(0);
        assertEquals("freeps, action, phase(fellowship)", first.getString("when"));
        assertEquals("play(aragorn)", first.getString("allow"));
        assertTrue(first.getBooleanValue("once"));
        assertEquals(1, state.getJSONArray("kids").getJSONArray(0).size());
        assertEquals("matched", state.getJSONArray("substatuses").getJSONArray(0).getString(0));
        assertTrue("The game is in the block, between its decisions", state.getJSONObject("position").getBooleanValue("underWay"));

        //A step can be written for the next decision before it is answered, to hang a message on
        state = json(authoring.stepHere(player)).getJSONObject("state");
        assertEquals(2, under(state, 0).size());
        assertEquals(2, state.getJSONObject("position").getIntValue("sub"));
        assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));
        String second = state.getJSONArray("kids").getJSONArray(0).getString(1);
        assertEquals(List.of(second), state.getJSONArray("awaiting").toList(String.class));
        String gameBefore = state.getString("gameId");
        JSONObject merged = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "merge", "uid", second,
                "set", new JSONObject(Map.of("messages", List.of("Now pass.")))))).toJSONString(), null));
        assertFalse("What a block's step in hand says is changed where the game stands", merged.getBooleanValue("rebuilt"));
        assertEquals(gameBefore, merged.getString("gameId"));
        client.poll();
        assertEquals(1, client.lastStep().getIntValue("step"));
        assertEquals(2, client.lastStep().getIntValue("sub"));
        client.answer("");
        state = state();
        assertEquals("pass", under(state, 0).getJSONObject(1).getString("allow"));
        assertEquals(List.of(), state.getJSONArray("awaiting").toList(String.class));

        //On through the turn.  What the author has the opponent do goes into the block as well.
        boolean played = false;
        for (int i = 0; i < 40 && steps(state).size() == 1; i++) {
            JSONObject held = state.getJSONObject("waiting").getJSONObject("opponent");
            if (held != null && held.getBooleanValue("held")) {
                authoring.answer(player, played ? "pass" : "play(runner1)", true);
                played = true;
            } else {
                client.poll();
                client.answer("");
            }
            state = state();
        }
        assertEquals("The regroup phase is where the block ends: what is done there is a step of the script", 2, steps(state).size());
        assertTrue(steps(state).getJSONObject(1).getString("when"), steps(state).getJSONObject(1).getString("when").startsWith("freeps, action, phase(regroup), turn(1)"));
        JSONArray written = under(state, 0);
        assertTrue("Several decisions were written into the block: " + written, written.size() > 3);
        boolean runner = false;
        for (Object step : written) {
            assertTrue(((JSONObject) step).getBooleanValue("once"));
            runner |= "play(runner1)".equals(((JSONObject) step).getString("answer"));
        }
        assertTrue("The opponent's play is one of the block's steps: " + written, runner);

        //The script plays itself through the block, and to any of the steps in it
        state = json(authoring.goTo(player, 99)).getJSONObject("state");
        assertNull(state.getString("notice"), state.getString("notice"));
        assertEquals(written.size(), under(state, 0).size());
        for (Object status : state.getJSONArray("substatuses").getJSONArray(0))
            assertEquals("matched", status);
        state = json(authoring.goTo(player, 1, 2)).getJSONObject("state");
        assertNull(state.getString("notice"), state.getString("notice"));
        assertEquals(1, state.getJSONObject("position").getIntValue("step"));
        assertEquals(2, state.getJSONObject("position").getIntValue("sub"));
        assertTrue(state.getJSONObject("position").getBooleanValue("inHand"));
        assertEquals("The steps keep their names from one game to the next", second, state.getJSONArray("kids").getJSONArray(0).getString(1));
    }

    private void openTwoSkirmishes() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/two-skirmishes.hjson").toPath(),
                Files.readString(new File("src/test/resources/scenarios/two-skirmishes.hjson").toPath(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        authoring.open(player, "two-skirmishes", true);
    }

    /** The steps in a block are changed, put in, moved and taken out by name, as the steps of the script are. */
    @Test
    public void TheStepsInABlockAreChangedByName() throws Exception {
        openTwoSkirmishes();
        JSONObject state = state();
        String block = state.getJSONArray("uids").getString(0);
        String after = state.getJSONArray("uids").getString(1);
        List<String> kids = state.getJSONArray("kids").getJSONArray(0).toList(String.class);
        assertEquals(4, kids.size());
        assertEquals(List.of(), state.getJSONArray("kids").getJSONArray(1).toList(String.class));

        //One put in at the end, and one before another
        state = json(authoring.edit(player, edit(
                new JSONObject(Map.of("op", "insert", "into", block, "step", new JSONObject(Map.of("when", "shadow, action", "answer", "pass")))),
                new JSONObject(Map.of("op", "insert", "before", kids.get(0), "step", new JSONObject(Map.of("pause", true, "when", "freeps, assign", "messages", List.of("Read me."))))))
                .toJSONString(), 1)).getJSONObject("state");
        assertEquals(2, steps(state).size());
        assertEquals(6, under(state, 0).size());
        assertTrue(under(state, 0).getJSONObject(0).getBooleanValue("pause"));
        assertEquals("shadow, action", under(state, 0).getJSONObject(5).getString("when"));
        List<String> now = state.getJSONArray("kids").getJSONArray(0).toList(String.class);
        assertEquals("The ones that were there keep their names", kids, now.subList(1, 5));

        //Changed by its name, wherever it now comes
        state = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "merge", "uid", kids.get(2),
                "set", new JSONObject(Map.of("note", "The one about Aragorn")), "unset", List.of("id")))).toJSONString(), null)).getJSONObject("state");
        assertEquals("The one about Aragorn", under(state, 0).getJSONObject(3).getString("note"));
        assertNull(under(state, 0).getJSONObject(3).getString("id"));

        //Out of the block, where 'once' cannot be said; and back in, where it has to be
        state = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "move", "uid", kids.get(0), "before", after))).toJSONString(), 1)).getJSONObject("state");
        assertEquals(3, steps(state).size());
        assertEquals(5, under(state, 0).size());
        assertEquals("freeps, assign", steps(state).getJSONObject(1).getString("when"));
        assertFalse(steps(state).getJSONObject(1).containsKey("once"));
        assertEquals(kids.get(0), state.getJSONArray("uids").getString(1));
        state = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "move", "uid", kids.get(0), "into", block))).toJSONString(), 1)).getJSONObject("state");
        assertEquals(2, steps(state).size());
        assertEquals(6, under(state, 0).size());
        assertTrue(under(state, 0).getJSONObject(5).getBooleanValue("once"));
        assertEquals(kids.get(0), state.getJSONArray("kids").getJSONArray(0).getString(5));

        //Taken out
        state = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "delete", "uid", kids.get(0)))).toJSONString(), 1)).getJSONObject("state");
        assertEquals(5, under(state, 0).size());
        try {
            authoring.edit(player, edit(new JSONObject(Map.of("op", "merge", "uid", kids.get(0), "set", new JSONObject(Map.of("note", "gone"))))).toJSONString(), null);
            fail("That step is no longer there");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("no longer in the script"));
        }
        //A step of a block cannot say what only the block can
        try {
            authoring.edit(player, edit(new JSONObject(Map.of("op", "merge", "uid", kids.get(1), "set", new JSONObject(Map.of("until", "phase(regroup)"))))).toJSONString(), null);
            fail("A step in a block has no until");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("it has no 'until' or 'skipWhen' of its own"));
        }
    }

    /**
     * The parts of a step that can be written out in full are typed in as the file has them, in HJSON, with or
     * without their name in front; and are given back the same way.
     */
    @Test
    public void WhatAStepWaitsForIsTypedAsTheFileHasIt() throws Exception {
        openTwoSkirmishes();
        JSONObject state = state();
        String block = state.getJSONArray("uids").getString(0);
        String last = state.getJSONArray("uids").getString(1);
        assertTrue("Nothing is written out in full yet", state.getJSONObject("texts").isEmpty());

        String until = """
                until: {
                	# when the fighting is over
                	phase: regroup
                	require: {
                		type: canSpot
                		filter: alias(aragorn)
                	}
                }
                """;
        String passWhen = """
                [
                	{
                		type: canSpot
                		filter: alias(gandalf)
                	}
                ]""";
        state = json(authoring.edit(player, edit(
                new JSONObject(Map.of("op", "merge", "uid", block, "set", new JSONObject(Map.of("until", until)))),
                new JSONObject(Map.of("op", "merge", "uid", last, "set", new JSONObject(Map.of("allow", new JSONObject(Map.of("passWhen", passWhen)))))))
                .toJSONString(), 1)).getJSONObject("state");
        JSONObject read = steps(state).getJSONObject(0).getJSONObject("until");
        assertEquals("regroup", read.getString("phase"));
        assertEquals("alias(aragorn)", read.getJSONObject("require").getString("filter"));
        assertEquals("""
                {
                	phase: regroup
                	require: {
                		type: canSpot
                		filter: alias(aragorn)
                	}
                }""", state.getJSONObject("texts").getJSONObject(block).getString("until"));
        assertTrue(state.getJSONObject("texts").getJSONObject(last).getString("passWhen"),
                state.getJSONObject("texts").getJSONObject(last).getString("passWhen").contains("filter: alias(gandalf)"));

        //A line of terms is a line of terms, with its name in front or without
        state = json(authoring.edit(player, edit(new JSONObject(Map.of("op", "merge", "uid", block,
                "set", new JSONObject(Map.of("until", "until: freeps, action, phase(regroup)", "when", "turn(1)"))))).toJSONString(), 1)).getJSONObject("state");
        assertEquals("freeps, action, phase(regroup)", steps(state).getJSONObject(0).getString("until"));
        assertEquals("turn(1)", steps(state).getJSONObject(0).getString("when"));
        assertFalse(state.getJSONObject("texts").containsKey(block));

        try {
            authoring.edit(player, edit(new JSONObject(Map.of("op", "merge", "uid", block, "set", new JSONObject(Map.of("until", "{ phase: regroup"))))).toJSONString(), null);
            fail("That is not HJSON");
        } catch (ScenarioAuthoring.AuthoringException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith("until: that is not HJSON"));
        }
    }

    /** A card is given another name, and every part of the scenario that said the old one says the new. */
    @Test
    public void ACardIsRenamedWhereverTheScenarioNamesIt() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/named.hjson").toPath(), ScenarioTesting.scenario("""
                	opponent: {
                		fallback: pass
                		orders: [
                			{
                				when: shadow, cards, text(aragorn), offers(aragorn)
                				answer: choose(aragorn)
                			}
                		]
                	}
                	setup: {
                		freeps: {
                			hand: aragorn, swordarm
                			characters: gandalf
                		}
                		wounds: {
                			gandalf: 1
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship), offers(aragorn)
                			do: wound(gandalf, 1), move(servant1, freeps.hand)
                			messages: [
                				{
                					text: Play [[aragorn]], whose strength is {strength:aragorn}. Not [[Aragorn]] the name, nor aragorn the word.
                					anchor: card:aragorn:strength, zone:self.hand
                					highlight: card:aragorn, card:gandalf
                					advance: inspect(aragorn)
                				}
                				{
                					text: Then [[aragorn|the ranger]] is in play.
                					on: picked(aragorn)
                				}
                			]
                			allow: {
                				answers: play(aragorn), use(aragorn, text aragorn says)?
                				passWhen: {
                					type: canSpot
                					filter: alias(aragorn)
                				}
                				hints: {
                					aragorn: Yes, [[aragorn]].
                					swordarm: Not yet.
                				}
                			}
                			answer: play(aragorn)
                		}
                		{
                			when: {
                				seat: freeps
                				decision: action
                				text: Fellowship
                				require: {
                					type: canSpot
                					filter: alias(aragorn)
                				}
                			}
                			allow: play(swordarm)?, pass
                			answer: pass
                			end: complete
                		}
                	]
                """).replace("id: test", "id: named"), StandardCharsets.UTF_8);
        authoring.open(player, "named", true);
        JSONObject result = json(authoring.renameCard(player, "aragorn", "strider"));
        assertTrue("A fresh game of it, with the card under its new name", result.getBooleanValue("rebuilt"));
        JSONObject state = result.getJSONObject("state");
        JSONObject draft = state.getJSONObject("draft");
        assertTrue(draft.getJSONObject("cards").getJSONObject("freeps").containsKey("strider"));
        assertFalse(draft.getJSONObject("cards").getJSONObject("freeps").containsKey("aragorn"));
        assertEquals("It keeps its place among the cards", List.of("frodo", "strider", "gandalf"),
                new ArrayList<>(draft.getJSONObject("cards").getJSONObject("freeps").keySet()).subList(0, 3));
        assertEquals("strider, swordarm", draft.getJSONObject("setup").getJSONObject("freeps").getString("hand"));
        JSONObject order = draft.getJSONObject("opponent").getJSONArray("orders").getJSONObject(0);
        assertEquals("shadow, cards, text(aragorn), offers(strider)", order.getString("when"));
        assertEquals("choose(strider)", order.getString("answer"));

        JSONObject first = steps(state).getJSONObject(0);
        assertEquals("freeps, action, phase(fellowship), offers(strider)", first.getString("when"));
        assertEquals("Other cards, and places, are left alone", "wound(gandalf, 1), move(servant1, freeps.hand)", first.getString("do"));
        JSONObject message = first.getJSONArray("messages").getJSONObject(0);
        assertEquals("What a player reads is the same; what it is looked up by is the new name",
                "Play [[strider|aragorn]], whose strength is {strength:strider}. Not [[Aragorn]] the name, nor aragorn the word.", message.getString("text"));
        assertEquals("card:strider:strength, zone:self.hand", message.getString("anchor"));
        assertEquals("card:strider, card:gandalf", message.getString("highlight"));
        assertEquals("inspect(strider)", message.getString("advance"));
        assertEquals("Then [[strider|the ranger]] is in play.", first.getJSONArray("messages").getJSONObject(1).getString("text"));
        assertEquals("picked(strider)", first.getJSONArray("messages").getJSONObject(1).getString("on"));
        JSONObject allow = first.getJSONObject("allow");
        assertEquals("What is quoted from the game is not the card's name", "play(strider), use(strider, text aragorn says)?", allow.getString("answers"));
        assertEquals("alias(strider)", allow.getJSONArray("passWhen").getJSONObject(0).getString("filter"));
        assertEquals("Yes, [[strider|aragorn]].", allow.getJSONObject("hints").getString("strider"));
        assertEquals("play(strider)", first.getString("answer"));
        JSONObject when = steps(state).getJSONObject(1).getJSONObject("when");
        assertEquals("alias(strider)", when.getJSONObject("require").getString("filter"));

        //It still plays, and the board knows the card by its new name
        assertTrue(json(authoring.check(player)).getString("report"), json(authoring.check(player)).getBooleanValue("passed"));
        boolean found = false;
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            found |= ((JSONObject) card).getString("alias").equals("strider");
        assertTrue(found);

        for (String[] bad : new String[][] {{"strider", "gandalf"}, {"aragorn", "elessar"}, {"strider", "two words"}, {"strider", "9lives"}}) {
            try {
                authoring.renameCard(player, bad[0], bad[1]);
                fail("Should have been refused: " + bad[0] + " to " + bad[1]);
            } catch (ScenarioAuthoring.AuthoringException expected) {
                assertTrue(steps(state()).getJSONObject(0).getString("answer").equals("play(strider)"));
            }
        }
    }
}
