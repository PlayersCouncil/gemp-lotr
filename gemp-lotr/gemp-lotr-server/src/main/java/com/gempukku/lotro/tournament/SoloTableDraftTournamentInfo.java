package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.draft3.TableDraftDefinition;
import com.gempukku.lotro.draft3.TableDraftDefinitions;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;

import java.time.Duration;
import java.time.ZonedDateTime;

public class SoloTableDraftTournamentInfo extends TournamentInfo {

    public final TableDraftDefinition tableDraftDefinition;
    public final ZonedDateTime deckbuildingDeadline;
    public final Duration deckbuildingDuration;
    public final ZonedDateTime registrationDeadline;
    public final Duration registrationDuration;
    protected SoloTableDraftTournamentParams soloTableDraftParams;

    public SoloTableDraftTournamentInfo(TableDraftDefinition tableDraftDefinition, TournamentPrizes prizes, PairingMechanism pairing, LotroFormat format, SoloTableDraftTournamentParams params,
                                        String idPrefix, ZonedDateTime start, Tournament.Stage stage, int round) {
        super(prizes, pairing, format, params, idPrefix, start, stage, round);
        this.tableDraftDefinition = tableDraftDefinition;
        this.soloTableDraftParams = params;
        _params = params;

        deckbuildingDuration = Duration.ofMinutes(soloTableDraftParams.deckbuildingDuration);
        registrationDuration = Duration.ofMinutes(soloTableDraftParams.turnInDuration);

        deckbuildingDeadline = StartTime.plus(deckbuildingDuration);
        registrationDeadline = deckbuildingDeadline.plus(registrationDuration);
    }

    //Used by tournament queues to duplicate a template info with fresh parameters
    public SoloTableDraftTournamentInfo(SoloTableDraftTournamentInfo info, SoloTableDraftTournamentParams params) {
        this(info.tableDraftDefinition, info.Prizes, info.PairingMechanism, info.Format, params, info.IdPrefix, DateUtils.ParseDate(params.startTime),
                params.getInitialStage(), 0);
    }

    //Intermediary for consolidating both db constructors
    public SoloTableDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, ZonedDateTime start, SoloTableDraftTournamentParams params, TableDraftDefinitions tableDraftDefinitions) {
        this(tableDraftDefinitions.getTableDraftDefinition(params.soloTableDraftFormatCode), Tournament.getTournamentPrizes(productLibrary, params.prizes), tournamentService.getPairingMechanism(params.playoff),
                formatLibrary.getFormat(params.format), params, params.tournamentId, start, params.getInitialStage(), 0);
    }

    //Pulling directly from database
    public SoloTableDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.Tournament data, TableDraftDefinitions tableDraftDefinitions) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (SoloTableDraftTournamentParams) Tournament.parseInfo(data.type, data.parameters), tableDraftDefinitions);
        Stage = Tournament.Stage.parseStage(data.stage);
        Round = data.round;
    }

    //Pulling directly from scheduled database
    public SoloTableDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.ScheduledTournament data, TableDraftDefinitions tableDraftDefinitions) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (SoloTableDraftTournamentParams) Tournament.parseInfo(data.type, data.parameters), tableDraftDefinitions);
    }

    @Override
    public CollectionType generateCollectionInfo() {
        Collection = new CollectionType(Parameters().tournamentId, Parameters().name);
        return Collection;
    }

    public Tournament.Stage postRegistrationStage() {
        if(soloTableDraftParams.manualKickoff) {
            return Tournament.Stage.AWAITING_KICKOFF;
        }
        else {
            return Tournament.Stage.PLAYING_GAMES;
        }
    }

}

