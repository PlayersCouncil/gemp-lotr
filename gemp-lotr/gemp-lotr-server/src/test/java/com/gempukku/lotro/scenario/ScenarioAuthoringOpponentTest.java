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

import static org.junit.Assert.*;

/**
 * The authoring tool and the opponent: what the state says about its orders and its rules, and the changes the
 * Opponent tab makes to them one at a time.
 */
public class ScenarioAuthoringOpponentTest {
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
                ScenarioAuthoringOpponentTest::format, null, 0, System::currentTimeMillis);
        authoring = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioAuthoringOpponentTest::format, null, (who, live) -> {
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

    private JSONObject opponent() {
        return state().getJSONObject("opponent");
    }

    private void open(String opponent) throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/orders.hjson").toPath(), ScenarioTesting.scenario("""
                	opponent: {
                OPPONENT
                	}
                	setup: {
                		freeps: {
                			hand: aragorn
                		}
                		shadow: {
                			hand: runner1, runner2
                		}
                	}
                """.replace("OPPONENT", opponent)).replace("id: test", "id: orders"), StandardCharsets.UTF_8);
        authoring.open(player, "orders", true);
    }

    private static final String TWO_ORDERS = """
            		fallback: pass
            		orders: [
            			{
            				id: first
            				note: The runner goes down first.
            				once: true
            				when: opponent, action, phase(shadow)
            				answer: play(runner1)
            			}
            			{
            				when: opponent, assign
            				answer: assign(frodo: rest)
            			}
            		]
            """;

    @Test
    public void theStateSaysEachOrderPartByPart() throws Exception {
        open(TWO_ORDERS);
        JSONObject opponent = opponent();
        assertEquals("pass", opponent.getString("fallback"));
        assertEquals(List.of("fail", "pass", "bot", "sandbox"), opponent.getJSONArray("fallbacks").toJavaList(String.class));
        assertNull(opponent.getString("leftovers"));
        assertEquals("ringBearer", opponent.getJSONArray("leftoversChoices").getString(0));
        assertTrue(opponent.getJSONArray("leftoversChoices").contains("aragorn"));
        assertFalse(opponent.getJSONArray("leftoversChoices").contains("runner1"));
        assertFalse(opponent.getJSONArray("leftoversChoices").contains("swordarm"));
        assertTrue(opponent.getJSONArray("playsChoices").contains("runner1"));

        JSONArray orders = opponent.getJSONArray("orders");
        assertEquals(2, orders.size());
        JSONObject first = orders.getJSONObject(0);
        assertEquals(0, first.getIntValue("index"));
        assertEquals("first", first.getString("id"));
        assertEquals("The runner goes down first.", first.getString("note"));
        assertTrue(first.getBooleanValue("once"));
        assertEquals("opponent, action, phase(shadow)", first.getString("when"));
        assertEquals(List.of("play(runner1)"), first.getJSONArray("answers").toJavaList(String.class));
        assertFalse(first.getBooleanValue("spent"));
        assertTrue(first.getString("text"), first.getString("text").contains("once: true"));
        assertEquals(List.of("assign(frodo: rest)"), orders.getJSONObject(1).getJSONArray("answers").toJavaList(String.class));
        //Nothing of the opponent's is waiting at the start
        assertNull(opponent.getJSONObject("answering"));
    }

    @Test
    public void ordersAreChangedOneAtATime() throws Exception {
        open(TWO_ORDERS);
        //A new order at the front: the others are as they were
        authoring.edit(player, """
                {"opponent": {"orders": [{"op": "insert", "before": 0, "order": {"id": "third", "when": "opponent, cards", "answer": "pass"}}]}}""", null);
        JSONArray orders = opponent().getJSONArray("orders");
        assertEquals(List.of("third", "first"), List.of(orders.getJSONObject(0).getString("id"), orders.getJSONObject(1).getString("id")));
        assertEquals("The runner goes down first.", orders.getJSONObject(1).getString("note"));

        //Part of one, by its name; then one moved, by its place; then one deleted
        authoring.edit(player, """
                {"opponent": {"orders": [{"op": "merge", "id": "first", "set": {"answer": "play(runner2), play(runner1)"}, "unset": ["once"]}]}}""", null);
        orders = opponent().getJSONArray("orders");
        assertEquals(List.of("play(runner2)", "play(runner1)"), orders.getJSONObject(1).getJSONArray("answers").toJavaList(String.class));
        assertFalse(orders.getJSONObject(1).getBooleanValue("once"));
        assertEquals("The runner goes down first.", orders.getJSONObject(1).getString("note"));

        authoring.edit(player, "{\"opponent\": {\"orders\": [{\"op\": \"move\", \"index\": 0}]}}", null);
        orders = opponent().getJSONArray("orders");
        assertEquals("first", orders.getJSONObject(0).getString("id"));
        assertEquals("third", orders.getJSONObject(2).getString("id"));

        authoring.edit(player, "{\"opponent\": {\"orders\": [{\"op\": \"delete\", \"id\": \"third\"}], \"set\": {\"leftovers\": \"ringBearer\"}}}", null);
        JSONObject opponent = opponent();
        assertEquals(2, opponent.getJSONArray("orders").size());
        assertEquals("ringBearer", opponent.getString("leftovers"));
        assertEquals("pass", opponent.getString("fallback"));

        //A mistake is said, and nothing changes
        try {
            authoring.edit(player, "{\"opponent\": {\"orders\": [{\"op\": \"delete\", \"id\": \"nobody\"}]}}", null);
            fail();
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("no order called 'nobody'"));
        }
        try {
            authoring.edit(player, "{\"opponent\": {\"orders\": [{\"op\": \"merge\", \"index\": 0, \"set\": {\"answer\": \"play(nobody)\"}}]}}", null);
            fail();
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("opponent.orders[1].answer"));
        }
        assertEquals(2, opponent().getJSONArray("orders").size());

        //...and it is all in the file once saved, and comes back when it is opened again
        authoring.save(player, null, false, true);
        String saved = Files.readString(new File(canon, "tutorials/orders.hjson").toPath(), StandardCharsets.UTF_8);
        assertTrue(saved, saved.contains("leftovers: ringBearer"));
        assertTrue(saved, saved.contains("note: The runner goes down first."));
        authoring.close(player);
        authoring.open(player, "orders", true);
        assertEquals("ringBearer", opponent().getString("leftovers"));
        assertEquals("first", opponent().getJSONArray("orders").getJSONObject(0).getString("id"));
    }

    @Test
    public void theOpponentsWaitingDecisionSaysWhatWouldAnswerIt() throws Exception {
        //No order for the Shadow phase: its decision is left for the author, and the state says nothing answers it
        open("""
                		fallback: pass
                		orders: [
                			{
                				when: opponent, assign
                				answer: assign(frodo: rest)
                			}
                		]
                """);
        Client client = new Client(games.getLast());
        for (int i = 0; i < 10 && state().getJSONObject("waiting").getJSONObject("opponent") == null; i++) {
            client.poll();
            client.answer("");
        }
        JSONObject answering = opponent().getJSONObject("answering");
        assertNotNull(answering);
        assertNull(answering.get("order"));
        assertNull(answering.get("rule"));
        assertTrue(answering.getString("when"), answering.getString("when").startsWith("shadow, action, phase(shadow)"));

        //With a sandbox fallback, the same decision is one it has a rule for
        authoring.edit(player, "{\"opponent\": {\"set\": {\"fallback\": \"sandbox\"}}}", null);
        JSONObject opponent = opponent();
        assertEquals("sandbox", opponent.getString("fallback"));
    }
}
