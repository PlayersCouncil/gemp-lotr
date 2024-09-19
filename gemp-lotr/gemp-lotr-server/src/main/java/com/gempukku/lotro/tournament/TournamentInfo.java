package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.util.JsonUtils;

import java.time.ZonedDateTime;

public class TournamentInfo {

    public Tournament.Stage Stage;
    public int Round;
    public LotroFormat Format;
    protected TournamentParams _params;
    public TournamentPrizes Prizes;
    public PairingMechanism PairingMechanism;
    public CollectionType Collection;

    public String IdPrefix;

    public ZonedDateTime StartTime;

    public TournamentInfo(TournamentPrizes prizes, PairingMechanism pairing, LotroFormat format, TournamentParams params,
            String idPrefix, ZonedDateTime start, Tournament.Stage stage, int round) {
        _params = params;
        Stage = stage;
        Round = round;
        Prizes = prizes;
        PairingMechanism = pairing;
        Format = format;
        IdPrefix = idPrefix;
        StartTime = start;

        generateCollectionInfo();
    }

    //Used by tournament queues to duplicate a template info with fresh parameters
    public TournamentInfo(TournamentInfo info, TournamentParams params) {
        this(info.Prizes, info.PairingMechanism, info.Format, params, info.IdPrefix, DateUtils.ParseDate(params.startTime),
                params.getInitialStage(), 0);
    }

    //Intermediary for consolidating both db constructors
    public TournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, ZonedDateTime start, TournamentParams params) {
        this(Tournament.getTournamentPrizes(productLibrary, params.prizes), tournamentService.getPairingMechanism(params.playoff),
                formatLibrary.getFormat(params.format), params, params.tournamentId, start, params.getInitialStage(), 0);
    }

    //Pulling directly from database
    public TournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.Tournament data) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), Tournament.parseInfo(data.type, data.parameters));
        Stage = Tournament.Stage.parseStage(data.stage);
        Round = data.round;
    }

    //Pulling directly from scheduled database
    public TournamentInfo(TournamentService tournamentService, ProductLibrary productLibrary, LotroFormatLibrary formatLibrary, DBDefs.ScheduledTournament data) {
        this(tournamentService, productLibrary, formatLibrary, data.GetUTCStartDate(), Tournament.parseInfo(data.type, data.parameters));
    }

    public String generateTimestampId() {
        _params.tournamentId = IdPrefix + System.currentTimeMillis();
        generateCollectionInfo();

        return _params.tournamentId;
    }

    public CollectionType generateCollectionInfo() {
        if(_params.customCollection) {
            Collection = new CollectionType(_params.tournamentId, _params.name);
        }
        else {
            Collection = CollectionType.ALL_CARDS;
        }

        return Collection;
    }

    public TournamentParams Parameters() {
        return _params;
    }

    public DBDefs.Tournament ToDB() {
        var tourney = new DBDefs.Tournament();
        tourney.tournament_id = Parameters().tournamentId;
        tourney.name = Parameters().name;
        tourney.start_date = Parameters().startTime;
        tourney.type = Parameters().type.toString();
        tourney.parameters = JsonUtils.Serialize(Parameters());
        tourney.stage = Stage.toString();
        tourney.round = Round;
        return tourney;
    }

    public DBDefs.ScheduledTournament ToScheduledDB() {
        var tourney = new DBDefs.ScheduledTournament();
        tourney.tournament_id = Parameters().tournamentId;
        tourney.name = Parameters().name;
        tourney.format = Parameters().format;
        tourney.start_date = Parameters().startTime;
        tourney.type = Parameters().type.toString();
        tourney.parameters = JsonUtils.Serialize(Parameters());
        tourney.started = false;
        return tourney;
    }

}

