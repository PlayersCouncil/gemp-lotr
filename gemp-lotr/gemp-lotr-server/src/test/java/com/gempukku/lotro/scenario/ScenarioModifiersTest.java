package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoKind;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoOp;
import com.gempukku.lotro.scenario.ScenarioDefinition.Modification;
import com.gempukku.lotro.scenario.ScenarioDefinition.ModifierTime;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Modifiers put on one card by hand (CardModifiers): do: modify(...) and unmodify(...), setup.modifiers, the file
 * written back, what the table keeps of them, and the game they change.
 */
public class ScenarioModifiersTest {
    private static DoOp op(String written) throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("");
        return ScenarioParser.parseDo(def, Terms.parse(written, "do").getFirst(), "do");
    }

    private static void refused(String written, String fragment) {
        try {
            op(written);
            fail("'" + written + "' was read");
        } catch (Exception exp) {
            assertTrue(exp.getMessage(), exp instanceof ScenarioFormatException && exp.getMessage().contains(fragment));
        }
    }

    @Test
    public void aModifierIsWrittenAsADo() throws Exception {
        DoOp strength = op("modify(aragorn, strength, +2, turn)");
        assertEquals(DoKind.MODIFY, strength.kind());
        assertEquals("aragorn", strength.card());
        assertEquals(new Modification("strength", 2, ModifierTime.TURN), strength.modification());

        assertEquals(new Modification("fierce", 0, ModifierTime.PHASE), op("modify(runner1, fierce, phase)").modification());
        assertEquals(new Modification("damage", 1, ModifierTime.REMOVED), op("modify(aragorn, damage, +1)").modification());
        assertEquals("A numbered keyword's number is 1 when none is written", new Modification("defender", 1, ModifierTime.REMOVED),
                op("modify(aragorn, defender)").modification());
        assertEquals(new Modification("cost", -1, ModifierTime.REMOVED), op("modify(gandalf, cost, -1)").modification());
        assertEquals(new Modification("vitality", 3, ModifierTime.REMOVED), op("modify(aragorn, Vitality, 3)").modification());

        DoOp all = op("unmodify(aragorn)");
        assertEquals(DoKind.UNMODIFY, all.kind());
        assertNull(all.target());
        assertEquals("strength", op("unmodify(aragorn, strength)").target());

        refused("modify(runner1, fierce, +1)", "fierce takes no number");
        refused("modify(aragorn, strength)", "needs the amount");
        refused("modify(aragorn, strength, 0)", "is no change");
        refused("modify(aragorn, strength, two)", "is neither a number nor how long");
        refused("modify(aragorn, strength, +2, skirmish)", "is neither a number nor how long");
        refused("modify(aragorn, strength, turn, +2)", "comes after how long it lasts");
        refused("modify(aragorn, damage, -1)", "a keyword is not taken away");
        refused("modify(aragorn, roaming)", "not something a card can be modified in");
        refused("modify(nobody, strength, +1)", "nobody");
        refused("modify(aragorn)", "says what is modified");
        refused("unmodify(aragorn, wisdom)", "not something a card can be modified in");
    }

    @Test
    public void theSetupCanPutModifiersOnCards() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	setup: {
                		modifiers: {
                			aragorn: strength +2, damage + 1
                			runner1: fierce turn
                		}
                	}
                """);
        assertEquals(List.of(new Modification("strength", 2, ModifierTime.REMOVED), new Modification("damage", 1, ModifierTime.REMOVED)),
                def.setup.modifiers.get("aragorn"));
        assertEquals(List.of(new Modification("fierce", 0, ModifierTime.TURN)), def.setup.modifiers.get("runner1"));

        for (String[] wrong : new String[][] {
                {"aragorn: fierce +2", "setup.modifiers.aragorn: 'fierce +2': fierce takes no number"},
                {"nobody: fierce", "nobody"},
                {"aragorn: strength", "needs the amount"}}) {
            try {
                ScenarioTesting.parse("\tsetup: {\n\t\tmodifiers: {\n\t\t\t" + wrong[0] + "\n\t\t}\n\t}\n");
                fail(wrong[0]);
            } catch (ScenarioFormatException exp) {
                assertTrue(exp.getMessage(), exp.getMessage().contains(wrong[1]));
            }
        }
    }

    @Test
    public void aModifierThatMeansNothingOnItsCardIsAMistake() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	setup: {
                		modifiers: {
                			servant1: strength +2
                			aragorn: fierce
                		}
                	}
                	steps: [
                		{
                			when: freeps, action
                			do: modify(runner1, resistance, +1)
                		}
                	]
                """);
        List<String> problems = ScenarioValidator.validate(def, ScenarioTesting.library());
        assertEquals(problems.toString(), 3, problems.size());
        assertTrue(problems.get(0), problems.get(0).startsWith("setup.modifiers.servant1: 'strength +2': strength is for a character"));
        assertTrue(problems.get(1), problems.get(1).startsWith("setup.modifiers.aragorn: 'fierce': fierce is for a minion, and this is a companion"));
        assertTrue(problems.get(2), problems.get(2).startsWith("step 1.do: modify(runner1, resistance, +1): resistance is for a companion, and this is a minion"));

        //A game that plays its own opening says nothing of its board but the order of its decks
        ScenarioDefinition opening = ScenarioTesting.parse("""
                	setup: {
                		opening: game
                		modifiers: {
                			aragorn: strength +1
                		}
                	}
                """);
        assertTrue(ScenarioValidator.validate(opening, ScenarioTesting.library()).stream().anyMatch(one -> one.startsWith("setup.modifiers: ")));
    }

    @Test
    public void modifiersAreWrittenBackAsTheyWereRead() throws Exception {
        String rest = """
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		modifiers: {
                			aragorn: strength +2, damage +1 turn
                			runner1: fierce
                		}
                	}
                	steps: [
                		{
                			when: freeps, action
                			do: modify(aragorn, vitality, -1, phase), unmodify(runner1, fierce)
                		}
                	]
                """;
        ScenarioDefinition first = ScenarioTesting.parse(rest);
        JsonObject written = ScenarioWriter.toJson(first);
        JsonObject modifiers = written.get("setup").asObject().get("modifiers").asObject();
        assertEquals("strength +2, damage +1 turn", modifiers.getString("aragorn", null));
        assertEquals("fierce", modifiers.getString("runner1", null));
        ScenarioDefinition second = ScenarioParser.parse(written.toString());
        assertEquals(first.setup.modifiers, second.setup.modifiers);
        assertEquals(first.steps.getFirst().doOps, second.steps.getFirst().doOps);
        assertEquals(written.toString(), ScenarioWriter.toJson(second).toString());
        String hjson = ScenarioWriter.toHjson(first, ScenarioTesting.library());
        assertTrue(hjson, hjson.contains("\t\tmodifiers: {\n\t\t\taragorn: strength +2, damage +1 turn\n\t\t\trunner1: fierce\n\t\t}"));
        assertTrue(hjson, hjson.contains("do: modify(aragorn, vitality, -1, phase), unmodify(runner1, fierce)"));
    }

    @Test
    public void theBoardStartsWithItsModifiers() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		freeps: {
                			characters: aragorn
                			hand: gandalf
                		}
                		shadow: {
                			minions: runner1
                		}
                		modifiers: {
                			aragorn: strength +2, vitality -1, damage +1, defender +2
                			frodo: resistance +3
                			runner1: fierce, archer
                			gandalf: cost -1
                		}
                	}
                """);
        assertEquals(List.of(), table.Problems());
        var aragorn = table.Card("aragorn");
        assertEquals(8 + 2, table.GetStrength(aragorn));
        assertEquals(4 - 1, table.GetVitality(aragorn));
        assertEquals(1, table.game().getModifiersQuerying().getKeywordCount(table.game(), aragorn, Keyword.DAMAGE));
        assertEquals(aragorn.getBlueprint().getKeywordCount(Keyword.DEFENDER) + 2,
                table.game().getModifiersQuerying().getKeywordCount(table.game(), aragorn, Keyword.DEFENDER));
        assertTrue(table.HasKeyword(table.Card("runner1"), Keyword.FIERCE));
        assertTrue(table.HasKeyword(table.Card("runner1"), Keyword.ARCHER));
        assertFalse(table.HasKeyword(aragorn, Keyword.FIERCE));
        assertEquals(table.Card("frodo").getBlueprint().getResistance() + 3, table.GetResistance(table.Card("frodo")));
        assertEquals("The cost of a card in hand", 4 - 1, table.GetTwilightCost(table.Card("gandalf")));

        //What the table has put on, in order, with numbers counted from 1
        List<CardModifiers.Put> on = table.ModifiersOn(aragorn);
        assertEquals(4, on.size());
        assertEquals(List.of(1, 2, 3, 4), on.stream().map(CardModifiers.Put::id).toList());
        assertEquals(List.of("Strength +2", "Vitality -1", "Damage +1", "Defender +2"), on.stream().map(CardModifiers.Put::text).toList());
        Map<String, Object> json = table.DescribeModifiers(table.Card("runner1")).getFirst();
        assertEquals(6, json.get("id"));
        assertEquals("fierce", json.get("what"));
        assertNull(json.get("amount"));
        assertEquals("removed", json.get("until"));
        assertEquals("Fierce", json.get("text"));
        assertEquals("fierce", json.get("written"));

        //The card's information shows them, as it shows any modifier on it
        assertTrue(table.game().getModifiersQuerying().getModifiersAffecting(table.game(), aragorn).stream()
                .anyMatch(modifier -> "Strength +2".equals(modifier.getText(table.game(), aragorn))));

        //Taken off one at a time, one kind at a time, or all at once
        assertTrue(table.Unmodify(1));
        assertFalse("Once only", table.Unmodify(1));
        assertEquals(8, table.GetStrength(aragorn));
        assertEquals(1, table.Unmodify(aragorn, "damage"));
        assertEquals(0, table.game().getModifiersQuerying().getKeywordCount(table.game(), aragorn, Keyword.DAMAGE));
        assertEquals(2, table.Unmodify(aragorn, null));
        assertEquals(4, table.GetVitality(aragorn));
        assertEquals(List.of(), table.ModifiersOn(aragorn));
        assertTrue("The others are untouched", table.HasKeyword(table.Card("runner1"), Keyword.FIERCE));

        //...and the numbers go on counting from where they were
        assertEquals(9, table.Modify(aragorn, new Modification("strength", 1, ModifierTime.REMOVED)).id());
        try {
            table.Modify(table.Card("gandalf"), new Modification("fierce", 0, ModifierTime.REMOVED));
            fail("Fierce on a card in hand that is not a minion");
        } catch (IllegalArgumentException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("fierce is for a minion"));
        }
    }

    @Test
    public void aModifierForTheTurnOrThePhaseRunsOut() throws Exception {
        //A step's do in the Fellowship phase: one for the phase, one for the turn, one until taken off
        ScenarioDefinition def = ScenarioTesting.parse("""
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: runner1
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			do: modify(aragorn, strength, +1, phase), modify(aragorn, strength, +2, turn), modify(aragorn, damage, +1)
                			messages: [
                				Aragorn is strength {strength:aragorn}.
                			]
                			answer: pass
                		}
                	]
                """);
        ScenarioTable table = new ScenarioTable(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), ScenarioRunner.HUMAN, ScenarioRunner.OPPONENT);
        table.Start();
        ScenarioDirector director = new ScenarioDirector(table);
        Step step = director.match(ScenarioRunner.HUMAN, table.FreepsGetAwaitingDecision());
        assertEquals(1, step.number);
        var aragorn = table.Card("aragorn");
        assertEquals(8 + 3, table.GetStrength(aragorn));
        assertEquals(3, table.ModifiersOn(aragorn).size());
        assertEquals("The game's own reading of a stat has them", "Aragorn is strength 11.",
                director.present(step, ScenarioRunner.HUMAN, table.FreepsGetAwaitingDecision()).messages().getFirst().text());

        //Into the Maneuver phase: the one for the Fellowship phase is gone, from the game and from the list
        table.FreepsPass();
        for (int i = 0; i < 10 && !(table.FreepsAnyDecisionsAvailable() && table.GetCurrentPhase() == Phase.MANEUVER); i++)
            if (table.ShadowAnyDecisionsAvailable())
                table.ShadowPass();
            else if (table.FreepsAnyDecisionsAvailable())
                table.FreepsPass();
        assertEquals(Phase.MANEUVER, table.GetCurrentPhase());
        assertEquals(8 + 2, table.GetStrength(aragorn));
        assertEquals(List.of("turn", "removed"), table.DescribeModifiers(aragorn).stream().map(json -> json.get("until")).toList());

        //And into the next turn: only the one until taken off is left
        for (int i = 0; i < 40 && table.gameState().getTurnNumber() < 2; i++) {
            if (table.FreepsAnyDecisionsAvailable())
                table.FreepsDecided(com.gempukku.lotro.scenario.AnswerCompiler.leastAnswer(table.FreepsGetAwaitingDecision()));
            else if (table.ShadowAnyDecisionsAvailable())
                table.ShadowDecided(com.gempukku.lotro.scenario.AnswerCompiler.leastAnswer(table.ShadowGetAwaitingDecision()));
        }
        assertTrue(table.gameState().getTurnNumber() >= 2);
        assertEquals(8, table.GetStrength(aragorn));
        assertEquals(List.of("damage"), table.DescribeModifiers(aragorn).stream().map(json -> json.get("what")).toList());
        assertEquals(List.of(), table.Problems());
    }

    @Test
    public void theSetupsTurnIsTheTurnTheGameStartsIn() throws Exception {
        //A late start, in the Maneuver phase: one for the phase lasts out the Maneuver phase, one for the turn the turn
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		startAt: {
                			site: 2
                			phase: maneuver
                		}
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: runner1
                		}
                		modifiers: {
                			aragorn: strength +1 phase, strength +2 turn
                		}
                	}
                """);
        assertEquals(List.of(), table.Problems());
        var aragorn = table.Card("aragorn");
        assertEquals(Phase.MANEUVER, table.GetCurrentPhase());
        assertEquals(8 + 3, table.GetStrength(aragorn));
        table.FreepsPass();
        if (table.ShadowAnyDecisionsAvailable())
            table.ShadowPass();
        for (int i = 0; i < 6 && table.GetCurrentPhase() == Phase.MANEUVER; i++)
            if (table.FreepsAnyDecisionsAvailable())
                table.FreepsPass();
            else if (table.ShadowAnyDecisionsAvailable())
                table.ShadowPass();
        assertTrue(table.GetCurrentPhase().toString(), table.GetCurrentPhase() != Phase.MANEUVER);
        assertEquals(8 + 2, table.GetStrength(aragorn));

        //At the very start, one for the turn is the first turn's
        ScenarioTable first = ScenarioTesting.table("""
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		modifiers: {
                			aragorn: strength +2 turn
                		}
                	}
                """);
        assertEquals(1, first.gameState().getTurnNumber());
        assertEquals(8 + 2, first.GetStrength(first.Card("aragorn")));
    }

    @Test
    public void aModifierThatMeansNothingOnItsCardIsAProblemInTheGame() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	steps: [
                		{
                			when: freeps, action
                			do: modify(servant1, strength, +2), unmodify(aragorn)
                		}
                	]
                """);
        new ScenarioDirector(table).match(ScenarioRunner.HUMAN, table.FreepsGetAwaitingDecision());
        assertEquals(2, table.Problems().size());
        assertTrue(table.Problems().get(0), table.Problems().get(0).startsWith("modify(servant1, strength, +2): 'servant1': strength is for a character"));
        assertTrue(table.Problems().get(1), table.Problems().get(1).contains("'aragorn' has no modifier put on it by hand to take off"));
    }

    @Test
    public void theHeadlessRunnerPlaysAModifiedSkirmish() throws Exception {
        //Frodo (strength 3) wins his skirmish against the runner (strength 5) only with the step's +3
        String scenario = """
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		startAt: {
                			site: 2
                			phase: skirmish
                		}
                		shadow: {
                			minions: runner1
                		}
                		skirmishes: {
                			frodo: runner1
                		}
                	}
                	steps: [
                		{
                			when: freeps, nextSkirmish
                			answer: choose(frodo)
                		}
                		{
                			when: freeps, action, phase(skirmish)
                			do: modify(frodo, strength, +3, phase)
                			answer: pass
                		}
                		{
                			when: freeps, action, phase(regroup)
                			answer: pass
                			end: complete
                		}
                	]
                	outcome: {
                		lose: [
                			{
                				type: canSpot
                				filter: alias(frodo),wounded
                			}
                		]
                	}
                """;
        ScenarioRunReport report = ScenarioTesting.run(scenario);
        assertTrue(report.toString(), report.passed());
        assertEquals(ScenarioDefinition.RunnerLevel.FULL, report.reached);
        ScenarioRunReport without = ScenarioTesting.run(scenario.replace("do: modify(frodo, strength, +3, phase)", "note: no help"));
        assertFalse(report + "\n----\n" + without, without.passed());
    }

    @Test
    public void aCardRenamedIsRenamedInItsModifiers() {
        JsonObject root = JsonValue.readHjson(ScenarioTesting.scenario("""
                	setup: {
                		modifiers: {
                			aragorn: strength +2
                		}
                	}
                	steps: [
                		{
                			when: freeps, action
                			do: modify(aragorn, fierce), unmodify(aragorn, strength)
                		}
                	]
                """)).asObject();
        AliasRename.rename(root, "aragorn", "strider");
        assertEquals("strength +2", root.get("setup").asObject().get("modifiers").asObject().getString("strider", null));
        assertNull(root.get("setup").asObject().get("modifiers").asObject().get("aragorn"));
        assertEquals("modify(strider, fierce), unmodify(strider, strength)",
                root.get("steps").asArray().get(0).asObject().getString("do", null));
    }

    @Test
    public void aMistakenModifierIsLeftOutWhenTheFileIsOpened() {
        String file = ScenarioTesting.scenario("""
                	setup: {
                		modifiers: {
                			aragorn: strength +2
                			runner1: fierce +3
                			servant1: vitality +1
                		}
                	}
                """);
        ScenarioSalvage.Result result = ScenarioSalvage.read(file, def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(result.errors().toString(), result.def());
        assertEquals(Map.of("aragorn", List.of(new Modification("strength", 2, ModifierTime.REMOVED))), result.def().setup.modifiers);
        assertEquals(List.of("setup.modifiers.runner1", "setup.modifiers.servant1"),
                result.leftOut().stream().map(ScenarioSalvage.LeftOut::where).toList());
    }
}
