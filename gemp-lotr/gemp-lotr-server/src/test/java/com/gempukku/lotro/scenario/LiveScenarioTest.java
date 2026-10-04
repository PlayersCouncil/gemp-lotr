package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.ParticipantCommunicationVisitor;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A scenario in a live game: a mediator, a person's client on one side and the scripted opponent on the other.  The
 * client here is the least a real one does: it connects, polls for events, and answers decisions.
 */
public class LiveScenarioTest {
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);
    private static final String HUMAN = "Tester";

    @AfterClass
    public static void stopTimer() {
        TIMER.shutdownNow();
    }

    /** A person's browser, as far as the server can tell. */
    private static class Client implements ParticipantCommunicationVisitor {
        final LotroGameMediator mediator;
        final Player player = new Player(1, HUMAN, "", "u", null, null, null, null, false);
        int channel;
        final List<GameEvent> events = new ArrayList<>();
        /** The decision waiting for an answer, and where in the stream of events it arrived. */
        GameEvent decision;
        int decisionAt;
        JSONObject info, step, end, offScript;
        int stepAt;
        final List<Integer> stepsSeen = new ArrayList<>();

        Client(LotroGameMediator mediator) {
            this.mediator = mediator;
        }

        void connect() throws Exception {
            decision = null;
            info = step = end = offScript = null;
            mediator.signupUserForGame(player, this);
        }

        void poll() throws Exception {
            mediator.processVisitor(mediator.getCommunicationChannel(player, channel), channel, HUMAN, this);
        }

        @Override
        public void visitChannelNumber(int channelNumber) {
            channel = channelNumber;
        }

        @Override
        public void visitClock(Map<String, Integer> secondsLeft) { }

        @Override
        public void visitGameEvent(GameEvent event) {
            events.add(event);
            if (event.getType() == GameEvent.Type.DECISION) {
                decision = event;
                decisionAt = events.size();
            } else if (event.getType() == GameEvent.Type.SCENARIO) {
                JSONObject json = JSON.parseObject(event.getScenario());
                switch (json.getString("type")) {
                    case "info" -> info = json;
                    case "step" -> {
                        step = json;
                        stepAt = events.size();
                        stepsSeen.add(json.getIntValue("step"));
                    }
                    case "end" -> end = json;
                    case "offscript" -> offScript = json;
                    default -> fail("Unknown scenario event: " + json);
                }
            }
        }

        /** Waits until the game wants something of the person: an answer, an acknowledgement, or nothing ever again. */
        void await() throws Exception {
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                poll();
                if (decision != null || end != null || mediator.isFinished())
                    return;
                if (step != null && step.getBooleanValue("acknowledge"))
                    return;
                Thread.sleep(5);
            }
            fail("The game went quiet.  Last step seen: " + step);
        }

        void answer(String answer) throws Exception {
            GameEvent answering = decision;
            decision = null;
            step = null;
            mediator.playerAnswered(player, channel, answering.getAwaitingDecision().getAwaitingDecisionId(), answer);
        }

        /** Does what the step on screen allows: its first allowed answer, or a pass if that is all it allows. */
        void answerAsAllowed() throws Exception {
            JSONObject allow = step.getJSONObject("allow");
            assertNotNull("The step does not restrict the answer: " + step, allow);
            List<String> answers = allow.getList("answers", String.class);
            answer(answers.isEmpty() ? "" : answers.getFirst());
        }

        void acknowledge() {
            int number = step.getIntValue("step");
            step = null;
            mediator.scenarioAcknowledged(player, number);
        }
    }

    private static ScenarioDefinition opening() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(Files.readString(
                new File("src/test/resources/scenarios/into-the-wild-opening.hjson").toPath(), StandardCharsets.UTF_8));
        def.opponentDelayMs = 0;
        return def;
    }

    private static LiveScenario live(ScenarioDefinition def) throws Exception {
        return live(def, null);
    }

    private static LiveScenario live(ScenarioDefinition def, GameJournal earlier) throws Exception {
        return new LiveScenario(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), HUMAN,
                new ScenarioText(def, ScenarioTesting.library(), ScenarioTesting.format(def.format), null), TIMER, earlier);
    }

    /** Plays the person's side as the script allows until a given step is on screen and waiting for its answer. */
    private static void playUntil(Client client, int stepNumber) throws Exception {
        for (int i = 0; i < 50; i++) {
            client.await();
            assertNotNull("The game ended before step " + stepNumber, client.step);
            if (client.step.getIntValue("step") == stepNumber && client.decision != null)
                return;
            if (client.decision != null)
                client.answerAsAllowed();
            else
                client.acknowledge();
        }
        fail("Step " + stepNumber + " never came");
    }

    private static LotroGameMediator start(LiveScenario scenario) {
        LotroGameMediator mediator = new LotroGameMediator("1", scenario, LotroServer.SCENARIO_TIMER, false);
        mediator.startGame();
        return mediator;
    }

    @Test
    public void TheOpeningPlaysThroughWithAPersonInTheSeat() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();

        assertNotNull("The first thing a client is told is which scenario this is", client.info);
        assertEquals("into-the-wild-opening", client.info.getString("id"));
        assertEquals("The Shadow", client.info.getString("opponentName"));
        assertEquals(LiveScenario.OPPONENT_PLAYER, client.info.getString("opponent"));

        int acknowledged = 0;
        for (int i = 0; i < 50 && client.end == null; i++) {
            client.await();
            if (client.end != null || mediator.isFinished())
                break;
            if (client.decision != null) {
                assertNotNull("A decision arrived with no step: " + client.decision.getAwaitingDecision().getText(), client.step);
                assertTrue("The step has to reach the client before the decision it is about", client.stepAt < client.decisionAt);
                assertEquals("human", client.step.getString("seat"));
                client.answerAsAllowed();
            } else {
                assertEquals("opponent", client.step.getString("seat"));
                client.acknowledge();
                acknowledged++;
            }
        }
        client.poll();

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), client.stepsSeen);
        assertEquals("Only the opponent's play of the Bowman has anything to say", 1, acknowledged);
        assertNotNull(client.end);
        assertEquals("complete", client.end.getString("result"));
        assertTrue(mediator.isFinished());
        assertEquals(HUMAN, mediator.getWinner());
        assertEquals(LiveScenario.Result.COMPLETE, scenario.Result());
        assertEquals(List.of(), scenario.Problems());
    }

    @Test
    public void AStepCarriesItsMessagesAndWhatItAllows() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();

        JSONObject step = client.step;
        assertEquals(1, step.getIntValue("step"));
        List<JSONObject> messages = step.getList("messages", JSONObject.class);
        assertEquals(3, messages.size());
        assertEquals("The road goes ever on", messages.get(0).getString("header"));
        //Text is rendered: the template is filled in and the card link opens the scenario's own Frodo
        String html = messages.get(0).getString("html");
        assertTrue(html, html.contains("at site 1"));
        assertTrue(html, html.contains("class=\"cardHint patchnote-card\" value=\"10_121\""));
        assertTrue(messages.get(1).getString("html"), messages.get(1).getString("html").contains("<strong>adventure path</strong>"));
        //The first two wait for Next; the last stays up while the person answers
        assertEquals("next", messages.get(0).getString("advance"));
        assertEquals("next", messages.get(1).getString("advance"));
        assertEquals("decision", messages.get(2).getString("advance"));
        assertEquals("card:frodo", messages.get(0).getString("anchor"));
        //Cards are named by alias; the step says which card each alias is right now
        int frodoId = scenario.Table().Card("frodo").getCardId();
        assertEquals(frodoId, step.getJSONObject("cards").getIntValue("frodo"));

        JSONObject allow = step.getJSONObject("allow");
        assertTrue(allow.getBooleanValue("pass"));
        assertEquals(List.of(), allow.getList("answers", String.class));
    }

    @Test
    public void TheOpponentWaitsToBeAcknowledgedAndIsNotTimedOut() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        client.answerAsAllowed();
        client.await();

        assertEquals(2, client.step.getIntValue("step"));
        assertTrue(client.step.getBooleanValue("acknowledge"));
        assertNull("The opponent's step puts nothing to the person", client.decision);
        assertNull(client.step.get("allow"));
        for (JSONObject message : client.step.getList("messages", JSONObject.class))
            assertEquals("next", message.getString("advance"));

        //An ordinary bot forfeits after five seconds without an answer.  A scenario's opponent is waiting on purpose.
        Thread.sleep(5200);
        mediator.cleanup();
        client.poll();
        assertFalse(mediator.isFinished());
        assertNull(client.decision);
        assertTrue("The Bowman is not played until the person has read about twilight",
                scenario.Table().Card("bowman").getZone() == com.gempukku.lotro.common.Zone.HAND);

        //Acknowledging the wrong step does nothing
        mediator.scenarioAcknowledged(client.player, 7);
        Thread.sleep(100);
        client.poll();
        assertNull(client.decision);

        client.acknowledge();
        client.await();
        assertEquals(3, client.step.getIntValue("step"));
        assertEquals(com.gempukku.lotro.common.Zone.SHADOW_CHARACTERS, scenario.Table().Card("bowman").getZone());
    }

    @Test
    public void ReconnectingShowsTheSameStepAgain() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        client.answerAsAllowed();
        client.await();

        //In the middle of the opponent's step
        Client again = new Client(mediator);
        again.connect();
        assertNotNull(again.info);
        assertEquals(2, again.step.getIntValue("step"));
        assertTrue(again.step.getBooleanValue("acknowledge"));
        again.acknowledge();
        again.await();

        //In the middle of the person's own step
        assertEquals(3, again.step.getIntValue("step"));
        Client third = new Client(mediator);
        third.connect();
        assertEquals(3, third.step.getIntValue("step"));
        assertNotNull(third.decision);
        assertTrue(third.stepAt < third.decisionAt);
        assertEquals(again.step.getJSONObject("allow").toString(), third.step.getJSONObject("allow").toString());

        //A step that has been answered is not shown to somebody who reconnects afterwards
        third.answerAsAllowed();
        third.await();
        assertEquals(4, third.step.getIntValue("step"));
        Client fourth = new Client(mediator);
        fourth.connect();
        assertEquals(List.of(4), fourth.stepsSeen);
    }

    @Test
    public void TheScenarioDecidesWhereThePersonIsAskedToPass() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        //A client sends its own auto-pass settings with every request.  They would skip the very first step.
        mediator.setPlayerAutoPassSettings(HUMAN, EnumSet.allOf(Phase.class));
        Client client = new Client(mediator);
        client.connect();
        client.await();
        assertEquals(1, client.step.getIntValue("step"));
        assertNotNull(client.decision);
        assertFalse(mediator.getGame().shouldAutoPass(HUMAN, Phase.FELLOWSHIP));
        assertTrue(mediator.getGame().shouldAutoPass(HUMAN, Phase.ARCHERY));
    }

    private static final String TWO_STEPS = """
            	options: {
            		opponentDelayMs: 0
            	}
            	setup: {
            		freeps: {
            			hand: aragorn
            		}
            	}
            	steps: [
            		{
            			when: freeps, action, phase(fellowship)
            			messages: Pass, please.
            			answer: pass
            		}
            		{
            			when: freeps, choice, text(another move)
            			answer: option(No)
            			end: complete
            		}
            	]
            """;

    @Test
    public void LeavingTheScriptEndsTheRunAndOffersARestart() throws Exception {
        //The first step does not restrict the answer, so the person is free to do something the script did not plan
        LiveScenario scenario = live(ScenarioTesting.parse("""
                	outcome: {
                		offScript: {
                			message: That's not how this one goes, {title:aragorn} or no.
                		}
                	}
                """ + TWO_STEPS));
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        assertNull("An unrestricted step allows anything", client.step.get("allow"));

        //Play Aragorn instead of passing: the next thing the game asks is the Fellowship phase again
        String aragorn = String.valueOf(scenario.Table().Card("aragorn").getCardId());
        String[] cardIds = client.decision.getAwaitingDecision().getDecisionParameters().get("cardId");
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        String play = null;
        for (int i = 0; i < cardIds.length; i++)
            if (cardIds[i].equals(aragorn))
                play = actionIds[i];
        assertNotNull("Aragorn should be playable", play);
        client.answer(play);
        client.await();
        client.poll();

        assertNotNull(client.end);
        assertEquals("off_script", client.end.getString("result"));
        assertTrue(client.end.getBooleanValue("restart"));
        assertTrue(client.end.getString("message"), client.end.getString("message").contains("Aragorn, Heir of Elendil or no."));
        assertTrue(mediator.isFinished());
        assertEquals(LiveScenario.Result.OFF_SCRIPT, scenario.Result());
        assertEquals(1, scenario.Problems().size());
        assertTrue(scenario.Problems().getFirst(), scenario.Problems().getFirst().startsWith("off script: no step covers the player's decision: freeps,"));
    }

    @Test
    public void LeavingTheScriptCanBeAllowed() throws Exception {
        LiveScenario scenario = live(ScenarioTesting.parse("""
                	outcome: {
                		offScript: {
                			message: You're on your own now.
                			result: continue
                		}
                	}
                """ + TWO_STEPS));
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        String aragorn = String.valueOf(scenario.Table().Card("aragorn").getCardId());
        String[] cardIds = client.decision.getAwaitingDecision().getDecisionParameters().get("cardId");
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        for (int i = 0; i < cardIds.length; i++)
            if (cardIds[i].equals(aragorn)) {
                client.answer(actionIds[i]);
                break;
            }
        client.await();

        assertNull(client.end);
        assertFalse(mediator.isFinished());
        assertNotNull("The person is told once that the script has been left", client.offScript);
        assertTrue(client.offScript.getString("message").contains("You're on your own now."));
        assertNotNull(client.decision);
        assertNull(scenario.Result());

        //The game carries on as an ordinary one, and nothing more is said about it
        client.offScript = null;
        client.answer("");
        client.await();
        assertNull(client.offScript);
        assertFalse(mediator.isFinished());
    }

    @Test
    public void ConcedingIsNotAFailedAttempt() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        mediator.concede(client.player);
        assertTrue(mediator.isFinished());
        assertEquals(LiveScenario.Result.ABANDONED, scenario.Result());
    }

    @Test
    public void AGameLostByItsOwnRulesIsAFailedAttempt() throws Exception {
        //Frodo walks out alone with three wounds and meets an archer
        LiveScenario scenario = live(ScenarioTesting.parse("""
                	options: {
                		opponentDelayMs: 0
                		autoPass: true
                	}
                	opponent: {
                		fallback: pass
                	}
                	coverage: guided
                	setup: {
                		shadow: {
                			hand: bowman
                		}
                		wounds: {
                			frodo: 3
                		}
                	}
                	steps: [
                		{
                			when: opponent, action, phase(shadow)
                			answer: play(bowman)
                		}
                	]
                """));
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        for (int i = 0; i < 20 && !mediator.isFinished(); i++) {
            client.await();
            if (client.decision != null)
                client.answer(AnswerCompiler.leastAnswer(client.decision.getAwaitingDecision()));
        }
        client.poll();

        assertTrue(mediator.isFinished());
        assertEquals(LiveScenario.OPPONENT_PLAYER, mediator.getWinner());
        assertEquals(LiveScenario.Result.FAILED, scenario.Result());
        assertNotNull(client.end);
        assertEquals("failed", client.end.getString("result"));
    }

    @Test
    public void ANewGameCanPickUpAtACheckpoint() throws Exception {
        ScenarioDefinition def = opening();
        LiveScenario first = live(def);
        Client client = new Client(start(first));
        client.connect();
        assertNull("Nothing to go back to yet", first.CheckpointJournal());
        playUntil(client, 7);

        assertEquals(7, first.LastCheckpoint());
        GameJournal checkpoint = first.CheckpointJournal();
        assertNotNull(checkpoint);
        assertEquals("Five decisions of the person's and one of the opponent's lead up to it", 6, checkpoint.answers().size());
        String allowedThen = client.step.getJSONObject("allow").toString();
        List<Integer> handThen = first.Table().GetFreepsHand().stream().map(card -> card.getCardId()).toList();
        //Carry on past it; the checkpoint is still where it was
        client.answerAsAllowed();
        client.await();
        assertEquals(8, client.step.getIntValue("step"));
        assertEquals(checkpoint.write(), first.CheckpointJournal().write());

        //The journal goes by way of text, as it would if it had been stored
        GameJournal stored = GameJournal.read(checkpoint.write(), player -> player);
        LiveScenario second = live(def, stored);
        LotroGameMediator mediator = start(second);
        assertNull(second.Table().FollowProblem());
        assertEquals(6, second.Table().AnswersFollowed());

        Client again = new Client(mediator);
        again.connect();
        assertEquals(7, again.info.getIntValue("resumedAt"));
        assertEquals("Nothing from before the checkpoint is shown", List.of(7), again.stepsSeen);
        assertNotNull(again.decision);
        assertEquals(allowedThen, again.step.getJSONObject("allow").toString());
        assertEquals(handThen, second.Table().GetFreepsHand().stream().map(card -> card.getCardId()).toList());
        assertEquals(2, second.Table().GetWoundsOn(second.Table().Card("frodo")));
        assertEquals(2, second.Table().gameState().getTurnNumber());

        //The new game can be gone back to in its turn, and plays on to the end
        assertEquals(checkpoint.write(), second.CheckpointJournal().write());
        for (int i = 0; i < 10 && again.end == null; i++) {
            again.await();
            if (again.decision != null)
                again.answerAsAllowed();
        }
        again.poll();
        assertNotNull(again.end);
        assertEquals("complete", again.end.getString("result"));
        assertEquals(List.of(), second.Problems());
    }

    @Test
    public void AJournalFromADifferentGameIsNoticed() throws Exception {
        ScenarioDefinition def = opening();
        LiveScenario first = live(def);
        Client client = new Client(start(first));
        client.connect();
        playUntil(client, 7);
        GameJournal checkpoint = first.CheckpointJournal();

        //The same scenario, except that the opponent now has nothing to play on the first turn
        ScenarioDefinition changed = opening();
        changed.setup.zones.get(Seat.SHADOW).clear();
        LiveScenario second = live(changed, checkpoint);
        start(second);
        assertNotNull(second.Table().FollowProblem());
        assertTrue(second.Table().FollowProblem(), second.Table().FollowProblem().startsWith("answer 2 was given by the shadow seat to a CARD_ACTION_CHOICE:"));
        assertEquals(1, second.Table().AnswersFollowed());
    }

    /** Plays the person's side from the script's own answers (the session's rulings) until the game goes quiet. */
    private static void playScripted(LiveScenario scenario, Client client) throws Exception {
        for (int i = 0; i < 50 && client.end == null; i++) {
            client.await();
            if (client.end != null || client.mediator.isFinished())
                return;
            if (client.decision != null)
                client.answer(scenario.Session().Ruling(HUMAN).answer());
            else
                client.acknowledge();
        }
    }

    @Test
    public void AQuestionAskedFromInsideAnAnswerIsShownWithItsOwnStep() throws Exception {
        LiveScenario scenario = live(ScenarioParser.parse(ScenarioTesting.awkward("""
                	options: {
                		opponentDelayMs: 0
                	}
                	setup: {
                		freeps: {
                			characters: sam
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			messages: Use Sam.
                			answer: use(sam)
                		}
                		{
                			when: freeps, arbitrary, text(inspect)
                			messages: This is your deck.
                			answer: pass
                		}
                		{
                			when: freeps, arbitrary, text(Choose cards from deck)
                			messages: Take Bill.
                			answer: choose(bill)
                		}
                		{
                			when: freeps, cards, text(Attach Bill)
                			messages: Sam looks after him.
                			answer: choose(sam)
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			answer: pass
                			end: complete
                		}
                	]
                """)));
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();

        List<String> seen = new ArrayList<>();
        for (int i = 0; i < 10 && client.end == null; i++) {
            client.await();
            if (client.decision == null)
                break;
            assertNotNull("No step came with \"" + client.decision.getAwaitingDecision().getText() + "\"", client.step);
            assertTrue("The step has to reach the client before its decision", client.stepAt < client.decisionAt);
            seen.add(client.step.getIntValue("step") + ": " + client.decision.getAwaitingDecision().getText());
            client.answer(scenario.Session().Ruling(HUMAN).answer());
        }
        client.poll();

        assertEquals(List.of(
                "1: Play Fellowship action or Pass",
                "2: You may inspect the contents of your deck before retrieving cards",
                "3: Choose cards from deck",
                "4: Attach Bill the Pony. Choose target to attach to",
                "5: Play Fellowship action or Pass"), seen);
        assertEquals(LiveScenario.Result.COMPLETE, scenario.Result());
        assertEquals(List.of(), scenario.Problems());
        assertEquals(com.gempukku.lotro.common.Zone.ATTACHED, scenario.Table().Card("bill").getZone());
    }

    @Test
    public void WhenBothSeatsAreAskedAtOnceThePersonWaitsForTheStepInHand() throws Exception {
        //Master Proudfoot shows both players a card.  The script has the opponent look first, with something to say.
        LiveScenario scenario = live(ScenarioParser.parse(ScenarioTesting.awkward("""
                	options: {
                		opponentDelayMs: 0
                	}
                	setup: {
                		freeps: {
                			support: proudfoot
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			answer: use(proudfoot)
                		}
                		{
                			when: opponent, arbitrary, text(revealed)
                			messages: Your opponent gets to see it too.
                			answer: pass
                		}
                		{
                			when: freeps, arbitrary, text(revealed)
                			messages: And so do you.
                			answer: pass
                		}
                		{
                			when: freeps, action, phase(fellowship)
                			answer: pass
                			end: complete
                		}
                	]
                """)));
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        client.answer(scenario.Session().Ruling(HUMAN).answer());
        client.await();

        //Both seats have been asked.  Only the opponent's step is on screen, and the person has not been asked yet.
        assertEquals(2, client.step.getIntValue("step"));
        assertTrue(client.step.getBooleanValue("acknowledge"));
        assertNull("The person's own question waits for the opponent's step", client.decision);
        assertNotNull(scenario.Table().userFeedback().getAwaitingDecision(HUMAN));
        assertNull(scenario.Session().Ruling(HUMAN));

        //Reloading the page in the middle changes nothing
        Client again = new Client(mediator);
        again.connect();
        assertEquals(2, again.step.getIntValue("step"));
        assertNull(again.decision);

        again.acknowledge();
        again.await();
        assertEquals(3, again.step.getIntValue("step"));
        assertNotNull(again.decision);
        assertTrue(again.stepAt < again.decisionAt);
        assertTrue(again.decision.getAwaitingDecision().getText().endsWith("revealed card(s) from top of deck"));

        playScripted(scenario, again);
        again.poll();
        assertEquals(LiveScenario.Result.COMPLETE, scenario.Result());
        assertEquals(List.of(), scenario.Problems());
    }

    @Test
    public void WhatTheGameAsksAfterTheLastStepIsNotAProblem() throws Exception {
        //The step that ends the scenario is answered, and the game goes on to its next question before stopping
        LiveScenario scenario = live(ScenarioTesting.parse("""
                	options: {
                		opponentDelayMs: 0
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			answer: pass
                			end: complete
                		}
                	]
                """));
        Client client = new Client(start(scenario));
        client.connect();
        client.await();
        client.answer("");
        client.await();
        client.poll();

        assertEquals(LiveScenario.Result.COMPLETE, scenario.Result());
        assertEquals(List.of(), scenario.Problems());
        assertNotNull(client.end);
        assertNull("Nothing more is asked of the person once the scenario is over", client.decision);
    }

    @Test
    public void AGameThatCarriesOnOffScriptSaysSoOnce() throws Exception {
        LiveScenario scenario = live(ScenarioTesting.parse("""
                	outcome: {
                		offScript: {
                			message: You're on your own now.
                			result: continue
                		}
                	}
                """ + TWO_STEPS));
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        String aragorn = String.valueOf(scenario.Table().Card("aragorn").getCardId());
        String[] cardIds = client.decision.getAwaitingDecision().getDecisionParameters().get("cardId");
        String[] actionIds = client.decision.getAwaitingDecision().getDecisionParameters().get("actionId");
        for (int i = 0; i < cardIds.length; i++)
            if (cardIds[i].equals(aragorn)) {
                client.answer(actionIds[i]);
                break;
            }
        //A dozen decisions of an ordinary game, for both seats
        for (int i = 0; i < 12 && !mediator.isFinished(); i++) {
            client.await();
            if (client.decision == null)
                break;
            client.answer(AnswerCompiler.leastAnswer(client.decision.getAwaitingDecision()));
        }
        assertEquals("Leaving the script is one problem, however long the game goes on", 1, scenario.Problems().size());
    }

    @Test
    public void ConcedingTakesTheStepOffTheScreen() throws Exception {
        LiveScenario scenario = live(opening());
        LotroGameMediator mediator = start(scenario);
        Client client = new Client(mediator);
        client.connect();
        client.await();
        assertEquals(1, client.step.getIntValue("step"));
        mediator.concede(client.player);

        Client again = new Client(mediator);
        again.connect();
        assertNull("A game that is over has no step to show", again.step);
        assertNotNull(again.end);
        assertEquals("abandoned", again.end.getString("result"));
    }

    @Test
    public void ACheckpointIsReachedAgainHoweverTheGameOrdersItsOptions() throws Exception {
        //Three companions and four minions: the decisions before the checkpoint each offer several cards, and the
        // game lists them in a different order every time it is played
        ScenarioDefinition def = ScenarioTesting.parse("""
                	coverage: guided
                	options: {
                		opponentDelayMs: 0
                	}
                	opponent: {
                		fallback: pass
                	}
                	setup: {
                		freeps: {
                			characters: aragorn, gandalf, boromir
                		}
                		shadow: {
                			minions: bowman, marksman, runner1, runner2
                		}
                	}
                	steps: [
                		{
                			checkpoint: true
                			when: freeps, choice, text(another move)
                			answer: option(No)
                		}
                	]
                """);
        LiveScenario first = live(def);
        Client client = new Client(start(first));
        client.connect();
        for (int i = 0; i < 40; i++) {
            client.await();
            assertNotNull("The game ended before the checkpoint", client.decision);
            if (client.step != null && client.step.getIntValue("step") == 1)
                break;
            var decision = client.decision.getAwaitingDecision();
            String answer = AnswerCompiler.leastAnswer(decision);
            if (decision.getDecisionType() == com.gempukku.lotro.logic.decisions.AwaitingDecisionType.CARD_SELECTION) {
                //Whichever cards have the lowest IDs, so that the same cards are chosen whatever order they are listed in
                String[] ids = decision.getDecisionParameters().get("cardId").clone();
                java.util.Arrays.sort(ids, java.util.Comparator.comparingInt(Integer::parseInt));
                answer = String.join(",", java.util.Arrays.copyOf(ids, Integer.parseInt(decision.getDecisionParameters().get("min")[0])));
            }
            client.answer(answer);
        }
        GameJournal checkpoint = first.CheckpointJournal();
        assertNotNull(checkpoint);
        assertTrue("The test is only worth anything if several decisions lead up to the checkpoint", checkpoint.answers().size() > 5);
        List<Integer> woundsThen = List.of(first.Table().GetWoundsOn(first.Table().Card("aragorn")),
                first.Table().GetWoundsOn(first.Table().Card("gandalf")), first.Table().GetWoundsOn(first.Table().Card("boromir")),
                first.Table().GetWoundsOn(first.Table().Card("frodo")));

        for (int i = 0; i < 10; i++) {
            LiveScenario again = live(def, checkpoint);
            start(again);
            assertNull(again.Table().FollowProblem());
            assertEquals(Integer.valueOf(1), again.ResumedAt());
            assertEquals(woundsThen, List.of(again.Table().GetWoundsOn(again.Table().Card("aragorn")),
                    again.Table().GetWoundsOn(again.Table().Card("gandalf")), again.Table().GetWoundsOn(again.Table().Card("boromir")),
                    again.Table().GetWoundsOn(again.Table().Card("frodo"))));
        }
    }

    @Test
    public void APauseIsReadBeforeItsDecisionIsAsked() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	options: {
                		opponentDelayMs: 0
                	}
                	setup: {
                		freeps: {
                			hand: gandalf, aragorn
                		}
                	}
                	steps: [
                		{
                			pause: true
                			when: freeps, action, phase(fellowship)
                			messages: [
                				Somebody is coming with us.
                				{
                					text: Who?
                					choices: {
                						wizard: Gandalf
                						king: Aragorn
                					}
                				}
                			]
                		}
                		{
                			if: wizard
                			when: freeps, action, phase(fellowship)
                			allow: play(gandalf)
                		}
                		{
                			if: king
                			when: freeps, action, phase(fellowship)
                			allow: play(aragorn)
                		}
                		{
                			checkpoint: true
                			when: freeps, action, phase(fellowship)
                			allow: pass
                		}
                		{
                			pause: true
                			when: freeps
                			messages: That is all.
                			end: complete
                		}
                	]
                """);
        LiveScenario first = live(def);
        LotroGameMediator mediator = start(first);
        Client client = new Client(mediator);
        client.connect();
        client.await();

        //The pause is on the screen, and the decision it comes before has not been put to the person
        assertEquals(1, client.step.getIntValue("step"));
        assertTrue(client.step.getBooleanValue("pause"));
        assertTrue(client.step.getBooleanValue("acknowledge"));
        assertNull(client.decision);
        List<JSONObject> messages = client.step.getList("messages", JSONObject.class);
        assertEquals("next", messages.get(0).getString("advance"));
        assertEquals(List.of("wizard", "king"), messages.get(1).getList("choices", JSONObject.class).stream().map(choice -> choice.getString("name")).toList());

        //Read, choosing: the step the choice leads to arrives, and its decision after it
        mediator.scenarioAcknowledged(client.player, 1, "king");
        client.step = null;
        client.await();
        assertEquals(3, client.step.getIntValue("step"));
        assertNotNull(client.decision);
        assertTrue(client.stepAt < client.decisionAt);
        client.answerAsAllowed();
        client.await();
        assertEquals(4, client.step.getIntValue("step"));
        assertTrue(first.Table().Card("aragorn").getZone().isInPlay());

        //A game brought back to the checkpoint has made the same choice
        GameJournal checkpoint = first.CheckpointJournal();
        LiveScenario again = live(def, checkpoint);
        start(again);
        assertNull(again.Table().FollowProblem());
        assertEquals(Integer.valueOf(4), again.ResumedAt());
        assertEquals(java.util.Set.of("king"), again.Session().Director().flags());
        assertTrue(again.Table().Card("aragorn").getZone().isInPlay());

        //A pause can be the last thing in a scenario: reading it ends the game
        client.answerAsAllowed();
        client.await();
        assertEquals(5, client.step.getIntValue("step"));
        assertNull(client.decision);
        client.acknowledge();
        client.poll();
        assertNotNull(client.end);
        assertEquals("complete", client.end.getString("result"));
        assertTrue(mediator.isFinished());
        assertEquals(HUMAN, mediator.getWinner());
    }

    @Test
    public void AFailedScenarioCanSayWhy() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	options: {
                		opponentDelayMs: 0
                	}
                	coverage: guided
                	outcome: {
                		lose: {
                			type: canSpot
                			filter: name(Gandalf)
                		}
                		loseMessage: You were told to leave the wizard at home.
                	}
                	setup: {
                		freeps: {
                			hand: gandalf
                		}
                	}
                """);
        LotroGameMediator mediator = start(live(def));
        Client client = new Client(mediator);
        client.connect();
        client.await();
        var options = client.decision.getAwaitingDecision().getDecisionParameters();
        client.answer(options.get("actionId")[0]);
        client.poll();
        assertNotNull(client.end);
        assertEquals("failed", client.end.getString("result"));
        assertTrue(client.end.getString("message"), client.end.getString("message").contains("leave the wizard at home"));
    }


    /**
     * A block in a person's game: its steps come up in the order the person plays the stretch in, each saying which
     * of the block's steps it is; and the block is one place for a failed attempt to come back to.
     */
    @Test
    public void ABlockIsPlayedInThePersonsOrder() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(Files.readString(
                new File("src/test/resources/scenarios/two-skirmishes.hjson").toPath(), StandardCharsets.UTF_8));
        def.opponentDelayMs = 0;
        LiveScenario scenario = live(def);
        Client client = new Client(start(scenario));
        client.connect();

        //The assignment, with what it has to say
        client.await();
        assertEquals(1, client.step.getIntValue("step"));
        assertEquals(1, client.step.getIntValue("sub"));
        assertEquals(1, client.step.getJSONArray("messages").size());
        assertEquals("The block is the checkpoint, reached at its first decision", 1, scenario.LastCheckpoint());
        GameJournal checkpoint = scenario.CheckpointJournal();
        assertNotNull(checkpoint);
        client.answerAsAllowed();

        //Aragorn's skirmish first: not the order the script would play them in, and one it allows
        client.await();
        assertEquals(2, client.step.getIntValue("sub"));
        assertEquals(2, client.step.getJSONObject("allow").getList("answers", String.class).size());
        client.answer(String.valueOf(scenario.Table().Card("aragorn").getCardId()));
        client.await();
        assertEquals("The step for his skirmish", 3, client.step.getIntValue("sub"));
        assertEquals("aragornFights", client.step.getString("name"));
        assertEquals(1, client.step.getJSONArray("messages").size());
        client.answerAsAllowed();

        //The picking step again, with the one skirmish that is left; then the step for any other skirmish
        client.await();
        assertEquals(2, client.step.getIntValue("sub"));
        assertEquals(1, client.step.getJSONObject("allow").getList("answers", String.class).size());
        client.answerAsAllowed();
        client.await();
        assertEquals(4, client.step.getIntValue("sub"));
        client.answer("");

        //The block ends where it says, and the script goes on
        client.await();
        assertEquals(2, client.step.getIntValue("step"));
        assertEquals(0, client.step.getIntValue("sub"));
        assertEquals(checkpoint.write(), scenario.CheckpointJournal().write());
        client.answerAsAllowed();
        client.await();
        client.poll();
        assertNotNull(client.end);
        assertEquals("complete", client.end.getString("result"));
        assertEquals(List.of(), scenario.Problems());

        //Started again from the checkpoint, the game is at the block's first decision
        LiveScenario second = live(def, GameJournal.read(checkpoint.write(), player -> player));
        LotroGameMediator mediator = start(second);
        assertNull(second.Table().FollowProblem());
        Client again = new Client(mediator);
        again.connect();
        again.await();
        assertEquals(1, again.step.getIntValue("step"));
        assertEquals(1, again.step.getIntValue("sub"));
        assertEquals(1, second.LastCheckpoint());
    }
}
