package com.gempukku.lotro.game;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.db.GameHistoryFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * {@link GameHistoryService#getGameHistoryPage}: page bounds, which count the pager gets, and when the database is
 * asked at all.
 */
public class GameHistoryPageTest {
    private GameHistoryDAO _dao;
    private GameHistoryService _service;
    private Player _player;

    @Before
    public void setUp() {
        _dao = Mockito.mock(GameHistoryDAO.class);
        _service = new GameHistoryService(_dao);
        _player = new Player(1, "alice", "pass", "u", null, null, null, null, false);
        Mockito.when(_dao.getGameHistoryForPlayerCount(_player)).thenReturn(250);
        Mockito.when(_dao.getGameHistoryForPlayer(eq(_player), any(GameHistoryFilter.class), anyInt(), anyInt()))
                .thenReturn(List.of(new DBDefs.GameHistory()));
    }

    @Test
    public void unfilteredPageUsesTheTotalAsTheMatchingCount() {
        var page = _service.getGameHistoryPage(_player, GameHistoryFilter.NONE, 40, 20);

        assertEquals(250, page.matching());
        assertEquals(250, page.total());
        assertEquals(40, page.start());
        assertEquals(20, page.count());
        assertEquals(1, page.entries().size());
        Mockito.verify(_dao, Mockito.never()).getGameHistoryForPlayerCount(eq(_player), any(GameHistoryFilter.class));
        Mockito.verify(_dao).getGameHistoryForPlayer(_player, GameHistoryFilter.NONE, 40, 20);
    }

    @Test
    public void pageSizeIsBoundedOnTheServer() {
        var page = _service.getGameHistoryPage(_player, null, 0, 100000);
        assertEquals(GameHistoryService.MAX_HISTORY_PAGE_SIZE, page.count());
        Mockito.verify(_dao).getGameHistoryForPlayer(eq(_player), any(GameHistoryFilter.class), eq(0), eq(GameHistoryService.MAX_HISTORY_PAGE_SIZE));

        assertEquals(1, _service.getGameHistoryPage(_player, null, 0, 0).count());
        assertEquals(1, _service.getGameHistoryPage(_player, null, 0, -5).count());
        assertEquals(0, _service.getGameHistoryPage(_player, null, -10, 20).start());
    }

    @Test
    public void filteredPageCountsTheMatchesSeparately() {
        var filter = new GameHistoryFilter("PC-Movie Block", "bob", false, null, LocalDate.of(2026, 1, 1), null);
        Mockito.when(_dao.getGameHistoryForPlayerCount(_player, filter)).thenReturn(7);

        var page = _service.getGameHistoryPage(_player, filter, 0, 20);

        assertEquals(7, page.matching());
        assertEquals(250, page.total());
        Mockito.verify(_dao).getGameHistoryForPlayer(_player, filter, 0, 20);
    }

    @Test
    public void noQueryForAPageBeyondTheLastMatch() {
        var filter = new GameHistoryFilter(null, "bob", false, null, null, null);
        Mockito.when(_dao.getGameHistoryForPlayerCount(_player, filter)).thenReturn(7);

        var page = _service.getGameHistoryPage(_player, filter, 20, 20);
        assertTrue(page.entries().isEmpty());
        assertEquals(7, page.matching());

        Mockito.when(_dao.getGameHistoryForPlayerCount(_player, filter)).thenReturn(0);
        assertTrue(_service.getGameHistoryPage(_player, filter, 0, 20).entries().isEmpty());

        Mockito.verify(_dao, Mockito.never()).getGameHistoryForPlayer(eq(_player), ArgumentMatchers.any(GameHistoryFilter.class), anyInt(), anyInt());
    }

    @Test
    public void totalIsCachedAcrossPages() {
        _service.getGameHistoryPage(_player, null, 0, 20);
        _service.getGameHistoryPage(_player, null, 20, 20);
        _service.getGameHistoryPage(_player, new GameHistoryFilter("X", null, false, null, null, null), 0, 20);

        Mockito.verify(_dao, Mockito.times(1)).getGameHistoryForPlayerCount(_player);
    }

    @Test
    public void filterOptionsComeFromTheDaoWithTheEventCap() {
        Mockito.when(_dao.getGameHistoryFormats(_player)).thenReturn(List.of("PC-Movie Block", "PC-FotR Block"));
        Mockito.when(_dao.getGameHistoryEvents(_player, GameHistoryService.MAX_EVENT_OPTIONS)).thenReturn(List.of("Weekly Sealed"));

        assertEquals(List.of("PC-Movie Block", "PC-FotR Block"), _service.getGameHistoryFormats(_player));
        assertEquals(List.of("Weekly Sealed"), _service.getGameHistoryEvents(_player));
    }
}
