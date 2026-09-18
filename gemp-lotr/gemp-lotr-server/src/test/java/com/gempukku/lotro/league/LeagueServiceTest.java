package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.LeagueMatchDAO;
import com.gempukku.lotro.db.LeagueParticipationDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.db.vo.LeagueMatchResult;
import com.gempukku.lotro.prizes.PrizeService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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
}
