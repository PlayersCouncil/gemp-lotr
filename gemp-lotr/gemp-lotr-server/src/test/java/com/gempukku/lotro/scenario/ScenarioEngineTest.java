package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioDefinition.RunnerLevel;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;
import com.gempukku.lotro.scenario.ScenarioDirector.StepStatus;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * The scenario machinery against the real game: laying a board out, the rules a scenario can impose, deciding random
 * outcomes, keeping place in a script, and what the headless runner makes of scripts that are wrong.
 */
public class ScenarioEngineTest {
    private static final String HUMAN = ScenarioRunner.HUMAN;
    private static final String OPPONENT = ScenarioRunner.OPPONENT;

    /** Whoever the game is waiting on. */
    private static String decider(ScenarioTable table) {
        return table.FreepsAnyDecisionsAvailable() ? table.P1() : table.ShadowAnyDecisionsAvailable() ? table.P2() : null;
    }

    private static void assertFails(ScenarioRunReport report, String failureFragment) {
        assertFalse("Expected the run to fail:\n" + report, report.passed());
        assertTrue("Expected a failure containing '" + failureFragment + "':\n" + report,
                report.failures.stream().anyMatch(failure -> failure.contains(failureFragment)));
    }

    @Test
    public void theBoardIsLaidOutBeforeTheFirstTurnBegins() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		freeps: {
                			characters: aragorn
                			hand: gandalf, servant1
                			deck: boromir, swordarm
                			discard: servant2
                		}
                		shadow: {
                			minions: runner1
                			hand: bowman
                		}
                		wounds: {
                			aragorn: 2
                		}
                		burdens: 3
                	}
                """);

        assertEquals(List.of(), table.Problems());
        assertEquals(HUMAN, table.P1());
        assertEquals(OPPONENT, table.P2());

        assertEquals(Zone.FREE_CHARACTERS, table.Card("frodo").getZone());
        assertEquals(Zone.FREE_CHARACTERS, table.Card("aragorn").getZone());
        assertEquals(Zone.HAND, table.Card("gandalf").getZone());
        assertEquals(Zone.HAND, table.Card("servant1").getZone());
        assertEquals(Zone.DISCARD, table.Card("servant2").getZone());
        assertEquals(Zone.SHADOW_CHARACTERS, table.Card("runner1").getZone());
        assertEquals(Zone.HAND, table.Card("bowman").getZone());
        assertEquals(2, table.GetFreepsHandCount());
        assertEquals(1, table.GetShadowHandCount());

        assertSame(table.Card("boromir"), table.GetFromTopOfFreepsDeck(1));
        assertSame(table.Card("swordarm"), table.GetFromTopOfFreepsDeck(2));
        assertEquals("Cards the scenario does not place stay in their deck", Zone.DECK, table.Card("runner2").getZone());

        assertEquals(2, table.GetWoundsOn(table.Card("aragorn")));
        assertEquals("The burden bid for the first turn is replaced by the scenario's count", 3, table.GetBurdens());
        assertEquals(0, table.GetTwilight());

        //No starting fellowship, opening draw or mulligan: play begins at the first turn's first decision, and that
        // decision already knows about the board
        assertEquals(1, table.gameState().getTurnNumber());
        assertEquals(Phase.FELLOWSHIP, table.GetCurrentPhase());
        assertEquals(HUMAN, table.GetCurrentPlayer());
        assertTrue(table.AwaitingFellowshipPhaseActions());
        assertTrue(AnswerCompiler.options(table, table.FreepsGetAwaitingDecision()).contains("play(gandalf)"));
    }

    @Test
    public void copiesOfACardAreToldApartByTheOrderTheyAreDeclaredIn() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		freeps: {
                			hand: servant2
                			discard: servant1
                		}
                	}
                """);
        assertNotSame(table.Card("servant1"), table.Card("servant2"));
        assertEquals(Zone.DISCARD, table.Card("servant1").getZone());
        assertEquals(Zone.HAND, table.Card("servant2").getZone());
        assertEquals("servant1", table.AliasOf(table.Card("servant1")));
        assertSame("Aliases are also the game's own card labels, for the alias() filter",
                table.Card("servant2"), table.gameState().getLabeledCard("servant2"));
    }

    @Test
    public void aScenarioCanShrinkTheHandAndKeepOnePlayerOnTheFreePeoplesSide() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	rules: {
                		oneSided: true
                		human: [
                			{
                				type: modifier
                				modifier: {
                					type: modifyHandSize
                					amount: -4
                				}
                			}
                		]
                	}
                """);
        assertEquals(4, table.game().getModifiersQuerying().getHandSize(table.game(), HUMAN));
        assertEquals("The rule is the human seat's alone", 8, table.game().getModifiersQuerying().getHandSize(table.game(), OPPONENT));

        //Play the first turn out doing as little as possible
        while (table.gameState().getTurnNumber() == 1) {
            String player = decider(table);
            table.PlayerDecided(player, AnswerCompiler.leastAnswer(table.GetAwaitingDecision(player)));
        }

        assertEquals(2, table.gameState().getTurnNumber());
        assertEquals("The same player is the Free Peoples player again", HUMAN, table.GetCurrentPlayer());
        assertEquals("Reconciled to a hand of four", 4, table.GetFreepsHandCount());
        assertEquals("The opponent reconciled as far as its four-card deck allowed", 4, table.GetShadowHandCount());
    }

    @Test
    public void withoutTheRuleTurnsAlternateAsUsual() throws Exception {
        ScenarioTable table = ScenarioTesting.table("");
        while (table.gameState().getTurnNumber() == 1) {
            String player = decider(table);
            table.PlayerDecided(player, AnswerCompiler.leastAnswer(table.GetAwaitingDecision(player)));
        }
        assertEquals(OPPONENT, table.GetCurrentPlayer());
    }

    @Test
    public void randomOutcomesComeFromTheScenario() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		shadow: {
                			hand: runner1, bowman, marksman
                		}
                	}
                	rng: [
                		shuffle(freeps.deck: boromir, gandalf)
                		pick(shadow.hand: bowman)
                	]
                """);
        table.gameState().shuffleDeck(HUMAN);
        assertSame(table.Card("boromir"), table.GetFromTopOfFreepsDeck(1));
        assertSame(table.Card("gandalf"), table.GetFromTopOfFreepsDeck(2));
        assertSame("Cards the outcome does not name keep their order underneath", table.Card("aragorn"), table.GetFromTopOfFreepsDeck(3));

        List<PhysicalCard> picked = GameUtils.getRandomCards(table.game(), OPPONENT, Zone.HAND, table.GetShadowHand(), 1);
        assertEquals(List.of(table.Card("bowman")), picked);

        //With nothing left listed, the default is to change nothing and to pick from the front
        table.gameState().shuffleDeck(HUMAN);
        assertSame(table.Card("boromir"), table.GetFromTopOfFreepsDeck(1));
        picked = GameUtils.getRandomCards(table.game(), OPPONENT, Zone.HAND, table.GetShadowHand(), 2);
        assertEquals(List.of(table.GetShadowHand().get(0), table.GetShadowHand().get(1)), picked);

        assertEquals(List.of(), table.Problems());
        assertTrue(table.Random().remaining().isEmpty());
    }

    @Test
    public void aListedOutcomeThatDoesNotFitIsAProblemAndIsNotUsedUp() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	rng: [
                		pick(shadow.hand: bowman)
                	]
                """);
        table.gameState().shuffleDeck(HUMAN);
        assertEquals(1, table.Problems().size());
        assertTrue(table.Problems().getFirst(), table.Problems().getFirst().contains("the game shuffled freeps.deck"));
        assertEquals(1, table.Random().remaining().size());
    }

    @Test
    public void aStepsDirectChangesAndTemplates() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	steps: [
                		{
                			when: freeps, action
                			do: wound(frodo, 2), move(gandalf, freeps.hand), move(runner1, shadow.minions), twilight(5), burdens(+2)
                			messages: [
                				Frodo has {wounds:frodo} wounds and {vitality:frodo} vitality left. Twilight {twilight}, burdens {burdens}, site {site}, turn {turn}. {title:gandalf} costs {cost:gandalf}. The runner has strength {strength:runner1}.
                			]
                		}
                	]
                """);
        ScenarioDirector director = new ScenarioDirector(table);
        AwaitingDecision decision = table.FreepsGetAwaitingDecision();
        Step step = director.match(HUMAN, decision);
        assertNotNull(step);

        assertEquals(2, table.GetWoundsOn(table.Card("frodo")));
        assertEquals(Zone.HAND, table.Card("gandalf").getZone());
        assertEquals(Zone.SHADOW_CHARACTERS, table.Card("runner1").getZone());
        assertEquals(5, table.GetTwilight());
        assertEquals(2, table.GetBurdens());

        ScenarioDirector.Presentation shown = director.present(step, HUMAN, decision);
        assertNull("A step with no 'allow' restricts nothing", shown.allowed());
        assertEquals("Frodo has 2 wounds and 2 vitality left. Twilight 5, burdens 2, site 1, turn 1. "
                + "Gandalf, Stormcrow costs 4. The runner has strength 5.", shown.messages().getFirst().text());

        //Matching the same decision again does not repeat the changes
        director.match(HUMAN, decision);
        assertEquals(2, table.GetWoundsOn(table.Card("frodo")));
        assertEquals(List.of(), table.Problems());
    }

    @Test
    public void aGroupOfActionsInAnyOrder() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		freeps: {
                			hand: aragorn, gandalf
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			until: phase(regroup)
                			messages: [
                				Play both companions, in either order. Then pass.
                			]
                			allow: {
                				answers: play(aragorn), play(gandalf)
                				passWhen: [
                					{
                						type: canSpot
                						filter: alias(aragorn)
                					}
                					{
                						type: canSpot
                						filter: alias(gandalf)
                					}
                				]
                			}
                			answer: play(gandalf), play(aragorn), pass
                		}
                	]
                """);
        ScenarioDirector director = new ScenarioDirector(table);

        AwaitingDecision decision = table.FreepsGetAwaitingDecision();
        Step step = director.match(HUMAN, decision);
        ScenarioDirector.Presentation shown = director.present(step, HUMAN, decision);
        assertEquals(2, shown.allowed().size());
        assertFalse("Pass stays locked until both are in play", shown.passAllowed());
        assertEquals(1, shown.messages().size());
        table.PlayerDecided(HUMAN, director.scriptedAnswer(step, decision));
        director.answered(step);
        assertEquals(Zone.FREE_CHARACTERS, table.Card("gandalf").getZone());

        decision = table.FreepsGetAwaitingDecision();
        assertSame(step, director.match(HUMAN, decision));
        shown = director.present(step, HUMAN, decision);
        assertEquals("Gandalf has been played, so only Aragorn is left to allow", 1, shown.allowed().size());
        assertFalse(shown.passAllowed());
        assertTrue("The group's messages are shown once, at its first decision", shown.messages().isEmpty());
        table.PlayerDecided(HUMAN, director.scriptedAnswer(step, decision));
        director.answered(step);

        decision = table.FreepsGetAwaitingDecision();
        assertSame(step, director.match(HUMAN, decision));
        shown = director.present(step, HUMAN, decision);
        assertTrue(shown.allowed().isEmpty());
        assertTrue(shown.passAllowed());
        assertEquals("", director.scriptedAnswer(step, decision));
    }

    @Test
    public void aStepWhoseMomentPassesIsSkipped() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	coverage: guided
                	steps: [
                		{
                			when: freeps, action, phase(maneuver)
                			skipWhen: phase(regroup)
                		}
                		{
                			when: freeps, action, phase(regroup)
                		}
                	]
                """);
        ScenarioDirector director = new ScenarioDirector(table);

        //Nobody plays a minion, so there is no maneuver phase to speak of: the game goes from the move to regroup
        Step matched = null;
        while (matched == null) {
            String player = decider(table);
            AwaitingDecision decision = table.GetAwaitingDecision(player);
            matched = director.match(player, decision);
            if (matched == null)
                table.PlayerDecided(player, AnswerCompiler.leastAnswer(decision));
        }
        assertEquals("Steps are known by their place in the script", 2, matched.number);
        assertEquals(StepStatus.SKIPPED, director.statuses().get("1"));
        assertEquals(StepStatus.MATCHED, director.statuses().get("2"));
    }

    @Test
    public void thePersonCanSitInTheShadowSeat() throws Exception {
        ScenarioRunReport report = ScenarioTesting.run("""
                	human: shadow
                	rules: {
                		oneSided: true
                	}
                	setup: {
                		shadow: {
                			hand: runner1
                		}
                	}
                	steps: [
                		{
                			when: human, action, phase(shadow)
                			allow: play(runner1)
                			end: complete
                		}
                	]
                """);
        assertTrue(report.toString(), report.passed());
        assertEquals(RunnerLevel.FULL, report.reached);
        //The scripted opponent had nothing it could do in its fellowship phase, so it was never asked
        assertEquals(1, report.decisions.size());
        assertTrue(report.decisions.getFirst().decision().startsWith("shadow, action \"Play Shadow action or Pass\""));
    }

    @Test
    public void aScenarioThatDoesNotFitTheCardLibraryIsNotPlayed() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(ScenarioTesting.scenario("""
                	rules: {
                		freeps: [
                			{
                				type: modifier
                				modifier: {
                					type: makeEverythingFree
                				}
                			}
                		]
                	}
                	steps: [
                		{
                			id: a
                			when: {
                				seat: freeps
                				require: {
                					type: moonIsFull
                				}
                			}
                		}
                	]
                """).replace("aragorn: 4_109", "aragorn: 4_9999").replace("sites: {\n\t\tfreeps: 1_323", "sites: {\n\t\tfreeps: 1_89"));
        List<String> problems = ScenarioValidator.validate(def, ScenarioTesting.library());
        assertEquals(problems.toString(), 4, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("cards: 'aragorn': there is no card 4_9999"));
        assertTrue(problems.get(1), problems.get(1).contains("sites.freeps: 1_89 is not a site"));
        assertTrue(problems.get(2), problems.get(2).startsWith("rules.freeps:"));
        assertTrue(problems.get(3), problems.get(3).startsWith("step 1 (a).when.require:"));

        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
        assertFails(report, "there is no card 4_9999");
        assertTrue(report.decisions.isEmpty());
    }

    @Test
    public void strictScriptsMustCoverEveryDecisionOfThePlayer() throws Exception {
        assertFails(ScenarioTesting.run(""), "No step covers this decision");
    }

    @Test
    public void anAnswerThatDoesNotFitTheGameFailsTheRun() throws Exception {
        //Aragorn is in the deck, not in hand, so he cannot be played
        assertFails(ScenarioTesting.run("""
                	steps: [
                		{
                			when: freeps, action
                			allow: play(aragorn)
                		}
                	]
                """), "does not fit the decision: no such action is on offer");
    }

    @Test
    public void stepsAfterTheEndAndUnusedOutcomesFailTheRun() throws Exception {
        ScenarioRunReport report = ScenarioTesting.run("""
                	rng: [
                		shuffle(freeps.deck: boromir)
                	]
                	steps: [
                		{
                			when: freeps, action
                			allow: pass
                			end: complete
                		}
                		{
                			id: leftover
                			when: freeps
                		}
                	]
                """);
        assertFails(report, "step 2 (leftover) [when: freeps] was never reached");
        assertFails(report, "shuffle(freeps.deck: boromir) was never used");
        assertEquals(StepStatus.MATCHED, report.steps.get("1"));
        assertEquals("A step that was given a name carries it beside its number", StepStatus.UNREACHED, report.steps.get("2 (leftover)"));
    }

    @Test
    public void aStrictStepNeedsAnAnswerTheRunnerCanGive() throws Exception {
        assertFails(ScenarioTesting.run("""
                	steps: [
                		{
                			when: freeps, action
                		}
                	]
                """), "step 1 has no answer for the runner");
    }

    @Test
    public void aGuidedScriptIsProvedAsFarAsItGoes() throws Exception {
        ScenarioRunReport report = ScenarioTesting.run("""
                	coverage: guided
                	opponent: {
                		fallback: pass
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: pass
                		}
                	]
                """);
        assertTrue(report.toString(), report.passed());
        assertEquals(RunnerLevel.PREFIX, report.reached);
        assertEquals(RunnerLevel.PREFIX, report.required);
        assertEquals(StepStatus.MATCHED, report.steps.get("1"));
    }

    private static ScenarioRunReport runAwkward(String rest) throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(ScenarioTesting.awkward(rest));
        return ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
    }

    @Test
    public void AQuestionAskedFromInsideAnAnswerBelongsToTheNextStep() throws Exception {
        //Using Sam shows the deck, and choosing to stop looking is what asks which card to take.  The second question
        // has been asked before the game has finished taking the answer to the first.
        ScenarioRunReport report = runAwkward("""
                	setup: {
                		freeps: {
                			characters: sam
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			answer: use(sam)
                		}
                		{
                			when: freeps, arbitrary, text(inspect)
                			answer: pass
                		}
                		{
                			when: freeps, arbitrary, text(Choose cards from deck)
                			answer: choose(bill)
                		}
                		{
                			when: freeps, cards, text(Attach Bill)
                			answer: choose(sam)
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			answer: pass
                			end: complete
                		}
                	]
                """);
        assertTrue(report.toString(), report.passed());
        assertEquals(List.of("1", "2", "3", "4", "5"), report.decisions.stream().map(ScenarioRunReport.Decision::step).toList());
    }

    private static final String REVEAL_TO_BOTH = """
            	setup: {
            		freeps: {
            			support: proudfoot
            		}
            	}
            	steps: [
            		{
            			when: freeps, action, phase(fellowship)
            			answer: use(proudfoot)
            		}
            		{
            			when: FIRST, arbitrary, text(revealed)
            			answer: pass
            		}
            		{
            			when: SECOND, arbitrary, text(revealed)
            			answer: pass
            		}
            		{
            			when: freeps, action, phase(fellowship)
            			answer: pass
            			end: complete
            		}
            	]
            """;

    @Test
    public void WhenBothSeatsAreAskedAtOnceTheScriptSaysWhoseTurnItIs() throws Exception {
        //The game asks the Free Peoples seat first.  The script may take them in either order.
        ScenarioRunReport report = runAwkward(REVEAL_TO_BOTH.replace("FIRST", "freeps").replace("SECOND", "opponent"));
        assertTrue(report.toString(), report.passed());
        assertTrue(report.decisions.get(1).decision(), report.decisions.get(1).decision().startsWith("freeps, arbitrary"));
        assertTrue(report.decisions.get(2).decision(), report.decisions.get(2).decision().startsWith("shadow, arbitrary"));

        report = runAwkward(REVEAL_TO_BOTH.replace("FIRST", "opponent").replace("SECOND", "freeps"));
        assertTrue(report.toString(), report.passed());
        assertTrue(report.decisions.get(1).decision(), report.decisions.get(1).decision().startsWith("shadow, arbitrary"));
        assertTrue(report.decisions.get(2).decision(), report.decisions.get(2).decision().startsWith("freeps, arbitrary"));
        assertEquals(List.of("1", "2", "3", "4"), report.decisions.stream().map(ScenarioRunReport.Decision::step).toList());
    }

    /** A pause that ends in a choice, and a step for each thing that can be chosen. */
    private static final String CHOICE = """
            	setup: {
            		freeps: {
            			hand: gandalf, aragorn
            		}
            	}
            	steps: [
            		{
            			pause: true
            			when: freeps, action, phase(fellowship)
            			messages: [
            				Somebody is coming with us.
            				{
            					text: Who?
            					choices: {
            						wizard: Gandalf
            						king: Aragorn
            					}
            				}
            			]
            		}
            		{
            			if: wizard
            			when: freeps, action, phase(fellowship)
            			allow: play(gandalf)
            		}
            		{
            			unless: wizard
            			when: freeps, action, phase(fellowship)
            			allow: play(ARAGORN)
            		}
            		{
            			when: freeps, action, phase(fellowship)
            			allow: pass
            			end: complete
            		}
            	]
            """;

    @Test
    public void aPauseIsReadAndItsDecisionGoesToTheStepBehindIt() throws Exception {
        ScenarioTable table = new ScenarioTable(ScenarioTesting.parse(CHOICE.replace("ARAGORN", "aragorn")), ScenarioTesting.library(),
                ScenarioTesting.format("scenario"), HUMAN, OPPONENT);
        ScenarioSession session = new ScenarioSession(table);
        table.Start();

        //The pause is ruled on, and the decision it is read over is still the game's to ask
        ScenarioSession.Ruling ruling = session.Ruling(HUMAN);
        assertEquals(1, ruling.step().number);
        assertEquals(List.of("wizard", "king"), List.copyOf(ScenarioSession.ChoicesOf(ruling.step()).keySet()));
        assertNull(ruling.answer());
        AwaitingDecision decision = ruling.decision();

        assertFalse("Nothing but the pause in front of them can be read", session.Read(HUMAN, 2, "king"));
        assertTrue(session.Read(HUMAN, 1, "king"));
        assertEquals(java.util.Set.of("king"), session.Director().flags());

        //The same decision, now belonging to the step that the choice leads to
        ruling = session.Ruling(HUMAN);
        assertSame(decision, ruling.decision());
        assertEquals(3, ruling.step().number);
        assertEquals(StepStatus.BYPASSED, session.Director().statuses().get("2"));
        assertEquals(1, ruling.shown().allowed().size());

        //...and written down, for a game that follows this one
        assertTrue(table.Journal().entries().stream().anyMatch(entry ->
                entry.kind() == com.gempukku.lotro.game.GameJournal.Kind.NOTE && entry.value().equals("1:king")));
    }

    @Test
    public void everyWayThroughAScenarioIsPlayed() throws Exception {
        ScenarioRunReport report = ScenarioTesting.run(CHOICE.replace("ARAGORN", "aragorn"));
        assertTrue(report.toString(), report.passed());
        assertEquals(RunnerLevel.FULL, report.reached);
        //Each way bypasses one of the two steps; between them they reach both
        assertEquals(StepStatus.MATCHED, report.steps.get("2"));
        assertEquals(StepStatus.MATCHED, report.steps.get("3"));
        assertEquals(2, report.decisions.stream().filter(decision -> decision.number() < 0 && decision.decision().startsWith("Way through")).count());
        assertTrue(report.toString(), report.toString().contains("says: Who?"));

        //A mistake down one way is found, and said to be down that way
        assertFails(ScenarioTesting.run(CHOICE.replace("ARAGORN", "boromir")), "[choosing king] step 3");

        //A step that no way reaches is a mistake
        assertFails(ScenarioTesting.run(CHOICE.replace("ARAGORN", "aragorn").replace("unless: wizard", "if: wizard, king")), "was never reached");
    }

    @Test
    public void thePlayerIsAskedToPassWhereTheScriptSpeaksOverThePass() throws Exception {
        //Every phase is passed for the player, and the opponent is never asked when it has nothing to play.  A step
        // that is only that pass is used up by it with nobody asked; one with something to say has it asked for.
        ScenarioRunReport report = ScenarioTesting.run("""
                	options: {
                		autoPass: true
                	}
                	setup: {
                		shadow: {
                			minions: runner1
                		}
                	}
                	steps: [
                		{
                			when: opponent, action, phase(shadow)
                			answer: pass
                		}
                		{
                			when: freeps, action, phase(maneuver)
                			allow: pass
                		}
                		{
                			when: freeps, action, phase(archery)
                			messages: [
                				No arrows this time.
                			]
                			allow: pass
                			end: complete
                		}
                	]
                """);
        assertTrue(report.toString(), report.passed());
        List<String> asked = report.decisions.stream().map(ScenarioRunReport.Decision::decision).filter(d -> !d.trim().startsWith("says")).toList();
        assertEquals(asked.toString(), 1, asked.size());
        assertTrue(asked.get(0), asked.get(0).startsWith("freeps, action \"Play Archery action or Pass\""));
        assertTrue(report.toString(), report.toString().contains("matched   1"));
        assertTrue(report.toString(), report.toString().contains("matched   2"));

        //Without its message the last step would still be asked for: it ends the scenario
        report = ScenarioTesting.run("""
                	options: {
                		autoPass: true
                	}
                	setup: {
                		shadow: {
                			minions: runner1
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(archery)
                			allow: pass
                			end: complete
                		}
                	]
                """);
        assertTrue(report.toString(), report.passed());
        assertEquals(1, report.decisions.stream().filter(d -> !d.decision().trim().startsWith("says")).count());
    }

    @Test
    public void theOpponentFollowsItsStandingOrdersWhereNoStepSaysWhatToDo() throws Exception {
        String orders = """
                	options: {
                		autoPass: true
                	}
                	opponent: {
                		fallback: pass
                		orders: [
                			{
                				when: action, phase(shadow)
                				answer: play(marksman), play(runner1)
                			}
                		]
                	}
                	setup: {
                		shadow: {
                			hand: runner1, runner2
                		}
                	}
                	steps: [
                		{
                			when: freeps, assign
                			allow: ASSIGNMENT
                			end: complete
                		}
                	]
                """;
        //The Marksman is not in hand, so the order's second answer is the one taken; and it is taken only once,
        // because after that the Runner is not on offer either
        ScenarioRunReport report = ScenarioTesting.run(orders.replace("ASSIGNMENT", "assign(frodo: runner1)"));
        assertTrue(report.toString(), report.passed());
        assertTrue(report.toString(), report.decisions.stream().anyMatch(decision ->
                decision.note() != null && decision.note().contains("standing orders")));

        //An order is the opponent's
        try {
            ScenarioTesting.parse(orders.replace("when: action, phase(shadow)", "when: freeps, action"));
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("orders are the opponent's"));
        }
    }

    @Test
    public void anAllowedAnswerCanBeOneThatMayNotBeOnOffer() throws Exception {
        String either = """
                	setup: {
                		freeps: {
                			hand: gandalf
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: ALLOW
                			answer: play(gandalf)
                			end: complete
                		}
                	]
                """;
        assertTrue(ScenarioTesting.run(either.replace("ALLOW", "play(gandalf), play(aragorn)")).passed());
        //(the ? that once said so for one answer is still read)
        assertTrue(ScenarioTesting.run(either.replace("ALLOW", "play(gandalf)?, play(aragorn)?")).passed());
        //...but something it allows has to be
        assertFails(ScenarioTesting.run(either.replace("ALLOW", "play(aragorn), play(boromir)")), "allows nothing that this decision offers");
        //With no answer of its own, the script plays the first of them that is on offer
        assertTrue(ScenarioTesting.run(either.replace("ALLOW", "play(aragorn), play(gandalf)").replace("answer: play(gandalf)\n", "")).passed());
    }

    @Test
    public void thePersonMayTakeAnAllowedRouteTheRunnerDoesNot() throws Exception {
        //Two cards, either first.  The runner plays Gandalf and then Aragorn.
        ScenarioTable table = new ScenarioTable(ScenarioTesting.parse("""
                	setup: {
                		freeps: {
                			hand: gandalf, aragorn
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: play(gandalf)?, play(aragorn)?
                			answer: play(gandalf)
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			allow: play(gandalf)?, play(aragorn)?
                			answer: play(aragorn)
                		}
                	]
                """), ScenarioTesting.library(), ScenarioTesting.format("scenario"), HUMAN, OPPONENT);
        ScenarioSession session = new ScenarioSession(table);
        table.Start();

        //A person plays Aragorn first, which the step allows
        ScenarioSession.Ruling ruling = session.Ruling(HUMAN);
        assertEquals(2, ruling.shown().allowed().size());
        table.PlayerDecided(HUMAN, ruling.shown().allowed().get(1));
        assertTrue(table.Card("aragorn").getZone().isInPlay());

        //The runner's answer to the second step (Aragorn) is no longer on offer.  That is nothing to a person:
        // the step still applies, and still allows only what it allows
        ruling = session.Ruling(HUMAN);
        assertEquals(2, ruling.step().number);
        assertNull(ruling.problem());
        assertNotNull(ruling.noAnswer());
        assertNull(ruling.answer());
        assertEquals("Only Gandalf is left to play, and passing is still not allowed", 1, ruling.shown().allowed().size());
        assertFalse(ruling.shown().passAllowed());
    }


    /** The scenario in src/test/resources with a block in it: an assignment and two skirmishes, either fought first. */
    private static String twoSkirmishes() throws Exception {
        return java.nio.file.Files.readString(java.nio.file.Path.of("src/test/resources/scenarios/two-skirmishes.hjson"));
    }

    @Test
    public void aBlockTakesItsDecisionsInWhateverOrderTheyCome() throws Exception {
        String scenario = twoSkirmishes();
        //The runner fights Gandalf's skirmish first.  Every step of the block is used, the block is one place in the
        // script, and its steps are told apart in the report by where they come in it.
        ScenarioRunReport report = ScenarioRunner.run(ScenarioParser.parse(scenario), ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.passed());
        assertEquals(StepStatus.MATCHED, report.steps.get("1 (fights)"));
        assertEquals(StepStatus.MATCHED, report.steps.get("1.1"));
        assertEquals(StepStatus.MATCHED, report.steps.get("1.2"));
        assertEquals(StepStatus.MATCHED, report.steps.get("1.3 (aragornFights)"));
        assertEquals(StepStatus.MATCHED, report.steps.get("1.4"));
        assertEquals(StepStatus.MATCHED, report.steps.get("2"));
        List<String> order = report.decisions.stream().map(ScenarioRunReport.Decision::step).filter(java.util.Objects::nonNull).toList();
        assertEquals("Gandalf's skirmish, then Aragorn's", List.of("1.1", "1.2", "1.4", "1.2", "1.3 (aragornFights)", "2"), order);

        //The other way about, with nothing else changed: the same steps, in the order the game asks for them
        report = ScenarioRunner.run(ScenarioParser.parse(scenario.replace("answer: choose(gandalf), choose(aragorn)", "answer: choose(aragorn), choose(gandalf)")),
                ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.passed());
        order = report.decisions.stream().map(ScenarioRunReport.Decision::step).filter(java.util.Objects::nonNull).toList();
        assertEquals("Aragorn's skirmish, then Gandalf's", List.of("1.1", "1.2", "1.3 (aragornFights)", "1.2", "1.4", "2"), order);

        //A step the block never needed is not a failure: with Aragorn kept out of it, his step has nothing to apply to
        report = ScenarioRunner.run(ScenarioParser.parse(scenario
                        .replace("allow: assign(aragorn: runner1, gandalf: runner2)", "allow: assign(gandalf: runner1)")),
                ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.passed());
        assertEquals(StepStatus.SKIPPED, report.steps.get("1.3 (aragornFights)"));
    }

    @Test
    public void aStepCanBeForWhatComesAfterOneSkirmish() throws Exception {
        //Said between Gandalf's skirmish and the next, whichever order they come in
        String scenario = twoSkirmishes().replace("anyOrder: [", "skirmishes: true\n\t\t\tanyOrder: [").replace(
                "\t\t\t\t{\n\t\t\t\t\twhen: freeps, cards, text(next skirmish)",
                "\t\t\t\t{\n\t\t\t\t\tonce: true\n\t\t\t\t\twhen: freeps, cards, after(gandalf)\n\t\t\t\t\tmessages: [\n\t\t\t\t\t\tGandalf has won his.\n\t\t\t\t\t]\n"
                        + "\t\t\t\t\tallow: choose(aragorn)\n\t\t\t\t}\n\t\t\t\t{\n\t\t\t\t\twhen: freeps, cards, text(next skirmish)");
        ScenarioDefinition def = ScenarioParser.parse(scenario);
        assertTrue(def.steps.getFirst().skirmishes);
        assertEquals("gandalf", def.steps.getFirst().children.get(1).when.after);
        String written = ScenarioWriter.toJson(def).toString();
        assertTrue(written, written.contains("\"skirmishes\":true"));
        assertTrue(written, written.contains("after(gandalf)"));

        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.passed());
        List<String> order = report.decisions.stream().map(ScenarioRunReport.Decision::step).filter(java.util.Objects::nonNull).toList();
        assertEquals("Gandalf's skirmish, what comes after it, then Aragorn's", List.of("1.1", "1.3", "1.5", "1.2", "1.4 (aragornFights)", "2"), order);

        report = ScenarioRunner.run(ScenarioParser.parse(scenario.replace("answer: choose(gandalf), choose(aragorn)", "answer: choose(aragorn), choose(gandalf)")),
                ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.toString(), report.passed());
        order = report.decisions.stream().map(ScenarioRunReport.Decision::step).filter(java.util.Objects::nonNull).toList();
        assertEquals("Aragorn's first: nothing is said after it, and Gandalf's comes last", List.of("1.1", "1.3", "1.4 (aragornFights)", "1.3", "1.5", "2"), order);
    }

    @Test
    public void whatIsSaidAfterASkirmishIsSaidAtTheFirstDecisionAfterItEvenWhenTheBlockIsEnding() throws Exception {
        //Two pauses, one after each skirmish, written as they once were: for the choice of the next skirmish.  The
        // one after the last skirmish has no such choice to wait for: it is read at the first decision of the Regroup
        // phase, the one that ends the block.
        String pauses = "\t\t\t\t{\n\t\t\t\t\tpause: true\n\t\t\t\t\tonce: true\n\t\t\t\t\twhen: freeps, cards, after(gandalf), text(next skirmish)\n"
                + "\t\t\t\t\tmessages: [\n\t\t\t\t\t\tGandalf has won his.\n\t\t\t\t\t]\n\t\t\t\t}\n"
                + "\t\t\t\t{\n\t\t\t\t\tpause: true\n\t\t\t\t\tonce: true\n\t\t\t\t\twhen: freeps, cards, after(aragorn), text(next skirmish)\n"
                + "\t\t\t\t\tmessages: [\n\t\t\t\t\t\tAragorn has won his.\n\t\t\t\t\t]\n\t\t\t\t}\n";
        String scenario = twoSkirmishes().replace("anyOrder: [", "skirmishes: true\n\t\t\tanyOrder: [").replace(
                "\t\t\t\t{\n\t\t\t\t\twhen: freeps, cards, text(next skirmish)", pauses + "\t\t\t\t{\n\t\t\t\t\twhen: freeps, cards, text(next skirmish)");
        ScenarioDefinition def = ScenarioParser.parse(scenario);
        Step afterGandalf = def.steps.getFirst().children.get(1);
        assertEquals("gandalf", afterGandalf.when.after);
        assertNull("The choice of the next skirmish is no longer what it waits for", afterGandalf.when.text);
        assertNull(afterGandalf.when.decision);
        assertEquals("freeps, after(gandalf)", afterGandalf.when.source);

        for (String first : List.of("gandalf", "aragorn")) {
            String second = first.equals("gandalf") ? "aragorn" : "gandalf";
            ScenarioTable table = new ScenarioTable(ScenarioParser.parse(scenario), ScenarioTesting.library(), ScenarioTesting.format("scenario"), HUMAN, OPPONENT);
            ScenarioSession session = new ScenarioSession(table);
            table.Start();
            List<String> read = new java.util.ArrayList<>();
            for (int guard = 0; guard < 40 && session.Director().currentStep() != null && session.Director().currentStep().number == 1; guard++) {
                ScenarioSession.Ruling ruling = session.Ruling(HUMAN);
                if (ruling == null) {
                    ScenarioSession.Ruling theirs = session.Ruling(OPPONENT);
                    table.PlayerDecided(OPPONENT, theirs != null && theirs.answer() != null ? theirs.answer() : "");
                    continue;
                }
                if (ruling.step().pause) {
                    read.add(ruling.shown().messages().getFirst().text());
                    assertTrue(session.Read(HUMAN, ruling.step().number, null));
                    continue;
                }
                if ("next skirmish".equals(ruling.step().when.text))
                    table.PlayerDecided(HUMAN, String.valueOf(table.Card(read.isEmpty() ? first : second).getCardId()));
                else
                    table.PlayerDecided(HUMAN, ruling.answer() != null ? ruling.answer() : ruling.shown().allowed() != null && !ruling.shown().allowed().isEmpty() ? ruling.shown().allowed().getFirst() : "");
            }
            String name = first.equals("gandalf") ? "Gandalf" : "Aragorn", other = second.equals("gandalf") ? "Gandalf" : "Aragorn";
            assertEquals(first + " first", List.of(name + " has won his.", other + " has won his."), read);
            assertEquals("The script is at the Regroup phase's step", "2", session.Ruling(HUMAN).step().label());
        }
    }

    @Test
    public void aChoiceCanPickACardByItsSkirmishAndNarrowItWithCardFilters() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(twoSkirmishes());
        //Each card it names, or picks by its skirmish, begins a pick; numbers and filters narrow the pick before them
        List<ScenarioDefinition.Pick> picks = ScenarioParser.parsePicks(def,
                Terms.parse("choose(skirmishing(aragorn), title(Goblin Runner), 2, frodo, gandalf, not(exhausted))", "test").getFirst(), "test");
        assertEquals(3, picks.size());
        assertTrue(picks.get(0).skirmishing());
        assertEquals("aragorn", picks.get(0).against());
        assertEquals("title(Goblin Runner)", picks.get(0).filter());
        assertEquals(2, picks.get(0).ordinal());
        assertTrue(picks.get(1).plain());
        assertEquals("gandalf", picks.get(2).alias());
        assertEquals("not(exhausted)", picks.get(2).filter());
        try {
            ScenarioParser.parse(twoSkirmishes().replace("allow: choose(aragorn)?, choose(gandalf)?", "allow: choose(aragorn, exhaused)"));
            fail("A filter the card builder does not know is a mistake");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("neither a card of the scenario nor a card filter"));
        }

        //not(exhausted): Aragorn cannot be picked once he is exhausted
        String narrowed = twoSkirmishes().replace("allow: choose(aragorn)?, choose(gandalf)?", "allow: choose(aragorn, not(exhausted)), choose(gandalf)");
        for (boolean exhausted : List.of(false, true)) {
            String text = exhausted ? narrowed.replace("\t\tshadow: {\n\t\t\tminions: runner1, runner2\n\t\t}",
                    "\t\tshadow: {\n\t\t\tminions: runner1, runner2\n\t\t}\n\t\twounds: {\n\t\t\taragorn: 3\n\t\t}") : narrowed;
            ScenarioTable table = new ScenarioTable(ScenarioParser.parse(text), ScenarioTesting.library(), ScenarioTesting.format("scenario"), HUMAN, OPPONENT);
            ScenarioSession session = new ScenarioSession(table);
            table.Start();
            ScenarioSession.Ruling ruling = session.Ruling(HUMAN);
            assertEquals("1.1", ruling.step().label());
            table.PlayerDecided(HUMAN, ruling.answer() != null ? ruling.answer() : ruling.shown().allowed().getFirst());
            ruling = session.Ruling(HUMAN);
            assertEquals("1.2", ruling.step().label());
            assertEquals(exhausted ? List.of(String.valueOf(table.Card("gandalf").getCardId()))
                    : List.of(String.valueOf(table.Card("aragorn").getCardId()), String.valueOf(table.Card("gandalf").getCardId())), ruling.shown().allowed());

            if (!exhausted) {
                //In Aragorn's skirmish: the minion skirmishing him, picked by that
                table.PlayerDecided(HUMAN, String.valueOf(table.Card("aragorn").getCardId()));
                PhysicalCard runner = table.Card("runner1");
                List<PhysicalCard> all = List.of(table.Card("runner1"), table.Card("runner2"), table.Card("gandalf"));
                ScenarioDefinition.Pick against = ScenarioParser.parsePicks(def, Terms.parse("choose(skirmishing(aragorn))", "test").getFirst(), "test").getFirst();
                assertEquals(List.of(runner), table.Picked(against, HUMAN, all));
                ScenarioDefinition.Pick any = ScenarioParser.parsePicks(def, Terms.parse("choose(skirmishing, minion)", "test").getFirst(), "test").getFirst();
                assertEquals(List.of(runner), table.Picked(any, HUMAN, all));
                ScenarioDefinition.Pick second = ScenarioParser.parsePicks(def, Terms.parse("choose(skirmishing(gandalf))", "test").getFirst(), "test").getFirst();
                assertEquals("Gandalf's skirmish is not being fought", List.of(), table.Picked(second, HUMAN, all));
            }
        }
    }

    @Test
    public void sitesAreNamedAsCardsAre() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(twoSkirmishes());
        assertEquals(List.of("site1", "site2", "site3", "site4"), def.siteAliases.get(Seat.FREEPS));
        assertEquals(List.of("site1opp", "site2opp", "site3opp", "site4opp"), def.siteAliases.get(Seat.SHADOW));
        assertTrue("A path with its sites by their number is written as a list", ScenarioWriter.toJson(def).toString().contains("\"freeps\":\"1_323, 1_331, 1_340, 2_118\""));

        //Named otherwise, the path is written with its names
        String named = twoSkirmishes().replace("freeps: 1_323, 1_331, 1_340, 2_118", "freeps: {\n\t\t\tshire: 1_323\n\t\t\tbree: 1_331\n\t\t\tsite3: 1_340\n\t\t\tsite4: 2_118\n\t\t}");
        def = ScenarioParser.parse(named);
        assertEquals(List.of("shire", "bree", "site3", "site4"), def.siteAliases.get(Seat.FREEPS));
        assertEquals(List.of("1_323", "1_331", "1_340", "2_118"), def.sites.get(Seat.FREEPS));
        assertEquals("bree", ScenarioParser.parse(ScenarioWriter.toJson(def).toString()).siteAliases.get(Seat.FREEPS).get(1));

        ScenarioTable table = new ScenarioTable(def, ScenarioTesting.library(), ScenarioTesting.format("scenario"), HUMAN, OPPONENT);
        table.Start();
        assertTrue(table.Problems().toString(), table.Problems().isEmpty());
        assertEquals(2, table.Card("bree").getBlueprint().getSiteNumber());
        assertEquals(HUMAN, table.Card("bree").getOwner());
        assertEquals(OPPONENT, table.Card("site2opp").getOwner());
        assertEquals(4, table.Card("site4opp").getBlueprint().getSiteNumber());
        try {
            ScenarioParser.parse(named.replace("bree: 1_331", "gandalf: 1_331"));
            fail("A site cannot have a card's name");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("already the name"));
        }
    }

    @Test
    public void aBlocksStepsApplyOnceOrForAsLongAsItLasts() throws Exception {
        ScenarioTable table = new ScenarioTable(ScenarioParser.parse(twoSkirmishes()), ScenarioTesting.library(), ScenarioTesting.format("scenario"), HUMAN, OPPONENT);
        ScenarioSession session = new ScenarioSession(table);
        table.Start();
        ScenarioDirector director = session.Director();

        //The assignment: the block's first decision, and so the place the script arrives at
        ScenarioSession.Ruling ruling = session.Ruling(HUMAN);
        assertEquals("1.1", ruling.step().label());
        assertTrue(director.arrives(ruling.step()));
        assertEquals(1, ruling.shown().messages().size());
        table.PlayerDecided(HUMAN, ruling.answer());

        //A person takes Aragorn's skirmish first, which the step allows and the runner does not do
        ruling = session.Ruling(HUMAN);
        assertEquals("1.2", ruling.step().label());
        assertFalse("The block has been arrived at already", director.arrives(ruling.step()));
        assertEquals(2, ruling.shown().allowed().size());
        table.PlayerDecided(HUMAN, String.valueOf(table.Card("aragorn").getCardId()));

        //His skirmish is the step that says so, though it comes after the picking in the block and before the step
        // for any other skirmish
        ruling = session.Ruling(HUMAN);
        assertEquals("1.3 (aragornFights)", ruling.step().label());
        assertEquals("Aragorn's skirmish.", ruling.shown().messages().getFirst().text());
        table.PlayerDecided(HUMAN, "");

        //The picking step applies again, for as long as the block lasts; what it allows is what is still on offer
        ruling = session.Ruling(HUMAN);
        assertEquals("1.2", ruling.step().label());
        assertTrue(director.isContinuing(ruling.step()));
        assertEquals(1, ruling.shown().allowed().size());
        table.PlayerDecided(HUMAN, ruling.shown().allowed().getFirst());

        //Gandalf's skirmish: the step for Aragorn's is 'once' and spent, and would not fit anyway
        ruling = session.Ruling(HUMAN);
        assertEquals("1.4", ruling.step().label());
        table.PlayerDecided(HUMAN, "");

        //The regroup phase is where the block ends, and the step after it takes over
        ruling = session.Ruling(HUMAN);
        assertEquals("2", ruling.step().label());
        assertEquals(2, director.currentStep().number);
    }


    @Test
    public void aScenarioCanEndInsideABlock() throws Exception {
        //The player is passed through every phase.  The step for Aragorn's skirmish names no phase, and is asked for
        // all the same: a card is only ever fighting in a skirmish.  It ends the scenario, with one of the block's
        // steps never having been needed; that is not a step the scenario failed to reach.
        String ending = """
                	options: {
                		autoPass: fellowship, maneuver, archery, assignment, skirmish, regroup
                	}
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			characters: aragorn, gandalf
                		}
                		shadow: {
                			minions: runner1, runner2
                		}
                	}
                	steps: [
                		{
                			when: WHEN
                			until: phase(regroup)
                			anyOrder: [
                				{
                					once: true
                					when: freeps, assign
                					answer: assign(aragorn: runner1, gandalf: runner2)
                				}
                				{
                					when: freeps, cards, text(next skirmish)
                					answer: choose(gandalf), choose(aragorn)
                				}
                				{
                					when: opponent, cards
                					answer: choose(runner1)
                				}
                				{
                					id: last
                					when: LAST
                					answer: pass
                					end: complete
                				}
                			]
                		}
                	]
                """;
        //(a second site with nothing to do in a skirmish, so that being asked there is the script's doing)
        java.util.function.Function<String, ScenarioRunReport> run = rest -> {
            try {
                return ScenarioRunner.run(ScenarioParser.parse(ScenarioTesting.scenario(rest).replace("1_331", "1_329")),
                        ScenarioTesting.library(), ScenarioTesting.format("scenario"));
            } catch (Exception exp) {
                throw new RuntimeException(exp);
            }
        };
        ScenarioRunReport report = run.apply(ending.replace("WHEN", "turn(1)").replace("LAST", "freeps, fighting(aragorn)"));
        assertTrue(report.toString(), report.passed());
        assertEquals(StepStatus.MATCHED, report.steps.get("1.4 (last)"));
        assertEquals(StepStatus.UNREACHED, report.steps.get("1.3"));
        assertTrue(report.toString(), report.warnings.isEmpty());
        List<String> order = report.decisions.stream().map(ScenarioRunReport.Decision::step).filter(java.util.Objects::nonNull).toList();
        assertEquals("Nothing is asked in Gandalf's skirmish, which no step is about", List.of("1.1", "1.2", "1.2", "1.4 (last)"), order);

        //A phase the block names for all its steps is one its steps need not name: the pass is asked for in it
        report = run.apply(ending.replace("WHEN", "phase(skirmish)").replace("LAST", "freeps, action")
                .replace("steps: [", "steps: [\n\t\t{\n\t\t\twhen: freeps, assign\n\t\t\tanswer: assign(aragorn: runner1, gandalf: runner2)\n\t\t}\n\t\t{\n\t\t\twhen: freeps, cards, text(next skirmish)\n\t\t\tanswer: choose(gandalf)\n\t\t}"));
        assertTrue(report.toString(), report.passed());
        order = report.decisions.stream().map(ScenarioRunReport.Decision::step).filter(java.util.Objects::nonNull).toList();
        assertEquals("The first skirmish's pass is the block's, though its step says only whose it is", List.of("1", "2", "3.4 (last)"), order);
    }
}
