package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

/**
 * A message's own condition ('require'), and the two things reported about pauses that wait for a skirmish to be
 * over or for what ends their block.
 */
public class ScenarioMessageConditionsTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    private static final String ARAGORN_HERE = """
            {
            						type: canSpot
            						filter: name(Aragorn)
            					}""";
    private static final String ARAGORN_AWAY = """
            {
            						type: cantSpot
            						filter: name(Aragorn)
            					}""";

    /** The person plays Aragorn and Gandalf, then passes: three decisions of one step that repeats. */
    private static final String THREE_TIMES = """
            	setup: {
            		freeps: {
            			hand: aragorn, gandalf
            		}
            	}
            	steps: [
            		{
            			when: freeps, action, phase(fellowship)
            			until: phase(maneuver)
            			messages: [
            				{
            					text: Before Aragorn.
            					require: AWAY
            				}
            				{
            					text: With Aragorn, once.
            					require: HERE
            				}
            				{
            					text: With Aragorn, every time.
            					once: false
            					require: HERE
            				}
            				Said the first time.
            			]
            			answer: play(aragorn), play(gandalf), pass
            		}
            	]
            """.replace("AWAY", ARAGORN_AWAY).replace("HERE", ARAGORN_HERE);

    private static List<String> said(Ruling ruling) {
        List<String> texts = new ArrayList<>();
        for (ScenarioDirector.RenderedMessage message : ruling.shown().messages())
            texts.add(message.text());
        return texts;
    }

    @Test
    public void aMessageIsSaidWhenItsConditionHoldsAsItsStepIsPutUp() throws Exception {
        ScenarioSession session = ScenarioOpponentRulesTest.session(THREE_TIMES);
        List<List<String>> each = new ArrayList<>();
        ScenarioOpponentRulesTest.playTo(session, ruling -> {
            if (ruling.step() != null && ruling.step().number == 1)
                each.add(said(ruling));
            return ruling.step() == null;
        });
        assertEquals(List.of(
                List.of("Before Aragorn.", "Said the first time."),
                //('once', with a condition: the first time its step applies with the condition holding)
                List.of("With Aragorn, once.", "With Aragorn, every time."),
                List.of("With Aragorn, every time.")), each);
    }

    @Test
    public void aPauseWithNothingToSayIsPassedOver() throws Exception {
        String rest = """
                	setup: {
                		freeps: {
                			hand: aragorn
                		}
                	}
                	steps: [
                		{
                			pause: true
                			when: freeps, action, phase(fellowship)
                			messages: [
                				{
                					text: Aragorn is already here.
                					require: HERE
                				}
                			]
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			allow: play(aragorn)
                			end: complete
                		}
                	]
                """.replace("HERE", ARAGORN_HERE);
        ScenarioSession session = ScenarioOpponentRulesTest.session(rest);
        Ruling first = ScenarioOpponentRulesTest.playTo(session, ruling -> true);
        assertEquals("The pause had nothing to say, so the decision went straight to the step behind it", 2, first.step().number);
        assertEquals(ScenarioDirector.StepStatus.MATCHED, session.Director().statuses().get("1"));
        //...the same headless, and the runner says so as a read pause
        assertTrue(ScenarioTesting.run(rest).toString(), ScenarioTesting.run(rest).passed());
    }

    /** A person's page: it polls, keeps the step events, and answers what the step allows. */
    private static class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        final Player player = new Player(1, "Tester", "", "u", null, null, null, null, false);
        int channel;
        GameEvent decision;
        final List<JSONObject> steps = new ArrayList<>();
        JSONObject end;

        Client(LotroGameMediator mediator) throws Exception {
            this.mediator = mediator;
            mediator.signupUserForGame(player, this);
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
                    steps.add(json);
                else if (json.getString("type").equals("end"))
                    end = json;
            }
        }

        void playToTheEnd() throws Exception {
            long deadline = System.currentTimeMillis() + 15_000;
            while (System.currentTimeMillis() < deadline && end == null) {
                mediator.processVisitor(mediator.getCommunicationChannel(player, channel), channel, "Tester", this);
                if (decision != null) {
                    GameEvent answering = decision;
                    decision = null;
                    JSONObject allow = steps.isEmpty() ? null : steps.getLast().getJSONObject("allow");
                    String answer = allow != null && !allow.getJSONArray("answers").isEmpty() ? allow.getJSONArray("answers").getString(0)
                            : AnswerCompiler.leastAnswer(answering.getAwaitingDecision());
                    mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
                } else {
                    Thread.sleep(5);
                }
            }
            assertNotNull("The scenario never ended", end);
        }
    }

    @Test
    public void thePageIsToldWhichOfTheFilesMessagesEachIsAndClosingMessagesHaveConditionsToo() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	options: {
                		opponentDelayMs: 0
                	}
                	setup: {
                		freeps: {
                			hand: aragorn
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			messages: [
                				{
                					text: Not said.
                					require: HERE
                				}
                				Said, the second of the file's.
                			]
                			allow: play(aragorn)
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			allow: pass
                			end: {
                				result: complete
                				messages: [
                					{
                						text: Aragorn came.
                						require: HERE
                					}
                					{
                						text: Aragorn stayed away.
                						require: AWAY
                					}
                				]
                			}
                		}
                	]
                """.replace("HERE", ARAGORN_HERE).replace("AWAY", ARAGORN_AWAY));
        LiveScenario scenario = new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), "Tester",
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER);
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        Client client = new Client(mediator);
        client.playToTheEnd();
        JSONArray messages = client.steps.getFirst().getJSONArray("messages");
        assertEquals(1, messages.size());
        assertEquals(1, messages.getJSONObject(0).getIntValue("index"));
        JSONArray closing = client.end.getJSONArray("messages");
        assertEquals(1, closing.size());
        assertTrue(closing.getJSONObject(0).getString("html").contains("Aragorn came."));
    }

    @Test
    public void conditionsAreReadWrittenBackRenamedAndLeftOutWhenMistaken() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(THREE_TIMES.replace("name(Aragorn)", "alias(aragorn)"));
        assertNotNull(def.steps.getFirst().messages.getFirst().require);
        assertNull(def.steps.getFirst().messages.getLast().require);
        String written = ScenarioWriter.toHjson(def, null);
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());
        org.hjson.JsonObject json = ScenarioWriter.toJson(def);
        AliasRename.rename(json, "aragorn", "strider");
        assertTrue(json.toString(), json.toString().contains("alias(strider)") && !json.toString().contains("alias(aragorn)"));

        List<String> problems = ScenarioValidator.validate(ScenarioTesting.parse(THREE_TIMES.replace("type: cantSpot", "type: noSuchRequirement")), ScenarioTesting.library());
        assertTrue(problems.toString(), problems.stream().anyMatch(problem -> problem.startsWith("step 1.messages[1].require: ")));
        ScenarioSalvage.Result salvaged = ScenarioSalvage.read(ScenarioTesting.scenario(THREE_TIMES.replace("type: cantSpot", "type: noSuchRequirement")),
                d -> ScenarioValidator.validate(d, ScenarioTesting.library()));
        assertNotNull(salvaged.errors().toString(), salvaged.def());
        assertEquals("step 1.messages[1].require", salvaged.leftOut().getFirst().where());
        try {
            ScenarioTesting.parse("""
                	steps: [
                		{
                			pause: true
                			when: freeps, action
                			messages: [
                				{
                					text: Which?
                					choices: {
                						one: One
                						two: Two
                					}
                					require: HERE
                				}
                			]
                		}
                	]
                """.replace("HERE", ARAGORN_HERE));
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("a message that offers 'choices' is always said"));
        }
    }

    /** Three skirmishes: Aragorn's, Boromir's and Gandalf's, in the order the person chooses them. */
    private static final String THREE_SKIRMISHES = """
            	options: {
            		autoPass: true
            	}
            	rules: {
            		oneSided: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	setup: {
            		freeps: {
            			characters: aragorn, boromir, gandalf
            		}
            		shadow: {
            			minions: runner1, runner2, marksman
            		}
            	}
            	steps: [
            		{
            			when: freeps, assign
            			answer: assign(aragorn: runner1, boromir: runner2, gandalf: marksman)
            		}
            		{
            			when: turn(1)
            			until: phase(regroup)
            			skirmishes: true
            			anyOrder: [
            				PAUSE
            			]
            		}
            	]
            """;

    /** Aragorn's skirmish is the one chosen first; after it, whichever is offered first. */
    private static String aragornFirst(ScenarioSession session, Ruling ruling) {
        if (DecisionKinds.of(ruling.decision()) != DecisionKinds.Kind.NEXT_SKIRMISH)
            return null;
        String aragorn = String.valueOf(session.Table().Card("aragorn").getCardId());
        return java.util.Arrays.asList(ruling.decision().getDecisionParameters().get("cardId")).contains(aragorn) ? aragorn : null;
    }

    @Test
    public void aPauseAfterASkirmishIsNotSaidInTheMiddleOfTheNextOne() throws Exception {
        //Waits for the first decision after Aragorn's skirmish, and for a skirmish to be being fought: which is
        // never both, as there is a choice of the next skirmish between them.  It used to wait on, through the next
        // skirmish, and be said in the middle of it.
        String pause = """
                {
                					pause: true
                					once: true
                					when: {
                						terms: freeps, after(aragorn)
                						require: {
                							type: canSpot
                							filter: inSkirmish
                						}
                					}
                					messages: [
                						Late.
                					]
                				}""";
        ScenarioSession session = ScenarioOpponentRulesTest.session(THREE_SKIRMISHES.replace("PAUSE", pause));
        List<String> heard = new ArrayList<>();
        ScenarioOpponentRulesTest.playTo(session, ruling -> {
            if (ruling.step() != null && ruling.step().pause)
                heard.add(session.Table().gameState().getSkirmish() == null ? "between skirmishes"
                        : "in " + session.Table().AliasOf(session.Table().gameState().getSkirmish().getFellowshipCharacter()) + "'s skirmish");
            return session.Table().GetCurrentPhase() == com.gempukku.lotro.common.Phase.REGROUP;
        }, ruling -> aragornFirst(session, ruling));
        assertEquals(List.of(), heard);

        //Said where it can be: the first decision after Aragorn's skirmish (the choice of the next)
        ScenarioSession plain = ScenarioOpponentRulesTest.session(THREE_SKIRMISHES.replace("PAUSE", """
                {
                					pause: true
                					once: true
                					when: freeps, after(aragorn)
                					messages: [
                						Now.
                					]
                				}"""));
        List<String> now = new ArrayList<>();
        ScenarioOpponentRulesTest.playTo(plain, ruling -> {
            if (ruling.step() != null && ruling.step().pause)
                now.add(DecisionKinds.of(ruling.decision()) + " " + (plain.Table().gameState().getSkirmish() == null));
            return plain.Table().GetCurrentPhase() == com.gempukku.lotro.common.Phase.REGROUP;
        }, ruling -> aragornFirst(plain, ruling));
        assertEquals(List.of("NEXT_SKIRMISH true"), now);
    }

    @Test
    public void aStepOfABlockThatWaitsForWhatEndsTheBlockIsSaidToBeAMistake() throws Exception {
        //As designed: the decision that fits a block's until is the step after the block's.  So the pause is never
        // come to, and the runner says why.
        String rest = THREE_SKIRMISHES.replace("PAUSE", """
                {
                					pause: true
                					when: phase(regroup)
                					messages: [
                						Never.
                					]
                				}""");
        ScenarioRunReport report = ScenarioTesting.run(rest);
        assertTrue(report.toString(), report.warnings.stream().anyMatch(warning -> warning.startsWith("step 2.1") && warning.contains("waits for exactly what ends its block")));
        assertNotEquals(ScenarioDirector.StepStatus.MATCHED, report.steps.get("2.1"));
    }
}
