package com.gempukku.lotro.hall;

import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.tournament.DailyTournamentPrizes;
import com.gempukku.lotro.tournament.NoPrizes;
import com.gempukku.lotro.tournament.PrizeSummary;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * The prize props of a tournament queue in the hall long-poll: {@code prizes} is the legacy HTML description, and
 * {@code prizesLabel} / {@code prizesLines} (a JSON array of strings) are the same prizes as data for the new hall.
 */
public class HallQueuePrizePropsTest {

    /** Runs a channel over one queue with these prizes and returns the props it would send for it. */
    private static Map<String, String> queuePropsFor(PrizeSummary prizes) {
        Player viewer = Mockito.mock(Player.class);
        Mockito.when(viewer.getName()).thenReturn("Ketura");

        HallServer hallServer = Mockito.mock(HallServer.class);
        Mockito.doAnswer(invocation -> {
            HallInfoVisitor visitor = invocation.getArgument(1);
            visitor.visitTournamentQueue("q1", 0, "Default", "Fellowship Block", "constructed", "Daily",
                    prizes, "Swiss", "At 8 players", 0, "", false, true, false, -1, false, false, null,
                    true, false, false);
            return null;
        }).when(hallServer).processHall(Mockito.any(), Mockito.any());

        Map<String, Map<String, String>> added = new LinkedHashMap<>();
        HallChannelVisitor visitor = Mockito.mock(HallChannelVisitor.class);
        Mockito.doAnswer(invocation -> {
            added.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(visitor).addTournamentQueue(Mockito.anyString(), Mockito.anyMap());

        new HallCommunicationChannel(1).processCommunicationChannel(hallServer, viewer, visitor);
        return added.get("q1");
    }

    @Test
    public void breakdownIsSentAsLegacyHtmlAndAsData() {
        PrizeSummary prizes = new DailyTournamentPrizes("daily", null).getPrizeSummary();
        Map<String, String> props = queuePropsFor(prizes);

        assertEquals(prizes.toLegacyHtml(), props.get("prizes"));
        assertEquals("Prize Breakdown", props.get("prizesLabel"));
        assertEquals("[\"2+ Wins: Required for Event Awards and Tengwar cards\","
                + "\"Top 4: Get Tengwar cards (up to 4 for 1st place)\","
                + "\"Top 10: Get Event Award (up to 10 for 1st place)\","
                + "\"Top 32: Receive random foils (more for higher placement)\"]", props.get("prizesLines"));
    }

    @Test
    public void noPrizesHasALabelAndNoLines() {
        Map<String, String> props = queuePropsFor(new NoPrizes().getPrizeSummary());

        assertEquals("No prizes", props.get("prizes"));
        assertEquals("No prizes", props.get("prizesLabel"));
        assertEquals("[]", props.get("prizesLines"));
    }
}
