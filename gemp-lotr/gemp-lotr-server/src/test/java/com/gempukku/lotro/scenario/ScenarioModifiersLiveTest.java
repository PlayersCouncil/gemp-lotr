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
import org.junit.AfterClass;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Modifiers put on by hand in a live game: made through the authoring tool (to the start and to the board as it
 * stands), shown in its state, saved; and a game taken back across a step that put one on.
 */
public class ScenarioModifiersLiveTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    private static final String AUTHOR = "Tester";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File canon;
    private ScenarioAuthoring authoring;
    private final Player player = new Player(1, AUTHOR, "", "u", null, null, null, null, false);
    private final List<LotroGameMediator> games = new ArrayList<>();

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    @Before
    public void setUp() throws Exception {
        File users = temp.newFolder("scenarios-user");
        canon = new File(users, "public");
        canon.mkdirs();
        ScenarioLibrary library = new ScenarioLibrary(temp.newFolder("shipped"), users, ScenarioTesting.library(),
                ScenarioModifiersLiveTest::format, null, 0, System::currentTimeMillis);
        authoring = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioModifiersLiveTest::format, null, (who, live) -> {
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

    private static JSONObject json(Map<String, Object> result) {
        return JSON.parseObject(JSON.toJSONString(result));
    }

    private static JSONObject cardNamed(JSONObject state, String alias) {
        for (Object card : state.getJSONObject("board").getJSONArray("cards"))
            if (((JSONObject) card).getString("alias").equals(alias))
                return (JSONObject) card;
        throw new AssertionError("No card called " + alias);
    }

    @Test
    public void theAuthorPutsModifiersOnTheStartAndOnTheBoardAsItStands() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/blank.hjson").toPath(), ScenarioTesting.scenario("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			hand: runner1
                		}
                	}
                """).replace("id: test", "id: blank"), StandardCharsets.UTF_8);
        JSONObject state = json(authoring.open(player, "blank", true)).getJSONObject("state");

        //What the Modify... menu offers
        JSONObject modify = state.getJSONObject("modify");
        assertEquals(List.of("phase", "turn", "removed"), modify.getList("until", String.class));
        JSONObject strength = modify.getJSONArray("words").getJSONObject(0);
        assertEquals("strength", strength.getString("what"));
        assertEquals("Strength", strength.getString("label"));
        assertEquals("stat", strength.getString("kind"));
        assertEquals("signed", strength.getString("number"));
        assertEquals(List.of("COMPANION", "ALLY", "MINION"), strength.getList("types", String.class));
        JSONObject fierce = null;
        for (Object word : modify.getJSONArray("words"))
            if (((JSONObject) word).getString("what").equals("fierce"))
                fierce = (JSONObject) word;
        assertNotNull(fierce);
        assertEquals("none", fierce.getString("number"));
        assertEquals("keyword", fierce.getString("kind"));
        assertEquals(List.of(), cardNamed(state, "aragorn").getJSONArray("modifiers"));

        //To the start: written into setup.modifiers, and the fresh game has it
        JSONObject result = json(authoring.apply(player, "modify(aragorn, strength, +2), modify(runner1, fierce)", true));
        assertTrue(result.getBooleanValue("rebuilt"));
        state = result.getJSONObject("state");
        JSONObject modifiers = state.getJSONObject("draft").getJSONObject("setup").getJSONObject("modifiers");
        assertEquals("strength +2", modifiers.getString("aragorn"));
        assertEquals("fierce", modifiers.getString("runner1"));
        JSONArray on = cardNamed(state, "aragorn").getJSONArray("modifiers");
        assertEquals(1, on.size());
        assertEquals("strength", on.getJSONObject(0).getString("what"));
        assertEquals(2, on.getJSONObject(0).getIntValue("amount"));
        assertEquals("removed", on.getJSONObject(0).getString("until"));
        assertEquals("Strength +2", on.getJSONObject(0).getString("text"));
        assertEquals("fierce", cardNamed(state, "runner1").getJSONArray("modifiers").getJSONObject(0).getString("what"));
        assertEquals("strength +2", state.getJSONObject("start").getJSONObject("modifiers").getJSONArray("aragorn").getJSONObject(0).getString("written"));

        //To the board as it stands: made at once, and written for the next step
        result = json(authoring.apply(player, "modify(aragorn, damage, +1, turn)", false));
        assertFalse(result.getBooleanValue("rebuilt"));
        state = result.getJSONObject("state");
        assertEquals(List.of("modify(aragorn, damage, +1, turn)"), state.getJSONArray("pending").toJavaList(String.class));
        on = cardNamed(state, "aragorn").getJSONArray("modifiers");
        assertEquals(2, on.size());
        assertEquals("turn", on.getJSONObject(1).getString("until"));
        assertEquals("damage +1 turn", on.getJSONObject(1).getString("written"));

        //...and a mistake is said, and nothing is written
        try {
            authoring.apply(player, "modify(aragorn, fierce)", false);
            fail("Fierce on a companion");
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("fierce is for a minion"));
        }
        try {
            authoring.apply(player, "modify(aragorn, fierce, +1)", false);
            fail("Fierce with a number");
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("takes no number"));
        }

        //Taking one off the start
        state = json(authoring.apply(player, "unmodify(runner1)", true)).getJSONObject("state");
        assertFalse(state.getJSONObject("draft").getJSONObject("setup").getJSONObject("modifiers").containsKey("runner1"));
        assertEquals(List.of(), cardNamed(state, "runner1").getJSONArray("modifiers"));

        //It survives being saved and opened again
        authoring.save(player, null, false, true);
        String saved = Files.readString(new File(canon, "tutorials/blank.hjson").toPath(), StandardCharsets.UTF_8);
        assertTrue(saved, saved.contains("\t\tmodifiers: {\n\t\t\taragorn: strength +2\n\t\t}"));
        state = json(authoring.open(player, "blank", true)).getJSONObject("state");
        assertEquals("strength +2", state.getJSONObject("draft").getJSONObject("setup").getJSONObject("modifiers").getString("aragorn"));
        assertEquals(1, cardNamed(state, "aragorn").getJSONArray("modifiers").size());
    }

    /* ------------------------------------------------------------------ a person's game */

    private static final String HUMAN = "Person";

    private static class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        final Player player = new Player(2, HUMAN, "", "u", null, null, null, null, false);
        int channel;
        GameEvent decision;
        JSONObject step, end;

        Client(LotroGameMediator mediator) {
            this.mediator = mediator;
        }

        void connect() throws Exception {
            decision = null;
            step = end = null;
            mediator.signupUserForGame(player, this);
        }

        void poll() throws Exception {
            mediator.processVisitor(mediator.getCommunicationChannel(player, channel), channel, HUMAN, this);
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
                if (json.getString("type").equals("step"))
                    step = json;
                else if (json.getString("type").equals("end"))
                    end = json;
            }
        }

        void await() throws Exception {
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                poll();
                if (decision != null || end != null || mediator.isFinished())
                    return;
                Thread.sleep(5);
            }
            fail("The game went quiet.  Last step seen: " + step);
        }

        void answer(String answer) throws Exception {
            GameEvent answering = decision;
            decision = null;
            step = null;
            mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
        }

        void answerAsAllowed() throws Exception {
            List<String> answers = step.getJSONObject("allow").getList("answers", String.class);
            answer(answers.isEmpty() ? "" : answers.getFirst());
        }
    }

    private static LiveScenario live(ScenarioDefinition def) throws Exception {
        return new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, null);
    }

    private static int strength(LiveScenario scenario, String alias) {
        ScenarioTable table = scenario.Table();
        return table.GetStrength(table.Card(alias));
    }

    private static List<Integer> ids(LiveScenario scenario, String alias) {
        ScenarioTable table = scenario.Table();
        return table.ModifiersOn(table.Card(alias)).stream().map(CardModifiers.Put::id).toList();
    }

    @Test
    public void aGameTakenBackHasTheModifiersItsStepsPutOnByThen() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			hand: aragorn
                		}
                		shadow: {
                			minions: runner1
                		}
                		modifiers: {
                			frodo: strength +1
                		}
                	}
                	steps: [
                		{
                			when: human, action, phase(fellowship)
                			allow: play(aragorn)
                		}
                		{
                			when: human, action, phase(fellowship)
                			do: modify(aragorn, strength, +3, turn), modify(aragorn, damage, +1, phase)
                			allow: pass
                		}
                		{
                			when: human, action, phase(maneuver)
                			allow: pass
                		}
                	]
                """);
        def.opponentDelayMs = 0;
        LiveScenario scenario = live(def);
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.connect();

        client.await();
        assertEquals(1, client.step.getIntValue("step"));
        int beforeAragorn = mediator.journalSize();
        client.answerAsAllowed();
        client.await();
        assertEquals(2, client.step.getIntValue("step"));
        int atTheModifiers = mediator.journalSize();
        assertEquals(8 + 3, strength(scenario, "aragorn"));
        assertEquals(List.of(2, 3), ids(scenario, "aragorn"));
        client.answerAsAllowed();
        client.await();
        assertEquals(3, client.step.getIntValue("step"));
        assertEquals("The one for the Fellowship phase is gone", List.of(2), ids(scenario, "aragorn"));
        assertEquals(8 + 3, strength(scenario, "aragorn"));

        //Back to step 2's decision: its do was done as the decision came, and is done again as the game is rebuilt
        LotroGameMediator.UndoReply reply = mediator.requestUndo(HUMAN, atTheModifiers);
        assertEquals(reply.message(), "done", reply.status());
        LiveScenario again = mediator.getScenario();
        assertTrue(again != scenario);
        client.connect();
        client.await();
        assertEquals(2, client.step.getIntValue("step"));
        assertEquals(8 + 3, strength(again, "aragorn"));
        assertEquals("The same numbers, in the same order", List.of(2, 3), ids(again, "aragorn"));
        assertEquals(List.of(1), ids(again, "frodo"));

        //Back to before Aragorn was played: no step has put anything on him yet
        reply = mediator.requestUndo(HUMAN, beforeAragorn);
        assertEquals(reply.message(), "done", reply.status());
        again = mediator.getScenario();
        client.connect();
        client.await();
        assertEquals(1, client.step.getIntValue("step"));
        assertEquals(List.of(), ids(again, "aragorn"));
        assertEquals(List.of(1), ids(again, "frodo"));
        assertEquals(8, strength(again, "aragorn"));

        //...and plays on as it did
        client.answerAsAllowed();
        client.await();
        assertEquals(2, client.step.getIntValue("step"));
        assertEquals(List.of(2, 3), ids(again, "aragorn"));
        assertEquals(List.of(), again.Problems());
    }
}
