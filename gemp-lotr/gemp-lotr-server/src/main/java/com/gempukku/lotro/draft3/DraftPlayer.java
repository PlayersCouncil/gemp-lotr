package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;

import java.util.List;
import java.util.Objects;

public class DraftPlayer {
    protected final TableDraft table;
    private final String name;

    public DraftPlayer(TableDraft table, String name) {
        this.table = table;
        this.name = name;
    }

    public boolean isBot() {
        return false;
    }

    public String getName() {
        return name;
    }

    public List<String> getCardsToPickFrom() {
        return table.getCardsToPickFrom(this);
    }

    public int getSecondsRemainingForPick() throws IllegalStateException {
        return table.getSecondsRemainingForPick();
    }

    public void chooseCard(String cardId) {
        table.chooseCard(this, cardId);
    }

    public CardCollection getCollection() {
        return table.getPickedCards(this);
    }

    public String getChosenCard() {
        return table.getChosenCard(this);
    }

    public TableDraft.TableStatus getTableStatus() {
        return table.getTableStatus();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DraftPlayer that = (DraftPlayer) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
