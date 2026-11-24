package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.UndoPolicy;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

/**
 * What puzzles add to a scenario: judging at a moment (outcome.judge), whether the person may take back what they
 * have done (options.undo), a score (outcome.score) and the eager opponent (opponent.fallback: eager).
 */
public class ScenarioPuzzleTest {
    static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    static final String HUMAN = "Tester";

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    /** A person's browser: it connects, polls, and answers what the step allows, or as little as it can. */
    static class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        final Player player = new Player(1, HUMAN, "", "u", null, null, null, null, false);
        int channel;
        GameEvent decision;
        JSONObject step, end, info;
        /** Answers to give, in order, to decisions no step restricts; then as little as can be. */
        final java.util.ArrayDeque<String> answers = new java.util.ArrayDeque<>();
        int decisionsSeen;

        Client(LotroGameMediator mediator) throws Exception {
            this.mediator = mediator;
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
                decisionsSeen++;
            } else if (event.getType() == GameEvent.Type.SCENARIO) {
                JSONObject json = JSON.parseObject(event.getScenario());
                switch (json.getString("type")) {
                    case "step" -> step = json;
                    case "end" -> end = json;
                    case "info" -> info = json;
                    default -> { }
                }
            }
        }

        void playToTheEnd() throws Exception {
            long deadline = System.currentTimeMillis() + 15_000;
            while (System.currentTimeMillis() < deadline && end == null) {
                poll();
                if (decision != null) {
                    GameEvent answering = decision;
                    decision = null;
                    AwaitingDecision awaiting = answering.getAwaitingDecision();
                    JSONObject allow = step == null ? null : step.getJSONObject("allow");
                    String answer = allow != null && !allow.getJSONArray("answers").isEmpty() ? allow.getJSONArray("answers").getString(0)
                            : AnswerCompiler.leastAnswer(awaiting);
                    step = null;
                    mediator.playerAnswered(player, channel, awaiting.getAwaitingDecisionId(), answer);
                } else {
                    Thread.sleep(5);
                }
            }
            poll();
            assertNotNull("The scenario never ended", end);
        }
    }

    static LiveScenario live(ScenarioDefinition def, Map<String, String> picks) throws Exception {
        return new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, null, picks);
    }

    static Client play(ScenarioDefinition def, Map<String, String> picks) throws Exception {
        LotroGameMediator mediator = new LotroGameMediator("1", live(def, picks), LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.playToTheEnd();
        return client;
    }

    static ScenarioDefinition roundTrip(ScenarioDefinition def) throws Exception {
        ScenarioDefinition again = ScenarioParser.parse(ScenarioWriter.toHjson(def, ScenarioTesting.library()));
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(again).toString());
        return again;
    }

    static void assertRejected(String rest, String fragment) {
        try {
            ScenarioTesting.parse(rest);
            fail("Expected the scenario to be rejected with a message containing: " + fragment);
        } catch (ScenarioFormatException exp) {
            assertTrue("Expected a message containing '" + fragment + "' but got: " + exp.getMessage(), exp.getMessage().contains(fragment));
        }
    }

    /*
     * The archery of puzzle 10, made small: one archer against Frodo and Aragorn, at site 3 in the maneuver phase.
     * The person puts the archery wound on whoever the step says, and the puzzle is judged at the end of archery:
     * won if Frodo has no wound.
     */
    private static final String ARCHERY = """
            	kind: puzzle
            	coverage: guided
            	options: {
            		opponentDelayMs: 0
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	setup: {
            		startAt: {
            			phase: maneuver
            			site: 3
            		}
            		freeps: {
            			characters: aragorn
            		}
            		shadow: {
            			minions: bowman
            		}
            	}
            	steps: [
            		{
            			when: human, archeryWound
            			allow: choose(WHO)
            		}
            	]
            	outcome: {
            		judge: {
            			at: endOf(archery)
            			win: {
            				type: cantSpot
            				filter: alias(frodo),wounded
            			}
            		}
            		winMessages: Frodo got through the archery.
            		loseMessages: Frodo was hit.
            		MORE
            	}
            """;

    private static String archery(String who, String more) {
        return ARCHERY.replace("WHO", who).replace("MORE", more);
    }

    /* ---------------------------------------------------------------- P1: judging at a moment */

    @Test
    public void aJudgeIsReadAndWrittenBackAsItWasWritten() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(archery("aragorn", ""));
        assertEquals(ScenarioDefinition.JudgeMoment.END_OF, def.outcome.judge.at());
        assertEquals(com.gempukku.lotro.common.Phase.ARCHERY, def.outcome.judge.phase());
        assertNull(def.outcome.judge.turn());
        assertNotNull(def.outcome.judge.win());
        roundTrip(def);
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("judge: {\n\t\t\tat: endOf(archery)\n\t\t\twin: {"));

        //The moment by itself, and on one turn only
        ScenarioDefinition alone = ScenarioTesting.parse("outcome: {\n judge: startOf(regroup), turn(2)\n}");
        assertEquals(ScenarioDefinition.JudgeMoment.START_OF, alone.outcome.judge.at());
        assertEquals(Integer.valueOf(2), alone.outcome.judge.turn());
        assertNull(alone.outcome.judge.win());
        assertTrue(ScenarioWriter.toHjson(roundTrip(alone), null).contains("judge: startOf(regroup), turn(2)"));
        assertEquals(ScenarioDefinition.JudgeMoment.END_OF_TURN, ScenarioTesting.parse("outcome: {\n judge: endOfTurn\n}").outcome.judge.at());
    }

    @Test
    public void aMomentThatIsNotOneIsRefusedWithWhatToWrite() {
        assertRejected("outcome: {\n judge: endOf(between_turns)\n}", "is not a phase of a turn");
        assertRejected("outcome: {\n judge: afterArchery\n}", "Write endOf(phase), startOf(phase) or endOfTurn");
        assertRejected("outcome: {\n judge: {\n win: {\n type: canSpot\n filter: companion\n}\n}\n}", "outcome.judge.at");
    }

    @Test
    public void theRunnerJudgesAtTheMoment() throws Exception {
        ScenarioRunReport won = ScenarioRunner.run(ScenarioTesting.parse(archery("aragorn", "")), ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(won.toString(), won.completed);
        assertTrue(won.stopped, won.stopped.startsWith("judged at endOf(archery): won"));

        ScenarioRunReport failed = ScenarioRunner.run(ScenarioTesting.parse(archery("frodo", "")), ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertFalse(failed.completed);
        assertTrue(failed.failures.toString(), failed.failures.contains("The scripted path is judged failed at endOf(archery)"));
    }

    @Test
    public void aLiveGameEndsAtTheMomentWonOrFailedWithTheUsualClosingWords() throws Exception {
        Client won = play(ScenarioTesting.parse(archery("aragorn", "")), Map.of());
        assertEquals("complete", won.end.getString("result"));
        assertTrue(won.end.getJSONArray("messages").toString(), won.end.getJSONArray("messages").toString().contains("Frodo got through the archery."));
        //(it ended at the archery's end: nobody was assigned to anybody)
        assertTrue(won.mediator.isFinished());

        Client failed = play(ScenarioTesting.parse(archery("frodo", "")), Map.of());
        assertEquals("failed", failed.end.getString("result"));
        assertTrue(failed.end.getJSONArray("messages").toString().contains("Frodo was hit."));
    }

    @Test
    public void loseIsStillLookedAtAndCountsAtTheMoment() throws Exception {
        //Aragorn wounded is a loss, whether or not Frodo is
        String lose = "lose: {\n type: canSpot\n filter: alias(aragorn),wounded\n}";
        Client client = play(ScenarioTesting.parse(archery("aragorn", lose)), Map.of());
        assertEquals("failed", client.end.getString("result"));
    }

    /* ---------------------------------------------------------------- P6: undo */

    @Test
    public void undoIsOffInAPuzzleAndOnElsewhereUnlessTheScenarioSays() throws Exception {
        assertFalse(ScenarioTesting.parse("kind: puzzle").undoAllowed());
        assertTrue(ScenarioTesting.parse("kind: tutorial").undoAllowed());
        ScenarioDefinition on = ScenarioTesting.parse("kind: puzzle\noptions: {\n undo: on\n}");
        assertTrue(on.undoAllowed());
        assertTrue(ScenarioWriter.toHjson(roundTrip(on), null).contains("undo: on"));
        ScenarioDefinition off = ScenarioTesting.parse("options: {\n undo: off\n}");
        assertFalse(off.undoAllowed());
        //(left out, nothing is written)
        assertFalse(ScenarioWriter.toHjson(ScenarioTesting.parse("kind: puzzle"), null).contains("undo"));
        assertRejected("options: {\n undo: maybe\n}", "options.undo: has to be on");
    }

    @Test
    public void aPuzzlesGameCannotBeTakenBack() throws Exception {
        ScenarioDefinition puzzle = ScenarioTesting.parse(archery("aragorn", ""));
        LotroGameMediator mediator = new LotroGameMediator("1", live(puzzle, Map.of()), LotroServer.SCENARIO_TIMER, false);
        assertEquals(UndoPolicy.OFF, mediator.getUndoPolicy());
        ScenarioDefinition tutorial = ScenarioTesting.parse(archery("aragorn", "").replace("kind: puzzle", "kind: tutorial"));
        assertEquals(UndoPolicy.SOLO, new LotroGameMediator("2", live(tutorial, Map.of()), LotroServer.SCENARIO_TIMER, false).getUndoPolicy());
        ScenarioDefinition allowed = ScenarioTesting.parse(archery("aragorn", "").replace("autoPass: true", "autoPass: true\n undo: on"));
        assertEquals(UndoPolicy.SOLO, new LotroGameMediator("3", live(allowed, Map.of()), LotroServer.SCENARIO_TIMER, false).getUndoPolicy());
    }

    /* ---------------------------------------------------------------- P7: scores */

    @Test
    public void aScoreIsReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("outcome: {\n score: {\n value: strength:aragorn\n better: lower\n label: Aragorn's strength\n}\n}");
        assertEquals("strength:aragorn", def.outcome.score.value());
        assertFalse(def.outcome.score.higherBetter());
        assertEquals("Aragorn's strength", def.outcome.score.label());
        roundTrip(def);
        //(with its braces, as message text has it, it is the same)
        assertEquals("burdens", ScenarioTesting.parse("outcome: {\n score: {\n value: \"{burdens}\"\n}\n}").outcome.score.value());
        //A card definition's value
        ScenarioDefinition dsl = ScenarioTesting.parse("outcome: {\n score: {\n value: {\n type: forEachWound\n filter: companion\n}\n}\n}");
        assertNotNull(dsl.outcome.score.valueJson());
        roundTrip(dsl);
        assertEquals(List.of(), ScenarioValidator.validate(dsl, ScenarioTesting.library()));

        assertRejected("outcome: {\n score: {\n value: title:aragorn\n}\n}", "not a number");
        assertRejected("outcome: {\n score: {\n value: strength:nobody\n}\n}", "no card called 'nobody'");
        assertRejected("outcome: {\n score: {\n better: higher\n}\n}", "needs a 'value'");
        ScenarioDefinition bad = ScenarioTesting.parse("outcome: {\n score: {\n value: {\n type: forEachNothing\n}\n}\n}");
        assertTrue(ScenarioValidator.validate(bad, ScenarioTesting.library()).toString(), ScenarioValidator.validate(bad, ScenarioTesting.library())
                .stream().anyMatch(one -> one.startsWith("outcome.score.value")));
    }

    @Test
    public void theScoreIsReadAtTheJudgingMomentAndTheEndSaysItAndTheBest() throws Exception {
        //Aragorn's strength (8) at the end of archery; the person's best before was 5
        String score = "score: {\n value: strength:aragorn\n label: Aragorn's strength\n}";
        ScenarioDefinition def = ScenarioTesting.parse(archery("aragorn", score));
        LiveScenario scenario = live(def, Map.of());
        scenario.SetPreviousBest(5);
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.playToTheEnd();
        JSONObject scored = client.end.getJSONObject("score");
        assertEquals(8, scored.getIntValue("value"));
        assertEquals("Aragorn's strength", scored.getString("label"));
        assertEquals("higher", scored.getString("better"));
        assertEquals(8, scored.getIntValue("best"));
        assertTrue(scored.getBooleanValue("newBest"));
        assertEquals(Integer.valueOf(8), scenario.Score());
        assertFalse(client.end.getBooleanValue("unlisted"));
    }

    @Test
    public void aScoredPuzzleWithNoWinIsCompleteOnceItHasBeenPlayedToItsEnd() throws Exception {
        //Frodo takes the wound, which would fail the judge's win: with no win, the moment is the end, and it is complete
        String rest = archery("frodo", "score: {\n value: wounds:frodo\n better: lower\n}")
                .replaceAll("(?s)\t\tjudge: \\{.*?\t\t\\}\n\t\twinMessages", "\t\tjudge: endOf(archery)\n\t\twinMessages");
        ScenarioDefinition def = ScenarioTesting.parse(rest);
        assertNull(def.outcome.judge.win());
        Client client = play(def, Map.of());
        assertEquals("complete", client.end.getString("result"));
        assertEquals(1, client.end.getJSONObject("score").getIntValue("value"));
        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.stopped, report.stopped.contains("won, scoring 1"));
    }

    @Test
    public void anUnscoredScenarioSaysNoScore() throws Exception {
        Client client = play(ScenarioTesting.parse(archery("aragorn", "")), Map.of());
        assertTrue(client.end.containsKey("score"));
        assertNull(client.end.get("score"));
        assertEquals("play", client.info.getString("mode"));
        assertEquals("none", client.info.getString("level"));
        assertFalse(client.info.getBooleanValue("undo"));
        assertFalse(client.info.containsKey("slots"));
    }

    /* ---------------------------------------------------------------- P8: the eager opponent */

    @Test
    public void theEagerOpponentTakesWhatItIsOfferedAndTheFallbackIsWrittenBack() throws Exception {
        //Its Shadow phase: it plays the minion it has in hand, unasked (the pass fallback would not)
        String rest = """
                	coverage: guided
                	options: {
                		opponentDelayMs: 0
                		autoPass: true
                	}
                	opponent: {
                		fallback: FALLBACK
                	}
                	setup: {
                		startAt: {
                			phase: shadow
                			site: 2
                			twilight: 6
                		}
                		shadow: {
                			hand: runner1
                		}
                	}
                """;
        ScenarioDefinition eager = ScenarioTesting.parse(rest.replace("FALLBACK", "eager"));
        assertEquals(ScenarioDefinition.Fallback.EAGER, eager.fallback);
        assertTrue(ScenarioWriter.toHjson(roundTrip(eager), null).contains("fallback: eager"));
        ScenarioRunReport report = ScenarioRunner.run(eager, ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.toString().contains("eager, taking all it can"));
        assertTrue(report.toString(), report.decisions.stream().anyMatch(one -> one.decision().contains("play(runner1)") && one.note() != null
                && one.note().contains("eager")));

        ScenarioRunReport passing = ScenarioRunner.run(ScenarioTesting.parse(rest.replace("FALLBACK", "pass")), ScenarioTesting.library(),
                ScenarioTesting.format("scenario"));
        assertFalse(passing.toString(), passing.toString().contains("eager"));
    }

    @Test
    public void theEagerAnswersAreThePlainestThatTakeSomething() {
        assertEquals("0", AnswerCompiler.eagerAnswer(new com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision(1, "Do it?", new String[] {"Yes", "No"}) {
            @Override
            protected void validDecisionMade(int index, String result) { }
        }));
        assertEquals("1", AnswerCompiler.eagerAnswer(new com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision(1, "Do it?", new String[] {"No", "Yes"}) {
            @Override
            protected void validDecisionMade(int index, String result) { }
        }));
        assertEquals("5", AnswerCompiler.eagerAnswer(new com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision(1, "How many?", 0, 5) {
            @Override
            public void decisionMade(String result) { }
        }));
    }
}
