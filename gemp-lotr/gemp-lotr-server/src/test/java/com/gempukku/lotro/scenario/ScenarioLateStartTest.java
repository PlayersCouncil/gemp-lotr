package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * A scenario that begins down the path, on a later turn, or part of the way through a turn (setup.startAt and what
 * goes with it: setup.skirmishes, setup.controlled, setup.path, setup.twilight), and the markers and site control
 * that come with it (do: marker, control, liberate; rules.markers).
 */
public class ScenarioLateStartTest {
    private static String decider(ScenarioTable table) {
        return table.FreepsAnyDecisionsAvailable() ? table.P1() : table.ShadowAnyDecisionsAvailable() ? table.P2() : null;
    }

    private static List<String> problems(String rest) throws Exception {
        return ScenarioValidator.validate(ScenarioTesting.parse(rest), ScenarioTesting.library());
    }

    private static void assertProblem(String rest, String fragment) throws Exception {
        List<String> problems = problems(rest);
        assertTrue("Expected a problem containing '" + fragment + "': " + problems, problems.stream().anyMatch(one -> one.contains(fragment)));
    }

    private static void assertRejected(String rest, String fragment) {
        try {
            ScenarioTesting.parse(rest);
            fail("Expected the scenario to be rejected with a message containing: " + fragment);
        } catch (ScenarioFormatException exp) {
            assertTrue("Expected a message containing '" + fragment + "' but got: " + exp.getMessage(), exp.getMessage().contains(fragment));
        }
    }

    /** A scenario written back out and read again is the same scenario. */
    private static ScenarioDefinition roundTrip(ScenarioDefinition def) throws Exception {
        String written = ScenarioWriter.toHjson(def, ScenarioTesting.library());
        ScenarioDefinition again = ScenarioParser.parse(written);
        assertEquals(written, ScenarioWriter.toHjson(again, ScenarioTesting.library()));
        return again;
    }

