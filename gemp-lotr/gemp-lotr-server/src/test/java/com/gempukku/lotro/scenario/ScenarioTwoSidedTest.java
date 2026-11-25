package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.scenario.ScenarioDefinition.When;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import org.hjson.JsonValue;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Scenarios in which both seats take turns (rules.oneSided absent): the person is the Free Peoples player on their
 * own turns and the Shadow player on the opponent's.  The two fixtures (two-sided-first: the person in the first
 * seat; two-sided-second: in the second, beginning down the path) are played through by ScenarioCanonTest; here
 * what happens in them is looked at, and what a seat's role changes is tried a piece at a time.
 */
public class ScenarioTwoSidedTest {
    static ScenarioDefinition fixture(String name) throws Exception {
        return ScenarioParser.parse(Files.readString(new File("src/test/resources/scenarios/" + name + ".hjson").toPath(), StandardCharsets.UTF_8));
    }

    /** A 'when' as a step would have it. */
    private static When when(ScenarioDefinition def, String terms) throws ScenarioFormatException {
        return ScenarioParser.parseStep(def, JsonValue.readHjson("{when: \"" + terms + "\"}"), 1).when;
    }

    private static boolean at(Ruling ruling, String label) {
        return ruling.step() != null && ruling.step().label().startsWith(label);
    }

    @Test
    public void theFixturesPlayThroughBothRoles() throws Exception {
        for (String name : List.of("two-sided-first", "two-sided-second")) {
            ScenarioDefinition def = fixture(name);
            assertFalse(def.oneSided);
            ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
            assertTrue(report.toString(), report.passed());
        }
    }

    @Test
    public void thePersonInTheFirstSeatTakesControlOfASiteOnTheOpponentsTurn() throws Exception {
        ScenarioDefinition def = fixture("two-sided-first");
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        ScenarioTable table = session.Table();

        //Turn 1: the person is the Free Peoples player; the opponent, as the Shadow player, assigns its leftover
        // Goblin to the person's Ring-bearer
        List<String> opponentAssigned = new ArrayList<>();
        Ruling ruling = ScenarioOpponentRulesTest.playTo(session, one -> {
            if (one.player().equals(table.OpponentPlayer()) && one.decision().getDecisionType() == AwaitingDecisionType.ASSIGN_MINIONS)
                opponentAssigned.add(table.gameState().getTurnNumber() + ": " + (one.answer() == null || one.answer().isEmpty() ? "nothing"
                        : AnswerCompiler.decompile(table, one.decision(), one.answer())));
            return at(one, "5");
        });
        assertNotNull(ruling);
        assertEquals(List.of("1: assign(frodo: orc2)"), opponentAssigned);
        assertEquals(2, table.gameState().getTurnNumber());
        assertEquals(Side.SHADOW, table.RoleOf(table.HumanPlayer()));
        assertEquals(Side.FREE_PEOPLE, table.RoleOf(table.OpponentPlayer()));
        assertEquals(Seat.SHADOW, table.TurnSeat());
        //Both fellowships have moved on to site 2
        assertEquals(2, table.gameState().getPlayerPosition(table.HumanPlayer()));
        assertEquals(2, table.gameState().getPlayerPosition(table.OpponentPlayer()));

        //Freca's own text takes control of site 1, for the person, who is the Shadow player
        ruling = ScenarioOpponentRulesTest.playTo(session, one -> {
            if (one.player().equals(table.OpponentPlayer()) && one.decision().getDecisionType() == AwaitingDecisionType.ASSIGN_MINIONS)
                opponentAssigned.add(table.gameState().getTurnNumber() + ": " + (one.answer() == null || one.answer().isEmpty() ? "nothing"
                        : AnswerCompiler.decompile(table, one.decision(), one.answer())));
            return at(one, "10");
        });
        assertNotNull(ruling);
        assertEquals(table.HumanPlayer(), table.gameState().getSite(1).getCardController());
        //As the Free Peoples player the opponent assigns as its fallback has it, not its leftovers: those are the
        // Shadow player's, and on its own turn they would have gone to its own Ring-bearer
        assertEquals(List.of("1: assign(frodo: orc2)", "2: nothing"), opponentAssigned);
        assertNull(session.Director().leftovers());

        //The person's side in a skirmish is the Shadow side now
        ruling = ScenarioOpponentRulesTest.playTo(session, one -> at(one, "11.1"));
        assertEquals("Your minion is 5 against 3.", ruling.shown().messages().getFirst().text());
    }

