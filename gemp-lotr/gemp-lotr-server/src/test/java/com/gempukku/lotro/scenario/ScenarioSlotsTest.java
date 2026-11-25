package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.PhysicalCard;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * A puzzle's slots and pool (cards the solver chooses), the game built with the picks or held without them, and the
 * author's solutions: the 'solution' runner level, and a solved game whose picks no solution lists.
 */
public class ScenarioSlotsTest {
    /** Five slots, one of each kind of place, around Aragorn on the table. */
    private static final String SLOTS = """
            	kind: puzzle
            	coverage: guided
            	options: {
            		opponentDelayMs: 0
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	pool: set:1
            	slots: {
            		weapon: {
            			zone: attached
            			on: aragorn
            			filter: cardtype:POSSESSION
            			label: a weapon for Aragorn
            		}
            		friend: {
            			zone: support
            			filter: cardtype:ALLY
            			tokens: 2
            			wounds: 1
            		}
            		trick: hand
            		deep: {
            			zone: deck
            			position: 2
            		}
            		other: {
            			zone: characters
            			filter: cardtype:COMPANION
            		}
            	}
            	setup: {
            		freeps: {
            			characters: aragorn
            			deck: gandalf, boromir, servant1
            		}
            	}
            """;

    private static Map<String, String> picks(String... pairs) {
        Map<String, String> picks = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2)
            picks.put(pairs[i], pairs[i + 1]);
        return picks;
    }

    /** Ranger's Sword, Farmer Maggot, Power According to His Stature, Dagger Strike, Merry. */
    private static final Map<String, String> GOOD = picks("weapon", "1_112", "friend", "1_288", "trick", "1_308", "deep", "1_102", "other", "1_302");

    private static void assertRejected(String rest, String fragment) {
        try {
            ScenarioTesting.parse(rest);
            fail("Expected the scenario to be rejected with a message containing: " + fragment);
        } catch (ScenarioFormatException exp) {
            assertTrue("Expected a message containing '" + fragment + "' but got: " + exp.getMessage(), exp.getMessage().contains(fragment));
        }
    }

    private static ScenarioTable table(ScenarioDefinition def, Map<String, String> picks) throws Exception {
        ScenarioTable table = new ScenarioTable(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), ScenarioRunner.HUMAN, ScenarioRunner.OPPONENT);
        table.Picks(picks);
        table.Start();
        return table;
    }

    /* ---------------------------------------------------------------- the file */

    @Test
    public void slotsAndThePoolAreReadAndWrittenBackAsWritten() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SLOTS);
        assertEquals("set:1", def.pool);
        assertEquals(List.of("weapon", "friend", "trick", "deep", "other"), List.copyOf(def.slots.keySet()));
        ScenarioDefinition.Slot weapon = def.slots.get("weapon");
        assertEquals(ScenarioDefinition.SlotZone.ATTACHED, weapon.zone());
        assertEquals("aragorn", weapon.on());
        assertEquals("cardtype:POSSESSION", weapon.filter());
        assertEquals("a weapon for Aragorn", weapon.label());
        assertEquals(Seat.FREEPS, weapon.seat());
        assertEquals(2, def.slots.get("friend").tokens());
        assertEquals(1, def.slots.get("friend").wounds());
        assertEquals(ScenarioDefinition.SlotZone.HAND, def.slots.get("trick").zone());
        assertEquals(Integer.valueOf(2), def.slots.get("deep").position());
        ScenarioDefinition again = ScenarioPuzzleTest.roundTrip(def);
        String written = ScenarioWriter.toHjson(again, ScenarioTesting.library());
        assertTrue(written, written.contains("pool: set:1\n"));
        assertTrue(written, written.contains("\t\ttrick: hand\n"));
        assertTrue(written, written.contains("\t\t\tfilter: cardtype:POSSESSION\n"));
        assertEquals(List.of(), ScenarioValidator.validate(def, ScenarioTesting.library()));
    }

    @Test
    public void aSlotsCardIsNamedAsAnyCardIsBeforeItIsKnown() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SLOTS + """
                	steps: [
                		{
                			when: human, action, offers(trick)
                			allow: play(trick)
                			messages: Play {title:trick}.
                		}
                	]
                	outcome: {
                		win: {
                			type: canSpot
                			filter: alias(weapon)
                		}
                	}
                """);
        assertEquals("trick", def.steps.getFirst().when.offers);
        assertEquals(List.of(), ScenarioValidator.validate(def, ScenarioTesting.library()));
    }

    @Test
    public void aMistakenSlotSaysWhereAndWhatToWrite() {
        assertRejected("slots: {\n x: {\n zone: attached\n}\n}", "slots.x.on: a card attached to another says which");
        assertRejected("slots: {\n x: {\n zone: attached\n on: nobody\n}\n}", "there is no card called 'nobody' among the scenario's cards");
        assertRejected("slots: {\n x: {\n zone: hand\n position: 2\n}\n}", "only a card in the draw deck has a place in it");
        assertRejected("slots: {\n aragorn: hand\n}", "already the name of one of the scenario's cards");
        assertRejected("slots: {\n x: table\n}", "is not a place for a slot's card");
        assertRejected("slots: {\n nowhere: site\n}", "There is no site called 'nowhere'");
        assertRejected("slots: {\n x: hand\n}\nsetup: {\n freeps: {\n hand: x\n}\n}", "setup.freeps.hand: 'x' is a slot");
        assertRejected("slots: {\n x: hand\n}\nsolutions: [\n {\n name: one\n}\n]", "solutions[1].picks: says nothing for the slot 'x'");
        assertRejected("slots: {\n x: hand\n}\nsolutions: [\n {\n name: one\n picks: {\n y: 1_2\n}\n}\n]", "there is no slot called 'y'");
    }

    @Test
    public void filtersTheSearchDoesNotUnderstandAreSaidSo() throws Exception {
        List<String> problems = ScenarioValidator.validate(ScenarioTesting.parse("pool: format:fotr_block\nslots: {\n x: {\n zone: hand\n filter: cardtype:condition\n}\n"
                + " y: {\n zone: hand\n filter: colour:red\n}\n z: {\n zone: minions\n}\n}"), ScenarioTesting.library());
        assertTrue(problems.toString(), problems.stream().anyMatch(one -> one.startsWith("pool:") && one.contains("not by a format")));
        assertTrue(problems.toString(), problems.stream().anyMatch(one -> one.startsWith("slots.x.filter:") && one.contains("in capitals")));
        assertTrue(problems.toString(), problems.stream().anyMatch(one -> one.startsWith("slots.y.filter:") && one.contains("has no 'colour'")));
        assertTrue(problems.toString(), problems.stream().anyMatch(one -> one.startsWith("slots.z:") && one.contains("seat: shadow")));
    }

    /* ---------------------------------------------------------------- whether a pick fits */

    @Test
    public void aPickIsCheckedAgainstItsBoundThePoolAndWhatTheGameAllows() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SLOTS);
        Map<String, String> checked = SlotBounds.check(def, GOOD, true, ScenarioTesting.library());
        assertTrue(checked.toString(), SlotBounds.allFit(checked));
        var library = ScenarioTesting.library();
        //Not its bound: a condition is not a possession
        assertTrue(SlotBounds.why(def, "weapon", "1_317", GOOD, library), SlotBounds.why(def, "weapon", "1_317", GOOD, library)
                .contains("is not a weapon for Aragorn"));
        //Not the pool's: set 4
        assertTrue(SlotBounds.why(def, "trick", "4_109", GOOD, library).contains("not one of the cards this puzzle is played with (set:1)"));
        //Not where the game would have it: an event among the characters, a site in a hand
        assertTrue(SlotBounds.why(def, "other", "1_102", GOOD, library).contains("only a companion or an ally is among the characters"));
        assertTrue(SlotBounds.why(def, "trick", "1_319", GOOD, library).contains("a site goes on the adventure path"));
        assertTrue(SlotBounds.why(def, "trick", "1_2", GOOD, library).contains("The One Ring"));
        //A second Aragorn in play: the declared one is already there
        assertTrue(SlotBounds.why(def, "other", "1_89", GOOD, library), SlotBounds.why(def, "other", "1_89", GOOD, library).contains("unique"));
        //...but in hand a copy of a unique card is no matter
        assertNull(SlotBounds.why(def, "trick", "1_89", GOOD, library));
        //Two slots in play with the same unique card
        Map<String, String> twice = new LinkedHashMap<>(GOOD);
        twice.put("friend", "1_288");
        ScenarioDefinition two = ScenarioTesting.parse(SLOTS.replace("filter: cardtype:COMPANION", "filter: cardtype:ALLY").replace("zone: characters", "zone: support"));
        twice.put("other", "1_288");
        assertTrue(SlotBounds.check(two, twice, true, library).toString(), SlotBounds.why(two, "other", "1_288", twice, library).contains("unique"));
        //An empty slot, when every slot is asked about; and a slot that is not there
        Map<String, String> some = picks("weapon", "1_112", "nothing", "1_2");
        Map<String, String> partly = SlotBounds.check(def, some, true, library);
        assertNull(partly.get("weapon"));
        assertTrue(partly.get("friend").contains("is empty"));
        assertTrue(partly.get("nothing").contains("no slot called"));
        assertFalse(SlotBounds.check(def, picks("weapon", "1_112"), false, library).containsKey("friend"));
    }

    @Test
    public void aSiteSlotTakesASiteOfTheSameNumber() throws Exception {
        //site2 is on the path; the solver may put another site 2 there
        ScenarioDefinition def = ScenarioTesting.parse("slots: {\n site2: {\n zone: site\n label: the site\n}\n}");
        assertEquals(Seat.FREEPS, def.slots.get("site2").seat());
        var library = ScenarioTesting.library();
        assertNull(SlotBounds.why(def, "site2", "1_327", Map.of(), library));
        assertTrue(SlotBounds.why(def, "site2", "1_340", Map.of(), library).contains("and this slot is site 2"));
        assertTrue(SlotBounds.why(def, "site2", "1_102", Map.of(), library).contains("this slot is for a site"));
        ScenarioTable table = table(def, Map.of("site2", "1_327"));
        assertEquals("1_327", table.Card("site2").getBlueprintId());
    }

    @Test
    public void picksAreTheSameCardByNameWhateverTheirPrinting() {
        var library = ScenarioTesting.library();
        assertTrue(SlotBounds.same(picks("a", "1_102"), picks("a", "1_102"), library));
        assertFalse(SlotBounds.same(picks("a", "1_102"), picks("a", "1_116"), library));
        assertFalse(SlotBounds.same(picks("a", "1_102"), picks("b", "1_102"), library));
    }

    /* ---------------------------------------------------------------- the game */

    @Test
    public void theGameIsBuiltWithThePicksWhereTheirSlotsSay() throws Exception {
        ScenarioTable table = table(ScenarioTesting.parse(SLOTS), GOOD);
        assertEquals(List.of(), table.Problems());
        PhysicalCard weapon = table.Card("weapon");
        assertEquals("1_112", weapon.getBlueprintId());
        assertEquals(Zone.ATTACHED, weapon.getZone());
        assertSame(table.Card("aragorn"), weapon.getAttachedTo());
        PhysicalCard friend = table.Card("friend");
        assertEquals(Zone.SUPPORT, friend.getZone());
        assertEquals(1, table.GetWoundsOn(table.Card("friend")));
        assertEquals(Zone.HAND, table.Card("trick").getZone());
        assertEquals(Zone.FREE_CHARACTERS, table.Card("other").getZone());
        //Second from the top of the draw deck, the setup's order kept around it
        List<? extends PhysicalCard> deck = table.gameState().getDeck(table.P1());
        assertSame(table.Card("gandalf"), deck.get(0));
        assertSame(table.Card("deep"), deck.get(1));
        assertSame(table.Card("boromir"), deck.get(2));
        assertEquals(GOOD, table.Picks());
    }

    @Test
    public void withoutPicksTheSlotsAreEmpty() throws Exception {
        ScenarioTable table = table(ScenarioTesting.parse(SLOTS), Map.of());
        assertEquals(List.of(), table.Problems());
        try {
            table.Card("weapon");
            fail("An empty slot has no card");
        } catch (IllegalStateException expected) {
            //as it should
        }
        assertTrue(table.gameState().getAttachedCards(table.Card("aragorn")).isEmpty());
    }

    @Test
    public void aPuzzleStartedWithoutPicksIsHeldWithNobodyAskedAnything() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SLOTS);
        LiveScenario held = ScenarioPuzzleTest.live(def, Map.of());
        assertTrue(held.Holding());
        LotroGameMediator mediator = new LotroGameMediator("1", held, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        ScenarioPuzzleTest.Client client = new ScenarioPuzzleTest.Client(mediator);
        Thread.sleep(50);
        client.poll();
        assertEquals("edit", client.info.getString("mode"));
        assertEquals("puzzle", client.info.getString("level"));
        JSONArray slots = client.info.getJSONArray("slots");
        assertEquals(5, slots.size());
        JSONObject weapon = slots.getJSONObject(0);
        assertEquals("weapon", weapon.getString("id"));
        assertEquals("a weapon for Aragorn", weapon.getString("label"));
        assertEquals("attached", weapon.getString("zone"));
        assertEquals("aragorn", weapon.getString("on"));
        assertEquals("set:1 cardtype:POSSESSION", weapon.getString("bound"));
        assertNull(weapon.get("pick"));
        assertEquals(2, slots.getJSONObject(1).getJSONObject("comes").getIntValue("tokens"));
        assertEquals(0, client.decisionsSeen);
        assertNull(client.end);
        assertFalse(mediator.isFinished());
    }

    @Test
    public void aGameWithPicksIsPlayedAndTakenBackWithThem() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SLOTS);
        LiveScenario scenario = ScenarioPuzzleTest.live(def, GOOD);
        assertFalse(scenario.Holding());
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        ScenarioPuzzleTest.Client client = new ScenarioPuzzleTest.Client(mediator);
        Thread.sleep(50);
        client.poll();
        assertEquals("play", client.info.getString("mode"));
        assertEquals("puzzle", client.info.getString("level"));
        assertEquals("1_112", client.info.getJSONArray("slots").getJSONObject(0).getString("pick"));
        assertEquals("Ranger's Sword", client.info.getJSONArray("slots").getJSONObject(0).getString("pickName"));
        assertTrue(client.decisionsSeen > 0);
        //A take-back builds the game again with the same picks
        LiveScenario again = scenario.Again(scenario.Table().Journal().upTo(0));
        assertEquals(GOOD, again.Picks());
    }

    /* ---------------------------------------------------------------- solutions */

    /** One slot, a skirmish event in hand; the archery of puzzle 10 in small, the wound left open for the solution. */
    private static final String SOLVED = """
            	kind: puzzle
            	coverage: guided
            	options: {
            		opponentDelayMs: 0
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	pool: set:1
            	slots: {
            		trick: {
            			zone: hand
            			filter: cardtype:EVENT
            		}
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
            	outcome: {
            		judge: {
            			at: endOf(archery)
            			win: {
            				type: cantSpot
            				filter: alias(frodo),wounded
            			}
            		}
            	}
            	solutions: [
            		{
            			name: key
            			note: Aragorn takes the arrow
            			picks: {
            				trick: 1_116
            			}
            			answers: [
            				choose(aragorn)
            			]
            		}
            		SECOND
            	]
            """;

    @Test
    public void solutionsAreReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SOLVED.replace("SECOND", ""));
        ScenarioDefinition.Solution key = def.solution("key");
        assertEquals(Map.of("trick", "1_116"), key.picks());
        assertEquals("choose(aragorn)", key.answers().getFirst().source());
        assertEquals("Aragorn takes the arrow", key.note());
        String written = ScenarioWriter.toHjson(ScenarioPuzzleTest.roundTrip(def), ScenarioTesting.library());
        assertTrue(written, written.contains("\t\t\t\t# Swordarm of the White Tower\n\t\t\t\ttrick: 1_116\n"));
        assertTrue(written, written.contains("\t\t\tanswers: [\n\t\t\t\tchoose(aragorn)\n\t\t\t]"));
    }

    @Test
    public void theSolutionLevelProvesEverySolutionReachesTheWin() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SOLVED.replace("SECOND", ""));
        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertEquals(report.toString(), ScenarioDefinition.RunnerLevel.SOLUTION, report.required);
        assertEquals(report.toString(), ScenarioDefinition.RunnerLevel.SOLUTION, report.reached);
        assertTrue(report.toString(), report.passed());
        assertTrue(report.solutions.containsKey("key"));
        assertNull(report.solutions.get("key"));
        assertTrue(report.toString(), report.toString().contains("(the solution's answer 1: choose(aragorn))"));
    }

    @Test
    public void aSolutionThatDoesNotSolveItFailsTheRun() throws Exception {
        String wrong = "{\n\t\t\tname: wrong\n\t\t\tpicks: {\n\t\t\t\ttrick: 1_102\n\t\t\t}\n\t\t\tanswers: [\n\t\t\t\tchoose(frodo)\n\t\t\t]\n\t\t}";
        ScenarioDefinition def = ScenarioTesting.parse(SOLVED.replace("SECOND", wrong));
        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertFalse(report.passed());
        assertNull(report.solutions.get("key"));
        assertTrue(report.solutions.get("wrong"), report.solutions.get("wrong").contains("judged failed at endOf(archery)"));
        assertTrue(report.failures.toString(), report.failures.stream().anyMatch(one -> one.startsWith("solution 'wrong':")));
        //An answer that does not fit its decision says which answer it was
        String unfit = "{\n\t\t\tname: unfit\n\t\t\tpicks: {\n\t\t\t\ttrick: 1_102\n\t\t\t}\n\t\t\tanswers: [\n\t\t\t\tchoose(gandalf)\n\t\t\t]\n\t\t}";
        ScenarioRunReport bad = ScenarioRunner.run(ScenarioTesting.parse(SOLVED.replace("SECOND", unfit)), ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(bad.failures.toString(), bad.failures.stream().anyMatch(one -> one.contains("its answer 1, choose(gandalf), does not fit")));
    }

    @Test
    public void aSolutionWhosePickItsSlotDoesNotTakeIsAMistakeInTheFile() throws Exception {
        String outside = "{\n\t\t\tname: outside\n\t\t\tpicks: {\n\t\t\t\ttrick: 4_109\n\t\t\t}\n\t\t}";
        List<String> problems = ScenarioValidator.validate(ScenarioTesting.parse(SOLVED.replace("SECOND", outside)), ScenarioTesting.library());
        assertTrue(problems.toString(), problems.stream().anyMatch(one -> one.startsWith("solutions[2].picks.trick:")));
    }

    @Test
    public void aPuzzleWithSlotsAndNoSolutionCannotBeProved() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SLOTS);
        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format("scenario"));
        assertTrue(report.failures.toString(), report.failures.stream().anyMatch(one -> one.contains("has slots and no solution")));
    }

    /* ---------------------------------------------------------------- unlisted solutions */

    private static final String ARROW_STEP = """
            	steps: [
            		{
            			when: human, archeryWound
            			allow: choose(aragorn)
            		}
            	]
            """;

    @Test
    public void aSolvedGameWithPicksNoSolutionListsIsUnlisted() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SOLVED.replace("SECOND", "") + ARROW_STEP);
        //Dagger Strike, where the key has Swordarm of the White Tower
        ScenarioPuzzleTest.Client other = ScenarioPuzzleTest.play(def, Map.of("trick", "1_102"));
        assertEquals("complete", other.end.getString("result"));
        assertTrue(other.end.getBooleanValue("unlisted"));
        assertTrue(((LiveScenario) other.mediator.getScenario()).Unlisted());

        ScenarioPuzzleTest.Client listed = ScenarioPuzzleTest.play(def, Map.of("trick", "1_116"));
        assertEquals("complete", listed.end.getString("result"));
        assertFalse(listed.end.getBooleanValue("unlisted"));
    }

    @Test
    public void aPuzzleWithNoSlotsIsNeverUnlisted() throws Exception {
        ScenarioPuzzleTest.Client client = ScenarioPuzzleTest.play(ScenarioTesting.parse(SOLVED.replace("SECOND", "")
                .replaceAll("(?s)\tpool: set:1\n\tslots: \\{.*?\n\t\\}\n", "")
                .replaceAll("(?s)\t\t\tpicks: \\{.*?\n\t\t\t\\}\n", "") + ARROW_STEP), Map.of());
        assertEquals("complete", client.end.getString("result"));
        assertFalse(client.end.getBooleanValue("unlisted"));
    }
}
