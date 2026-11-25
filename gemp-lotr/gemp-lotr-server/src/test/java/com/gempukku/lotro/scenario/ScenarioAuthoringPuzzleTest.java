package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The authoring tool and puzzles: what the state says of a puzzle (its judge, score, undo, slots, pool and
 * solutions), the patches that change them, and recording, playing and deleting a solution.
 */
public class ScenarioAuthoringPuzzleTest {
    private static final String AUTHOR = "Tester";

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File canon;
    private ScenarioAuthoring authoring;
    private final Player player = new Player(1, AUTHOR, "", "u", null, null, null, null, false);
    private final List<LotroGameMediator> games = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        File users = temp.newFolder("scenarios-user");
        canon = new File(users, "public");
        canon.mkdirs();
        ScenarioLibrary library = new ScenarioLibrary(temp.newFolder("shipped"), users, ScenarioTesting.library(),
                ScenarioAuthoringPuzzleTest::format, null, 0, System::currentTimeMillis);
        authoring = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioAuthoringPuzzleTest::format, null, (who, live) -> {
            LotroGameMediator mediator = new LotroGameMediator(String.valueOf(games.size() + 1), live, LotroServer.AUTHORING_TIMER, false);
            mediator.startGame();
            games.add(mediator);
            return mediator;
        });
    }

    private static LotroFormat format(String code) {
        try {
            return ScenarioTesting.format(code);
        } catch (CardNotFoundException exp) {
            return null;
        }
    }

    /** The author's browser, as far as the game can tell. */
    private class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        int channel;
        GameEvent decision;

        Client(LotroGameMediator mediator) throws Exception {
            this.mediator = mediator;
            mediator.signupUserForGame(player, this);
        }

        void poll() throws Exception {
            mediator.processVisitor(mediator.getCommunicationChannel(player, channel), channel, AUTHOR, this);
        }

        @Override
        public void visitChannelNumber(int channelNumber) {
            channel = channelNumber;
        }

        @Override
        public void visitClock(Map<String, Integer> secondsLeft) { }

        @Override
        public void visitGameEvent(GameEvent event) {
            if (event.getType() == GameEvent.Type.DECISION)
                decision = event;
        }

        void await() throws Exception {
            long deadline = System.currentTimeMillis() + 10_000;
            while (decision == null && System.currentTimeMillis() < deadline) {
                poll();
                if (decision == null)
                    Thread.sleep(5);
            }
        }

        void answer(String answer) throws Exception {
            assertNotNull("The game is not asking the author anything", decision);
            GameEvent answering = decision;
            decision = null;
            mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
            poll();
        }
    }

    private JSONObject state() {
        return JSON.parseObject(JSON.toJSONString(authoring.state(player)));
    }

    private JSONObject puzzle() {
        return state().getJSONObject("puzzle");
    }

    /** The archery of puzzle 10, made small, with one slot (a card in hand) and one solution. */
    private static final String PUZZLE = """
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
            			label: a skirmish event
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
            			picks: {
            				trick: 1_116
            			}
            			answers: [
            				choose(aragorn)
            			]
            		}
            	]
            """;

    private void open(String rest) throws Exception {
        Files.createDirectories(new File(canon, "puzzles").toPath());
        Files.writeString(new File(canon, "puzzles/arrow.hjson").toPath(), ScenarioTesting.scenario(rest).replace("id: test", "id: arrow"),
                StandardCharsets.UTF_8);
        authoring.open(player, "arrow", true);
    }

    private LiveScenario live() {
        return games.getLast().getScenario();
    }

    @Test
    public void theStateSaysThePuzzleAndTheDraftIsPlayedWithItsFirstSolutionsPicks() throws Exception {
        open(PUZZLE);
        JSONObject puzzle = puzzle();
        assertFalse(puzzle.getBooleanValue("undo"));
        assertNull(puzzle.get("undoSaid"));
        JSONObject judge = puzzle.getJSONObject("judge");
        assertEquals("endOf(archery)", judge.getString("at"));
        assertEquals("endOf", judge.getString("moment"));
        assertEquals("archery", judge.getString("phase"));
        assertTrue(judge.getString("win"), judge.getString("win").contains("type: cantSpot"));
        assertEquals(List.of("startOf", "endOf", "endOfTurn", "skirmishResolution"), puzzle.getJSONObject("moments").getJSONArray("moments").toJavaList(String.class));
        assertNull(puzzle.get("score"));
        assertEquals("set:1", puzzle.getString("pool"));
        JSONArray slots = puzzle.getJSONArray("slots");
        assertEquals("trick", slots.getJSONObject(0).getString("id"));
        assertEquals("1_116", slots.getJSONObject(0).getString("pick"));
        assertEquals("set:1 cardtype:EVENT", slots.getJSONObject(0).getString("bound"));
        assertTrue(puzzle.getJSONArray("slotZones").contains("deadPile"));
        JSONObject key = puzzle.getJSONArray("solutions").getJSONObject(0);
        assertEquals("key", key.getString("name"));
        assertEquals("Swordarm of the White Tower", key.getJSONObject("pickNames").getString("trick"));
        assertEquals(List.of("choose(aragorn)"), key.getJSONArray("answers").toJavaList(String.class));
        assertEquals("key", puzzle.getString("playingWith"));
        assertNull(puzzle.get("recording"));
        //The game has the pick in the hand
        assertEquals("1_116", live().Table().Card("trick").getBlueprintId());
        assertFalse(live().Holding());
    }

    @Test
    public void aDraftWithSlotsAndNoSolutionIsPlayedEmptyAndSaysSo() throws Exception {
        open(PUZZLE.replaceAll("(?s)\tsolutions: \\[.*", ""));
        assertTrue(state().getString("notice"), state().getString("notice").contains("has no solution yet"));
        assertNull(puzzle().getJSONArray("slots").getJSONObject(0).get("pick"));
        assertFalse("An author's game is not held", live().Holding());
    }

    @Test
    public void thePuzzlesPartsAreChangedByTopLevelPatchesAndSaved() throws Exception {
        open(PUZZLE);
        authoring.edit(player, """
                {"top": {
                  "options": {"opponentDelayMs": 0, "autoPass": "true", "undo": "on"},
                  "pool": "set:1-2",
                  "slots": {"trick": {"zone": "hand", "filter": "cardtype:EVENT culture:GONDOR", "label": "a Gondor event"}},
                  "outcome": {"judge": {"at": "startOf(regroup)", "win": "{\\n type: canSpot\\n filter: alias(aragorn)\\n}"},
                              "score": {"value": "wounds:aragorn", "better": "lower", "label": "Wounds"}}
                }, "opponent": {"set": {"fallback": "eager"}}}
                """, null);
        JSONObject puzzle = puzzle();
        assertTrue(puzzle.getBooleanValue("undo"));
        assertEquals("on", puzzle.getString("undoSaid"));
        assertEquals("startOf(regroup)", puzzle.getJSONObject("judge").getString("at"));
        assertEquals("wounds:aragorn", puzzle.getJSONObject("score").getString("value"));
        assertEquals("lower", puzzle.getJSONObject("score").getString("better"));
        assertEquals("set:1-2 cardtype:EVENT culture:GONDOR", puzzle.getJSONArray("slots").getJSONObject(0).getString("bound"));
        assertEquals("eager", state().getJSONObject("opponent").getString("fallback"));
        String source = authoring.source(player);
        assertTrue(source, source.contains("undo: on"));
        assertTrue(source, source.contains("judge: {\n\t\t\tat: startOf(regroup)"));
        assertTrue(source, source.contains("fallback: eager"));
        //Saved, and opened again: the same
        authoring.save(player, null, true, true);
        authoring.close(player);
        authoring.open(player, "arrow", true);
        assertEquals("startOf(regroup)", puzzle().getJSONObject("judge").getString("at"));
        assertEquals("a Gondor event", puzzle().getJSONArray("slots").getJSONObject(0).getString("label"));
        //A mistake is said, and nothing changes
        try {
            authoring.edit(player, "{\"top\": {\"slots\": {\"trick\": {\"zone\": \"table\"}}}}", null);
            fail("A slot in no place is refused");
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("is not a place for a slot's card"));
        }
    }

    @Test
    public void aPlayIsRecordedAsASolutionAndPlayedAgainAndDeleted() throws Exception {
        open(PUZZLE);
        //(the passes the game makes by itself past the script are written into it as the draft opens, as they always are)
        String steps = state().getJSONObject("draft").getJSONArray("steps").toString();
        authoring.solution(player, "record", "dagger", "{\"trick\": \"1_102\"}");
        assertEquals("dagger", puzzle().getString("recording"));
        assertEquals("dagger", puzzle().getString("playingWith"));
        assertEquals("1_102", live().Table().Card("trick").getBlueprintId());
        Client client = new Client(games.getLast());
        //Passing until the archery wound, which goes on Aragorn
        long deadline = System.currentTimeMillis() + 10_000;
        while (live().Result() == null && System.currentTimeMillis() < deadline) {
            client.await();
            if (client.decision == null)
                break;
            client.answer(client.decision.getAwaitingDecision().getText().contains("archery wound")
                    ? String.valueOf(live().Table().Card("aragorn").getCardId()) : "");
            Thread.sleep(20);
        }
        JSONObject dagger = null;
        for (Object one : puzzle().getJSONArray("solutions"))
            if ("dagger".equals(((JSONObject) one).getString("name")))
                dagger = (JSONObject) one;
        assertNotNull(dagger);
        List<String> answers = dagger.getJSONArray("answers").toJavaList(String.class);
        assertEquals(answers.toString(), "choose(aragorn)", answers.getLast());
        assertTrue(answers.toString(), answers.subList(0, answers.size() - 1).stream().allMatch("pass"::equals));
        assertEquals(Map.of("trick", "1_102"), dagger.getJSONObject("picks").toJavaObject(Map.class));
        //Nothing was written into the script
        assertEquals(steps, state().getJSONObject("draft").getJSONArray("steps").toString());
        assertTrue(authoring.source(player).contains("name: dagger"));
        authoring.solution(player, "stop", null, null);
        assertNull(puzzle().get("recording"));

        //Played again: the script and the solution's answers play themselves to the judge
        authoring.solution(player, "play", "dagger", null);
        deadline = System.currentTimeMillis() + 5000;
        while (live().Result() == null && System.currentTimeMillis() < deadline)
            Thread.sleep(5);
        assertEquals(LiveScenario.Result.COMPLETE, live().Result());
        assertTrue(state().getString("notice"), state().getString("notice").contains("Played solution 'dagger': " + answers.size() + " of its " + answers.size() + " answers."));
        assertTrue(puzzle().getJSONObject("verdict").getBooleanValue("won"));

        authoring.solution(player, "delete", "dagger", null);
        assertEquals(1, puzzle().getJSONArray("solutions").size());
        try {
            authoring.solution(player, "record", "bad", "{\"trick\": \"4_109\"}");
            fail("A pick its slot does not take is refused");
        } catch (ScenarioAuthoring.AuthoringException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("not one of the cards this puzzle is played with"));
        }
    }

    /** Decipher's puzzle 10, as the fixtures have it, opened in the authoring tool. */
    private void openKeep() throws Exception {
        Files.createDirectories(new File(canon, "puzzles").toPath());
        Files.writeString(new File(canon, "puzzles/keep.hjson").toPath(),
                Files.readString(new File("src/test/resources/scenarios/puzzles/10-defending-the-keep.hjson").toPath(), StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);
        authoring.open(player, "decipher-10-defending-the-keep", true);
    }

    @Test
    public void playingASolutionGoesAsFarAsTheRunnerDoes() throws Exception {
        //(the eager opponent answers by rule past the end of the script; that is not where a solution's play stops)
        openKeep();
        authoring.solution(player, "play", "published", null);
        long deadline = System.currentTimeMillis() + 10_000;
        while (live().Result() == null && System.currentTimeMillis() < deadline)
            Thread.sleep(5);
        assertEquals(state().getString("notice"), LiveScenario.Result.COMPLETE, live().Result());
        assertTrue(state().getString("notice"), state().getString("notice").contains("Played solution 'published': 4 of its 4 answers."));
        assertTrue(puzzle().getJSONObject("verdict").getBooleanValue("won"));
    }

    @Test
    public void aPuzzlesDraftWritesNoStepsByItselfAndCheckSaysWhenAStepTakesASolutionsDecision() throws Exception {
        openKeep();
        //"Write down what I do" is off in a puzzle's draft until the author says otherwise
        assertFalse(state().getBooleanValue("recording"));
        assertFalse(state().getBooleanValue("recordingSaid"));
        String steps = state().getJSONObject("draft").getJSONArray("steps").toString();
        JSONObject check = JSON.parseObject(JSON.toJSONString(authoring.check(player)));
        assertTrue(check.getString("report"), check.getBooleanValue("passed"));
        //(null for a solution that solves it: the map as the page is sent it, nulls and all)
        Map<?, ?> verdicts = (Map<?, ?>) authoring.check(player).get("solutions");
        assertTrue(verdicts.toString(), verdicts.containsKey("published"));
        assertNull(verdicts.get("published"));
        //The author passes in the maneuver phase: nothing is written
        Client client = new Client(games.getLast());
        client.await();
        client.answer("");
        Thread.sleep(50);
        assertEquals(steps, state().getJSONObject("draft").getJSONArray("steps").toString());

        //Turned on, and played again from the start: the pass is written as a step, and that step takes the decision
        // the published solution's first answer was for
        authoring.settings(player, true, null, null);
        assertTrue(state().getBooleanValue("recording"));
        assertTrue(state().getBooleanValue("recordingSaid"));
        authoring.solution(player, "use", "published", null);
        client = new Client(games.getLast());
        client.await();
        client.answer("");
        Thread.sleep(50);
        assertNotEquals(steps, state().getJSONObject("draft").getJSONArray("steps").toString());
        check = JSON.parseObject(JSON.toJSONString(authoring.check(player)));
        assertFalse(check.getBooleanValue("passed"));
        String verdict = check.getJSONObject("solutions").getString("published");
        assertNotNull(verdict);
        assertTrue(verdict, verdict.startsWith("does not solve it: "));
        assertTrue(verdict, verdict.contains("where the solution's answer 1, use(condition), would have gone"));
    }

    @Test
    public void aSingleValuePartGivenAsTextStaysTheText() throws Exception {
        open(PUZZLE);
        //(HJSON would read set:1-6 by itself as the object {set: "1-6"})
        authoring.edit(player, "{\"topText\": {\"pool\": \"set:1-6\", \"title\": \"Archery: the hard way\"}}", null);
        assertEquals("set:1-6", puzzle().getString("pool"));
        assertEquals("Archery: the hard way", state().getJSONObject("draft").getString("title"));
        //...with its name in front, the same; and a part that is an object is still read as one, braces or not
        authoring.edit(player, "{\"topText\": {\"pool\": \"pool: set:1-2\", \"options\": \"opponentDelayMs: 0\\nautoPass: true\\nundo: on\"}}", null);
        assertEquals("set:1-2", puzzle().getString("pool"));
        assertTrue(puzzle().getBooleanValue("undo"));
    }

    @Test
    public void aSlotIsRenamedAsACardIsEverywhere() throws Exception {
        open(PUZZLE + """
                	steps: [
                		{
                			when: human, action, offers(trick)
                			allow: play(trick), pass
                		}
                	]
                """);
        authoring.renameCard(player, "trick", "gambit");
        String source = authoring.source(player);
        assertTrue(source, source.contains("\tslots: {\n\t\tgambit: {"));
        assertTrue(source, source.contains("gambit: 1_116"));
        assertTrue(source, source.contains("offers(gambit)"));
        assertTrue(source, source.contains("play(gambit)"));
        assertFalse(source, source.contains("trick"));
    }

    @Test
    public void aFileWithAMistakenSlotOpensWithTheMistakeLeftOut() {
        String text = ScenarioTesting.scenario(PUZZLE.replace("filter: cardtype:EVENT", "filter: cardtype:event"));
        ScenarioSalvage.Result read = ScenarioSalvage.read(text, def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(read.errors().toString(), read.def());
        assertEquals("slots.trick.filter", read.leftOut().getFirst().where());
        assertNull(read.def().slots.get("trick").filter());
        //A judge with a moment that is none goes, and the rest is read
        ScenarioSalvage.Result judged = ScenarioSalvage.read(ScenarioTesting.scenario(PUZZLE.replace("endOf(archery)", "endOf(lunch)")),
                def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(judged.errors().toString(), judged.def());
        assertNull(judged.def().outcome.judge);
    }
}
