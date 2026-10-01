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
                			id: change
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
                			id: prepare
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
                			id: maneuver
                			when: freeps, action, phase(maneuver)
                			skipWhen: phase(regroup)
                		}
                		{
                			id: regroup
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
        assertEquals("regroup", matched.id);
        assertEquals(StepStatus.SKIPPED, director.statuses().get("maneuver"));
        assertEquals(StepStatus.MATCHED, director.statuses().get("regroup"));
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
                			id: play-a-minion
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
        assertTrue(problems.get(3), problems.get(3).startsWith("step 'a'.when.require:"));

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
                			id: impossible
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
                			id: only
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
        assertFails(report, "step 'leftover' was never reached");
        assertFails(report, "shuffle(freeps.deck: boromir) was never used");
        assertEquals(StepStatus.MATCHED, report.steps.get("only"));
        assertEquals(StepStatus.UNREACHED, report.steps.get("leftover"));
    }

    @Test
    public void aStrictStepNeedsAnAnswerTheRunnerCanGive() throws Exception {
        assertFails(ScenarioTesting.run("""
                	steps: [
                		{
                			id: open
                			when: freeps, action
                		}
                	]
                """), "step 'open' has no answer for the runner");
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
                			id: first
                			when: freeps, action, phase(fellowship)
                			allow: pass
                		}
                	]
                """);
        assertTrue(report.toString(), report.passed());
        assertEquals(RunnerLevel.PREFIX, report.reached);
        assertEquals(RunnerLevel.PREFIX, report.required);
        assertEquals(StepStatus.MATCHED, report.steps.get("first"));
    }
}
