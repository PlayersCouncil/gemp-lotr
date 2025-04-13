package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft2.SoloDraft;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;

import java.time.Duration;
import java.time.ZonedDateTime;

public class SoloDraftTournamentInfo extends TournamentInfo {

    public final SoloDraft SoloDraft;
    public final ZonedDateTime DeckbuildingDeadline;
    public final Duration DeckbuildingDuration;
    public final ZonedDateTime RegistrationDeadline;
    public final Duration RegistrationDuration;
    protected SoloDraftTournamentParams _soloDraftParams;

    public SoloDraftTournamentInfo(SoloDraft soloDraft, TournamentPrizes prizes, PairingMechanism pairing, LotroFormat format, SoloDraftTournamentParams params,
                                   String idPrefix, ZonedDateTime start, Tournament.Stage stage, int round) {
        super(prizes, pairing, format, params, idPrefix, start, stage, round);
        SoloDraft = soloDraft;
        _soloDraftParams = params;
        _params = params;

        DeckbuildingDuration = Duration.ofMinutes(_soloDraftParams.deckbuildingDuration);
        RegistrationDuration = Duration.ofMinutes(_soloDraftParams.turnInDuration);

        DeckbuildingDeadline = StartTime.plus(DeckbuildingDuration);
        RegistrationDeadline = DeckbuildingDeadline.plus(RegistrationDuration);
    }

    //Used by tournament queues to duplicate a template info with fresh parameters
    public SoloDraftTournamentInfo(SoloDraftTournamentInfo info, SoloDraftTournamentParams params) {
        this(info.SoloDraft, info.Prizes, info.PairingMechanism, info.Format, params, info.IdPrefix, DateUtils.ParseDate(params.startTime),
                params.getInitialStage(), 0);
    }

    //Intermediary for consolidating both db constructors
    public SoloDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, ZonedDateTime start, SoloDraftTournamentParams params, SoloDraftDefinitions soloDraftDefinitions) {
        this(soloDraftDefinitions.getSoloDraft(params.soloDraftFormatCode), Tournament.getTournamentPrizes(productLibrary, params.prizes), tournamentService.getPairingMechanism(params.playoff),
                formatLibrary.getFormat(params.format), params, params.tournamentId, start, params.getInitialStage(), 0);
    }

    //Pulling directly from database
    public SoloDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.Tournament data, SoloDraftDefinitions soloDraftDefinitions) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (SoloDraftTournamentParams) Tournament.parseInfo(data.type, data.parameters), soloDraftDefinitions);
        Stage = Tournament.Stage.parseStage(data.stage);
        Round = data.round;
    }

    //Pulling directly from scheduled database
    public SoloDraftTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.ScheduledTournament data, SoloDraftDefinitions soloDraftDefinitions) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (SoloDraftTournamentParams) Tournament.parseInfo(data.type, data.parameters), soloDraftDefinitions);
    }

    @Override
    public CollectionType generateCollectionInfo() {
        Collection = new CollectionType(Parameters().tournamentId, Parameters().name);
        return Collection;
    }

    public Tournament.Stage PostRegistrationStage() {
        if(_soloDraftParams.manualKickoff) {
            return Tournament.Stage.AWAITING_KICKOFF;
        }
        else {
            return Tournament.Stage.PLAYING_GAMES;
        }
    }

}

