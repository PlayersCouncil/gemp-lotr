package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.scenario.DecisionKinds.Kind;
import com.gempukku.lotro.scenario.ScenarioDefinition.When;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import org.hjson.JsonValue;
import org.junit.Test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * The 'when' terms for what a decision is for (nextSkirmish, archeryWound...), each met in a real game: the game's
 * wording is what they are recognised by, so a change to it fails here and not in somebody's scenario.
 */
public class ScenarioDecisionKindsTest {
    /** A 'when' as a step would have it. */
    private static When when(ScenarioDefinition def, String terms) throws ScenarioFormatException {
        return ScenarioParser.parseStep(def, JsonValue.readHjson("{when: \"" + terms + "\"}"), 1).when;
    }

    /** How the fixtures say each kind with text(...), which has to fit the same decisions as the term. */
    private static final Map<Kind, String> AS_TEXT = Map.of(
            Kind.NEXT_SKIRMISH, "text(next skirmish)",
            Kind.ARCHERY_WOUND, "text(archery wound)",
            Kind.RECONCILE, "text(reconcile)",
            Kind.SANCTUARY, "text(sanctuary healing)",
            Kind.ANOTHER_MOVE, "text(another move)",
            Kind.OPTIONAL_RESPONSES, "text(optional responses)",
            Kind.REQUIRED_RESPONSES, "text(required responses)",
            Kind.CHOOSE_SITE, "text(choose site to play)");

    /**
     * Plays a game to its end (or to where 'enough' says), checking at every decision that each kind's term fits it
     * exactly when it is of that kind, that not(kind) fits it exactly when it is not, and that the fixtures' text(...)
     * for it fits the same decisions.
     * @return The kinds that were met.
     */
    static Set<Kind> meet(ScenarioDefinition def, java.util.function.BiPredicate<ScenarioTable, Ruling> enough) throws Exception {
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        ScenarioDirector director = session.Director();
        Map<Kind, When> terms = new EnumMap<>(Kind.class);
        Map<Kind, When> nots = new EnumMap<>(Kind.class);
        Map<Kind, When> texts = new EnumMap<>(Kind.class);
        for (Kind kind : Kind.values()) {
            terms.put(kind, when(def, kind.term));
            nots.put(kind, when(def, "not(" + kind.term + ")"));
            if (AS_TEXT.containsKey(kind))
                texts.put(kind, when(def, AS_TEXT.get(kind)));
        }
        Set<Kind> met = EnumSet.noneOf(Kind.class);
        ScenarioOpponentRulesTest.playTo(session, ruling -> {
            Kind kind = DecisionKinds.of(ruling.decision());
            if (session.Table().IsOpening(ruling.decision()))
                return enough.test(session.Table(), ruling);
            if (kind != null)
                met.add(kind);
            for (Kind each : Kind.values()) {
                String said = each.term + " at \"" + ruling.decision().getText() + "\"";
                assertEquals(said, kind == each, director.matches(terms.get(each), ruling.player(), ruling.decision()));
                assertEquals("not(" + said + ")", kind != each, director.matches(nots.get(each), ruling.player(), ruling.decision()));
                if (texts.containsKey(each))
                    assertEquals(AS_TEXT.get(each) + " and " + said, director.matches(texts.get(each), ruling.player(), ruling.decision()),
                            director.matches(terms.get(each), ruling.player(), ruling.decision()));
            }
            return enough.test(session.Table(), ruling);
        }, null);
        return met;
    }

    @Test
    public void aTurnOfSkirmishesArcheryReconcilingMovingAndASanctuary() throws Exception {
        //Three minions on the table, one an archer; three companions to fight them, one wounded; Gandalf, the Grey
        // Pilgrim, whose draw at the start of a turn is an optional trigger; and a second move, to a sanctuary
        ScenarioDefinition def = ScenarioParser.parse(ScenarioTesting.scenario("""
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
                			characters: aragorn, boromir, pilgrim
                		}
                		shadow: {
                			minions: runner1, runner2, bowman
                			hand: marksman
                		}
                		wounds: {
                			aragorn: 1
                		}
                	}
                	steps: [
                		{
                			when: freeps, assign
                			answer: assign(aragorn: runner1, boromir: runner2, pilgrim: bowman)
                		}
                		{
                			when: freeps, anotherMove
                			answer: option(Yes)
                		}
                	]
                """).replace("boromir: 0_65", "boromir: 0_65\n\t\t\tpilgrim: 2_122"));
        //(to the first decision of the second turn's phases, past its start at the sanctuary)
        Set<Kind> met = meet(def, (table, ruling) -> table.gameState().getTurnNumber() >= 2 && table.GetCurrentPhase() != Phase.BETWEEN_TURNS);
        assertEquals(EnumSet.of(Kind.NEXT_SKIRMISH, Kind.ARCHERY_WOUND, Kind.RECONCILE, Kind.ANOTHER_MOVE, Kind.SANCTUARY,
                Kind.OPTIONAL_RESPONSES, Kind.PHASE_ACTIONS), met);
    }

