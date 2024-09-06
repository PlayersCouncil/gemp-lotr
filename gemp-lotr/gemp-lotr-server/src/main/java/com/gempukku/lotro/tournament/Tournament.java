package com.gempukku.lotro.tournament;

import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.DeckRenderer;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.draft.Draft;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.SortAndFilterCards;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.tournament.action.TournamentProcessAction;

import java.util.List;

public interface Tournament {
    enum Stage {
        DRAFT("Drafting"),
        DECK_BUILDING("Deck building"),
        AWAITING_KICKOFF("Awaiting kickoff"),
        PAUSED("Paused"),
        PREPARING("Preparing"),
        PLAYING_GAMES("Playing games"),
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

    static TournamentPrizes getTournamentPrizes(ProductLibrary productLibrary, String prizesScheme) {
        if (prizesScheme == null || prizesScheme.equals("none"))
            return new NoPrizes();

        return new DailyTournamentPrizes(prizesScheme, productLibrary);
    }

    static PairingMechanism getPairingMechanism(String pairingType) {
        pairingType = pairingType.toLowerCase().trim();
        if (pairingType.equals("singleelimination"))
            return new SingleEliminationPairing("singleelimination");
        if (pairingType.equals("swiss"))
            return new SwissPairingMechanism("swiss");
        if (pairingType.equals("swiss-3"))
            return new SwissPairingMechanism("swiss-3", 3);
        if (pairingType.equals("wc-swiss"))
            return new ChampionshipSwissPairingMechanism("wc-swiss", 8);

        return null;
    }

    String getTournamentId();
    String getFormat();
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
    void playerSubmittedDeck(String player, LotroDeck deck);
    LotroDeck getPlayerDeck(String player);
    boolean dropPlayer(String player);

    Draft getDraft();

    List<PlayerStanding> getCurrentStandings();

    boolean isPlayerInCompetition(String player);

    String produceReport(DeckRenderer renderer) throws CardNotFoundException;
}
