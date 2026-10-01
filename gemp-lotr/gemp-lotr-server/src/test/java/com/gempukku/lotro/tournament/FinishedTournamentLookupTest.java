package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.GameHistoryDAO;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.FormatNames;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * {@link TournamentService#getTournamentById} for a tournament that is not in the active map - what the tournament
 * detail view, deck and report endpoints do for every finished tournament the event browser opens.
 */
public class FinishedTournamentLookupTest {
    private TournamentDAO _tournamentDao;
    /** Knows no format at all, as for a tournament played in a format that has since been retired. */
    private LotroFormatLibrary _formatLibrary;
    private TournamentService _service;

    @Before
    public void setUp() {
        _tournamentDao = Mockito.mock(TournamentDAO.class);
        _formatLibrary = Mockito.mock(LotroFormatLibrary.class);
        _service = new TournamentService(Mockito.mock(CollectionsManager.class), Mockito.mock(ProductLibrary.class),
                null, _tournamentDao, Mockito.mock(TournamentPlayerDAO.class), Mockito.mock(TournamentMatchDAO.class),
                Mockito.mock(GameHistoryDAO.class), Mockito.mock(LotroCardBlueprintLibrary.class), _formatLibrary,
                Mockito.mock(SoloDraftDefinitions.class), Mockito.mock(TableDraftDefinitions.class), null);
    }

    private DBDefs.Tournament stored(String id, String stage) {
        var row = new DBDefs.Tournament();
        row.tournament_id = id;
        row.name = "Old Event";
        row.start_date = LocalDateTime.of(2019, 5, 4, 19, 0);
        row.type = "CONSTRUCTED";
        row.parameters = "{\"tournamentId\":\"" + id + "\",\"name\":\"Old Event\",\"format\":\"ancient_block\","
                + "\"type\":\"CONSTRUCTED\",\"playoff\":\"SWISS\",\"prizes\":\"NONE\"}";
        row.stage = stage;
        row.round = 4;
        Mockito.when(_tournamentDao.getTournamentById(id)).thenReturn(row);
        Mockito.when(_tournamentDao.getTournament(id)).thenReturn(row);
        return row;
    }

    @Test
    public void aFinishedTournamentIsNotMadeLive() {
        // The stage column holds the enum name.  Before, it was compared with "Finished", never matched, and the
        // finished tournament was put into the active map - shown in the hall as a live tournament until the
        // next processTournaments pass removed it again.
        stored("t1", "FINISHED");

        var tournament = _service.getTournamentById("t1");

        assertNotNull(tournament);
        assertEquals(Tournament.Stage.FINISHED, tournament.getTournamentStage());
        assertTrue(_service.getLiveTournaments().isEmpty());
    }

    @Test
    public void theHumanReadableStageIsAlsoRecognised() {
        stored("t1", "Finished");

        _service.getTournamentById("t1");

        assertTrue(_service.getLiveTournaments().isEmpty());
    }

    @Test
    public void anUnfinishedTournamentLoadedFromTheDatabaseIsStillMadeLive() {
        stored("t2", "PAUSED");

        var tournament = _service.getTournamentById("t2");

        assertEquals(1, _service.getLiveTournaments().size());
        assertSame(tournament, _service.getTournamentById("t2"));
    }

    @Test
    public void aFinishedTournamentInARetiredFormatStillLoadsAndShowsItsRawCode() {
        // Before: BaseTournament.RefreshTournamentInfo called Format.getCode() on the null format, so the tournament
        // could not be constructed at all and every detail/deck/report request for it was a 500.
        stored("t1", "FINISHED");

        var tournament = _service.getTournamentById("t1");

        assertEquals("ancient_block", tournament.getFormatCode());
        assertEquals("ancient_block", FormatNames.nameOrCode(_formatLibrary, tournament.getFormatCode()));
    }

    @Test
    public void anUnknownTournamentIsNull() {
        assertNull(_service.getTournamentById("nope"));
    }
}
