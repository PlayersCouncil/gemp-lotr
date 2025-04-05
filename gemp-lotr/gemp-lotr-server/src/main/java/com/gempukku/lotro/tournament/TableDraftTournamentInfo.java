package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft3.TableDraftDefinition;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TableDraftTournamentInfo extends TournamentInfo {

    public final TableDraftDefinition tableDraftDefinition;
    public ZonedDateTime deckbuildingDeadline;
    public final Duration deckbuildingDuration;
    public ZonedDateTime registrationDeadline;
    public final Duration registrationDuration;
    protected TableDraftTournamentParams tableDraftParams;

    public TableDraftTournamentInfo(TableDraftDefinition tableDraftDefinition, TournamentPrizes prizes, PairingMechanism pairing, LotroFormat format, TableDraftTournamentParams params,
                                    String idPrefix, ZonedDateTime start, Tournament.Stage stage, int round) {
        super(prizes, pairing, format, params, idPrefix, start, stage, round);
        this.tableDraftDefinition = tableDraftDefinition;
        this.tableDraftParams = params;
        _params = params;

        deckbuildingDuration = Duration.ofMinutes(tableDraftParams.deckbuildingDuration);
        registrationDuration = Duration.ofMinutes(tableDraftParams.turnInDuration);
//
        deckbuildingDeadline = StartTime.plus(deckbuildingDuration);
//        registrationDeadline = deckbuildingDeadline.plus(registrationDuration);
    }

    //Used by tournament queues to duplicate a template info with fresh parameters
    public TableDraftTournamentInfo(TableDraftTournamentInfo info, TableDraftTournamentParams params) {
        this(info.tableDraftDefinition, info.Prizes, info.PairingMechanism, info.Format, params, info.IdPrefix, DateUtils.ParseDate(params.startTime),
                params.getInitialStage(), 0);
    }

    //Intermediary for consolidating both db constructors
    public TableDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, ZonedDateTime start, TableDraftTournamentParams params, TableDraftDefinitions tableDraftDefinitions) {
        this(tableDraftDefinitions.getTableDraftDefinition(params.tableDraftFormatCode), Tournament.getTournamentPrizes(productLibrary, params.prizes), tournamentService.getPairingMechanism(params.playoff),
                formatLibrary.getFormat(params.format), params, params.tournamentId, start, params.getInitialStage(), 0);
    }

    //Pulling directly from database
    public TableDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.Tournament data, TableDraftDefinitions tableDraftDefinitions) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (TableDraftTournamentParams) Tournament.parseInfo(data.type, data.parameters), tableDraftDefinitions);
        Stage = Tournament.Stage.parseStage(data.stage);
        Round = data.round;
    }

    //Pulling directly from scheduled database
    public TableDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.ScheduledTournament data, TableDraftDefinitions tableDraftDefinitions) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (TableDraftTournamentParams) Tournament.parseInfo(data.type, data.parameters), tableDraftDefinitions);
    }

    @Override
    public CollectionType generateCollectionInfo() {
        Collection = new CollectionType(Parameters().tournamentId, Parameters().name);
        return Collection;
    }

    public Tournament.Stage postRegistrationStage() {
        if(tableDraftParams.manualKickoff) {
            return Tournament.Stage.AWAITING_KICKOFF;
        }
        else {
            return Tournament.Stage.PLAYING_GAMES;
        }
    }

}