    @Test
    public void requiredTriggersThatComeTogetherAreOrdered() throws Exception {
        //Lurtz, with his sword, beats Aragorn: the wounds of losing and the sword's discard come at once (tutorial 2)
        ScenarioDefinition def = ScenarioParser.parse(ScenarioTesting.scenario("""
                	options: {
                		autoPass: true
                	}
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: lurtz
                		}
                		attach: {
                			sword: lurtz
                		}
                	}
                	steps: [
                		{
                			when: freeps, assign
                			answer: assign(aragorn: lurtz)
                		}
                	]
                """).replace("marksman: 1_176", "marksman: 1_176\n\t\t\tlurtz: 1_127\n\t\t\tsword: 2_43"));
        Set<Kind> met = meet(def, (table, ruling) -> DecisionKinds.of(ruling.decision()) == Kind.REQUIRED_RESPONSES);
        assertTrue(met.toString(), met.contains(Kind.REQUIRED_RESPONSES));
    }

    @Test
    public void aSiteOfAnUnnumberedPathIsChosen() throws Exception {
        //The After Shadows vignette's opening: the Ranger of Westernesse, played, may play the fellowship's next site
        ScenarioDefinition def = ScenarioParser.parse("""
                {
                	id: test
                	title: Test
                	format: expanded
                	opponent: {
                		fallback: pass
                	}
                	cards: {
                		freeps: {
                			frodo: 10_121
                			ranger: 11_65
                		}
                		shadow: {
                			oppFrodo: 10_121
                		}
                	}
                	ringBearer: {
                		freeps: frodo
                		shadow: oppFrodo
                	}
                	sites: {
                		freeps: 1_320, 11_261, 11_249, 11_234
                		shadow: 11_236, 11_247, 11_242
                	}
                	rules: {
                		oneSided: true
                	}
                	setup: {
                		freeps: {
                			hand: ranger
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phaseActions, phase(fellowship)
                			answer: play(ranger)
                		}
                		{
                			when: freeps, optionalResponses
                			answer: use(ranger)
                		}
                	]
                }
                """);
        Set<Kind> met = meet(def, (table, ruling) -> DecisionKinds.of(ruling.decision()) == Kind.CHOOSE_SITE);
        assertTrue(met.toString(), met.containsAll(EnumSet.of(Kind.PHASE_ACTIONS, Kind.OPTIONAL_RESPONSES, Kind.CHOOSE_SITE)));
    }

    @Test
    public void kindsAreReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, cards, nextSkirmish
                			allow: choose(aragorn)
                		}
                		{
                			when: freeps, cards, phase(skirmish), not(nextSkirmish), not(archeryWound)
                			allow: pass
                		}
                		{
                			when: {
                				terms: freeps, choice, anotherMove
                				require: {
                					type: phase
                					phase: regroup
                				}
                			}
                			allow: option(No)
                		}
                		{
                			when: {
                				seat: freeps
                				kind: reconcile
                				require: {
                					type: phase
                					phase: regroup
                				}
                			}
                			allow: pass
                		}
                	]
                """);
        assertEquals(java.util.List.of(Kind.NEXT_SKIRMISH), def.steps.get(0).when.kinds);
        assertEquals(java.util.List.of(Kind.NEXT_SKIRMISH, Kind.ARCHERY_WOUND), def.steps.get(1).when.notKinds);
        assertEquals(java.util.List.of(Kind.ANOTHER_MOVE), def.steps.get(2).when.kinds);
        assertEquals(java.util.List.of(Kind.RECONCILE), def.steps.get(3).when.kinds);
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("when: freeps, cards, phase(skirmish), not(nextSkirmish), not(archeryWound)"));
        assertTrue(written, written.contains("kind: anotherMove"));
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());
        try {
            ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, not(skirmish)
                			allow: pass
                		}
                	]
                """);
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("nextSkirmish, archeryWound"));
        }
    }
}
