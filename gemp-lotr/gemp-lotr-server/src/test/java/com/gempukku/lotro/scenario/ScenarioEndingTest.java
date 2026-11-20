package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

/**
 * How a scenario ends other than by a step's 'end': its outcome coming true, and the game itself ending (a
 * Ring-bearer killed), with what is said then and what it counts as.
 */
public class ScenarioEndingTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    private static final String HUMAN = "Tester";

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    /** A person's browser: it connects, polls, and answers as little as it can. */
    private static class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        final Player player = new Player(1, HUMAN, "", "u", null, null, null, null, false);
        int channel;
        GameEvent decision;
        JSONObject step, end;

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
            } else if (event.getType() == GameEvent.Type.SCENARIO) {
                JSONObject json = JSON.parseObject(event.getScenario());
                if (json.getString("type").equals("step"))
                    step = json;
                else if (json.getString("type").equals("end"))
                    end = json;
            }
        }

        /** Plays the person's side, answering what the step allows or as little as can be, until the scenario ends. */
        void playToTheEnd() throws Exception {
            long deadline = System.currentTimeMillis() + 15_000;
            while (System.currentTimeMillis() < deadline && end == null) {
                poll();
                if (decision != null) {
                    GameEvent answering = decision;
                    decision = null;
                    JSONObject allow = step == null ? null : step.getJSONObject("allow");
                    String answer = allow != null && !allow.getJSONArray("answers").isEmpty() ? allow.getJSONArray("answers").getString(0)
                            : AnswerCompiler.leastAnswer(answering.getAwaitingDecision());
                    step = null;
                    mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
                } else {
                    Thread.sleep(5);
                }
            }
            poll();
            assertNotNull("The scenario never ended", end);
        }
    }

    private static Client play(String rest) throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(rest);
        LiveScenario scenario = new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER);
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.playToTheEnd();
        return client;
    }

    /** Frodo walks out alone with three wounds and meets an archer: the game ends with the person losing it. */
    private static final String FRODO_FALLS = """
            	options: {
            		opponentDelayMs: 0
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	coverage: guided
            	setup: {
            		shadow: {
            			hand: bowman
            		}
            		wounds: {
            			frodo: 3
            		}
            	}
            	steps: [
            		{
            			when: opponent, action, phase(shadow)
            			answer: play(bowman)
            		}
            	]
            	outcome: {
            		loseMessage: Frodo is gone.
            		ENDINGS
            	}
            """;

    @Test
    public void aGameLostByItsOwnRulesIsAFailureWithItsLoseMessageAsItWas() throws Exception {
        Client client = play(FRODO_FALLS.replace("ENDINGS", ""));
        assertEquals("failed", client.end.getString("result"));
        assertTrue(client.end.getJSONArray("messages").isEmpty());
        assertTrue(client.end.getString("message"), client.end.getString("message").contains("Frodo is gone."));
    }

    @Test
    public void aGameLostByItsOwnRulesCanCountAsCompleteAndSaySomething() throws Exception {
        Client client = play(FRODO_FALLS.replace("ENDINGS", """
                gameLost: {
                			result: complete
                			messages: [
                				{
                					header: The Shadow falls
                					text: "Frodo is in the dead pile, and the twilight pool has {twilight} in it."
                				}
                				That is how it goes.
                			]
                		}"""));
        assertEquals("complete", client.end.getString("result"));
        JSONArray messages = client.end.getJSONArray("messages");
        assertEquals(2, messages.size());
        assertEquals("The Shadow falls", messages.getJSONObject(0).getString("header"));
        //(filled in from the board as it is at the end)
        assertTrue(messages.getJSONObject(0).getString("html"), messages.getJSONObject(0).getString("html").matches("(?s).*has \\d+ in it.*"));
        assertNull("Nothing failed, so there is no lose message", client.end.getString("message"));
    }

    private static final String ARAGORN = """
            	options: {
            		opponentDelayMs: 0
            	}
            	opponent: {
            		fallback: pass
            	}
            	coverage: guided
            	setup: {
            		freeps: {
            			hand: aragorn
            		}
            	}
            	steps: [
            		{
            			when: freeps, action, phase(fellowship)
            			allow: play(aragorn)
            		}
            	]
            	outcome: {
            		WHICH: {
            			type: canSpot
            			filter: name(Aragorn)
            		}
            		ENDINGS
            	}
            """;

    @Test
    public void theOutcomeComingTrueSaysItsMessages() throws Exception {
        Client won = play(ARAGORN.replace("WHICH", "win").replace("ENDINGS", """
                winMessages: [
                			{
                				header: Well met
                				text: "{title:aragorn} is here."
                			}
                		]"""));
        assertEquals("complete", won.end.getString("result"));
        assertEquals("Well met", won.end.getJSONArray("messages").getJSONObject(0).getString("header"));
        assertTrue(won.end.getJSONArray("messages").getJSONObject(0).getString("html").contains("Aragorn, Heir of Elendil is here."));

        Client lost = play(ARAGORN.replace("WHICH", "lose").replace("ENDINGS", """
                loseMessage: Not him.
                		loseMessages: [
                			He should not have come.
                		]"""));
        assertEquals("failed", lost.end.getString("result"));
        assertTrue(lost.end.getJSONArray("messages").getJSONObject(0).getString("html").contains("He should not have come."));
        assertTrue(lost.end.getString("message").contains("Not him."));
    }

    /** The other way about: the person, as the Shadow player, kills a wounded Frodo in a skirmish. */
    private static final String SHADOW_WINS = """
            	human: shadow
            	rules: {
            		oneSided: true
            	}
            	options: {
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	setup: {
            		shadow: {
            			hand: runner1
            		}
            		wounds: {
            			frodo: 3
            		}
            	}
            	steps: [
            		{
            			when: shadow, action, phase(shadow)
            			allow: play(runner1)
            		}
            		{
            			when: shadow, action, phase(shadow)
            			allow: pass
            		}
            		{
            			when: shadow, assign
            			allow: assign(frodo: runner1)
            		}
            		{
            			when: shadow, action, phase(skirmish)
            			until: phase(regroup)
            			allow: pass
            			answer: pass
            		}
            	]
            	OUTCOME
            """;

    @Test
    public void theRunnerTakesTheGamesEndAsTheScenarioSays() throws Exception {
        //Without a word about it, a game that ends by itself before the scenario does is a mistake in the script
        ScenarioRunReport report = ScenarioTesting.run(SHADOW_WINS.replace("OUTCOME", ""));
        assertFalse(report.toString(), report.passed());
        assertTrue(report.toString(), report.failures.stream().anyMatch(f -> f.contains("The game ended before the scenario did")));

        report = ScenarioTesting.run(SHADOW_WINS.replace("OUTCOME", """
                outcome: {
                		gameWon: complete
                	}"""));
        assertTrue(report.toString(), report.passed());
        assertTrue(report.stopped, report.stopped.contains("outcome.gameWon: complete"));

        //...and a game lost that counts as a failure is one on the scripted path
        String strict = """
                	options: {
                		autoPass: true
                	}
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		shadow: {
                			hand: marksman
                		}
                		wounds: {
                			frodo: 3
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			do: twilight(10)
                			allow: pass
                		}
                		{
                			when: opponent, action, phase(shadow)
                			answer: play(marksman)
                		}
                	]
                	outcome: {
                		ENDING
                	}
                """;
        report = ScenarioTesting.run(strict.replace("ENDING", "gameLost: fail"));
        assertFalse(report.toString(), report.passed());
        assertTrue(report.toString(), report.failures.stream().anyMatch(f -> f.contains("The scripted path ends in failure")));
        report = ScenarioTesting.run(strict.replace("ENDING", "gameLost: complete"));
        assertTrue(report.toString(), report.passed());
    }

    @Test
    public void endingsAreReadWrittenBackAndRenamed() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(ARAGORN.replace("WHICH", "win").replace("ENDINGS", """
                winMessages: [
                			"{title:aragorn} came."
                		]
                		loseMessages: [
                			{
                				header: No
                				text: Not this way.
                			}
                		]
                		gameWon: complete
                		gameLost: {
                			result: complete
                			messages: [
                				"{strength:aragorn} was not enough."
                			]
                		}"""));
        assertEquals(1, def.outcome.winMessages.size());
        assertEquals("No", def.outcome.loseMessages.getFirst().header);
        assertEquals(ScenarioDefinition.EndResult.COMPLETE, def.outcome.gameWon.result);
        assertTrue(def.outcome.gameWon.messages.isEmpty());
        assertEquals(ScenarioDefinition.EndResult.COMPLETE, def.outcome.gameLost.result);
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("gameWon: complete"));
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());

        org.hjson.JsonObject json = ScenarioWriter.toJson(def);
        AliasRename.rename(json, "aragorn", "strider");
        String renamed = json.get("outcome").toString();
        assertTrue(renamed, renamed.contains("{title:strider} came.") && renamed.contains("{strength:strider} was not enough."));

        try {
            ScenarioTesting.parse(ARAGORN.replace("WHICH", "win").replace("ENDINGS", """
                    gameLost: {
                    			messages: [
                    				{
                    					text: Wait.
                    					on: ready
                    				}
                    			]
                    		}"""));
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("outcome.gameLost.messages: closing messages"));
        }
        ScenarioSalvage.Result salvaged = ScenarioSalvage.read(ScenarioTesting.scenario(ARAGORN.replace("WHICH", "win")
                .replace("ENDINGS", "winMessages: \"{title:nobody}\"")), d -> java.util.List.of());
        assertNotNull(salvaged.def());
        assertEquals("outcome.winMessages[1].text", salvaged.leftOut().getFirst().where());
    }
}
