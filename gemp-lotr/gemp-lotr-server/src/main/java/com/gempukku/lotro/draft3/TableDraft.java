package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;

import java.util.List;

public interface TableDraft {
    DraftPlayer registerPlayer(String name);
    DraftPlayer getPlayer(String name);
    boolean removePlayer(String name);
    void advanceDraft();
    List<String> getCardsToPickFrom(DraftPlayer draftPlayer);
    String getChosenCard(DraftPlayer draftPlayer);
    void chooseCard(DraftPlayer who, String what);
    boolean isFinished();
    int getSecondsRemainingForPick() throws IllegalStateException;
    CardCollection getPickedCards(DraftPlayer draftPlayer);
    TableStatus getTableStatus();

    class PlayerStatus {
        private String name;
        private boolean hasChosenCard;

        public PlayerStatus(String name, boolean hasChosenCard) {
            this.name = name;
            this.hasChosenCard = hasChosenCard;
        }

        public String getName() {
            return name;
        }

        public boolean hasChosenCard() {
            return hasChosenCard;
        }
    }

    class TableStatus {
        private List<PlayerStatus> playerStatuses;
        private boolean pickOrderAscending;

        public TableStatus(List<PlayerStatus> playerStatuses, boolean pickOrderAscending) {
            this.playerStatuses = playerStatuses;
            this.pickOrderAscending = pickOrderAscending;
        }

        public List<PlayerStatus> getPlayerStatuses() {
            return playerStatuses;
        }

        public boolean isPickOrderAscending() {
            return pickOrderAscending;
        }
    }
}
