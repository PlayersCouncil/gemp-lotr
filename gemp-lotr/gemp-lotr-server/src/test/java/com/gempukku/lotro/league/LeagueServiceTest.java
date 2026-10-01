package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.LeagueMatchDAO;
import com.gempukku.lotro.db.LeagueParticipationDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.game.state.RTMDGameInfo;
import com.gempukku.lotro.prizes.PrizeService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class LeagueServiceTest extends AbstractAtTest {

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayed() throws Exception {

        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        StringBuilder sb = new StringBuilder();
        sb.append("20120502" + "," + "default" + "," + "0.7"
                + "," + "1" + "," + "1" );
        for (int i = 0; i < 1; i++)
            sb.append("," + "lotr_block" + "," + "7" + "," + "2");


        List<League> leagues = new ArrayList<>();
        League league = new League("League name", 5000, 123, League.LeagueType.CONSTRUCTED, sb.toString(), 0);
        leagues.add(league);

        LeagueSerieInfo leagueSerie = league.getLeagueData(_productLibrary, _formatLibrary, null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.any(ZonedDateTime.class))).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getCodeStr())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, null, _cardLibrary, _formatLibrary, _productLibrary, null);

        assertTrue(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player2");

        assertTrue(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getCodeStr());

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getCodeStr(), leagueSerie.getName(), "player1", "player2");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player3");

        assertFalse(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getCodeStr(), leagueSerie.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);
    }

    @Test
    public void testJoiningLeagueAfterMaxGamesPlayedWithPreloadedDb() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        StringBuilder sb = new StringBuilder();
        sb.append("20120502" + "," + "default" + "," + "0.7"
                + "," + "1" + "," + "1" );
        for (int i = 0; i < 1; i++)
            sb.append("," + "lotr_block" + "," + "7" + "," + "2");


        List<League> leagues = new ArrayList<>();
        League league = new League("League name", 5000, 123, League.LeagueType.CONSTRUCTED, sb.toString(), 0);
        leagues.add(league);

        LeagueSerieInfo leagueSerie = league.getLeagueData(_productLibrary, _formatLibrary, null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.any(ZonedDateTime.class))).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();
        matches.add(new LeagueMatchResult(leagueSerie.getName(), "player1", "player2"));

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getCodeStr())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, null, _cardLibrary, _formatLibrary, _productLibrary, null);

        assertTrue(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertTrue(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player3");

        Mockito.verify(leagueMatchDAO).getLeagueMatches(league.getCodeStr());

        Mockito.verify(leagueMatchDAO).addPlayedMatch(league.getCodeStr(), leagueSerie.getName(), "player1", "player3");
        Mockito.verifyNoMoreInteractions(leagueMatchDAO);

        assertFalse(leagueService.canPlayRankedGame(league, leagueSerie, "player1"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player2"));
        assertFalse(leagueService.canPlayRankedGameAgainst(league, leagueSerie, "player1", "player3"));
    }

    @Test
    public void prizeTiersAreAwardedWhenALeagueEnds() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        // a league that ended long ago and is still at status 0, and one that has already been closed
        League ended = new League("Ended league", 0, 123, League.LeagueType.CONSTRUCTED,
                "20120502,default,0.7,1,1,lotr_block,7,2", 0);
        League closed = new League("Closed league", 0, 124, League.LeagueType.CONSTRUCTED,
                "20120502,default,0.7,1,1,lotr_block,7,2", 1);
        Mockito.when(leagueDao.loadActiveLeagues(Mockito.any(ZonedDateTime.class))).thenReturn(List.of(ended, closed));

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);
        Mockito.when(leagueMatchDAO.getLeagueMatches(Mockito.anyString())).thenReturn(new HashSet<>());
        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        Mockito.when(leagueParticipationDAO.getUsersParticipating("123")).thenReturn(Set.of("player1", "player2"));
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        PrizeService prizeService = Mockito.mock(PrizeService.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager,
                null, _cardLibrary, _formatLibrary, _productLibrary, null, prizeService);
        leagueService.getActiveLeagues();

        Mockito.verify(leagueDao).setStatus(ended, 1);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlayerStanding>> standings = ArgumentCaptor.forClass(List.class);
        Mockito.verify(prizeService).awardLeagueTiers(Mockito.same(ended), standings.capture());
        assertEquals(2, standings.getValue().size());
        Mockito.verifyNoMoreInteractions(prizeService);

        // loading the leagues again on the same day does not re-process them
        leagueService.getActiveLeagues();
        Mockito.verifyNoMoreInteractions(prizeService);

        // and a service without a prize service (the old constructor) still closes leagues
        LeagueService plain = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager,
                null, _cardLibrary, _formatLibrary, _productLibrary, null);
        plain.getActiveLeagues();
        Mockito.verify(leagueDao, Mockito.times(2)).setStatus(ended, 1);
    }

    @Test
    public void testStandings() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        StringBuilder sb = new StringBuilder();
        sb.append("20120502" + "," + "default" + "," + "0.7"
                + "," + "1" + "," + "1" );
        for (int i = 0; i < 1; i++)
            sb.append("," + "lotr_block" + "," + "7" + "," + "2");


        List<League> leagues = new ArrayList<>();
        League league = new League("League name", 5000, 123, League.LeagueType.CONSTRUCTED, sb.toString(), 0);
        leagues.add(league);

        LeagueSerieInfo leagueSerie = league.getLeagueData(_productLibrary, _formatLibrary,null).getSeries().getFirst();

        Mockito.when(leagueDao.loadActiveLeagues(Mockito.any(ZonedDateTime.class))).thenReturn(leagues);

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);

        Set<LeagueMatchResult> matches = new HashSet<>();

        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getCodeStr())).thenReturn(new HashSet<>(matches));

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        Set<String> players = new HashSet<>();
        players.add("player1");
        players.add("player2");
        players.add("player3");
        Mockito.when(leagueParticipationDAO.getUsersParticipating(league.getCodeStr())).thenReturn(players);
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO, collectionsManager, null, _cardLibrary, _formatLibrary,  _productLibrary, null);

        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player2");
        leagueService.reportLeagueGameResult(league, leagueSerie, "player1", "player3");
        leagueService.reportLeagueGameResult(league, leagueSerie, "player2", "player3");

        final List<PlayerStanding> leagueSerieStandings = leagueService.getLeagueSerieStandings(league, leagueSerie);
        assertEquals(3, leagueSerieStandings.size());
        assertEquals("player1", leagueSerieStandings.getFirst().playerName);
        assertEquals(4, leagueSerieStandings.getFirst().points);
        assertEquals(2, leagueSerieStandings.getFirst().gamesPlayed);
        assertEquals(1, leagueSerieStandings.getFirst().standing);
        assertEquals("player2", leagueSerieStandings.get(1).playerName);
        assertEquals(3, leagueSerieStandings.get(1).points);
        assertEquals(2, leagueSerieStandings.get(1).gamesPlayed);
        assertEquals(2, leagueSerieStandings.get(1).standing);
        assertEquals("player3", leagueSerieStandings.get(2).playerName);
        assertEquals(2, leagueSerieStandings.get(2).points);
        assertEquals(2, leagueSerieStandings.get(2).gamesPlayed);
        assertEquals(3, leagueSerieStandings.get(2).standing);

        final List<PlayerStanding> leagueStandings = leagueService.getLeagueStandings(league);
        assertEquals(3, leagueStandings.size());
        assertEquals("player1", leagueStandings.getFirst().playerName);
        assertEquals(4, leagueStandings.getFirst().points);
        assertEquals(2, leagueStandings.getFirst().gamesPlayed);
        assertEquals(1, leagueStandings.getFirst().standing);
        assertEquals("player2", leagueStandings.get(1).playerName);
        assertEquals(3, leagueStandings.get(1).points);
        assertEquals(2, leagueStandings.get(1).gamesPlayed);
        assertEquals(2, leagueStandings.get(1).standing);
        assertEquals("player3", leagueStandings.get(2).playerName);
        assertEquals(2, leagueStandings.get(2).points);
        assertEquals(2, leagueStandings.get(2).gamesPlayed);
        assertEquals(3, leagueStandings.get(2).standing);
    }

    /**
     * RTMD's "top N%" league-placement requirement must be resolved against the whole league's standings
     * (every serie combined), never a single serie's standings — even the one a player happens to dominate.
     * <p>
     * "leader" wins big in Serie 1 alone and is never seen in Serie 2. "serieTwoStar" only ever plays (and
     * sweeps) Serie 2, but that alone is not enough to overtake "leader" once both series are combined: with
     * 10 total participants and a top-10% cutoff of one place, only "leader" qualifies.
     */
    @Test
    public void rtmdPlacementUsesStandingsAcrossAllSeriesNotJustOne() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);

        LeagueParams params = new LeagueParams();
        params.name = "Race to Mount Doom";
        params.start = LocalDateTime.of(2026, 3, 1, 0, 0);
        params.cost = 0;
        params.series.add(new LeagueParams.SerieData("fotr_block", 7, 10));
        params.series.add(new LeagueParams.SerieData("fotr_block", 7, 10));
        params.racePath = new ArrayList<>(List.of("92_3"));
        params.raceVisualPath = new ArrayList<>(List.of("90_11"));
        params.raceAdvancementMode = RTMDLeague.AdvanceType.WIN;
        params.raceAdvanceFactor = 1;

        League league = new League(params.name, params.cost, 456, League.LeagueType.RTMD, params.toString(), 0);

        LeagueSerieInfo serieOne = league.getLeagueData(_productLibrary, _formatLibrary, null).getSeries().get(0);
        LeagueSerieInfo serieTwo = league.getLeagueData(_productLibrary, _formatLibrary, null).getSeries().get(1);
        assertEquals("Serie 1", serieOne.getName());
        assertEquals("Serie 2", serieTwo.getName());

        Set<String> players = new HashSet<>(List.of(
                "leader", "serieTwoStar", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"));

        Set<LeagueMatchResult> matches = new HashSet<>();
        // Serie 1: "leader" sweeps every filler. "serieTwoStar" never plays this serie.
        for (String filler : List.of("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"))
            matches.add(new LeagueMatchResult(serieOne.getName(), "leader", filler));
        // Serie 2: "serieTwoStar" is the only player with any wins here, so it tops the serie-2-only standings.
        matches.add(new LeagueMatchResult(serieTwo.getName(), "serieTwoStar", "f1"));
        matches.add(new LeagueMatchResult(serieTwo.getName(), "serieTwoStar", "f2"));

        LeagueMatchDAO leagueMatchDAO = Mockito.mock(LeagueMatchDAO.class);
        Mockito.when(leagueMatchDAO.getLeagueMatches(league.getCodeStr())).thenReturn(matches);

        LeagueParticipationDAO leagueParticipationDAO = Mockito.mock(LeagueParticipationDAO.class);
        Mockito.when(leagueParticipationDAO.getUsersParticipating(league.getCodeStr())).thenReturn(players);

        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);

        LeagueService leagueService = new LeagueService(leagueDao, leagueMatchDAO, leagueParticipationDAO,
                collectionsManager, null, _cardLibrary, _formatLibrary, _productLibrary, null);

        // Sanity check: taken on its own, Serie 2's standings are led by "serieTwoStar", not "leader".
        // (The list itself is not returned in rank order - BestOfOneStandingsProducer rebuilds it from a
        // HashMap - so rank is read from each entry's `standing` field, exactly as RTMDTitles.placementOf does.)
        List<PlayerStanding> serieTwoStandings = leagueService.getLeagueSerieStandings(league, serieTwo);
        assertEquals(1, standingOf(serieTwoStandings, "serieTwoStar").standing);
        assertTrue(standingOf(serieTwoStandings, "leader").standing > 1);

        // But the RTMD game info must be built from the whole-league (all-series) standings.
        RTMDGameInfo info = leagueService.buildRTMDGameInfo(league, List.of("leader", "serieTwoStar"));
        assertNotNull(info);

        RTMDGameInfo.LeaguePlacement leaderPlacement = info.getLeaguePlacement("leader");
        RTMDGameInfo.LeaguePlacement serieTwoStarPlacement = info.getLeaguePlacement("serieTwoStar");

        assertNotNull(leaderPlacement);
        assertNotNull(serieTwoStarPlacement);

        assertEquals(10, leaderPlacement.participants());
        assertEquals(10, serieTwoStarPlacement.participants());

        assertEquals(1, leaderPlacement.rank());
        assertEquals(2, serieTwoStarPlacement.rank());

        // "leader" dominates the league overall and is within the top 10%...
        assertTrue(leaderPlacement.isWithinTopPercentage(10));
        // ...but "serieTwoStar" merely topping one serie is not enough once all series are counted.
        assertFalse(serieTwoStarPlacement.isWithinTopPercentage(10));
    }

    private static PlayerStanding standingOf(List<PlayerStanding> standings, String playerName) {
        for (PlayerStanding standing : standings)
            if (standing.playerName.equals(playerName))
                return standing;
        throw new AssertionError("No standing found for player " + playerName);
    }
}
