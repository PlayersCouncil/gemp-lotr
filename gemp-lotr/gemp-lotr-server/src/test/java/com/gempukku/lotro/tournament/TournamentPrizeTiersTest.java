package com.gempukku.lotro.tournament;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.hall.TableHolder;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.prizes.PrizeItem;
import com.gempukku.lotro.prizes.PrizeService;
import com.gempukku.lotro.prizes.PrizeTier;
import com.gempukku.util.JsonUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;

/**
 * The configurable prize tiers of a tournament ({@link TournamentParams#prizeTiers}) are stored with the tournament's
 * parameters and handed to the PrizeService, with the final standings, when the tournament finishes.
 */
public class TournamentPrizeTiersTest extends AbstractAtTest {

    private static final String TIERS_JSON = """
            [
              {"kind":"PLACEMENT","from":1,"to":1,"games":0,"scope":"EVENT","label":"Winner",
               "items":[{"blueprintId":"9_1*","promise":null,"count":2},{"blueprintId":null,"promise":"Champion promo","count":1}]},
              {"kind":"PARTICIPATION","from":1,"to":1,"games":1,"scope":"EVENT","label":null,
               "items":[{"blueprintId":"1_2","promise":null,"count":1}]}
            ]
            """;

    @Test
    public void oldParametersWithoutTiersStillParse() {
        var params = Tournament.parseInfo("CONSTRUCTED", """
                {
                    format: fotr_block
                    playoff: SWISS
                    type: CONSTRUCTED
                    prizes: DAILY
                    topPrize: "2x 9_1*"
                    participationGames: 3
                }
                """);
        assertNotNull(params);
        assertEquals("fotr_block", params.format);
        assertNotNull("missing prizeTiers must read as an empty list", params.prizeTiers);
        assertTrue(params.prizeTiers.isEmpty());

        var sealed = Tournament.parseInfo("SEALED", "{\"format\":\"fotr_block\",\"sealedFormatCode\":\"fotr_sealed\",\"type\":\"SEALED\"}");
        assertTrue(sealed instanceof SealedTournamentParams);
        assertNotNull(sealed.prizeTiers);
    }

    @Test
    public void tiersRoundTripThroughStoredParameters() {
        var params = new TournamentParams();
        params.tournamentId = "t9";
        params.name = "Round trip";
        params.format = "fotr_block";
        params.type = Tournament.TournamentType.CONSTRUCTED;
        params.prizeTiers.add(PrizeTier.placement(1, 3, PrizeItem.card("9_1*", 2), PrizeItem.promise("Champion promo", 1)));
        params.prizeTiers.add(PrizeTier.participation(4, PrizeTier.Scope.EVENT, PrizeItem.card("1_2", 1)));

        String stored = JsonUtils.Serialize(params);
        assertTrue(stored, stored.contains("\"prizeTiers\""));

        var parsed = Tournament.parseInfo("CONSTRUCTED", stored);
        assertEquals(2, parsed.prizeTiers.size());
        assertEquals(PrizeTier.Kind.PLACEMENT, parsed.prizeTiers.get(0).kind);
        assertEquals(3, parsed.prizeTiers.get(0).to);
        assertEquals("9_1*", parsed.prizeTiers.get(0).items.get(0).blueprintId);
        assertEquals(2, parsed.prizeTiers.get(0).items.get(0).count);
        assertEquals("Champion promo", parsed.prizeTiers.get(0).items.get(1).promise);
        assertEquals(PrizeTier.Kind.PARTICIPATION, parsed.prizeTiers.get(1).kind);
        assertEquals(4, parsed.prizeTiers.get(1).games);
    }

    @Test
    public void finishedTournamentAwardsItsTiersWithTheFinalStandings() {
        var tournamentService = Mockito.mock(TournamentService.class);
        var prizeService = Mockito.mock(PrizeService.class);
        Mockito.when(tournamentService.getPrizeService()).thenReturn(prizeService);
        var collectionsManager = Mockito.mock(CollectionsManager.class);

        var tournament = playedOutTournament(tournamentService, "t1", "Prize Tourney");

        tournament.finishTournament(collectionsManager);

        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());

        // the automatic (DAILY) prizes are still handed out by the tournament itself
        Mockito.verify(collectionsManager, Mockito.atLeastOnce()).addItemsToPlayerCollection(
                Mockito.eq(true), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(CollectionType.MY_CARDS), Mockito.anyCollection());

        // ... and the configurable tiers go through the PrizeService, exactly once, for this tournament
        ArgumentCaptor<PrizeService.EventRef> event = ArgumentCaptor.forClass(PrizeService.EventRef.class);
        ArgumentCaptor<List<PrizeTier>> tiers = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<PlayerStanding>> standings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(prizeService, Mockito.times(1)).awardTiers(event.capture(), tiers.capture(), standings.capture(), Mockito.isNull());

        assertEquals(PrizeService.KIND_TOURNAMENT, event.getValue().kind());
        assertEquals("t1", event.getValue().id());
        assertEquals("Prize Tourney", event.getValue().name());
        assertNull(event.getValue().campaign());

