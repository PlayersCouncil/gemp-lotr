package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.SealedEventDefinition;
import com.gempukku.lotro.packs.ProductLibrary;

import java.time.Duration;
import java.time.ZonedDateTime;

public class SealedTournamentInfo extends TournamentInfo {

    public final SealedEventDefinition SealedDefinition;
    public final ZonedDateTime DeckbuildingDeadline;
    public final Duration DeckbuildingDuration;
    public final ZonedDateTime RegistrationDeadline;
    public final Duration RegistrationDuration;
    protected SealedTournamentParams _sealedParams;

    public SealedTournamentInfo(SealedEventDefinition sealedDef, TournamentPrizes prizes, PairingMechanism pairing, LotroFormat format, SealedTournamentParams params,
            String idPrefix, ZonedDateTime start, Tournament.Stage stage, int round) {
        super(prizes, pairing, format, params, idPrefix, start, stage, round);
        SealedDefinition = sealedDef;
        _sealedParams = params;
        _params = params;

        DeckbuildingDuration = Duration.ofMinutes(_sealedParams.deckbuildingDuration);
        RegistrationDuration = Duration.ofMinutes(_sealedParams.turnInDuration);

        DeckbuildingDeadline = StartTime.plus(DeckbuildingDuration);
        RegistrationDeadline = DeckbuildingDeadline.plus(RegistrationDuration);
    }

    //Used by tournament queues to duplicate a template info with fresh parameters
    public SealedTournamentInfo(SealedTournamentInfo info, SealedTournamentParams params) {
        this(info.SealedDefinition, info.Prizes, info.PairingMechanism, info.Format, params, info.IdPrefix, DateUtils.ParseDate(params.startTime),
                params.getInitialStage(), 0);
    }

    //Intermediary for consolidating both db constructors
    public SealedTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, ZonedDateTime start, SealedTournamentParams params) {
        this(formatLibrary.GetSealedTemplate(params.sealedFormatCode), Tournament.getTournamentPrizes(productLibrary, params.prizes), tournamentService.getPairingMechanism(params.playoff),
                formatLibrary.getFormat(params.format), params, params.tournamentId, start, params.getInitialStage(), 0);
    }

    //Pulling directly from database
    public SealedTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.Tournament data) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (SealedTournamentParams) Tournament.parseInfo(data.type, data.parameters));
        Stage = Tournament.Stage.parseStage(data.stage);
        Round = data.round;
    }

    //Pulling directly from scheduled database
    public SealedTournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.ScheduledTournament data) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), (SealedTournamentParams) Tournament.parseInfo(data.type, data.parameters));
    }

    @Override
    public CollectionType generateCollectionInfo() {
        Collection = new CollectionType(Parameters().tournamentId, Parameters().name);
        return Collection;
    }

    public Tournament.Stage PostRegistrationStage() {
        if(_sealedParams.manualKickoff) {
            return Tournament.Stage.AWAITING_KICKOFF;
        }
        else {
            return Tournament.Stage.PLAYING_GAMES;
        }
    }

}

