package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;
import com.gempukku.util.JsonUtils;

import java.util.List;

public interface Tournament {
    enum Stage {
        STARTING("Starting"),
        DRAFT("Drafting"),
        DECK_BUILDING("Deck-building"),
        DECK_REGISTRATION("Registering Decks"),
        AWAITING_KICKOFF("Awaiting Kickoff"),
        PREPARING("Preparing"),
        PLAYING_GAMES("Playing Games"),
        PAUSED("Paused Between Rounds"),
        FINISHED("Finished");

        private final String _humanReadable;

        Stage(String humanReadable) {
            _humanReadable = humanReadable;
        }

        public String getHumanReadable() {
            return _humanReadable;
        }

        public static Stage parseStage(String name) {
            String nameCaps = name.toUpperCase().replace(' ', '_').replace('-', '_');
            String nameLower = name.toLowerCase();

            for (Stage stage : values()) {
                if (stage.getHumanReadable().toLowerCase().equals(nameLower)
                        || stage.toString().equals(nameCaps))
                    return stage;
            }
            return null;
        }
    }

    enum TournamentType {
        CONSTRUCTED,
        SEALED,
        SOLODRAFT,
        TABLE_SOLODRAFT;

        public static TournamentType parse(String name) {
            String nameCaps = name.toUpperCase().trim().replace(' ', '_').replace('-', '_');

            for (TournamentType type : values()) {
                if (type.toString().equals(nameCaps))
                    return type;
            }
            return null;
        }
    }

    enum PairingType {
        SINGLE_ELIMINATION,
        SWISS,
        SWISS_3,
        WC_SWISS,
        TEST;

        public static PairingType parse(String name) {
            String nameCaps = name.toUpperCase().trim().replace(' ', '_').replace('-', '_');

            for (PairingType type : values()) {
                if (type.toString().equals(nameCaps))
                    return type;
            }
            return null;
        }
    }

    enum PrizeType {
        NONE,
        DAILY,
        ON_DEMAND;

        public static PrizeType parse(String name) {
            String nameCaps = name.toUpperCase().trim().replace(' ', '_').replace('-', '_');

            for (PrizeType type : values()) {
                if (type.toString().equals(nameCaps))
                    return type;
            }
            return PrizeType.NONE;
        }
    }

    static TournamentPrizes getTournamentPrizes(ProductLibrary productLibrary, PrizeType prize) {

        switch (prize) {
            case DAILY -> {
                return new DailyTournamentPrizes(prize.name(), productLibrary);
            }
            case ON_DEMAND -> {
                //Currently busted, reverting to Daily for now
                return new DailyTournamentPrizes(prize.name(), productLibrary);
            }
        }

        return new NoPrizes();
    }

    static TournamentPrizes getTournamentPrizes(ProductLibrary productLibrary, String prizesScheme) {
        return getTournamentPrizes(productLibrary, PrizeType.parse(prizesScheme));
    }

    static PairingMechanism getPairingMechanism(PairingType type) {
        if(type == null)
            return null;

        switch (type) {
            case SINGLE_ELIMINATION -> {
                return new SingleEliminationPairing(PairingType.SINGLE_ELIMINATION.name());
            }
            case SWISS -> {
                return new SwissPairingMechanism(PairingType.SWISS.name());
            }
            case SWISS_3 -> {
                return new SwissPairingMechanism(PairingType.SWISS_3.name(), 3);
            }
            case WC_SWISS -> {
                return new ChampionshipSwissPairingMechanism(PairingType.WC_SWISS.name(), 10);
            }
        }

        return null;
    }

    static PairingMechanism getPairingMechanism(String pairingType) {
        return getPairingMechanism(PairingType.parse(pairingType));
    }

    static TournamentParams parseInfo(TournamentType type, String unparsed) {
        switch (type) {
            case CONSTRUCTED -> {
                return JsonUtils.Convert(unparsed, TournamentParams.class);
            }
            case SEALED -> {
                return JsonUtils.Convert(unparsed, SealedTournamentParams.class);
            }
            case SOLODRAFT -> {
                return JsonUtils.Convert(unparsed, SoloDraftTournamentParams.class);
            }
            case TABLE_SOLODRAFT -> {
                return JsonUtils.Convert(unparsed, SoloTableDraftTournamentParams.class);
            }
        }

        return null;
    }

    static TournamentParams parseInfo(String type, String unparsed) {
        return parseInfo(TournamentType.parse(type), unparsed);
    }

    String getTournamentId();
    String getFormatCode();
    CollectionType getCollectionType();
    String getTournamentName();
    String getPlayOffSystem();

    void RefreshTournamentInfo();

    Stage getTournamentStage();
    int getCurrentRound();
    int getPlayersInCompetitionCount();
    String getPlayerList();

    List<TournamentProcessAction> advanceTournament(CollectionsManager collectionsManager);

    void reportGameFinished(String winner, String loser);

    void playerChosenCard(String playerName, String cardId);
    boolean playerSubmittedDeck(String player, LotroDeck deck);
    void issuePlayerMaterial(String player);
    LotroDeck getPlayerDeck(String player);
    String dropPlayer(String player);

    Draft getDraft();

    List<PlayerStanding> getCurrentStandings();

    boolean isPlayerInCompetition(String player);
    boolean isPlayerAbandoned(String player);

    String produceReport(DeckRenderer renderer) throws CardNotFoundException;

    TournamentInfo getInfo();
}
