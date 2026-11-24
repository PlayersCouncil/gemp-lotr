package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.bots.ScenarioBot;
import com.gempukku.lotro.bots.rl.fotrstarters.CardFeatures;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * opponent.fallback: bot in a live game: Gemp's bot answers for the scenario's opponent seat wherever no step or
 * order does, through the scenario's own way of answering for the opponent, and the same way again in a game taken
 * back and built again from its journal.
 */
public class ScenarioBotTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    private static final String HUMAN = "Person";
    private static ScenarioBot bot;

    @BeforeClass
    public static void loadBot() {
        File models = new File("../bot-models");
        if (!models.isDirectory())
            models = new File("bot-models");
        assumeTrue("the bot's trained models are not here", models.isDirectory());
        CardFeatures.init(ScenarioTesting.library());
        bot = ScenarioBot.fromFolder(models);
    }

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    /** The person's browser: it answers whatever it is asked as little as it can, and reads every step. */
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
            long deadline = System.currentTimeMillis() + 20_000;
            while (System.currentTimeMillis() < deadline) {
                poll();
                if (decision != null || end != null || mediator.isFinished())
                    return;
                if (step != null && step.getBooleanValue("acknowledge")) {
                    int number = step.getIntValue("step");
                    step = null;
                    mediator.scenarioAcknowledged(player, number);
                }
                Thread.sleep(5);
            }
            fail("The game went quiet");
        }

        void answerLeast() throws Exception {
            GameEvent answering = decision;
            decision = null;
            mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(),
                    AnswerCompiler.leastAnswer(answering.getAwaitingDecision()));
        }
    }

    /**
     * One turn: the person has the fellowship (Frodo and Aragorn, made strong enough by hand to come through whatever
     * the bot assigns them), the bot a handful of Moria minions and the pool to play them.
     */
    private static ScenarioDefinition oneTurn(String opponentExtra) throws Exception {
        return turns(opponentExtra, 1);
    }

    private static ScenarioDefinition turns(String opponentExtra, int turns) throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	coverage: guided
                	opponent: {
                		fallback: bot
                """ + opponentExtra + """
                	}
                	setup: {
                		twilight: 6
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			hand: runner1, runner2, bowman, marksman
                		}
                		modifiers: {
                			frodo: strength +12, vitality +3
                			aragorn: strength +6
                		}
                	}
                	steps: [
                		{
                			when: human, action, phase(regroup), turn(TURN)
                			end: complete
                		}
                	]
                """.replace("TURN", String.valueOf(turns)));
        def.opponentDelayMs = 0;
        return def;
    }

    private static LiveScenario live(ScenarioDefinition def) throws Exception {
        LiveScenario live = new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, null);
        live.SetBot(bot);
        return live;
    }

    /** Where every card is, its wounds, and the pool, burdens and phase: enough to tell two boards apart. */
    private static Map<String, String> board(LiveScenario scenario) {
        ScenarioTable table = scenario.Table();
        Map<String, String> board = new LinkedHashMap<>();
        for (String alias : scenario.Definition().cards.keySet()) {
            PhysicalCard card = table.Card(alias);
            board.put(alias, card.getZone() + "/" + table.gameState().getWounds(card));
        }
        board.put("twilight", String.valueOf(table.GetTwilight()));
        board.put("burdens", String.valueOf(table.GetBurdens()));
        board.put("phase", String.valueOf(table.GetCurrentPhase()));
        var skirmish = table.gameState().getSkirmish();
        board.put("skirmish", skirmish == null ? "none" : table.AliasOf(skirmish.getFellowshipCharacter()) + " against " + skirmish.getShadowCharacters().size());
        return board;
    }

    /**
     * Plays the person's side (as little as they can) until the scenario's regroup step, noting the journal's length
     * and the board at each of the person's decisions.
     */
    private static List<Object[]> playTheTurn(Client client, LiveScenario[] scenario) throws Exception {
        List<Object[]> seen = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            client.await();
            scenario[0] = client.mediator.getScenario();
            if (client.end != null || client.mediator.isFinished())
                return seen;
            if (client.step != null && client.step.getIntValue("step") == 1 && client.decision != null)
                return seen;
            seen.add(new Object[] {client.mediator.journalSize(), board(scenario[0])});
            client.answerLeast();
        }
        fail("The turn never ended");
        return seen;
    }

    private static List<GameJournal.Entry> opponentAnswers(LiveScenario scenario) {
        List<GameJournal.Entry> answers = new ArrayList<>();
        for (GameJournal.Entry entry : scenario.Table().Journal().answers())
            if (entry.player().equals(Seat.SHADOW.key()))
                answers.add(entry);
        return answers;
    }

    @Test
    public void theBotPlaysTheOpponentWhereNoStepDoes() throws Exception {
        LiveScenario scenario = live(oneTurn(""));
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.connect();
        LiveScenario[] now = {scenario};
        List<Object[]> seen = playTheTurn(client, now);
        assertEquals("The person's turn came to its Regroup phase", Phase.REGROUP, scenario.Table().GetCurrentPhase());
        assertNotNull(client.step);

        //The bot played minions in the Shadow phase, and assigned them: each of the minions has been in play (they
        // are on the table, or were killed or discarded on the way)
        int played = 0;
        for (String minion : List.of("runner1", "runner2", "bowman", "marksman"))
            if (scenario.Table().Card(minion).getZone() != Zone.HAND && scenario.Table().Card(minion).getZone() != Zone.DECK)
                played++;
        assertTrue("The bot played no minion: " + board(scenario), played > 0);
        //...and assigned them (the person assigned none): there were skirmishes
        boolean fought = false;
        for (Object[] one : seen)
            fought |= !((Map<?, ?>) one[1]).get("skirmish").equals("none");
        assertTrue("There was no skirmish: the bot assigned none of its minions", fought);
        assertEquals(List.of(), scenario.Problems());
    }

    @Test
    public void aGameTakenBackAcrossTheBotsAnswersComesToTheSameBoard() throws Exception {
        LiveScenario scenario = live(oneTurn(""));
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.connect();
        LiveScenario[] now = {scenario};
        List<Object[]> first = playTheTurn(client, now);
        Map<String, String> end = board(scenario);
        List<GameJournal.Entry> botAnswers = opponentAnswers(scenario);
        assertTrue("The bot answered something: " + botAnswers, !botAnswers.isEmpty());
        assertTrue(first.size() >= 2);

        //Back to the person's first decision, before the bot has done anything, and the turn played again the same
        // way: the bot answers afresh, and as it did
        LotroGameMediator.UndoReply reply = mediator.requestUndo(HUMAN, (Integer) first.getFirst()[0]);
        assertEquals(reply.message(), "done", reply.status());
        assertTrue(mediator.getScenario() != scenario);
        client.connect();
        List<Object[]> second = playTheTurn(client, now);
        assertEquals(end, board(now[0]));
        assertEquals(botAnswers.toString(), opponentAnswers(now[0]).toString());
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++)
            assertEquals("At the person's decision " + (i + 1), first.get(i)[1], second.get(i)[1]);

        //Back to a decision of the person's after the bot has played: the game built from the journal is the board
        // as it was there
        Object[] later = first.get(first.size() - 1);
        reply = mediator.requestUndo(HUMAN, (Integer) later[0]);
        assertEquals(reply.message(), "done", reply.status());
        client.connect();
        client.await();
        assertEquals(later[1], board(mediator.getScenario()));
        assertEquals(List.of(), mediator.getScenario().Problems());
    }

    @Test
    public void inATwoSidedGameTheBotPlaysWhicheverRoleItsSeatHas() throws Exception {
        //Turn 1 is the person's, the bot the Shadow player; turn 2 the bot's own, the bot the Free Peoples player, and
        // the person its Shadow player, to the Regroup phase of that turn
        LiveScenario scenario = live(turns("", 2));
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.connect();
        playTheTurn(client, new LiveScenario[] {scenario});
        ScenarioTable table = scenario.Table();
        assertEquals(2, table.gameState().getTurnNumber());
        assertEquals(Phase.REGROUP, table.GetCurrentPhase());
        assertEquals("It is the bot's turn", table.OpponentPlayer(), table.gameState().getCurrentPlayerId());
        assertTrue("The bot's fellowship moved on", table.gameState().getPlayerPosition(table.OpponentPlayer()) >= 2);
        assertEquals(List.of(), scenario.Problems());
    }

    @Test
    public void ordersStillComeBeforeTheBot() throws Exception {
        //A standing order that the opponent passes in the Shadow phase: the bot never gets to play a minion
        LiveScenario scenario = live(oneTurn("""
                		orders: [
                			{
                				when: action, phase(shadow)
                				answer: pass
                			}
                		]
                """));
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.connect();
        playTheTurn(client, new LiveScenario[] {scenario});
        for (String minion : List.of("runner1", "runner2", "bowman", "marksman"))
            assertEquals(minion, Zone.HAND, scenario.Table().Card(minion).getZone());
        assertEquals(List.of(), scenario.Problems());
    }

    @Test
    public void withoutABotTheOpponentDoesAsLittleAsItCan() throws Exception {
        ScenarioDefinition def = oneTurn("");
        LiveScenario scenario = new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, null);
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.connect();
        playTheTurn(client, new LiveScenario[] {scenario});
        assertEquals(Zone.HAND, scenario.Table().Card("runner1").getZone());
        assertEquals(1, scenario.Problems().size());
        assertTrue(scenario.Problems().getFirst(), scenario.Problems().getFirst().contains("no bot to play the opponent"));
    }

    @Test
    public void theHeadlessRunnerStillStopsWhereTheBotWouldPlay() throws Exception {
        ScenarioRunReport report = ScenarioRunner.run(oneTurn(""), ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.passed());
        assertEquals(ScenarioDefinition.RunnerLevel.PREFIX, report.reached);
    }
}
