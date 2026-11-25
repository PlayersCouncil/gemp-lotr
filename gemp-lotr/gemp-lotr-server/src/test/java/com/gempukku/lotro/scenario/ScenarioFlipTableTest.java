package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

/**
 * Who plays a sandbox's other seat (options.otherSeat, LiveScenario.SetOtherSeat: the script, the bot or the
 * person) and the table seen from that seat (LotroGameMediator.sandboxFlip), in a live game with the person's page
 * connected: the default sandbox, with a Goblin Runner in the Shadow seat's hand for it to have something to do in
 * its Shadow phase.
 */
public class ScenarioFlipTableTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    private static final String HUMAN = LiveScenarioTest.HUMAN;
    private static final String SHADOW = LiveScenario.OPPONENT_PLAYER;

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    private static String file() throws Exception {
        return Files.readString(new File("../gemp-lotr-cards/src/main/resources/scenarios/sandboxes/sandbox.hjson").toPath(), StandardCharsets.UTF_8);
    }

    private static ScenarioDefinition sandbox(String otherSeat) throws Exception {
        String text = file();
        if (otherSeat != null)
            text = text.replaceFirst("\tcoverage: guided\n", "\tcoverage: guided\n\toptions: {\n\t\totherSeat: " + otherSeat + "\n\t}\n");
        ScenarioDefinition def = ScenarioParser.parse(text);
        def.opponentDelayMs = 0;
        return def;
    }

    private static LiveScenario live(ScenarioDefinition def, GameJournal earlier, LiveScenario.Bot bot) throws Exception {
        LiveScenario live = new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, earlier);
        if (bot != null)
            live.SetBot(bot);
        return live;
    }

    /** A bot for the tests (there are no trained models here): it plays the first thing it is offered. */
    private static final LiveScenario.Bot EAGER_BOT = (game, player, decision, seed) -> AnswerCompiler.eagerAnswer(decision);

    private record Game(LotroGameMediator mediator, LiveScenarioTest.Client client) {
        LiveScenario scenario() {
            return mediator.getScenario();
        }

        Map<String, Object> state() {
            return mediator.sandboxState(HUMAN);
        }

        void edit(String json) {
            assertNull(mediator.sandboxEdit(HUMAN, ScenarioSandboxTest.json(json)));
        }

        AwaitingDecision waiting(String player) {
            return scenario().Table().userFeedback().getAwaitingDecision(player);
        }

        String zoneOf(String alias) {
            return BoardView.zoneOf(scenario().Table().Card(alias));
        }

        /** The action id a decision offers for a card, or null. */
        String actionFor(AwaitingDecision decision, String alias) {
            int cardId = scenario().Table().Card(alias).getCardId();
            String[] cardIds = decision.getDecisionParameters().get("cardId");
            String[] actionIds = decision.getDecisionParameters().get("actionId");
            for (int i = 0; cardIds != null && i < cardIds.length; i++)
                if (cardIds[i].equals(String.valueOf(cardId)))
                    return actionIds[i];
            return null;
        }

        /** The page reads the game again from nothing, as it does on a resync notice. */
        void reopen() throws Exception {
            client.events.clear();
            client.connect();
        }

        /** Whether the page has been told of a card (its blueprint shown), since it last read the game afresh. */
        boolean shown(String alias) {
            int cardId = scenario().Table().Card(alias).getCardId();
            for (GameEvent event : client.events)
                if (event.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY && event.getCardId() != null && event.getCardId() == cardId)
                    return true;
            return false;
        }

        String participant() {
            for (GameEvent event : client.events)
                if (event.getType() == GameEvent.Type.PARTICIPANTS)
                    return event.getParticipantId();
            return null;
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

        /** Waits for the game to be waiting on this player. */
        AwaitingDecision awaitDecisionOf(String player) throws Exception {
            long deadline = System.currentTimeMillis() + 5000;
            while (waiting(player) == null && System.currentTimeMillis() < deadline)
                Thread.sleep(5);
            return waiting(player);
        }
    }

    private static Game begin(LiveScenario live) throws Exception {
        LotroGameMediator mediator = LiveScenarioTest.start(live);
        LiveScenarioTest.Client client = new LiveScenarioTest.Client(mediator);
        client.connect();
        client.poll();
        return new Game(mediator, client);
    }

    /** The runner into the Shadow seat's hand and twilight to play it; Start; the person passes the fellowship phase. */
    private static void toTheShadowPhase(Game game) throws Exception {
        game.edit("[{op: 'add', blueprint: '1_178', to: 'shadow.hand'}, {op: 'twilight', set: 2}]");
        assertNull(game.mediator().sandboxStart(HUMAN));
        game.client().poll();
        AwaitingDecision fellowship = game.waiting(HUMAN);
        assertNotNull(fellowship);
        game.client().await();
        game.client().answer("");
        game.client().poll();
    }

    @Test
    public void WithThePersonInTheOtherSeatItsDecisionWaitsAndIsTheirsOnceFlipped() throws Exception {
        Game game = begin(live(sandbox(null), null, null));
        //The default sandbox: the script plays the other seat, the person looks from their own
        assertEquals("script", game.client().info.getString("otherSeat"));
        assertEquals(List.of("script", "person"), game.client().info.getList("otherSeats", String.class));
        assertEquals("freeps", game.client().info.getString("seat"));
        assertTrue(game.client().info.getBooleanValue("canFlip"));
        assertEquals("freeps", game.client().info.getString("waitingOn"));
        assertEquals("script", game.state().get("otherSeat"));
        assertEquals("This server has no bot to play the other seat.", game.mediator().sandboxOtherSeat(HUMAN, "bot"));

        //Given to the person while stopped: the page hears it
        assertNull(game.mediator().sandboxOtherSeat(HUMAN, "person"));
        game.client().poll();
        assertEquals("person", game.client().info.getString("otherSeat"));
        toTheShadowPhase(game);

        //The Shadow phase: the other seat's decision waits, and the person's page is not asked it
        AwaitingDecision shadowPhase = game.awaitDecisionOf(SHADOW);
        assertNotNull("The Shadow seat is asked its Shadow phase", shadowPhase);
        assertNotNull(game.actionFor(shadowPhase, "goblinrunner"));
        Thread.sleep(100);
        assertTrue("Nobody answers for the other seat", shadowPhase == game.waiting(SHADOW));
        assertNull(game.waiting(HUMAN));
        game.client().poll();
        assertNull("The person's own page is not asked the other seat's decision", game.client().decision);
        assertEquals("shadow", game.client().info.getString("waitingOn"));
        assertEquals("shadow", game.state().get("waitingOn"));
        assertFalse("Their page is not shown the other seat's hand", game.shown("goblinrunner"));

        //Flipped: the page is the Shadow seat's, with its decision and its hand; the old channel is gone
        int oldChannel = game.client().channel;
        assertNull(game.mediator().sandboxFlip(HUMAN, null));
        try {
            game.mediator().getCommunicationChannel(game.client().player, oldChannel);
            fail("The page's old channel is given up on a flip");
        } catch (SubscriptionConflictException expected) { }
        game.reopen();
        assertEquals(SHADOW, game.participant());
        assertNotNull(game.client().decision);
        assertEquals(SHADOW, game.client().decision.getParticipantId());
        assertTrue(game.shown("goblinrunner"));
        assertEquals("shadow", game.client().info.getString("seat"));
        assertEquals("shadow", game.state().get("seat"));
        //(an edit's zone alone is still the seat the person sits in)
        assertNull(game.mediator().sandboxStop(HUMAN));
        game.edit("{op: 'add', blueprint: '1_50', to: 'hand'}");
        assertEquals("freeps", ((Map<?, ?>) ((List<?>) game.state().get("cards")).stream()
                .filter(card -> ((Map<?, ?>) card).get("alias").equals("legolas")).findFirst().orElseThrow()).get("owner"));
        assertNull(game.mediator().sandboxStart(HUMAN));
        shadowPhase = game.awaitDecisionOf(SHADOW);
        game.client().await();

        //The person's answer is the Shadow seat's
        game.client().answer(game.actionFor(shadowPhase, "goblinrunner"));
        game.client().poll();
        assertEquals("minions", game.zoneOf("goblinrunner"));

        //A take-back while flipped lands before the play, still flipped, still waiting on the Shadow seat
        game.revertTo("Started play");
        assertNull(game.scenario().Table().FollowProblem());
        assertEquals("person", game.scenario().OtherSeat().name().toLowerCase());
        assertEquals("hand", game.zoneOf("goblinrunner"));
        assertTrue(game.scenario().Stopped());
        game.reopen();
        assertEquals(SHADOW, game.participant());
        assertEquals("shadow", game.client().info.getString("seat"));

        //Flipped back: the person's own page again, not asked the Shadow seat's decision
        assertNull(game.mediator().sandboxFlip(HUMAN, "freeps"));
        game.reopen();
        assertEquals(HUMAN, game.participant());
        assertEquals("freeps", game.client().info.getString("seat"));
        assertFalse(game.shown("goblinrunner"));

        //Back to before the other seat was given to the person: the script plays it, and answers its Shadow phase by itself
        game.revertTo("Gave the shadow seat to you");
        assertEquals("script", game.scenario().OtherSeat().name().toLowerCase());
        game.reopen();
        assertEquals("script", game.client().info.getString("otherSeat"));
        toTheShadowPhase(game);
        game.client().await();
        assertNotNull("Past the Shadow phase, the person is asked again", game.waiting(HUMAN));
        assertNull(game.waiting(SHADOW));
        assertEquals("hand", game.zoneOf("goblinrunner"));
        assertEquals(List.of(), game.scenario().Problems());
    }

    @Test
    public void TheBotAndTheScriptAnswerByThemselvesAndAnAnswerFromTheFlippedPageIsRefused() throws Exception {
        Game game = begin(live(sandbox("bot"), null, EAGER_BOT));
        assertEquals("bot", game.client().info.getString("otherSeat"));
        assertEquals(List.of("script", "bot", "person"), game.client().info.getList("otherSeats", String.class));
        //Flipped before the Shadow phase: the page sees it, but the bot answers
        assertNull(game.mediator().sandboxFlip(HUMAN, "shadow"));
        toTheShadowPhaseFlipped(game);
        long deadline = System.currentTimeMillis() + 5000;
        while (!"minions".equals(game.zoneOf("goblinrunner")) && System.currentTimeMillis() < deadline)
            Thread.sleep(5);
        assertEquals("The bot played the runner", "minions", game.zoneOf("goblinrunner"));

        //The script (fallback pass) answers by itself too, and the flipped page's answer for that seat is refused
        Game scripted = begin(live(sandbox("person"), null, null));
        toTheShadowPhase(scripted);
        assertNull(scripted.mediator().sandboxFlip(HUMAN, "opponent"));
        scripted.reopen();
        AwaitingDecision shadowPhase = scripted.awaitDecisionOf(SHADOW);
        assertNotNull(shadowPhase);
        //(given back to the script while it waits: the script answers it, and the flipped page's answer meanwhile is refused)
        scripted.scenario().Definition().opponentDelayMs = 300;
        scripted.client().await();
        assertNull(scripted.mediator().sandboxOtherSeat(HUMAN, "script"));
        scripted.client().answer(scripted.actionFor(shadowPhase, "goblinrunner"));
        scripted.client().poll();
        assertTrue(scripted.client().events.stream().anyMatch(event -> event.getType() == GameEvent.Type.SEND_WARNING
                && event.getMessage().startsWith("The other seat is not yours to answer for")));
        assertEquals("hand", scripted.zoneOf("goblinrunner"));
        deadline = System.currentTimeMillis() + 5000;
        while (scripted.waiting(SHADOW) == shadowPhase && System.currentTimeMillis() < deadline)
            Thread.sleep(5);
        assertTrue("The script answered the waiting decision", scripted.waiting(SHADOW) != shadowPhase);
        assertEquals("hand", scripted.zoneOf("goblinrunner"));
        assertEquals(List.of(), scripted.scenario().Problems());
    }

    private static void toTheShadowPhaseFlipped(Game game) throws Exception {
        game.edit("[{op: 'add', blueprint: '1_178', to: 'shadow.hand'}, {op: 'twilight', set: 2}]");
        assertNull(game.mediator().sandboxStart(HUMAN));
        AwaitingDecision fellowship = game.awaitDecisionOf(HUMAN);
        assertNotNull(fellowship);
        //(a flipped page answering the person's own seat is refused as any page answering another player's decision:
        // it is not asked it; the person's own answer is given here by flipping back for it)
        assertNull(game.mediator().sandboxFlip(HUMAN, "freeps"));
        game.reopen();
        game.client().await();
        game.client().answer("");
        assertNull(game.mediator().sandboxFlip(HUMAN, "shadow"));
        game.reopen();
    }

    @Test
    public void WhoPlaysTheOtherSeatIsRebuiltFromTheJournal() throws Exception {
        Game game = begin(live(sandbox(null), null, null));
        assertNull(game.mediator().sandboxOtherSeat(HUMAN, "person"));
        toTheShadowPhase(game);
        assertNotNull(game.awaitDecisionOf(SHADOW));
        GameJournal journal = game.scenario().Table().Journal();
        LotroGameMediator rebuilt = LiveScenarioTest.start(live(sandbox(null), GameJournal.read(journal.write(), GameJournal.PlayerKeys.NAMES), null));
        assertNull(rebuilt.getScenario().Table().FollowProblem());
        assertEquals(ScenarioDefinition.OtherSeat.PERSON, rebuilt.getScenario().OtherSeat());
        Thread.sleep(100);
        AwaitingDecision waiting = rebuilt.getScenario().Table().userFeedback().getAwaitingDecision(SHADOW);
        assertNotNull("The rebuilt game waits on the Shadow seat too", waiting);
        assertEquals(GameJournal.fingerprint(game.waiting(SHADOW)), GameJournal.fingerprint(waiting));
        assertEquals("shadow", rebuilt.sandboxState(HUMAN).get("waitingOn"));
        //A setting the file starts with is in force from the start, and none is the script
        assertEquals(ScenarioDefinition.OtherSeat.PERSON, begin(live(sandbox("person"), null, null)).scenario().OtherSeat());
        assertEquals("'nobody' is not who can play the other seat: it is script, person.", game.mediator().sandboxOtherSeat(HUMAN, "nobody"));
    }
}
