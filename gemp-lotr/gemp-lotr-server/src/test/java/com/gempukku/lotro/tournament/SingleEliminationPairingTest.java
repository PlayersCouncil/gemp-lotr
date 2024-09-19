package com.gempukku.lotro.tournament;

import static org.junit.Assert.*;

import com.gempukku.lotro.at.AbstractAtTest;
import org.junit.Test;

import java.util.*;

public class SingleEliminationPairingTest extends AbstractAtTest {
    private final SingleEliminationPairing _pairing = new SingleEliminationPairing("single-elimination");

    @Test
    public void correctlyDetectsFinishedTournament() {
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3", "p4"));
        Set<String> playersWithoutFirst = new HashSet<>(Arrays.asList("p2", "p3", "p4"));
        Set<String> playersWithoutTwo = new HashSet<>(Arrays.asList("p3", "p4"));
        Set<String> playersWithoutThree = new HashSet<>(List.of("p4"));

        assertTrue(_pairing.isFinished(1, allPlayers, allPlayers));
        assertTrue(_pairing.isFinished(1, allPlayers, playersWithoutFirst));
        assertFalse(_pairing.isFinished(1, allPlayers, playersWithoutTwo));
        assertFalse(_pairing.isFinished(1, allPlayers, playersWithoutThree));
        assertFalse(_pairing.isFinished(1, allPlayers, Collections.emptySet()));
    }

    @Test
    public void shouldDropLoser() {
        assertTrue(_pairing.shouldDropLoser());
    }

    @Test
    public void pairingDoneCorrectlyForEvenNumberOfPlayers() {
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3", "p4"));

        Map<String, String> pairingResults = new HashMap<>();
        Set<String> byeResults = new HashSet<>();

        assertFalse(_pairing.pairPlayers(1, allPlayers, Collections.emptySet(), Collections.emptyMap(), null, null, pairingResults, byeResults));

        assertEquals(0, byeResults.size());
        assertEquals(2, pairingResults.size());

        Set<String> pairedPlayers = new HashSet<>();
        for (Map.Entry<String, String> pairing : pairingResults.entrySet()) {
            pairedPlayers.add(pairing.getKey());
            pairedPlayers.add(pairing.getValue());
        }

        assertEquals(4, pairedPlayers.size());
    }

    @Test
    public void pairingDoneCorrectlyForOddNumberOfPlayers() {
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3"));

        Map<String, String> pairingResults = new HashMap<>();
        Set<String> byeResults = new HashSet<>();

        assertFalse(_pairing.pairPlayers(1, allPlayers, Collections.emptySet(), Collections.emptyMap(), null, null, pairingResults, byeResults));

        assertEquals(1, byeResults.size());
        assertEquals(1, pairingResults.size());

        Set<String> pairedPlayers = new HashSet<>();
        pairedPlayers.addAll(byeResults);
        for (Map.Entry<String, String> pairing : pairingResults.entrySet()) {
            pairedPlayers.add(pairing.getKey());
            pairedPlayers.add(pairing.getValue());
        }

        assertEquals(3, pairedPlayers.size());
    }

    @Test
    public void playerWithLowestNumberOfByesGetsIt() {
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3"));

        Map<String, String> pairingResults = new HashMap<>();
        Set<String> byeResults = new HashSet<>();

        Map<String, Integer> playerByes = new HashMap<>();
        playerByes.put("p1", 1);
        playerByes.put("p2", 2);
        playerByes.put("p3", 3);

        assertFalse(_pairing.pairPlayers(1, allPlayers, Collections.emptySet(), playerByes, null, null, pairingResults, byeResults));

        assertEquals(1, byeResults.size());
        assertTrue(byeResults.contains("p1"));
        assertEquals(1, pairingResults.size());

        Set<String> pairedPlayers = new HashSet<>();
        pairedPlayers.addAll(byeResults);

        for (Map.Entry<String, String> pairing : pairingResults.entrySet()) {
            pairedPlayers.add(pairing.getKey());
            pairedPlayers.add(pairing.getValue());
        }

        assertEquals(3, pairedPlayers.size());
    }
    
    @Test
    public void droppedPlayersNotIncluded() {
        Set<String> allPlayers = new HashSet<>(Arrays.asList("p1", "p2", "p3", "p4"));
        Set<String> droppedPlayers = new HashSet<>(List.of("p4"));

        Map<String, String> pairingResults = new HashMap<>();
        Set<String> byeResults = new HashSet<>();

        assertFalse(_pairing.pairPlayers(1, allPlayers, droppedPlayers, Collections.emptyMap(), null, null, pairingResults, byeResults));

        assertEquals(1, byeResults.size());
        assertEquals(1, pairingResults.size());

        Set<String> pairedPlayers = new HashSet<>();
        pairedPlayers.addAll(byeResults);
        for (Map.Entry<String, String> pairing : pairingResults.entrySet()) {
            pairedPlayers.add(pairing.getKey());
            pairedPlayers.add(pairing.getValue());
        }

        assertEquals(3, pairedPlayers.size());
    }
}