    @Test
    public void aGameCanBeginInTheManeuverPhaseAtSiteThree() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		startAt: {
                			phase: maneuver
                			site: 3
                			twilight: 4
                		}
                		freeps: {
                			characters: aragorn
                			hand: servant1
                		}
                		shadow: {
                			minions: bowman, runner1
                			hand: marksman
                		}
                		wounds: {
                			frodo: 1
                		}
                	}
                """);
        assertEquals(List.of(), table.Problems());
        //Where the game is
        assertEquals(Phase.MANEUVER, table.GetCurrentPhase());
        assertEquals(1, table.gameState().getTurnNumber());
        assertEquals(3, table.gameState().getCurrentSiteNumber());
        assertEquals(1, table.gameState().getMoveCount());
        assertEquals(4, table.GetTwilight());
        for (int site = 1; site <= 3; site++) {
            assertNotNull("site " + site + " is on the path", table.gameState().getSite(site));
            assertEquals(Zone.ADVENTURE_PATH, table.gameState().getSite(site).getZone());
            assertEquals("The Free Peoples seat's sites, by default", table.P1(), table.gameState().getSite(site).getOwner());
        }
        assertNull("site 4 has not been reached", table.gameState().getSite(4));
        assertEquals("Rivendell Terrace", table.gameState().getCurrentSite().getBlueprint().getTitle());
        assertSame(table.Card("site3"), table.gameState().getCurrentSite());
        assertEquals("The Shadow seat's marker stays at site 1", 1, table.gameState().getPlayerPosition(table.P2()));

        //The first thing asked is the Free Peoples player's maneuver action: nothing from earlier in the turn is
        assertTrue(table.FreepsDecisionAvailable("Play Maneuver action or Pass"));
        //Rivendell Terrace is a sanctuary, and the turn did not start here, as far as the game is concerned: no healing
        assertEquals(1, table.gameState().getWounds(table.Card("frodo")));

        //...and the rest of the turn runs as it always does: archery, assignment, skirmish, regroup
        table.FreepsPass();
        table.ShadowPass();
        assertEquals(Phase.ARCHERY, table.GetCurrentPhase());
        table.PassCurrentPhaseActions();
        //one archery wound from the Bowman, for the Free Peoples player to place: cards on the table are live
        assertTrue(table.FreepsDecisionAvailable("wound"));
        table.FreepsChooseCard(table.Card("aragorn"));
        assertEquals(1, table.gameState().getWounds(table.Card("aragorn")));
        assertEquals(Phase.ASSIGNMENT, table.GetCurrentPhase());
        table.PassCurrentPhaseActions();
        table.FreepsAssignToMinions(table.Card("aragorn"), table.Card("bowman"));
        table.ShadowDeclineAssignments();
        table.FreepsResolveSkirmish(table.Card("aragorn"));
        assertEquals(Phase.SKIRMISH, table.GetCurrentPhase());
        table.PassCurrentPhaseActions();
        assertEquals("Aragorn (8) beat the Bowman (4) by double and killed it", Zone.DISCARD, table.Card("bowman").getZone());
        assertEquals(Phase.REGROUP, table.GetCurrentPhase());
        table.PassCurrentPhaseActions();
        //The fellowship has moved once this turn (to get here), so it may move once more
        assertTrue(table.FreepsDecisionAvailable("another move"));
    }

    @Test
    public void aGameCanBeginWithTheSkirmishesAlreadyAssigned() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		startAt: {
                			phase: skirmish
                			site: 4
                			opponentSite: 2
                		}
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: bowman, runner1, runner2
                		}
                		skirmishes: {
                			frodo: runner1
                			aragorn: bowman + runner2
                		}
                	}
                """);
        assertEquals(List.of(), table.Problems());
        assertEquals(4, table.gameState().getPlayerPosition(table.P1()));
        assertEquals(2, table.gameState().getPlayerPosition(table.P2()));
        assertEquals(2, table.gameState().getAssignments().size());
        //Two skirmishes are waiting, and the Free Peoples player chooses which to fight first
        assertTrue(table.FreepsDecisionAvailable("next skirmish"));
        table.FreepsResolveSkirmish(table.Card("aragorn"));
        assertEquals(Phase.SKIRMISH, table.GetCurrentPhase());
        assertEquals(2, table.gameState().getSkirmish().getShadowCharacters().size());
        assertTrue(table.FreepsDecisionAvailable("Choose action to play or Pass"));
    }

    @Test
    public void theTurnCounterBeginsWhereTheStartSays() throws Exception {
        String rest = """
                	coverage: guided
                	setup: {
                		startAt: {
                			site: 3
                			turn: 3
                		}
                		freeps: {
                			hand: aragorn
                		}
                		wounds: {
                			frodo: 2
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, turn(3), phase(fellowship)
                			allow: play(aragorn)
                			answer: play(aragorn)
                		}
                		{
                			when: freeps, action, turn(3), phase(fellowship)
                			allow: pass
                			end: complete
                		}
                	]
                """;
        ScenarioTable table = ScenarioTesting.table(rest);
        assertEquals(3, table.gameState().getTurnNumber());
        assertEquals(Phase.FELLOWSHIP, table.GetCurrentPhase());
        assertEquals(0, table.gameState().getMoveCount());
        assertEquals("No sanctuary healing at the start: the turn began some time ago", 2, table.GetWoundsOn(table.Card("frodo")));
        assertEquals("Nor did the game tell anybody it was the start of a turn", 0,
                table.gameState().getMessageRecord() == null ? 0 : table.gameState().getMessageRecord().stream().filter(one -> one.contains("Start of turn")).count());

        ScenarioRunReport report = ScenarioTesting.run(rest);
        assertTrue(report.toString(), report.passed());
    }

    @Test
    public void aTwilightPoolAloneIsTheStartOfTurnOneWithTwilightInIt() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		twilight: 3
                	}
                """);
        assertEquals(Phase.FELLOWSHIP, table.GetCurrentPhase());
        assertEquals(1, table.gameState().getTurnNumber());
        assertEquals(1, table.gameState().getCurrentSiteNumber());
        assertEquals(3, table.GetTwilight());

        ScenarioDefinition def = ScenarioTesting.parse("""
                	setup: {
                		twilight: 3
                	}
                """);
        assertEquals("One place in the definition", 3, def.setup.twilight);
        assertTrue(def.setup.lateStart());
        //Written back as it was written
        assertTrue(ScenarioWriter.toHjson(def, null), ScenarioWriter.toHjson(def, null).contains("\t\ttwilight: 3\n"));
        roundTrip(def);
        //...or in the start, beside the rest of it
        def = ScenarioTesting.parse("""
                	setup: {
                		startAt: {
                			site: 2
                			phase: shadow
                			twilight: 3
                		}
                	}
                """);
        assertEquals(3, def.setup.twilight);
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("\t\tstartAt: {\n\t\t\tsite: 2\n\t\t\tphase: shadow\n\t\t\ttwilight: 3\n\t\t}"));
        roundTrip(def);
        assertRejected("""
                	setup: {
                		startAt: {
                			twilight: 2
                		}
                		twilight: 3
                	}
                """, "say it once");
    }

    @Test
    public void theFileFormatOfTheStart() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	setup: {
                		startAt: {
                			site: 4
                			opponentSite: 2
                			phase: assignment
                			turn: 2
                			moves: 2
                			twilight: 5
                		}
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: bowman, runner1, runner2
                		}
                		skirmishes: {
                			frodo: runner1
                			aragorn: "bowman + runner2"
                		}
                		controlled: {
                			shadow: site1
                		}
                		path: site1, site2opp
                	}
                """);
        ScenarioDefinition.Setup setup = def.setup;
        assertEquals(4, setup.site);
        assertEquals(Integer.valueOf(2), setup.opponentSite);
        assertEquals(Phase.ASSIGNMENT, setup.phase);
        assertEquals(2, setup.turn);
        assertEquals(2, setup.moves());
        assertEquals(5, setup.twilight);
        assertEquals(Map.of("frodo", List.of("runner1"), "aragorn", List.of("bowman", "runner2")), setup.skirmishes);
        assertEquals(Map.of(Seat.SHADOW, List.of("site1")), setup.controlled);
        assertEquals(List.of("site1", "site2opp"), setup.path);
        assertEquals(List.of(), ScenarioValidator.validate(def, ScenarioTesting.library()));

        String written = ScenarioWriter.toHjson(def, ScenarioTesting.library());
        assertTrue(written, written.contains("""
                		startAt: {
                			site: 4
                			opponentSite: 2
                			phase: assignment
                			turn: 2
                			moves: 2
                			twilight: 5
                		}
                """.replace("                ", "")));
        assertTrue(written, written.contains("\t\tskirmishes: {\n\t\t\tfrodo: runner1\n\t\t\taragorn: bowman + runner2\n\t\t}"));
        assertTrue(written, written.contains("\t\tcontrolled: {\n\t\t\tshadow: site1\n\t\t}"));
        assertTrue(written, written.contains("\t\tpath: site1, site2opp\n"));
        ScenarioDefinition again = roundTrip(def);
        assertEquals(setup.skirmishes, again.setup.skirmishes);
        assertEquals(setup.controlled, again.setup.controlled);
        assertEquals(setup.path, again.setup.path);

        //What each part is when it is left out
        def = ScenarioTesting.parse("""
                	setup: {
                		startAt: {
                			site: 3
                			phase: regroup
                			moves: 1
                		}
                	}
                """);
        assertNull("One move is what a start after the Fellowship phase has anyway", def.setup.moves);
        assertEquals(1, def.setup.moves());
        assertEquals(1, def.setup.opponentSite(def));
        assertFalse(ScenarioWriter.toHjson(def, null).contains("moves"));
        //A phase by itself
        def = ScenarioTesting.parse("""
                	setup: {
                		startAt: maneuver
                	}
                """);
        assertEquals(Phase.MANEUVER, def.setup.phase);
        assertFalse("The very start is no late start", ScenarioTesting.parse("").setup.lateStart());
        //A marker alone is not where the turn is
        assertFalse(ScenarioTesting.parse("\tsetup: {\n\t\tstartAt: {\n\t\t\topponentSite: 3\n\t\t}\n\t}").setup.lateStart());

        assertRejected("\tsetup: {\n\t\tstartAt: {\n\t\t\tphase: between_turns\n\t\t}\n\t}", "a game cannot begin in the between_turns phase");
        assertRejected("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 10\n\t\t}\n\t}", "there is no site 10");
        assertRejected("\tsetup: {\n\t\tstartAt: {\n\t\t\tday: 1\n\t\t}\n\t}", "unrecognized field 'day'");
        assertRejected("\tsetup: {\n\t\tskirmishes: {\n\t\t\trunner1: frodo\n\t\t}\n\t}", "is the Shadow seat's");
        assertRejected("\tsetup: {\n\t\tskirmishes: {\n\t\t\tfrodo: aragorn\n\t\t}\n\t}", "what a character skirmishes is minions");
        assertRejected("\tsetup: {\n\t\tskirmishes: {\n\t\t\tfrodo: runner1\n\t\t\taragorn: runner1\n\t\t}\n\t}", "assigned to two skirmishes");
        assertRejected("\tsetup: {\n\t\tcontrolled: {\n\t\t\tshadow: moria\n\t\t}\n\t}", "neither a site's number");
        assertRejected("\tsetup: {\n\t\tpath: site1, aragorn\n\t}", "'aragorn' is not one of the scenario's sites");
        assertRejected("\trules: {\n\t\tmarkers: sideways\n\t}", "rules.markers: has to be together");
    }

    @Test
    public void theValidatorSaysWhereAStartCannotBe() throws Exception {
        //(the test scenario has four sites)
        assertProblem("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 6\n\t\t}\n\t}", "the Free Peoples seat's sites have no site 5");
        assertProblem("\tsetup: {\n\t\tstartAt: {\n\t\t\tphase: shadow\n\t\t}\n\t}", "begins at site 2 or later");
        assertProblem("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 3\n\t\t\tmoves: 1\n\t\t}\n\t}", "has not moved yet this turn");
        assertProblem("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 2\n\t\t\tphase: shadow\n\t\t\tmoves: 2\n\t\t}\n\t}", "cannot have moved 2 time(s)");
        assertProblem("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 2\n\t\t\tphase: skirmish\n\t\t}\n\t}", "needs its skirmishes");
        String skirmishes = """
                	setup: {
                		startAt: {
                			site: 2
                			phase: PHASE
                		}
                		shadow: {
                			minions: runner1
                		}
                		skirmishes: {
                			CHARACTER: runner1 + runner2
                		}
                	}
                """;
        List<String> problems = problems(skirmishes.replace("PHASE", "skirmish").replace("CHARACTER", "aragorn"));
        assertEquals(problems.toString(), 2, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("setup.skirmishes.aragorn: 'aragorn' is not on the table"));
        assertTrue(problems.get(1), problems.get(1).contains("'runner2' is not on the table"));
        assertProblem(skirmishes.replace("PHASE", "maneuver").replace("CHARACTER", "frodo"), "a game that begins in the maneuver phase has none");
        assertTrue("A Ring-bearer is always on the table", problems(skirmishes.replace("PHASE", "skirmish").replace("CHARACTER", "frodo")
                .replace("minions: runner1", "minions: runner1, runner2")).isEmpty());

        String controlled = """
                	RULES
                	setup: {
                		startAt: {
                			site: 4
                			OPPONENT
                		}
                		controlled: {
                			shadow: site3, 2
                		}
                	}
                """;
        problems = problems(controlled.replace("RULES", "").replace("OPPONENT", ""));
        assertEquals(problems.toString(), 2, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("site 3 cannot be controlled when the game begins"));
        assertTrue(problems.get(0), problems.get(0).contains("the Shadow seat's marker is at site 1"));
        assertTrue(problems(controlled.replace("RULES", "").replace("OPPONENT", "opponentSite: 4")).isEmpty());
        assertProblem(controlled.replace("RULES", "").replace("OPPONENT", "opponentSite: 3"), "site 3 cannot be controlled");
        assertTrue("Level with the fellowship, under markers: together",
                problems(controlled.replace("RULES", "rules: {\n\t\toneSided: true\n\t\tmarkers: together\n\t}").replace("OPPONENT", "")).isEmpty());
        assertProblem(controlled.replace("RULES", "rules: {\n\t\tmarkers: together\n\t}").replace("OPPONENT", "opponentSite: 4"),
                "'together' is for a one-sided game");

        assertProblem("\tsetup: {\n\t\tpath: site1, site3\n\t}", "'site3' is site 3, so it cannot be site 2");
    }

    @Test
    public void whoseSiteIsLaidIsTheFreePeoplesSeatsUnlessThePathSays() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		startAt: {
                			site: 3
                		}
                		path: site1opp, site2opp
                	}
                """);
        assertEquals(List.of(), table.Problems());
        assertSame(table.Card("site1opp"), table.gameState().getSite(1));
        assertEquals(table.P2(), table.gameState().getSite(1).getOwner());
        assertEquals("The site the opening laid went back to its deck", Zone.ADVENTURE_DECK, table.Card("site1").getZone());
        assertSame(table.Card("site2opp"), table.gameState().getSite(2));
        assertSame("Past the path's end, the Free Peoples seat's own", table.Card("site3"), table.gameState().getSite(3));
    }

    /** A start at site 3 in the maneuver phase, with a checkpoint in its assignment phase. */
    static final String CHECKPOINTED = """
                	options: {
                		opponentDelayMs: 0
                	}
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		startAt: {
                			site: 3
                			phase: maneuver
                			twilight: 2
                		}
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: runner1
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(maneuver)
                			allow: pass
                		}
                		{
                			when: freeps, action, phase(archery)
                			allow: pass
                		}
                		{
                			checkpoint: true
                			when: freeps, action, phase(assignment)
                			allow: pass
                		}
                		{
                			when: freeps, assign
                			allow: assign(aragorn: runner1)
                		}
                		{
                			when: freeps, cards, text(next skirmish)
                			allow: choose(aragorn)
                		}
                		{
                			when: freeps, action, phase(skirmish)
                			allow: pass
                		}
                		{
                			when: freeps, action, phase(regroup)
                			allow: pass
                			end: complete
                		}
                	]
                """;

    @Test
    public void aLateStartPlaysHeadlessAndPicksUpAtItsCheckpoint() throws Exception {
        String rest = CHECKPOINTED;
        ScenarioRunReport report = ScenarioTesting.run(rest);
        assertTrue(report.toString(), report.passed());

        //(that it picks up at its checkpoint in a live game: LiveScenarioTest.ALateStartPicksUpAtItsCheckpoint)
    }

    @Test
    public void inATwoSidedGameTheStartIsTheFreePeoplesSeatsTurn() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		startAt: {
                			site: 2
                			opponentSite: 3
                			phase: regroup
                		}
                	}
                """);
        assertEquals(Phase.REGROUP, table.GetCurrentPhase());
        assertEquals(table.P1(), table.gameState().getCurrentPlayerId());
        //Through the regroup, staying where it is: the next turn is the Shadow seat's, with its marker where it was
        for (int i = 0; i < 20 && table.gameState().getTurnNumber() == 1; i++) {
            String player = decider(table);
            AwaitingDecision decision = table.userFeedback().getAwaitingDecision(player);
            table.PlayerDecided(player, AnswerCompiler.leastAnswer(decision));
        }
        assertEquals(2, table.gameState().getTurnNumber());
        assertEquals(table.P2(), table.gameState().getCurrentPlayerId());
        //(the scripted seat passes its Fellowship phase without being asked, and so moves on from site 3)
        assertEquals(4, table.gameState().getCurrentSiteNumber());
        assertEquals("The other fellowship's marker stayed where the first turn left it", 2, table.gameState().getPlayerPosition(table.P1()));
    }

    @Test
    public void aMistakenPartOfTheStartIsLeftOutToOpenTheFile() throws Exception {
        String file = ScenarioTesting.scenario("""
                	setup: {
                		startAt: {
                			site: 3
                			phase: skirmish
                		}
                		shadow: {
                			minions: runner1
                		}
                		skirmishes: {
                			frodo: runner1
                			aragorn: nobody
                		}
                		controlled: {
                			shadow: 3
                		}
                	}
                """);
        ScenarioSalvage.Result read = ScenarioSalvage.read(file, def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(read.errors().toString(), read.def());
        assertEquals(read.leftOut().toString(), 2, read.leftOut().size());
        assertEquals("setup.skirmishes.aragorn", read.leftOut().get(0).where());
        assertEquals("setup.controlled.shadow", read.leftOut().get(1).where());
        assertEquals(Map.of("frodo", List.of("runner1")), read.def().setup.skirmishes);
        assertEquals(3, read.def().setup.site);
    }

    /* ------------------------------------------------------------------ a path whose sites have no numbers (A4) */

    /** The test scenario on a Shadows-block path: three sites for each seat, none of them numbered. */
    private static String shadowsPath(String rest) {
        return ScenarioTesting.scenario(rest).replace("format: scenario", "format: scenario_shadows").replace("""
                		freeps: 1_323, 1_331, 1_340, 2_118
                		shadow: 1_323, 1_331, 1_340, 2_118
                """.replace("                ", ""), """
                		freeps: {
                			road: 11_236
                			silverlode: 11_261
                			bog: 11_249
                		}
                		shadow: {
                			oppRoad: 11_236
                			inn: 11_242
                			guardroom: 11_247
                		}
                """.replace("                ", ""));
    }

    private static ScenarioTable shadowsTable(String rest) throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(shadowsPath(rest));
        assertEquals(List.of(), ScenarioValidator.validate(def, ScenarioTesting.library()));
        ScenarioTable table = new ScenarioTable(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), ScenarioRunner.HUMAN, ScenarioRunner.OPPONENT);
        table.Start();
        return table;
    }

    @Test
    public void aPathWithoutNumbersIsLaidOutByTheScenario() throws Exception {
        assertFalse("The scenario format of the Shadows block is not offered in the hall", ScenarioTesting.format("scenario_shadows").isOrderedSites());

        //The game's opening asks which site is site 1; the table answers it with the first site listed
        ScenarioTable table = shadowsTable("");
        assertTrue("The board was laid out: the opening did not wait for the site", table.Arranged());
        assertEquals(List.of(), table.Problems());
        assertSame(table.Card("road"), table.gameState().getSite(1));
        assertEquals(Phase.FELLOWSHIP, table.GetCurrentPhase());
        assertTrue(table.FreepsDecisionAvailable("Play Fellowship action or Pass"));

        //A start further on lays the seat's sites in the order the scenario lists them
        table = shadowsTable("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 3\n\t\t}\n\t}");
        assertEquals(List.of(), table.Problems());
        assertSame(table.Card("road"), table.gameState().getSite(1));
        assertSame(table.Card("silverlode"), table.gameState().getSite(2));
        assertSame(table.Card("bog"), table.gameState().getSite(3));
        assertSame(table.Card("bog"), table.gameState().getCurrentSite());

        //...or the path says which, in which order
        table = shadowsTable("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 2\n\t\t}\n\t\tpath: bog, inn\n\t}");
        assertEquals(List.of(), table.Problems());
        assertSame("Site 1 is chosen as the path says", table.Card("bog"), table.gameState().getSite(1));
        assertSame(table.Card("inn"), table.gameState().getSite(2));
        assertEquals(table.P2(), table.gameState().getSite(2).getOwner());

        //Past four sites of its own, a path with three runs out
        assertProblem2(shadowsPath("\tsetup: {\n\t\tstartAt: {\n\t\t\tsite: 4\n\t\t}\n\t}"), "the Free Peoples seat's sites have no site 4");
    }

    private static void assertProblem2(String file, String fragment) throws Exception {
        List<String> problems = ScenarioValidator.validate(ScenarioParser.parse(file), ScenarioTesting.library());
        assertTrue("Expected a problem containing '" + fragment + "': " + problems, problems.stream().anyMatch(one -> one.contains(fragment)));
    }

    @Test
    public void aSiteToPlayIsChosenByItsName() throws Exception {
        //Moving on to a site that is not on the path has the Shadow seat play one, and choose which: choose(site)
        ScenarioRunReport report = ScenarioRunner.run(ScenarioParser.parse(shadowsPath("""
                	rules: {
                		oneSided: true
                	}
                	opponent: {
                		fallback: pass
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: pass
                		}
                		{
                			when: opponent, arbitrary, text(Choose site to play)
                			answer: choose(inn)
                		}
                		{
                			when: {
                				terms: freeps, action, phase(regroup)
                				require: {
                					type: canSpot
                					filter: siteNumber(2),alias(inn)
                				}
                			}
                			allow: pass
                			end: complete
                		}
                	]
                """)), ScenarioTesting.library(), ScenarioTesting.format("scenario_shadows"));
        assertTrue(report.toString(), report.passed());
    }

    /* ------------------------------------------------------------------ markers and site control (A2, A5) */

    @Test
    public void aSiteIsControlledOnlyOnceEveryMarkerIsPastIt() throws Exception {
        //As the game has it (TakeControlOfASiteEffect, GameUtils.anySiteAvailableToControl): in a one-sided game the
        // Shadow seat's marker stays at site 1, and no site can be controlled
        ScenarioTable apart = ScenarioTesting.table("""
                	rules: {
                		oneSided: true
                	}
                	setup: {
                		startAt: {
                			site: 3
                		}
                	}
                """);
        assertFalse(GameUtils.anySiteAvailableToControl(apart.game()));
        assertFalse(apart.Holds("[{\"type\": \"SiteAvailableToControl\"}]", apart.P2()));
        //...unless its marker is put further on
        apart.Apply(ScenarioParser.parseDo(apart.Definition(), com.gempukku.lotro.scenario.Terms.parse("marker(shadow, 3)", "do").getFirst(), "do"));
        assertEquals(3, apart.gameState().getPlayerPosition(apart.P2()));
        assertTrue(GameUtils.anySiteAvailableToControl(apart.game()));
        assertTrue(new com.gempukku.lotro.logic.effects.TakeControlOfASiteEffect(apart.Card("oppFrodo"), apart.P2()).isPlayableInFull(apart.game()));

        //rules.markers: together keeps it level with the fellowship, from the start and as the fellowship moves
        ScenarioTable together = ScenarioTesting.table("""
                	rules: {
                		oneSided: true
                		markers: together
                	}
                	setup: {
                		startAt: {
                			site: 2
                		}
                	}
                """);
        assertEquals(2, together.gameState().getPlayerPosition(together.P2()));
        assertTrue(GameUtils.anySiteAvailableToControl(together.game()));
        together.FreepsPass();
        assertEquals(3, together.gameState().getPlayerPosition(together.P1()));
        assertEquals(3, together.gameState().getPlayerPosition(together.P2()));

        //...and without it, nothing is different from what one-sided games have always shown
        ScenarioTable plain = ScenarioTesting.table("""
                	rules: {
                		oneSided: true
                	}
                """);
        plain.FreepsPass();
        assertEquals(2, plain.gameState().getPlayerPosition(plain.P1()));
        assertEquals(1, plain.gameState().getPlayerPosition(plain.P2()));
    }

    private static void apply(ScenarioTable table, String op) throws Exception {
        for (Terms.Term term : Terms.parse(op, "do"))
            table.Apply(ScenarioParser.parseDo(table.Definition(), term, "do"));
    }

    @Test
    public void markersAndControlAreSetDirectly() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	rules: {
                		oneSided: true
                	}
                	setup: {
                		startAt: {
                			site: 3
                			opponentSite: 3
                		}
                		controlled: {
                			shadow: 1
                		}
                	}
                """);
        assertEquals(List.of(), table.Problems());
        assertEquals(table.P2(), table.Card("site1").getCardController());
        assertEquals(Zone.SUPPORT, table.Card("site1").getZone());

        apply(table, "control(site2, shadow)");
        assertEquals(List.of(), table.Problems());
        assertEquals(table.P2(), table.Card("site2").getCardController());
        apply(table, "liberate(1)");
        assertNull(table.Card("site1").getCardController());
        assertEquals(Zone.ADVENTURE_PATH, table.Card("site1").getZone());

        //A marker moved on lays the sites up to it, and the fellowship's has the site it stands at take over
        apply(table, "marker(freeps, 4)");
        assertEquals(4, table.gameState().getCurrentSiteNumber());
        assertSame(table.Card("site4"), table.gameState().getCurrentSite());

        //What cannot be done is said
        apply(table, "control(4, shadow)");
        assertTrue(table.Problems().toString(), table.Problems().getLast().contains("site 4 cannot be controlled: the freeps seat's marker is at site 4"));
        apply(table, "liberate(3)");
        assertTrue(table.Problems().getLast(), table.Problems().getLast().contains("controlled by nobody"));

        //The parser's side of it
        ScenarioDefinition def = table.Definition();
        ScenarioDefinition.DoOp op = ScenarioParser.parseDo(def, Terms.parse("control(site2, shadow)", "do").getFirst(), "do");
        assertEquals(ScenarioDefinition.DoKind.CONTROL, op.kind());
        assertEquals("site2", op.card());
        assertEquals("shadow", op.target());
        op = ScenarioParser.parseDo(def, Terms.parse("marker(opponent, 5)", "do").getFirst(), "do");
        assertEquals(ScenarioDefinition.DoKind.MARKER, op.kind());
        assertEquals(5, op.amount());
        try {
            ScenarioParser.parseDo(def, Terms.parse("control(moria, shadow)", "do").getFirst(), "do");
            fail();
        } catch (ScenarioFormatException expected) {
            assertTrue(expected.getMessage().contains("neither a site's number"));
        }
    }
}