        assertEquals(2, tiers.getValue().size());
        assertEquals("Winner", tiers.getValue().get(0).label);
        assertEquals("9_1*", tiers.getValue().get(0).items.get(0).blueprintId);
        assertEquals("Champion promo", tiers.getValue().get(0).items.get(1).promise);
        assertEquals(PrizeTier.Kind.PARTICIPATION, tiers.getValue().get(1).kind);

        // the standings are the tournament's own: p1 beat p2 and p3, p2 beat p3
        assertSame(tournament.getCurrentStandings(), standings.getValue());
        Map<String, PlayerStanding> byName = new HashMap<>();
        for (PlayerStanding standing : standings.getValue())
            byName.put(standing.playerName, standing);
        assertEquals(3, byName.size());
        assertEquals(1, byName.get("p1").standing);
        assertEquals(2, byName.get("p2").standing);
        assertEquals(3, byName.get("p3").standing);
        assertEquals(2, byName.get("p1").gamesPlayed);
        assertEquals(2, byName.get("p3").gamesPlayed);
    }

    @Test
    public void tiersAreSkippedWhenNoPrizeServiceIsWired() {
        var tournamentService = Mockito.mock(TournamentService.class);
        var collectionsManager = Mockito.mock(CollectionsManager.class);

        var tournament = playedOutTournament(tournamentService, "t2", "No service");
        tournament.finishTournament(collectionsManager);

        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());
        Mockito.verify(collectionsManager, Mockito.atLeastOnce()).addItemsToPlayerCollection(
                Mockito.eq(true), Mockito.anyString(), Mockito.eq("p1"), Mockito.eq(CollectionType.MY_CARDS), Mockito.anyCollection());
    }

    @Test
    public void prizeServiceFailureDoesNotStopTheTournamentFromFinishing() {
        var tournamentService = Mockito.mock(TournamentService.class);
        var prizeService = Mockito.mock(PrizeService.class);
        Mockito.when(tournamentService.getPrizeService()).thenReturn(prizeService);
        Mockito.doThrow(new RuntimeException("db down")).when(prizeService)
                .awardTiers(Mockito.any(), Mockito.anyList(), Mockito.anyList(), Mockito.any());
        var collectionsManager = Mockito.mock(CollectionsManager.class);

        var tournament = playedOutTournament(tournamentService, "t3", "Broken");
        tournament.finishTournament(collectionsManager);

        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());
        Mockito.verify(prizeService).awardTiers(Mockito.any(), Mockito.anyList(), Mockito.anyList(), Mockito.any());
    }

    /**
     * A constructed tournament loaded from (mocked) storage in the "Playing Games" stage, with prize tiers in its
     * stored parameters and three finished matches: p1 > p2, p1 > p3, p2 > p3.
     */
    private static ConstructedTournament playedOutTournament(TournamentService tournamentService, String tournamentId, String name) {
        var tourneyData = new DBDefs.Tournament();
        tourneyData.tournament_id = tournamentId;
        tourneyData.name = name;
        tourneyData.stage = "Playing Games";
        tourneyData.round = 2;
        tourneyData.type = Tournament.TournamentType.CONSTRUCTED.toString();
        tourneyData.start_date = DateUtils.Now().minus(Duration.ofMinutes(10)).toLocalDateTime();
        tourneyData.parameters = "{ \"tournamentId\": \"" + tournamentId + "\", \"name\": \"" + name + "\", \"format\": \"fotr_block\","
                + " \"playoff\": \"SWISS\", \"type\": \"CONSTRUCTED\", \"prizes\": \"DAILY\", \"prizeTiers\": " + TIERS_JSON + " }";

        Set<String> players = new HashSet<>(Arrays.asList("p1", "p2", "p3"));
        Map<String, LotroDeck> decks = new HashMap<>();
        for (String player : players)
            decks.put(player, new LotroDeck(player));

        var tables = Mockito.mock(TableHolder.class);
        Mockito.when(tables.getTournamentTables(tournamentId)).thenReturn(new ArrayList<>());

        Mockito.when(tournamentService.retrieveTournamentData(tournamentId)).thenReturn(tourneyData);
        Mockito.when(tournamentService.retrieveTournamentPlayers(tournamentId)).thenReturn(players);
        Mockito.when(tournamentService.retrievePlayerDecks(tournamentId, "fotr_block")).thenReturn(decks);
        Mockito.when(tournamentService.retrieveAbandonedPlayers(tournamentId)).thenReturn(new HashSet<>());
        Mockito.when(tournamentService.retrieveTournamentByes(tournamentId)).thenReturn(new HashMap<>());
        Mockito.when(tournamentService.getPairingMechanism(Tournament.PairingType.SWISS)).thenReturn(Tournament.getPairingMechanism(Tournament.PairingType.SWISS));
        Mockito.when(tournamentService.retrieveMatchups(tournamentId)).thenReturn(List.of(
                new TournamentMatch("p1", "p2", "p1", 1),
                new TournamentMatch("p1", "p3", "p1", 2),
                new TournamentMatch("p2", "p3", "p2", 2)));

        return new ConstructedTournament(tournamentService, null, _productLibrary, _formatLibrary, null, null, tables, tournamentId);
    }
}