    @Test
    public void thePersonInTheSecondSeatBeginsDownThePathAsTheShadowPlayer() throws Exception {
        ScenarioDefinition def = fixture("two-sided-second");
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        ScenarioTable table = session.Table();
        //The first turn is the first seat's, the opponent's: its fellowship at site 3, the person's marker at site 2
        Ruling ruling = ScenarioOpponentRulesTest.playTo(session, one -> at(one, "1"));
        assertEquals(1, table.gameState().getTurnNumber());
        assertEquals(Side.SHADOW, table.RoleOf(table.HumanPlayer()));
        assertEquals(4, table.gameState().getPlayerPosition(table.OpponentPlayer()));
        assertEquals(2, table.gameState().getPlayerPosition(table.HumanPlayer()));
        assertEquals("shadow", ScenarioParser.roleName(table.RoleOf(ruling.player())));

        //The fierce Uruk's second assignment is the person's, as the Shadow player
        ruling = ScenarioOpponentRulesTest.playTo(session, one -> at(one, "5.1"));
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, ruling.decision().getDecisionType());
        assertTrue(table.gameState().isFierceSkirmishes());

        //Turn 2 is the person's: the opponent, as the Shadow player, takes control of site 1 by its standing order
        ruling = ScenarioOpponentRulesTest.playTo(session, one -> at(one, "7"));
        assertEquals(2, table.gameState().getTurnNumber());
        assertEquals(Side.FREE_PEOPLE, table.RoleOf(table.HumanPlayer()));
        assertEquals(table.OpponentPlayer(), table.gameState().getSite(1).getCardController());
        assertEquals(java.util.Set.of(1), session.Director().ordersSpent());
    }

    @Test
    public void asNamesTheRoleTheSeatIsPlayingNow() throws Exception {
        ScenarioDefinition def = fixture("two-sided-first");
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        ScenarioTable table = session.Table();
        ScenarioDirector director = session.Director();
        When asFreeps = when(def, "human, as(freeps)");
        When asShadow = when(def, "human, as(shadow)");
        When opponentAsShadow = when(def, "opponent, as(shadow)");
        List<String> seen = new ArrayList<>();
        ScenarioOpponentRulesTest.playTo(session, one -> {
            if (one.player().equals(table.HumanPlayer())) {
                boolean freeps = director.matches(asFreeps, one.player(), one.decision());
                boolean shadow = director.matches(asShadow, one.player(), one.decision());
                assertNotEquals("one role or the other at " + one.decision().getText(), freeps, shadow);
                assertEquals(table.gameState().getTurnNumber() % 2 == 1, freeps);
                if (!seen.contains(table.gameState().getTurnNumber() + (freeps ? " freeps" : " shadow")))
                    seen.add(table.gameState().getTurnNumber() + (freeps ? " freeps" : " shadow"));
            } else {
                assertEquals(table.gameState().getTurnNumber() % 2 == 1, director.matches(opponentAsShadow, one.player(), one.decision()));
            }
            return table.gameState().getTurnNumber() > 3;
        });
        assertEquals(List.of("1 freeps", "2 shadow", "3 freeps", "4 shadow"), seen);
    }

    @Test
    public void autoPassCanBeSaidForEachRole() throws Exception {
        //Regroup is passed through as the Free Peoples player only: on the opponent's turns the person is asked
        String rest = """
                	opponent: {
                		fallback: pass
                	}
                	options: {
                		autoPass: {
                			freeps: fellowship, regroup
                			shadow: shadow
                		}
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			characters: marksman
                		}
                	}
                """;
        ScenarioDefinition def = withOppBoromir(rest);
        assertEquals(java.util.EnumSet.of(Phase.FELLOWSHIP, Phase.REGROUP), def.autoPassFor(Side.FREE_PEOPLE));
        assertEquals(java.util.EnumSet.of(Phase.SHADOW), def.autoPassFor(Side.SHADOW));
        ScenarioRunReport explored = ScenarioRunner.explore(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
        List<String> regroups = new ArrayList<>();
        //(a player who has something to play is asked whatever their settings say: only the passes count)
        for (ScenarioRunReport.Decision decision : explored.decisions)
            if (decision.decision().startsWith("freeps") && decision.decision().contains("offering nothing but Pass")
                    && decision.decision().matches(".*\"Play (Regroup|Fellowship|Shadow) action or Pass\".*"))
                regroups.add(decision.decision().replaceAll(".*\"Play (\\w+) action.*\\[turn (\\d+).*", "$2 $1"));
        assertTrue(explored.toString(), regroups.size() >= 2);
        //Asked in the Regroup phase on even turns (the Shadow player's), never in the Fellowship or Regroup phases of
        // odd ones, nor in the Shadow phase of even ones
        for (String one : regroups)
            assertTrue(regroups.toString(), one.matches("\\d*[02468] Regroup"));

        //Written as one list, it holds in both roles, as it always has: the person is passed through Regroup on
        // every turn
        ScenarioDefinition list = withOppBoromir(rest.replaceAll("autoPass: \\{[^}]*}", "autoPass: fellowship, shadow, regroup"));
        assertNull(list.autoPassByRole);
        explored = ScenarioRunner.explore(list, ScenarioTesting.library(), ScenarioTesting.format(list.format));
        for (ScenarioRunReport.Decision decision : explored.decisions)
            assertFalse(decision.decision(), decision.decision().startsWith("freeps") && decision.decision().contains("\"Play Regroup action or Pass\" offering nothing but Pass"));
    }

    /** The test scenario, with a Boromir of the Shadow seat's own on the table beside its Ring-bearer. */
    private static ScenarioDefinition withOppBoromir(String rest) throws ScenarioFormatException {
        return ScenarioParser.parse(ScenarioTesting.scenario(rest.replace("characters: marksman", "characters: oppBoromir"))
                .replace("marksman: 1_176", "marksman: 1_176\n\t\t\toppBoromir: 0_65"));
    }

    @Test
    public void theShadowPlayersRestIsAssignedAsTheShadowPlayerHasIt() throws Exception {
        //The person, in the first seat, assigns both minions to the opponent's Ring-bearer on the opponent's turn: as
        // the Shadow player they may (the Free Peoples player's one-minion-each does not hold for them)
        ScenarioDefinition def = ScenarioParser.parse(Files.readString(new File("src/test/resources/scenarios/two-sided-first.hjson").toPath(),
                StandardCharsets.UTF_8).replace("allow: assign(boromir: freca, oppFrodo: ravager)", "allow: assign(boromir: rest)"));
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        Ruling ruling = ScenarioOpponentRulesTest.playTo(session, one -> at(one, "10"));
        assertEquals("assign(boromir: ravager + freca)", AnswerCompiler.decompile(session.Table(), ruling.decision(), ruling.answer()));
    }

    @Test
    public void theSandboxPlaysAsTheShadowPlayerFromEitherSeat() throws Exception {
        //The person in the second seat; the opponent sits in the first and is the Shadow player on the person's turns
        String text = Files.readString(new File("src/test/resources/scenarios/two-sided-second.hjson").toPath(), StandardCharsets.UTF_8);
        String orders = text.substring(text.indexOf("\t\torders: ["), text.indexOf("\t\t]\n\t}\n\tsetup") + 4);
        ScenarioDefinition def = ScenarioParser.parse(text.replace(orders, "").replace("fallback: pass", "fallback: sandbox"));
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        ScenarioTable table = session.Table();
        List<String> played = new ArrayList<>();
        ScenarioOpponentRulesTest.playTo(session, one -> {
            if (one.player().equals(table.OpponentPlayer()) && table.GetCurrentPhase() == Phase.SHADOW && one.answer() != null && !one.answer().isEmpty())
                played.add(table.gameState().getTurnNumber() + ": " + AnswerCompiler.decompile(table, one.decision(), one.answer()));
            return at(one, "7");
        });
        //(the Ravager first: Freca wants another Dunland Man in play to take control of a site, but plays all the same)
        assertEquals(List.of("2: play(ravager)", "2: play(freca)"), played);
        //...and it assigns its leftovers to the person's Ring-bearer as the Shadow player, not as the Free Peoples one
        assertEquals(ScenarioDefinition.RING_BEARER, session.Director().leftovers());
    }

    @Test
    public void inATwoSidedGameTheLeftoversAreThePersonsCharacters() throws Exception {
        String twoSided = """
                	human: shadow
                	opponent: {
                		leftovers: LEFTOVERS
                	}
                """;
        //(the person sits in the second seat: the Shadow seat's cards are theirs)
        assertEquals("marksman", ScenarioTesting.parse(twoSided.replace("LEFTOVERS", "marksman")).leftovers);
        try {
            ScenarioTesting.parse(twoSided.replace("LEFTOVERS", "aragorn"));
            fail("The opponent's own character is not one it assigns minions to");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("'aragorn' is the opponent's own"));
        }
        //One-sided, as it always was: the Free Peoples seat's (here the opponent's)
        String oneSided = twoSided + "\trules: {\n\t\toneSided: true\n\t}\n";
        assertEquals("aragorn", ScenarioTesting.parse(oneSided.replace("LEFTOVERS", "aragorn")).leftovers);
        try {
            ScenarioTesting.parse(oneSided.replace("LEFTOVERS", "marksman"));
            fail("A Shadow card is not one minions are assigned to");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("is a Shadow card"));
        }
    }

    @Test
    public void theOldSideNamesKeepTheirMeaningInATwoSidedGame() throws Exception {
        String step = """
                	STEPS
                	steps: [
                		{
                			when: freeps, action
                			messages: [
                				"{fpStrength} against {shadowStrength}"
                			]
                		}
                	]
                """;
        ScenarioDefinition one = ScenarioTesting.parse(step.replace("STEPS", "rules: {\n\t\toneSided: true\n\t}"));
        assertEquals("{selfStrength} against {oppStrength}", one.steps.getFirst().messages.getFirst().text);
        ScenarioDefinition two = ScenarioTesting.parse(step.replace("STEPS", ""));
        assertEquals("{fpStrength} against {shadowStrength}", two.steps.getFirst().messages.getFirst().text);
    }

    @Test
    public void asAndAutoPassForEachRoleAreReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	options: {
                		autoPass: {
                			freeps: fellowship, regroup
                			shadow: false
                		}
                	}
                	steps: [
                		{
                			when: human, action, phase(maneuver), as(shadow)
                			allow: pass
                		}
                		{
                			when: {
                				seat: human
                				as: freeps
                				require: {
                					type: canSpot
                					filter: name(Aragorn)
                				}
                			}
                			allow: pass
                		}
                	]
                """);
        assertEquals(Side.SHADOW, def.steps.get(0).when.role);
        assertEquals(Side.FREE_PEOPLE, def.steps.get(1).when.role);
        String written = ScenarioWriter.toHjson(def, ScenarioTesting.library());
        assertTrue(written, written.contains("when: human, action, phase(maneuver), as(shadow)"));
        assertTrue(written, written.contains("as: freeps"));
        assertTrue(written, written.contains("autoPass: {\n\t\t\tfreeps: fellowship, regroup\n\t\t\tshadow: false\n\t\t}"));
        ScenarioDefinition again = ScenarioParser.parse(written);
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(again).toString());
        assertEquals(java.util.EnumSet.noneOf(Phase.class), again.autoPassFor(Side.SHADOW));
        //...and the terms a step's 'when' is composed of, when the authoring tool puts one together
        When composed = when(def, "human, action, turn(2), as(shadow)");
        assertEquals("human, action, turn(2), as(shadow)", composed.source);
        assertEquals("freeps, action, turn(2), as(shadow)", ScenarioWriter.terms(composed));

        for (String wrong : List.of("as(human)", "as(both)")) {
            try {
                when(def, "human, " + wrong);
                fail(wrong);
            } catch (ScenarioFormatException exp) {
                assertTrue(exp.getMessage(), exp.getMessage().contains("is not a role"));
            }
        }
        try {
            ScenarioTesting.parse("\toptions: {\n\t\tautoPass: {\n\t\t\tfreeps: fellowship\n\t\t\thuman: regroup\n\t\t}\n\t}\n");
            fail("autoPass is said for a role, not a seat");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("options.autoPass"));
        }
    }

    @Test
    public void aMistakenRoleIsLeftOutWhenTheFileIsOpenedWithItsMistakes() throws Exception {
        String text = Files.readString(new File("src/test/resources/scenarios/two-sided-first.hjson").toPath(), StandardCharsets.UTF_8)
                .replace("when: human, assign, as(freeps), turn(1)", "when: human, assign, as(both), turn(1)")
                .replace("\t\t\tshadow: maneuver, archery, assignment, regroup", "\t\t\tshadow: maneuver, archery, assignment, regroup\n\t\t\thuman: shadow");
        ScenarioSalvage.Result read = ScenarioSalvage.read(text, def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(read.errors().toString(), read.def());
        List<String> where = read.leftOut().stream().map(ScenarioSalvage.LeftOut::where).toList();
        assertTrue(where.toString(), where.stream().anyMatch(one -> one.startsWith("options.autoPass")));
        assertTrue(where.toString(), where.stream().anyMatch(one -> one.startsWith("step 1")));
    }
}
