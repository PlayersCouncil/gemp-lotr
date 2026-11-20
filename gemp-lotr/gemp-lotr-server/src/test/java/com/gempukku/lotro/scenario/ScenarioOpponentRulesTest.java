package com.gempukku.lotro.scenario;

import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * The opponent answering by rule rather than by naming everything: assign(...) with rest and ringBearer, the
 * opponent's leftovers, orders that are good for one decision, and the sandbox opponent.
 */
public class ScenarioOpponentRulesTest {
    /**
     * Plays a scenario's game the way the headless runner does (the script's answer where it has one, as little as can
     * be done where it has none) until a ruling the test wants to look at.
     * @param instead What to answer in place of the script, where this says (null: the script's answer).
     */
    static Ruling playTo(ScenarioSession session, Predicate<Ruling> stop, java.util.function.Function<Ruling, String> instead) {
        ScenarioTable table = session.Table();
        for (int i = 0; i < 1000; i++) {
            if (table.GameIsFinished())
                return null;
            Ruling ruling = session.Ruling(table.P1()) != null ? session.Ruling(table.P1()) : session.Ruling(table.P2());
            if (ruling == null)
                return null;
            if (stop.test(ruling))
                return ruling;
            if (ruling.step() != null && ruling.step().pause) {
                session.Read(ruling.player(), ruling.step().number, null);
                continue;
            }
            String answer = instead == null ? null : instead.apply(ruling);
            if (answer == null)
                answer = ruling.answer() != null ? ruling.answer() : AnswerCompiler.leastAnswer(ruling.decision());
            table.PlayerDecided(ruling.player(), answer);
        }
        throw new AssertionError("The game went on for more than 1000 decisions");
    }

    static Ruling playTo(ScenarioSession session, Predicate<Ruling> stop) {
        return playTo(session, stop, null);
    }

    static ScenarioSession session(String rest) throws Exception {
        return session(ScenarioTesting.parse(rest));
    }

