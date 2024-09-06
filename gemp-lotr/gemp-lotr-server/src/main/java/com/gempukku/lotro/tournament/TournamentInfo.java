package com.gempukku.lotro.tournament;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.packs.ProductLibrary;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public record TournamentInfo(String tournamentId, String draftType, String name, String format,
                             ZonedDateTime startDate, CollectionType collectionType,
                             Tournament.Stage tournamentStage, int round, boolean manualKickoff,
                             PairingMechanism pairingMechanism, TournamentPrizes prizesScheme
) {

    public TournamentInfo(ProductLibrary library, DBDefs.Tournament info) {
        this(info.tournament_id, info.draft_type, info.name, info.format, info.GetUTCStartDate(),
                CollectionType.parseCollectionCode(info.collection), Tournament.Stage.parseStage(info.stage),
                info.round, info.manual_kickoff, Tournament.getPairingMechanism(info.pairing),
                Tournament.getTournamentPrizes(library, info.prizes));
    }

    public TournamentInfo(ProductLibrary library, DBDefs.ScheduledTournament info) {
        this(info.tournament_id, null, info.name, info.format, info.GetUTCStartDate(),
                CollectionType.ALL_CARDS, info.manual_kickoff ? Tournament.Stage.AWAITING_KICKOFF : Tournament.Stage.PLAYING_GAMES,
                0, info.manual_kickoff, Tournament.getPairingMechanism(info.playoff),
                Tournament.getTournamentPrizes(library, info.prizes));
    }

    public DBDefs.Tournament ToDB() {
        var tourney = new DBDefs.Tournament();
        tourney.tournament_id = this.tournamentId;
        tourney.start_date = this.startDate.toLocalDateTime();
        tourney.draft_type = this.draftType;
        tourney.name = this.name;
        tourney.format = this.format;
        tourney.collection = this.collectionType.getCode();
        tourney.stage = this.tournamentStage.getHumanReadable();
        tourney.round = this.round;
        tourney.manual_kickoff = this.manualKickoff;
        tourney.pairing = this.pairingMechanism.getRegistryRepresentation();
        tourney.prizes = this.prizesScheme.getRegistryRepresentation();

        return tourney;
    }

    public static List<TournamentInfo> fromDB(ProductLibrary library, List<DBDefs.Tournament> dbinfos) {
        List<TournamentInfo> infos = new ArrayList<>();
        for (var result : dbinfos) {
            infos.add(new TournamentInfo(library, result));
        }
        return infos;
    }

    public static List<TournamentInfo> fromSDB(ProductLibrary library, List<DBDefs.ScheduledTournament> dbinfos) {
        List<TournamentInfo> infos = new ArrayList<>();
        for (var result : dbinfos) {
            infos.add(new TournamentInfo(library, result));
        }
        return infos;
    }
}

