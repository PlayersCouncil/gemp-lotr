package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A sandbox in a live game (ScenarioSandbox): it opens stopped; Stop, edits and Start; taking it back across them; a
 * game rebuilt from its journal alone; the edits a player may send; what the player may see; how it ends.  The
 * sandbox is the default one the game ships (scenarios/sandboxes/sandbox.hjson): each seat Frodo 1_290, The One Ring
 * 1_2 and the Fellowship block's sites.
 */
public class ScenarioSandboxTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    private static final String HUMAN = LiveScenarioTest.HUMAN;

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    static ScenarioDefinition sandbox() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(Files.readString(
                new File("../gemp-lotr-cards/src/main/resources/scenarios/sandboxes/sandbox.hjson").toPath(), StandardCharsets.UTF_8));
        def.opponentDelayMs = 0;
        return def;
    }

    private static LiveScenario live(ScenarioDefinition def, GameJournal earlier) throws Exception {
        return new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, earlier);
    }

    /** A sandbox game with the person's page connected and caught up. */
    private record Game(LotroGameMediator mediator, LiveScenarioTest.Client client) {
        LiveScenario scenario() {
            return mediator.getScenario();
        }

        ScenarioSandbox sandbox() {
            return scenario().Sandbox();
        }

        Map<String, Object> state() {
            return mediator.sandboxState(HUMAN);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> card(String alias) {
            for (Map<String, Object> card : (List<Map<String, Object>>) state().get("cards"))
                if (card.get("alias").equals(alias))
                    return card;
            return null;
        }

        String zoneOf(String alias) {
            Map<String, Object> card = card(alias);
            return card == null ? null : (String) card.get("zone");
        }

        void edit(String json) {
            assertNull(mediator.sandboxEdit(HUMAN, json(json)));
        }

        String refused(String json) {
            String problem = mediator.sandboxEdit(HUMAN, json(json));
            assertNotNull("The edit was made: " + json, problem);
            return problem;
        }

        AwaitingDecision waiting() {
            return scenario().Table().userFeedback().getAwaitingDecision(HUMAN);
        }

        /** The action id the waiting choice of actions offers for a card, or null if it offers none. */
        String actionFor(String alias) {
            AwaitingDecision decision = waiting();
            int cardId = scenario().Table().Card(alias).getCardId();
            String[] cardIds = decision.getDecisionParameters().get("cardId");
            String[] actionIds = decision.getDecisionParameters().get("actionId");
            for (int i = 0; cardIds != null && i < cardIds.length; i++)
                if (cardIds[i].equals(String.valueOf(cardId)))
                    return actionIds[i];
            return null;
        }

        void answer(String answer) throws Exception {
            client.poll();
            assertNotNull(client.decision);
            client.answer(answer);
            client.poll();
        }

        void revertTo(String label) {
            Integer at = null;
            for (LotroGameMediator.RestorePointView point : mediator.getRestorePoints(HUMAN))
                if (at == null && point.label().equals(label))
                    at = point.entryCount();
            assertNotNull("No place to go back to called '" + label + "': " + mediator.getRestorePoints(HUMAN), at);
            LotroGameMediator.UndoReply reply = mediator.requestUndo(HUMAN, at);
            assertEquals(reply.message(), "done", reply.status());
        }
    }

    /** Edits as these tests write them, with single quotes and bare names, as the JSON a page sends. */
    static String json(String text) {
        return text.replace('\'', '"').replaceAll("([{,]\\s*)([A-Za-z]+):", "$1\"$2\":");
    }

    private static Game begin(ScenarioDefinition def) throws Exception {
        LotroGameMediator mediator = LiveScenarioTest.start(live(def, null));
        LiveScenarioTest.Client client = new LiveScenarioTest.Client(mediator);
        client.connect();
        client.poll();
        return new Game(mediator, client);
    }

    @Test
    public void ASandboxOpensStoppedAndTakesNoAnswers() throws Exception {
        Game game = begin(sandbox());
        assertTrue(game.sandbox().Stopped());
        assertNotNull(game.client().info);
        assertEquals("edit", game.client().info.getString("mode"));
        assertEquals("sandbox", game.client().info.getString("level"));
        assertTrue(game.client().info.getBooleanValue("canStart"));
        assertFalse(game.client().info.getBooleanValue("canStop"));
        assertEquals(ScenarioSandbox.TOOLS, game.client().info.getList("tools", String.class));
        //Every pile is open in the default sandbox: the sites in the adventure decks too, with their printed numbers
        assertEquals("path", game.zoneOf("site1"));
        assertEquals(1, game.card("site1").get("site"));
        assertEquals("adventureDeck", game.zoneOf("site2"));
        assertNull(game.card("site2").get("site"));
        assertEquals(2, game.card("site2").get("printed"));
        assertNull(game.card("frodo").get("printed"));

        //The fellowship phase's prompt is there, and an answer to it is refused while stopped
        AwaitingDecision waiting = game.waiting();
        assertNotNull(waiting);
        game.client().answer("");
        assertTrue(waiting == game.waiting());
        assertEquals(1, game.scenario().Table().gameState().getTurnNumber());

        //Started, the page hears it, and a pass is taken
        assertNull(game.mediator().sandboxStart(HUMAN));
        game.client().poll();
        assertEquals("play", game.client().info.getString("mode"));
        assertTrue(game.client().info.getBooleanValue("canStop"));
        assertTrue(waiting != game.waiting());
        game.answer("");
        assertTrue(game.scenario().Table().GetCurrentPhase() != com.gempukku.lotro.common.Phase.FELLOWSHIP);
        assertEquals(List.of(), game.scenario().Problems());
    }

    @Test
    public void StopEditStartAndTakeItBackAcrossTheEdit() throws Exception {
        Game game = begin(sandbox());
        assertNull(game.mediator().sandboxStart(HUMAN));
        assertNull(game.mediator().sandboxStop(HUMAN));
        assertEquals("edit", game.state().get("mode"));

        //Legolas into the hand, and three twilight in the pool; the prompt does not offer him until Start
        game.edit("[{op: 'add', blueprint: '1_50', to: 'freeps.hand'}, {op: 'twilight', set: 3}]");
        assertEquals("hand", game.zoneOf("legolas"));
        assertEquals(3, game.state().get("twilight"));
        int legolasId = (int) game.card("legolas").get("cardId");
        assertNull(game.actionFor("legolas"));
        game.edit("{op: 'twilight', set: 9}");

        assertNull(game.mediator().sandboxStart(HUMAN));
        assertNotNull(game.actionFor("legolas"));
        game.answer(game.actionFor("legolas"));
        assertEquals("characters", game.zoneOf("legolas"));

        //Back to before the play: the edit is still there, and so is the prompt that offered him
        game.revertTo("Played Legolas, Greenleaf");
        assertTrue(game.scenario() != null);
        assertNull(game.scenario().Table().FollowProblem());
        assertEquals("hand", game.zoneOf("legolas"));
        assertEquals(legolasId, game.card("legolas").get("cardId"));
        assertEquals(9, game.state().get("twilight"));
        assertEquals("play", game.state().get("mode"));
        assertNotNull(game.actionFor("legolas"));

        //Back to before the Stop: no Legolas, no twilight, and play goes on
        game.revertTo("Stopped play");
        assertNull(game.card("legolas"));
        assertEquals(0, game.state().get("twilight"));
        assertEquals("play", game.state().get("mode"));
        assertFalse(game.sandbox().Stopped());
        assertEquals(List.of(), game.scenario().Problems());
    }

    @Test
    public void AGameRebuiltFromTheJournalAloneComesToTheSameBoard() throws Exception {
        Game game = begin(sandbox());
        //(refused edits leave nothing behind, not even a card id spent: the rebuilt game never sees them)
        game.refused("{op: 'add', blueprint: '1_178', to: 'shadow.characters'}");
        game.refused("[{op: 'add', blueprint: '1_178', to: 'shadow.minions'}, {op: 'heal', card: 'goblinrunner'}]");
        game.edit("[{op: 'add', blueprint: '1_50', to: 'freeps.hand'}, {op: 'add', blueprint: '1_97', to: 'freeps.characters'},"
                + " {op: 'add', blueprint: '1_94', attach: 'boromir'}, {op: 'add', blueprint: '1_178', to: 'shadow.hand'},"
                + " {op: 'wounds', card: 'boromir', amount: 1}, {op: 'burdens', set: 2}]");
        assertNull(game.mediator().sandboxStart(HUMAN));
        game.answer(game.actionFor("legolas"));
        assertNull(game.mediator().sandboxStop(HUMAN));
        game.edit("{op: 'copy', card: 'legolas'}");
        assertEquals("characters", game.zoneOf("legolas2"));

        GameJournal journal = game.scenario().Table().Journal();
        LotroGameMediator rebuilt = LiveScenarioTest.start(live(sandbox(), GameJournal.read(journal.write(), GameJournal.PlayerKeys.NAMES)));
        assertNull(rebuilt.getScenario().Table().FollowProblem());
        assertEquals(strip(game.state()), strip(rebuilt.sandboxState(HUMAN)));
        assertTrue(rebuilt.getScenario().Stopped());
        assertEquals(GameJournal.fingerprint(game.waiting()),
                GameJournal.fingerprint(rebuilt.getScenario().Table().userFeedback().getAwaitingDecision(HUMAN)));
    }

    /** What a state says of the board, without what may differ between two games of the same board. */
    private static Map<String, Object> strip(Map<String, Object> state) {
        Map<String, Object> copy = new java.util.LinkedHashMap<>(state);
        copy.remove("canStop");
        copy.remove("canStart");
        return copy;
    }

    @Test
    public void EveryEditTheSandboxOffers() throws Exception {
        Game game = begin(sandbox());
        game.edit("[{op: 'add', blueprint: '1_97', to: 'freeps.characters'}, {op: 'add', blueprint: '1_92', attach: 'boromir'},"
                + " {op: 'add', blueprint: '1_178', to: 'shadow.minions'}, {op: 'add', blueprint: '1_50', to: 'freeps.deck.bottom'},"
                + " {op: 'add', blueprint: '1_50', to: 'shadow.discard'}, {op: 'add', blueprint: '1_94', to: 'freeps.deadPile'},"
                + " {op: 'add', blueprint: '1_94', to: 'freeps.removed'}, {op: 'add', blueprint: '1_178', stack: 'boromir', seat: 'shadow'}]");
        assertEquals("characters", game.zoneOf("boromir"));
        assertEquals("attached", game.zoneOf("armor"));
        assertEquals("boromir", game.card("armor").get("on"));
        assertEquals("minions", game.zoneOf("goblinrunner"));
        assertEquals("shadow", game.card("goblinrunner").get("owner"));
        assertEquals("deck", game.zoneOf("legolas"));
        assertEquals("discard", game.zoneOf("legolas2"));
        assertEquals("shadow", game.card("legolas2").get("owner"));
        assertEquals("deadPile", game.zoneOf("athelas"));
        assertEquals("removed", game.zoneOf("athelas2"));
        assertEquals("stacked", game.zoneOf("goblinrunner2"));
        assertEquals(true, game.card("boromir").get("added"));
        assertEquals(false, game.card("frodo").get("added"));

        //Copy: another in the same place
        game.edit("{op: 'copy', card: 'armor'}");
        assertEquals("boromir", game.card("armor2").get("on"));

        //Wounds and tokens, up and down; tokens of the card's own culture unless another is named
        game.edit("[{op: 'wounds', card: 'boromir', amount: 2}, {op: 'wounds', card: 'boromir', amount: -1},"
                + " {op: 'tokens', card: 'boromir', amount: 3}, {op: 'tokens', card: 'boromir', amount: -1},"
                + " {op: 'tokens', card: 'boromir', amount: 2, culture: 'sauron'}]");
        assertEquals(1, game.card("boromir").get("wounds"));
        assertEquals(2, game.card("boromir").get("tokens"));
        assertEquals(Map.of("gondor", 2, "sauron", 2), game.card("boromir").get("cultureTokens"));

        //Counters
        game.edit("[{op: 'twilight', set: 4}, {op: 'twilight', amount: 1}, {op: 'burdens', set: 3}, {op: 'threats', set: 1}]");
        assertEquals(5, game.state().get("twilight"));
        assertEquals(3, game.state().get("burdens"));
        assertEquals(1, game.state().get("threats"));

        //A card moved off the table takes nothing with it: what it bore and what was on it go to the discard pile
        game.edit("{op: 'move', card: 'boromir', to: 'hand'}");
        assertEquals("hand", game.zoneOf("boromir"));
        assertEquals("discard", game.zoneOf("armor"));
        assertEquals("discard", game.zoneOf("armor2"));
        assertEquals("discard", game.zoneOf("goblinrunner2"));
        assertEquals("shadow", game.card("goblinrunner2").get("owner"));

        //Attach and stack what is already there, and delete: out of the game, not discarded
        game.edit("[{op: 'move', card: 'boromir', to: 'characters'}, {op: 'attach', card: 'armor', to: 'boromir'},"
                + " {op: 'stack', card: 'legolas2', to: 'goblinrunner'}, {op: 'delete', card: 'goblinrunner'}]");
        assertEquals("boromir", game.card("armor").get("on"));
        assertNull(game.card("goblinrunner"));
        assertEquals("discard", game.zoneOf("legolas2"));
        assertTrue(game.refused("{op: 'move', card: 'goblinrunner', to: 'hand'}").contains("not in the game"));

        //The markers, and who controls a site; a site on the path from an adventure deck and by blueprint
        game.edit("[{op: 'marker', seat: 'freeps', site: 3}, {op: 'marker', seat: 'shadow', site: 3}, {op: 'control', site: 1, seat: 'shadow'}]");
        @SuppressWarnings("unchecked")
        Map<String, Object> markers = (Map<String, Object>) game.state().get("markers");
        assertEquals(3, markers.get("freeps"));
        assertEquals("shadow", site(game, 1).get("controlledBy"));
        game.edit("{op: 'liberate', site: 1}");
        assertNull(site(game, 1).get("controlledBy"));
        game.edit("[{op: 'site', number: 4, from: 'shadow'}, {op: 'site', number: 5, blueprint: '1_350'}]");
        assertEquals("shadow", site(game, 4).get("owner"));
        assertEquals("1_350", site(game, 5).get("blueprintId"));
        assertEquals("freeps", site(game, 5).get("owner"));

        //Everything played on from here as a game: Start, and the fellowship-phase prompt is asked again
        assertNull(game.mediator().sandboxStart(HUMAN));
        assertNotNull(game.waiting());
        assertEquals(List.of(), game.scenario().Problems());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> site(Game game, int number) {
        for (Map<String, Object> site : (List<Map<String, Object>>) game.state().get("path"))
            if (site.get("number").equals(number))
                return site;
        return Map.of();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void AModifierPutOnByHandIsTakenBackWithTheRest() throws Exception {
        Game game = begin(sandbox());
        int strength = game.scenario().Table().GetStrength(game.scenario().Table().Card("frodo"));
        game.edit("[{op: 'modify', card: 'frodo', what: 'strength', amount: 3}, {op: 'modify', card: 'frodo', what: 'damage', until: 'turn'}]");
        List<Map<String, Object>> modifiers = (List<Map<String, Object>>) game.card("frodo").get("modifiers");
        assertEquals(2, modifiers.size());
        assertEquals("strength", modifiers.get(0).get("what"));
        assertEquals(strength + 3, game.scenario().Table().GetStrength(game.scenario().Table().Card("frodo")));

        //One kind taken off again, in a list of edits of its own: a place to go back to
        game.edit("{op: 'unmodify', card: 'frodo', what: 'damage'}");
        assertEquals(1, ((List<Map<String, Object>>) game.card("frodo").get("modifiers")).size());
        assertNull(game.mediator().sandboxStart(HUMAN));
        assertNotNull(game.waiting());

        //Back to before the taking off: both are on Frodo again, in a game rebuilt from its journal
        game.revertTo("Took modifiers off Frodo, Son of Drogo");
        assertNull(game.scenario().Table().FollowProblem());
        assertEquals(2, ((List<Map<String, Object>>) game.card("frodo").get("modifiers")).size());
        assertEquals(strength + 3, game.scenario().Table().GetStrength(game.scenario().Table().Card("frodo")));
        assertEquals(List.of(), game.scenario().Problems());
    }

    @Test
    public void WhatIsRefused() throws Exception {
        Game game = begin(sandbox());
        //Not an edit; one not built here; not where it can go; the Ring-bearer and the Ring
        assertTrue(game.refused("{op: 'paint'}").contains("not an edit"));
        assertTrue(game.refused("{op: 'modify', card: 'frodo'}").contains("'what' is missing"));
        assertTrue(game.refused("{op: 'modify', card: 'frodo', what: 'glow'}").contains("not something a card can be modified in"));
        assertTrue(game.refused("{op: 'modify', card: 'frodo', what: 'strength'}").contains("needs the amount"));
        assertTrue(game.refused("{op: 'unmodify', id: 7}").contains("no modifier 7"));
        assertTrue(game.refused("{op: 'add', blueprint: '1_178', to: 'shadow.characters'}").contains("only a companion"));
        assertTrue(game.refused("{op: 'add', blueprint: '99_99', to: 'freeps.hand'}").contains("no card"));
        assertTrue(game.refused("{op: 'delete', card: 'frodo'}").contains("Ring-bearer"));
        assertTrue(game.refused("{op: 'move', card: 'ring', to: 'hand'}").contains("Ring"));
        assertTrue(game.refused("{op: 'move', card: 'oppFrodo', to: 'freeps.hand'}").contains("Ring-bearer"));
        game.edit("{op: 'add', blueprint: '1_178', to: 'shadow.hand'}");
        String other = game.refused("{op: 'move', card: 'goblinrunner', to: 'freeps.hand'}");
        assertTrue(other, other.contains("belongs to the shadow seat"));

        //A list is made whole or not at all: the second edit fails, and the first is taken back
        String problem = game.refused("[{op: 'add', blueprint: '1_50', to: 'freeps.hand'}, {op: 'wounds', card: 'legolas', amount: 1}]");
        assertTrue(problem, problem.startsWith("Edit 2:"));
        assertNull(game.card("legolas"));
        assertTrue(game.sandbox().Stopped());

        //Nobody else, and not while it is being played
        assertNotNull(game.mediator().sandboxEdit("Somebody", json("{op: 'twilight', set: 1}")));
        assertNotNull(game.mediator().sandboxStop(HUMAN));
        assertNull(game.mediator().sandboxStart(HUMAN));
        assertTrue(game.refused("{op: 'twilight', set: 1}").contains("stopped"));
        assertNotNull(game.mediator().sandboxStart(HUMAN));
        assertNull(game.mediator().sandboxState("Somebody"));
    }

    @Test
    public void ToolsNarrowWhatMayBeDoneAndSeen() throws Exception {
        ScenarioDefinition def = sandbox();
        def.tools = List.of("add", "counters");
        Game game = begin(def);
        game.edit("[{op: 'add', blueprint: '1_50', to: 'freeps.hand'}, {op: 'add', blueprint: '1_50', to: 'freeps.deck'}, {op: 'twilight', set: 2}]");
        assertTrue(game.refused("{op: 'move', card: 'legolas', to: 'discard'}").contains("leave out 'move'"));
        //Without piles: a draw deck is not looked in, nor the other seat's hand
        assertEquals(false, game.state().get("hiddenPilesOpen"));
        assertNull(game.card("legolas2"));
        assertNotNull(game.card("legolas"));
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> piles = (Map<String, Map<String, Object>>) game.state().get("piles");
        assertEquals(1, piles.get("freeps").get("deck"));
        assertEquals(def.tools, game.client().info.getList("tools", String.class));
    }

    @Test
    public void ASandboxWhoseGameEndsIsEndedNotFailed() throws Exception {
        Game game = begin(sandbox());
        //Frodo given wounds enough to die: the game sees to it as it starts again
        game.edit("{op: 'wounds', card: 'frodo', amount: 4}");
        assertNull(game.mediator().sandboxStart(HUMAN));
        game.client().poll();
        assertTrue(game.mediator().isFinished());
        assertEquals(LiveScenario.Result.ENDED, game.scenario().Result());
        assertNotNull(game.client().end);
        assertEquals("ended", game.client().end.getString("result"));
        assertEquals("opponent", game.client().end.getString("winner"));
        assertTrue(game.client().end.getBooleanValue("reset"));
    }

    @Test
    public void TheDefaultSandboxIsASandbox() throws Exception {
        ScenarioDefinition def = sandbox();
        assertEquals(ScenarioDefinition.Kind.SANDBOX, def.kind);
        assertTrue(def.steps.isEmpty());
        assertEquals(List.of(), ScenarioValidator.validate(def, ScenarioTesting.library()));
        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
        assertTrue(report.toString(), report.passed());
        List<String> names = new ArrayList<>(def.cards.keySet());
        assertEquals(List.of("frodo", "ring", "oppFrodo", "oppRing"), names);
    }
}
