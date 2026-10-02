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
        canon = temp.newFolder("canon");
        users = temp.newFolder("scenarios-user");
        library = new ScenarioLibrary(canon, users, ScenarioTesting.library(), ScenarioAuthoringTest::format, null, 0, System::currentTimeMillis);
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

    private static int wounds(JSONObject state, String alias) {
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("alias").equals(alias))
                return ((JSONObject) card).getIntValue("wounds");
        throw new AssertionError("No card called " + alias);
    }

    @Test
    public void ANewScenarioIsSavedWhereTheLibraryFindsIt() throws Exception {
        JSONObject result = json(authoring.create(player, "my-first", "My First"));
        assertTrue(result.getBooleanValue("rebuilt"));
        JSONObject state = result.getJSONObject("state");
        assertNull(state.getString("path"));
        assertEquals("My First", state.getJSONObject("draft").getString("title"));

        Client client = new Client(game());
        client.answer("");

        result = json(authoring.save(player, null, false, true));
        JSONObject saved = result.getJSONObject("saved");
        assertEquals("users/" + AUTHOR + "/my-first.hjson", saved.getString("path"));
        assertEquals(AUTHOR + "/my-first", saved.getString("id"));
        assertTrue(saved.toString(), saved.getBooleanValue("playable"));
        assertFalse(result.getJSONObject("state").getBooleanValue("dirty"));
        File file = new File(users, "users/" + AUTHOR + "/my-first.hjson");
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
        for (String bad : List.of("../escape.hjson", "/etc/passwd.hjson", "a/../../b.hjson", ".hidden.hjson", "campaigns/x.hjson", "x.txt", "a\\b.hjson")) {
            try {
                authoring.save(player, bad, true, true);
                fail("Should not have been saved to " + bad);
            } catch (ScenarioAuthoring.AuthoringException expected) {
                //(as it should be)
            }
        }
        JSONObject saved = json(authoring.save(player, "my drafts/brand new v1.2.hjson", true, true)).getJSONObject("saved");
        assertEquals("my drafts/brand new v1.2.hjson", saved.getString("path"));
        assertTrue(new File(canon, "my drafts/brand new v1.2.hjson").isFile());
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
}