    static ScenarioSession session(ScenarioDefinition def) throws Exception {
        ScenarioTable table = new ScenarioTable(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), ScenarioRunner.HUMAN, ScenarioRunner.OPPONENT);
        ScenarioSession session = new ScenarioSession(table);
        table.Start();
        return session;
    }

    private static boolean assigning(Ruling ruling, String player) {
        return ruling.player().equals(player) && ruling.decision().getDecisionType() == AwaitingDecisionType.ASSIGN_MINIONS;
    }

    /** What an answer to an assignment gave out, in the scenario's words. */
    private static String said(ScenarioSession session, Ruling ruling) {
        return AnswerCompiler.decompile(session.Table(), ruling.decision(), ruling.answer());
    }

    /** Three minions on the table at the start, and Aragorn beside Frodo; the person passes their assignment. */
    private static final String THREE_MINIONS = """
            	options: {
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            		ORDERS
            	}
            	setup: {
            		freeps: {
            			characters: aragorn
            		}
            		shadow: {
            			minions: runner1, runner2, bowman
            		}
            	}
            	steps: [
            		{
            			when: freeps, assign
            			answer: pass
            		}
            	]
            """;

    private static String threeMinions(String opponent) {
        return THREE_MINIONS.replace("ORDERS", opponent);
    }

    @Test
    public void restIsEveryMinionTheAnswerDoesNotName() throws Exception {
        ScenarioSession session = session(threeMinions("""
                orders: [
                			{
                				when: opponent, assign
                				answer: assign(frodo: rest, aragorn: runner2)
                			}
                		]"""));
        Ruling ruling = playTo(session, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertNotNull(ruling);
        //(the minions a pairing names are its own wherever it comes: rest takes what nobody named)
        assertEquals("assign(frodo: runner1 + bowman, aragorn: runner2)", said(session, ruling));
    }

    @Test
    public void ringBearerIsWhoeverBearsTheRing() throws Exception {
        ScenarioSession session = session(threeMinions("""
                orders: [
                			{
                				when: opponent, assign
                				answer: assign(aragorn: runner1, ringBearer: all)
                			}
                		]"""));
        Ruling ruling = playTo(session, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertEquals("assign(aragorn: runner1, frodo: runner2 + bowman)", said(session, ruling));
    }

    @Test
    public void aSecondRestGetsWhatTheFirstCouldNotTake() throws Exception {
        //The person's own assigning: one minion to a character without defender, so Frodo's rest gives him one and
        // leaves the others; the second rest is Aragorn's, whose defender +1 gives him room for both of them
        ScenarioSession session = session("""
                	options: {
                		autoPass: true
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: runner1, runner2, bowman
                		}
                	}
                	steps: [
                		{
                			when: freeps, assign
                			answer: assign(frodo: rest, aragorn: rest)
                		}
                	]
                """);
        Ruling ruling = playTo(session, r -> assigning(r, ScenarioRunner.HUMAN));
        assertEquals("assign(frodo: runner1, aragorn: runner2 + bowman)", said(session, ruling));
        //...and the game takes it
        session.Table().PlayerDecided(ruling.player(), ruling.answer());
        assertEquals(2, session.Table().gameState().getAssignments().size());
        assertEquals(2, session.Table().gameState().getAssignments().stream()
                .filter(a -> a.getFellowshipCharacter() == session.Table().Card("aragorn")).findFirst().orElseThrow().getShadowCharacters().size());
    }

    @Test
    public void aRestThatFindsNothingLeftIsStillAnAnswer() throws Exception {
        ScenarioSession session = session(threeMinions("""
                orders: [
                			{
                				when: opponent, assign
                				answer: assign(aragorn: runner1 + runner2 + bowman, frodo: rest)
                			}
                		]"""));
        Ruling ruling = playTo(session, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertEquals("assign(aragorn: runner1 + runner2 + bowman)", said(session, ruling));
    }

    @Test
    public void leftoversGoToTheirCharacterWhenNothingAnswers() throws Exception {
        //Without leftovers: the Shadow player assigns nothing (fallback pass), and nothing is fought
        ScenarioSession without = session(threeMinions(""));
        Ruling none = playTo(without, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertEquals("", none.answer() == null ? AnswerCompiler.leastAnswer(none.decision()) : none.answer());

        ScenarioSession with = session(threeMinions("leftovers: ringBearer"));
        Ruling ruling = playTo(with, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertEquals("assign(frodo: runner1 + runner2 + bowman)", said(with, ruling));

        ScenarioSession aragorn = session(threeMinions("leftovers: aragorn"));
        assertEquals("assign(aragorn: runner1 + runner2 + bowman)", said(aragorn, playTo(aragorn, r -> assigning(r, ScenarioRunner.OPPONENT))));
    }

    @Test
    public void leftoversComeAfterWhatAnOrderOrAStepAssigns() throws Exception {
        ScenarioSession order = session(threeMinions("""
                leftovers: ringBearer
                		orders: [
                			{
                				when: opponent, assign
                				answer: assign(aragorn: runner2)
                			}
                		]"""));
        assertEquals("assign(aragorn: runner2, frodo: runner1 + bowman)", said(order, playTo(order, r -> assigning(r, ScenarioRunner.OPPONENT))));

        //A step of the opponent's that assigns is followed by the leftovers too; one that passes, passes
        String steps = """
                	options: {
                		autoPass: true
                	}
                	opponent: {
                		fallback: pass
                		leftovers: ringBearer
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: runner1, runner2, bowman
                		}
                	}
                	steps: [
                		{
                			when: freeps, assign
                			answer: pass
                		}
                		{
                			when: opponent, assign
                			answer: ANSWER
                		}
                	]
                """;
        ScenarioSession assigns = session(steps.replace("ANSWER", "assign(aragorn: bowman)"));
        assertEquals("assign(aragorn: bowman, frodo: runner1 + runner2)", said(assigns, playTo(assigns, r -> assigning(r, ScenarioRunner.OPPONENT))));
        ScenarioSession passes = session(steps.replace("ANSWER", "pass"));
        assertEquals("", playTo(passes, r -> assigning(r, ScenarioRunner.OPPONENT)).answer());
    }

    @Test
    public void assignByRuleIsReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(threeMinions("""
                leftovers: ringBearer
                		orders: [
                			{
                				when: opponent, assign
                				answer: assign(aragorn: runner2, ringBearer: rest), assign(frodo: all)
                			}
                		]"""));
        assertEquals(ScenarioDefinition.RING_BEARER, def.leftovers);
        assertEquals(java.util.List.of(ScenarioDefinition.REST), def.orders.getFirst().answers().getFirst().assignments().get(ScenarioDefinition.RING_BEARER));
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("leftovers: ringBearer"));
        assertTrue(written, written.contains("answer: assign(aragorn: runner2, ringBearer: rest), assign(frodo: all)"));
        ScenarioDefinition again = ScenarioParser.parse(written);
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(again).toString());

        //A card that is called rest is that card
        ScenarioDefinition named = ScenarioParser.parse(ScenarioTesting.scenario("""
                	steps: [
                		{
                			when: freeps, assign
                			answer: assign(frodo: rest)
                		}
                	]
                """).replace("runner2: 1_178", "rest: 1_178"));
        assertEquals(java.util.List.of("rest"), named.steps.getFirst().answers.getFirst().assignments().get("frodo"));

        for (String[] wrong : new String[][] {
                {"leftovers: runner1", "is a Shadow card"},
                {"leftovers: nobody", "there is no card called 'nobody'"},
                {"plays: runner1", "goes with 'fallback: sandbox'"},
        }) {
            try {
                ScenarioTesting.parse(threeMinions(wrong[0]));
                fail(wrong[0]);
            } catch (ScenarioFormatException exp) {
                assertTrue(exp.getMessage(), exp.getMessage().contains(wrong[1]) && exp.getMessage().startsWith("opponent."));
            }
        }
        try {
            ScenarioTesting.parse(threeMinions("""
                orders: [
                			{
                				when: opponent, assign
                				answer: assign(frodo: runner1, frodo: runner2)
                			}
                		]"""));
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("is given minions twice"));
        }
    }

    @Test
    public void leftoversAreRenamedWithTheirCard() throws Exception {
        org.hjson.JsonObject json = ScenarioWriter.toJson(ScenarioTesting.parse(threeMinions("leftovers: aragorn")));
        AliasRename.rename(json, "aragorn", "strider");
        assertEquals("strider", json.get("opponent").asObject().getString("leftovers", null));
    }

    /**
     * The request's report about tutorial 2: a player who assigns nobody gets no fight, because nothing answers the
     * Shadow player's assignment but the fallback (pass).  Played on the author's own copy of it, where there is one
     * (-Dscenario.fixtures=the folder with public/tutorials in it).
     */
    @Test
    public void tutorialTwosUnassignedMinionsFightWithLeftovers() throws Exception {
        String folder = System.getProperty("scenario.fixtures");
        Assume.assumeTrue(folder != null);
        Path file = Path.of(folder, "public", "tutorials", "basics-02-items-damage.hjson");
        Assume.assumeTrue(Files.exists(file));
        String text = Files.readString(file, StandardCharsets.UTF_8);
        for (boolean leftovers : new boolean[] {false, true}) {
            ScenarioDefinition def = ScenarioParser.parse(leftovers ? text.replace("fallback: pass", "fallback: pass\n\t\tleftovers: ringBearer") : text);
            ScenarioSession session = session(def);
            //The person assigns nobody at the first assignment of turn 3 (step 63.1, which allows anything)
            boolean[] passed = {false};
            Ruling shadows = playTo(session, r -> passed[0] && assigning(r, ScenarioRunner.OPPONENT), r -> {
                if (assigning(r, ScenarioRunner.HUMAN) && session.Table().gameState().getTurnNumber() == 3 && !passed[0]) {
                    passed[0] = true;
                    return "";
                }
                return null;
            });
            assertNotNull(shadows);
            String answer = shadows.answer() == null ? AnswerCompiler.leastAnswer(shadows.decision()) : shadows.answer();
            if (leftovers)
                assertEquals("assign(frodo: troop + smith + lurtz)", AnswerCompiler.decompile(session.Table(), shadows.decision(), answer));
            else
                assertEquals("Nothing answers the Shadow player's assignment: it passes, and no minion fights", "", answer);
        }
    }

    private static final String ONCE = """
            	options: {
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            		orders: [
            			{
            				ONCE
            				note: Kept with the order, and never shown to anybody.
            				when: opponent, action, phase(shadow)
            				answer: play(runner1), play(runner2)
            			}
            		]
            	}
            	setup: {
            		freeps: {
            			characters: aragorn, gandalf, boromir
            		}
            		shadow: {
            			hand: runner1, runner2
            		}
            	}
            """;

    @Test
    public void anOrderThatIsOnceAnswersOneDecision() throws Exception {
        //Without 'once' the order plays a Runner each time it is asked, which is both of them
        ScenarioSession every = session(ONCE.replace("ONCE", ""));
        assertNotNull(playTo(every, r -> assigning(r, ScenarioRunner.HUMAN)));
        assertEquals(com.gempukku.lotro.common.Zone.SHADOW_CHARACTERS, every.Table().Card("runner2").getZone());
        assertEquals(java.util.Set.of(), every.Director().ordersSpent());

        ScenarioSession once = session(ONCE.replace("ONCE", "once: true"));
        assertNotNull(playTo(once, r -> assigning(r, ScenarioRunner.HUMAN)));
        assertEquals(com.gempukku.lotro.common.Zone.SHADOW_CHARACTERS, once.Table().Card("runner1").getZone());
        assertEquals("Used up by the first, the order says nothing to the second", com.gempukku.lotro.common.Zone.HAND,
                once.Table().Card("runner2").getZone());
        assertEquals(java.util.Set.of(0), once.Director().ordersSpent());

        //A game that follows this one's journal (a checkpoint, a fresh game of a draft) comes out the same
        ScenarioDefinition def = once.Definition();
        ScenarioTable table = new ScenarioTable(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), ScenarioRunner.HUMAN, ScenarioRunner.OPPONENT);
        ScenarioSession again = new ScenarioSession(table);
        table.Follow(once.Table().Journal());
        table.Start();
        assertNull(table.FollowProblem());
        assertEquals(com.gempukku.lotro.common.Zone.HAND, table.Card("runner2").getZone());
        assertEquals(java.util.Set.of(0), again.Director().ordersSpent());
        //...and goes on the same from there
        assertNotNull(playTo(again, r -> assigning(r, ScenarioRunner.HUMAN)));
        assertEquals(com.gempukku.lotro.common.Zone.HAND, table.Card("runner2").getZone());
    }

    @Test
    public void anOrdersOnceNoteAndNameAreReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(ONCE.replace("ONCE", "once: true\n\t\t\t\tid: runners"));
        ScenarioDefinition.Order order = def.orders.getFirst();
        assertTrue(order.once());
        assertEquals("runners", order.id());
        assertEquals("Kept with the order, and never shown to anybody.", order.note());
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("id: runners") && written.contains("once: true") && written.contains("note: Kept with the order"));
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());
        //(an order with none of them is written as it always was)
        assertFalse(ScenarioWriter.toJson(ScenarioTesting.parse(threeMinions(""))).toString().contains("once"));
        try {
            ScenarioTesting.parse(ONCE.replace("ONCE", "id: 2nd"));
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("opponent.orders[1].id:"));
        }
    }

    private static final String SANDBOX = """
            	options: {
            		autoPass: true
            	}
            	opponent: {
            		fallback: sandbox
            		PLAYS
            	}
            	setup: {
            		freeps: {
            			characters: aragorn, gandalf, boromir
            		}
            		shadow: {
            			hand: runner1, runner2, marksman
            		}
            	}
            	steps: [
            		{
            			when: freeps, assign
            			answer: pass
            		}
            	]
            """;

    @Test
    public void theSandboxOpponentPlaysWhatItCanAndAssignsItAllToTheRingBearer() throws Exception {
        ScenarioSession session = session(SANDBOX.replace("PLAYS", ""));
        Ruling assigning = playTo(session, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertNotNull(assigning);
        ScenarioTable table = session.Table();
        java.util.List<String> played = new java.util.ArrayList<>();
        for (String minion : java.util.List.of("runner1", "runner2", "marksman"))
            if (table.Card(minion).getZone() == com.gempukku.lotro.common.Zone.SHADOW_CHARACTERS)
                played.add(minion);
        assertFalse("It played what it could afford", played.isEmpty());
        assertEquals("assign(frodo: " + String.join(" + ", played) + ")", said(session, assigning));

        //The same game, the same answers
        ScenarioSession again = session(SANDBOX.replace("PLAYS", ""));
        assertEquals(said(session, assigning), said(again, playTo(again, r -> assigning(r, ScenarioRunner.OPPONENT))));

        //Told which cards it may play, it plays only those
        ScenarioSession told = session(SANDBOX.replace("PLAYS", "plays: runner2"));
        Ruling only = playTo(told, r -> assigning(r, ScenarioRunner.OPPONENT));
        assertEquals("assign(frodo: runner2)", said(told, only));
        assertEquals(com.gempukku.lotro.common.Zone.HAND, told.Table().Card("runner1").getZone());

        //...and the headless runner plays it as it plays a game with fallback: pass
        ScenarioRunReport report = ScenarioTesting.run(SANDBOX.replace("PLAYS", "").replace("answer: pass\n\t\t}", "answer: pass\n\t\t\tend: complete\n\t\t}"));
        assertTrue(report.toString(), report.passed());
    }

    @Test
    public void aSandboxIsReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(SANDBOX.replace("PLAYS", "plays: marksman, runner1"));
        assertEquals(ScenarioDefinition.Fallback.SANDBOX, def.fallback);
        assertEquals(java.util.List.of("marksman", "runner1"), def.sandboxPlays);
        String written = ScenarioWriter.toHjson(def, null);
        assertTrue(written, written.contains("fallback: sandbox") && written.contains("plays: marksman, runner1"));
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());
        org.hjson.JsonObject json = ScenarioWriter.toJson(def);
        AliasRename.rename(json, "marksman", "archer");
        assertEquals("archer, runner1", json.get("opponent").asObject().getString("plays", null));
        try {
            ScenarioTesting.parse(SANDBOX.replace("PLAYS", "plays: aragorn"));
            fail();
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("opponent.plays: 'aragorn' is one of the player's own cards"));
        }
    }

    @Test
    public void aMistakenRuleIsLeftOutToOpenTheFile() {
        ScenarioSalvage.Result result = ScenarioSalvage.read(ScenarioTesting.scenario(threeMinions("leftovers: nobody\n\t\tplays: runner1")), def -> java.util.List.of());
        assertNotNull(result.errors().toString(), result.def());
        assertNull(result.def().leftovers);
        assertEquals(java.util.List.of("opponent.leftovers", "opponent.plays"), result.leftOut().stream().map(ScenarioSalvage.LeftOut::where).toList());
    }

    @Test
    public void thePlayersOwnAllowCanAssignByRule() throws Exception {
        //The page is sent the answer it comes to now, as it is sent any allowed answer
        ScenarioSession session = session("""
                	options: {
                		autoPass: true
                	}
                	setup: {
                		freeps: {
                			characters: aragorn
                		}
                		shadow: {
                			minions: runner1, runner2
                		}
                	}
                	steps: [
                		{
                			when: freeps, assign
                			allow: assign(frodo: runner1, aragorn: rest)
                			end: complete
                		}
                	]
                """);
        Ruling ruling = playTo(session, r -> assigning(r, ScenarioRunner.HUMAN));
        assertEquals(1, ruling.shown().allowed().size());
        assertEquals("assign(frodo: runner1, aragorn: runner2)",
                AnswerCompiler.decompile(session.Table(), ruling.decision(), ruling.shown().allowed().getFirst()));
    }
}
