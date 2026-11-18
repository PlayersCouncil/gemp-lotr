package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * What a game that was taken back leaves behind it: the mark in each player's recording (which is what a replay is
 * made of), a summary that does not count the play that was undone, and a journal of the game as it stood before.
 */
public class RevertInReplayTest {
    private record Recorded(MediatorSitting sitting, String player, GameCommunicationChannel recording) { }

    /** A recorded game brought to a first turn in which whoever is asked has a card to lay on the table. */
    private static Recorded withACardToPlay() {
        for (int game = 0; game < 60; game++) {
            MediatorSitting sitting = new MediatorSitting();
            GameCommunicationChannel alice = sitting.record("Alice");
            GameCommunicationChannel bob = sitting.record("Bob");
            String player = sitting.toFirstTurn();
            if (sitting.mediator.getGame().getGameState().getCurrentPhase() == Phase.FELLOWSHIP && sitting.playOfACardThatStays(player) != null)
                return new Recorded(sitting, player, player.equals("Alice") ? alice : bob);
        }
        throw new AssertionError("No game in 60 began with a card to play");
    }

    private static ReplayMetadata summaryOf(String player, List<GameEvent> events) {
        ReplayMetadata summary = new ReplayMetadata();
        summary.Decks.put(player, summary.new DeckMetadata());
        summary.ParseReplay(player, events);
        return summary;
    }

    private static boolean playedA(ReplayMetadata summary, String blueprint) {
        return summary.PlayedCards.stream().anyMatch(id -> blueprint.equals(summary.AllCards.get(id.toString())));
    }

    @Test
    public void ACardPlayedAndKeptIsInTheSummary() {
        Recorded recorded = withACardToPlay();
        MediatorSitting sitting = recorded.sitting();
        String play = sitting.playOfACardThatStays(recorded.player());
        String blueprint = sitting.blueprintOf(recorded.player(), play);
        sitting.answer(recorded.player(), play);
        //(a card that wants a bearer is not down until it has one)
        while (sitting.asked() != null && sitting.asked().equals(recorded.player()) && sitting.playOfACardThatStays(recorded.player()) == null
                && !sitting.asked(recorded.player(), "Play Fellowship action"))
            sitting.play(1);
        sitting.mediator.concede(new Player(1, recorded.player(), "pass", "u", null, null, null, null, false));

        ReplayMetadata summary = summaryOf(recorded.player(), recorded.recording().consumeGameEvents());

        assertTrue(playedA(summary, blueprint));
        assertTrue(summary.Reverts.isEmpty());
    }

    @Test
    public void ACardPlayedAndTakenBackIsMarkedInTheRecordingAndLeftOutOfTheSummary() {
        Recorded recorded = withACardToPlay();
        MediatorSitting sitting = recorded.sitting();
        String player = recorded.player();
        String play = sitting.playOfACardThatStays(player);
        String blueprint = sitting.blueprintOf(player, play);
        sitting.answer(player, play);
        while (sitting.asked() != null && sitting.asked().equals(player) && sitting.playOfACardThatStays(player) == null
                && !sitting.asked(player, "Play Fellowship action"))
            sitting.play(1);

        assertEquals("done", sitting.mediator.requestUndo(player, null).status());
        sitting.mediator.concede(new Player(1, player, "pass", "u", null, null, null, null, false));

        List<GameEvent> events = recorded.recording().consumeGameEvents();
        GameEvent mark = null;
        int markAt = -1;
        for (int i = 0; i < events.size(); i++)
            if (events.get(i).getType() == GameEvent.Type.GAME_REVERTED) {
                mark = events.get(i);
                markAt = i;
            }
        assertNotNull("The recording says the game was taken back", mark);
        assertEquals(player, mark.getParticipantId());
        assertEquals("GREEN", mark.getTier());
        assertTrue(mark.getMessage(), mark.getMessage().contains("took the game back"));
        assertTrue("...and where the undone play began", mark.getIndex() > 0 && mark.getIndex() < markAt);
        boolean tableSentAgain = false;
        for (int i = mark.getIndex(); i < markAt; i++)
            tableSentAgain |= events.get(i).getType() == GameEvent.Type.PARTICIPANTS;
        assertTrue("The table as it now stands is in the recording ahead of the mark", tableSentAgain);

        ReplayMetadata summary = summaryOf(player, events);
        assertFalse("The card that was taken back was not played", playedA(summary, blueprint));

        assertEquals(1, sitting.mediator.getReverts().size());
        assertEquals("GREEN", sitting.mediator.getReverts().get(0).tier());
        assertEquals(1, sitting.mediator.getReverts().get(0).turn());
    }

    @Test
    public void TheJournalKeptOfAGameHoldsTheGameAsItStoodEachTimeItWasTakenBack() {
        Recorded recorded = withACardToPlay();
        MediatorSitting sitting = recorded.sitting();
        String player = recorded.player();
        sitting.answer(player, sitting.playOfACardThatStays(player));
        int entriesBefore = sitting.mediator.journalSize();
        assertEquals("done", sitting.mediator.requestUndo(player, null).status());
        int entriesAfter = sitting.mediator.journalSize();
        assertTrue(entriesAfter < entriesBefore);

        String kept = sitting.mediator.getAuditJournal();

        String[] parts = kept.split("(?m)^# .*\n");
        assertEquals("One journal for the game as it stood, one for the game as it is", 3, parts.length);
        assertEquals(entriesBefore, GameJournal.read(parts[1], GameJournal.PlayerKeys.NAMES).entries().size());
        assertEquals(entriesAfter, GameJournal.read(parts[2], GameJournal.PlayerKeys.NAMES).entries().size());
        assertTrue(kept, kept.contains("# The game as it ended\n"));
        assertTrue(kept.startsWith("# " + player + " took the game back"));
    }
}
