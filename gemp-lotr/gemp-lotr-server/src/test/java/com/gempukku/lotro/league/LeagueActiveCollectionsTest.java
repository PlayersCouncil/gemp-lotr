package com.gempukku.lotro.league;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.LeagueDAO;
import com.gempukku.lotro.db.LeagueMatchDAO;
import com.gempukku.lotro.db.LeagueParticipationDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * {@link LeagueService#getActiveLeagueCollections()}: the collections the admin add-items form offers besides
 * My cards and Trophies.
 */
public class LeagueActiveCollectionsTest extends AbstractAtTest {

    private static LeagueParams params(String name, long code, String collectionName, ZonedDateTime start, String format, int days) {
        var p = new LeagueParams();
        p.name = name;
        p.code = code;
        p.start = start.toLocalDateTime();
        p.cost = 0;
        p.maxRepeatMatches = 1;
        p.collectionName = collectionName;
        p.series.add(new LeagueParams.SerieData(format, days, 5));
        return p;
    }

    private static League league(League.LeagueType type, LeagueParams p) {
        return new League(p.name, p.cost, p.code, type, p.toString(), 0);
    }

    @Test
    public void listsSealedAndDraftCollectionsOnly() throws Exception {
        var now = DateUtils.Now();
        var running = now.minusDays(2);
        var upcoming = now.plusDays(3);

        List<League> leagues = new ArrayList<>();
        leagues.add(league(League.LeagueType.SEALED, params("Sealed Soon", 3001, "Sealed Soon Cards", upcoming, "fotr_block_sealed", 7)));
        leagues.add(league(League.LeagueType.SEALED, params("Sealed Now", 3002, "Sealed Now Cards", running.minusDays(1), "fotr_block_sealed", 7)));
        leagues.add(league(League.LeagueType.SOLODRAFT, params("Draft Now", 3003, "Draft Now Cards", running, "fotr_draft", 14)));
        leagues.add(league(League.LeagueType.CONSTRUCTED, params("Constructed Now", 3004, "default", running, "fotr_block", 7)));

        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);
        Mockito.when(leagueDao.loadActiveLeagues(Mockito.any(ZonedDateTime.class))).thenReturn(leagues);
        LeagueMatchDAO matchDao = Mockito.mock(LeagueMatchDAO.class);
        Mockito.when(matchDao.getLeagueMatches(Mockito.anyString())).thenReturn(new HashSet<>());
        LeagueParticipationDAO participationDao = Mockito.mock(LeagueParticipationDAO.class);
        Mockito.when(participationDao.getUsersParticipating(Mockito.anyString())).thenReturn(new HashSet<>());
        CollectionsManager collectionsManager = Mockito.mock(CollectionsManager.class);
        var soloDraftDefinitions = new SoloDraftDefinitions(new CollectionsManager(null, null, null, _cardLibrary, _productLibrary), _cardLibrary, _formatLibrary);

        var service = new LeagueService(leagueDao, matchDao, participationDao, collectionsManager, null,
                _cardLibrary, _formatLibrary, _productLibrary, soloDraftDefinitions);

        var collections = service.getActiveLeagueCollections();
        var codes = new ArrayList<String>();
        for (var c : collections)
            codes.add(c.code());

        assertEquals("sealed and draft only, ordered by start", List.of("3002", "3003", "3001"), codes);

        var sealedNow = collections.getFirst();
        assertEquals("Sealed Now", sealedNow.leagueName());
        assertEquals("Sealed Now Cards", sealedNow.collectionName());
        assertEquals(League.LeagueType.SEALED, sealedNow.type());
        assertNotNull(sealedNow.start());
        assertNotNull(sealedNow.end());
        assertTrue(sealedNow.end().isAfter(sealedNow.start()));

        assertEquals(League.LeagueType.SOLODRAFT, collections.get(1).type());
        assertTrue("upcoming league starts in the future", collections.get(2).start().isAfter(now));

        // every listed code is one the addItems endpoint can resolve
        for (var c : collections)
            assertNotNull(c.code(), service.getCollectionTypeByCode(c.code()));
    }

    @Test
    public void emptyWhenNoLeagues() throws Exception {
        LeagueDAO leagueDao = Mockito.mock(LeagueDAO.class);
        Mockito.when(leagueDao.loadActiveLeagues(Mockito.any(ZonedDateTime.class))).thenReturn(new ArrayList<>());
        var service = new LeagueService(leagueDao, Mockito.mock(LeagueMatchDAO.class), Mockito.mock(LeagueParticipationDAO.class),
                Mockito.mock(CollectionsManager.class), null, _cardLibrary, _formatLibrary, _productLibrary, null);
        assertTrue(service.getActiveLeagueCollections().isEmpty());
    }
}
