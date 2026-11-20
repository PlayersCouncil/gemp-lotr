package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
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

import static org.junit.Assert.*;

/**
 * The authoring tool with drafts whose seats take turns, and with drafts that play the game's own opening: what the
 * state says of the roles and of how the game opens, the 'when' the tool composes for a decision, steps written from
 * play on the opponent's turn and for the opponent as the Free Peoples player, fresh games played to a step of an
 * even turn or of the opening.
 */
public class ScenarioAuthoringTwoSidedTest {
    private static final String AUTHOR = "Tester";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File canon;
    private ScenarioAuthoring authoring;
    private final Player player = new Player(1, AUTHOR, "", "u", null, null, null, null, false);
    private final List<LotroGameMediator> games = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        File users = temp.newFolder("scenarios-user");
        canon = new File(users, "public");
        canon.mkdirs();
        ScenarioLibrary library = new ScenarioLibrary(temp.newFolder("shipped"), users, ScenarioTesting.library(),
                ScenarioAuthoringTwoSidedTest::format, null, 0, System::currentTimeMillis);
        authoring = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioAuthoringTwoSidedTest::format, null, (who, live) -> {
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
        JSONObject step;

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
            if (event.getType() == GameEvent.Type.DECISION)
                decision = event;
            if (event.getType() == GameEvent.Type.SCENARIO && "step".equals(JSON.parseObject(event.getScenario()).getString("type")))
                step = JSON.parseObject(event.getScenario());
        }

        void answer(String answer) throws Exception {
            assertNotNull("The game is not asking the author anything", decision);
            GameEvent answering = decision;
            decision = null;
            mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
            poll();
        }
    }

    private JSONObject state() {
        return JSON.parseObject(JSON.toJSONString(authoring.state(player)));
    }

    /** Waits for something to be true of the state (the opponent answers after its pause). */
    private JSONObject awaitState(java.util.function.Predicate<JSONObject> until) throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        JSONObject state = state();
        while (!until.test(state) && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
            state = state();
        }
        assertTrue("The game did not get there: " + state, until.test(state));
        return state;
    }

    private void open(String id, String text) throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        //(a scenario is opened by its id)
        Files.writeString(new File(canon, "tutorials/" + id + ".hjson").toPath(), text.replaceFirst("\tid: [a-z0-9-]+", "\tid: " + id), StandardCharsets.UTF_8);
        authoring.open(player, id, true);
    }

    private static String fixture(String name) throws Exception {
        return Files.readString(new File("src/test/resources/scenarios/" + name + ".hjson").toPath(), StandardCharsets.UTF_8);
    }

    /** The first two-sided fixture as far as the pause before the person's assignment on the opponent's turn. */
    private static String upToTheirAssignment() throws Exception {
        String text = fixture("two-sided-first");
        int cut = text.indexOf("\t\t{\n\t\t\twhen: human, assign, as(shadow)\n\t\t\tallow:");
        return text.substring(0, cut) + "\t]\n}\n";
    }

    @Test
    public void stepsWrittenOnTheOpponentsTurnSayTheRoleTheSeatIsPlaying() throws Exception {
        open("two-sided", upToTheirAssignment());
        JSONObject state = state();
        assertTrue(state.getBooleanValue("twoSided"));
        JSONObject autoPass = state.getJSONObject("autoPass");
        assertTrue(autoPass.getBooleanValue("byRole"));
        assertNull(autoPass.get("all"));
        assertEquals(List.of("fellowship", "maneuver", "archery", "assignment", "regroup"), autoPass.getJSONArray("freeps").toJavaList(String.class));
        assertEquals(List.of("maneuver", "archery", "assignment", "regroup"), autoPass.getJSONArray("shadow").toJavaList(String.class));

        //Played by the script to its end: the opponent's turn, the person's assignment as the Shadow player
        authoring.goTo(player, 99);
        state = state();
        JSONObject position = state.getJSONObject("position");
        assertEquals(2, position.getIntValue("turn"));
        assertEquals("opponent", position.getString("turnOf"));
        assertEquals("shadow", position.getJSONObject("roles").getString("human"));
        assertEquals("freeps", position.getJSONObject("roles").getString("opponent"));
        JSONObject waiting = state.getJSONObject("waiting").getJSONObject("human");
        assertEquals("freeps", waiting.getString("seat"));
        assertEquals("shadow", waiting.getString("role"));
        assertEquals("assign", waiting.getString("type"));
        //The freeps seat is the Shadow player here: the 'when' says so, as the phase does not
        assertEquals("freeps, assign, phase(assignment), turn(2), as(shadow)", state.getJSONObject("uncovered").getString("when"));

        //What the author does there is written with that 'when'
        Client client = new Client(games.getLast());
        client.poll();
        LiveScenario live = games.getLast().getScenario();
        String assignment = games.getLast().scenarioLocked(() -> {
            try {
                return AnswerCompiler.compile(live.Table(), live.Table().userFeedback().getAwaitingDecision(live.HumanPlayer()),
                        ScenarioParser.parseAnswer(live.Definition(), Terms.parse("assign(boromir: freca, oppFrodo: ravager)", "test").getFirst(), "test"));
            } catch (ScenarioFormatException | ScenarioScriptException exp) {
                throw new AssertionError(exp);
            }
        });
        client.answer(assignment);
        state = state();
        JSONObject written = state.getJSONObject("draft").getJSONArray("steps").getJSONObject(9);
        assertEquals("freeps, assign, phase(assignment), turn(2), as(shadow)", written.getString("when"));
        assertEquals("assign(boromir: freca, oppFrodo: ravager)", written.getString("allow"));

        //The opponent, the Free Peoples player on its own turn, chooses the next skirmish: left for the author
        state = awaitState(one -> one.getJSONObject("opponent").getJSONObject("answering") != null
                && one.getJSONObject("waiting").getJSONObject("opponent") != null
                && one.getJSONObject("waiting").getJSONObject("opponent").getBooleanValue("held"));
        assertEquals("shadow, cards, nextSkirmish, phase(assignment), turn(2), as(freeps)",
                state.getJSONObject("opponent").getJSONObject("answering").getString("when"));
        assertEquals("freeps", state.getJSONObject("waiting").getJSONObject("opponent").getString("role"));
        authoring.answer(player, "choose(boromir)", true);
        state = awaitState(one -> one.getJSONObject("draft").getJSONArray("steps").size() > 10);
        written = state.getJSONObject("draft").getJSONArray("steps").getJSONObject(10);
        assertEquals("shadow, cards, nextSkirmish, phase(assignment), turn(2), as(freeps)", written.getString("when"));
        assertEquals("choose(boromir)", written.getString("answer"));

        //A fresh game played by the script to the step on the opponent's turn
        authoring.goTo(player, 10);
        state = state();
        assertEquals(2, state.getJSONObject("position").getIntValue("turn"));
        assertEquals(10, state.getJSONObject("waiting").getJSONObject("human").getIntValue("step"));
        assertEquals("shadow", state.getJSONObject("position").getJSONObject("roles").getString("human"));
    }

    @Test
    public void autoPassForEachRoleIsSetAndSurvivesBeingSaved() throws Exception {
        open("two-sided", upToTheirAssignment());
        authoring.edit(player, """
                {"top": {"options": {"autoPass": {"freeps": "fellowship, regroup", "shadow": "shadow"}}}}""", null);
        JSONObject autoPass = state().getJSONObject("autoPass");
        assertEquals(List.of("fellowship", "regroup"), autoPass.getJSONArray("freeps").toJavaList(String.class));
        assertEquals(List.of("shadow"), autoPass.getJSONArray("shadow").toJavaList(String.class));
        authoring.save(player, null, false, true);
        String saved = Files.readString(new File(canon, "tutorials/two-sided.hjson").toPath(), StandardCharsets.UTF_8);
        assertTrue(saved, saved.contains("autoPass: {\n\t\t\tfreeps: fellowship, regroup\n\t\t\tshadow: shadow\n\t\t}"));
        autoPass = JSON.parseObject(JSON.toJSONString(authoring.open(player, "two-sided", true))).getJSONObject("state").getJSONObject("autoPass");
        assertEquals(List.of("shadow"), autoPass.getJSONArray("shadow").toJavaList(String.class));

        //One list again, which holds in both roles
        authoring.edit(player, """
                {"top": {"options": {"autoPass": "maneuver, regroup"}}}""", null);
        autoPass = state().getJSONObject("autoPass");
        assertFalse(autoPass.getBooleanValue("byRole"));
        assertEquals(List.of("maneuver", "regroup"), autoPass.getJSONArray("all").toJavaList(String.class));
        assertEquals(autoPass.getJSONArray("all"), autoPass.getJSONArray("shadow"));
    }

    @Test
    public void aDraftThatPlaysTheGamesOwnOpeningBeginsAtTheBid() throws Exception {
        open("opening", fixture("real-opening"));
        JSONObject state = state();
        assertEquals("game", state.getJSONObject("start").getString("opening"));
        assertEquals(List.of("board", "game"), state.getJSONObject("start").getJSONArray("openings").toJavaList(String.class));
        JSONObject waiting = state.getJSONObject("waiting").getJSONObject("human");
        assertEquals("bid", waiting.getString("kind"));
        assertEquals(1, waiting.getIntValue("step"));
        assertEquals(0, state.getJSONObject("position").getIntValue("turn"));

        //Played to a step: the opening is played from the script on the way
        authoring.goTo(player, 5);
        state = state();
        waiting = state.getJSONObject("waiting").getJSONObject("human");
        assertEquals("mulligan", waiting.getString("kind"));
        assertEquals(5, waiting.getIntValue("step"));
        LiveScenario live = games.getLast().getScenario();
        //(Aragorn went into the starting fellowship; the bid's burden is the game's)
        assertEquals(com.gempukku.lotro.common.Zone.FREE_CHARACTERS, live.Table().Card("aragorn").getZone());
        assertEquals(1, live.Table().gameState().getPlayerBurdens(live.HumanPlayer()));
        assertEquals(0, live.Table().gameState().getPlayerBurdens(live.OpponentPlayer()));

        //The board it starts with is only its decks, in order: anything else is refused, saying why
        try {
            authoring.apply(player, "move(gandalf, freeps.characters)", true);
            fail("A starting fellowship is chosen, not placed");
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("setup.opening: game"));
        }
        authoring.apply(player, "order(freeps.deck, gandalf, swordarm)", true);
        assertEquals("gandalf, swordarm, boromir", state().getJSONObject("draft").getJSONObject("setup").getJSONObject("freeps").getString("deck"));
    }

    @Test
    public void aStepForAnOpeningDecisionSaysWhatItIsFor() throws Exception {
        String text = fixture("real-opening");
        open("opening", text.substring(0, text.indexOf("	steps: [")) + "}\n");
        JSONObject state = state();
        assertEquals("freeps, number, bid", state.getJSONObject("uncovered").getString("when"));
        Client client = new Client(games.getLast());
        client.poll();
        client.answer("1");
        //The opponent's bid, left for the author
        state = awaitState(one -> one.getJSONObject("opponent").getJSONObject("answering") != null);
        assertEquals("shadow, number, bid", state.getJSONObject("opponent").getJSONObject("answering").getString("when"));
        authoring.answer(player, "number(0)", true);
        state = awaitState(one -> one.getJSONObject("uncovered") != null && "goFirst".equals(one.getJSONObject("waiting").getJSONObject("human").getString("kind")));
        assertEquals("freeps, choice, goFirst", state.getJSONObject("uncovered").getString("when"));
        List<String> whens = new ArrayList<>();
        state.getJSONObject("draft").getJSONArray("steps").forEach(step -> whens.add(((JSONObject) step).getString("when")));
        assertEquals(List.of("freeps, number, bid", "shadow, number, bid"), whens);
    }
}
