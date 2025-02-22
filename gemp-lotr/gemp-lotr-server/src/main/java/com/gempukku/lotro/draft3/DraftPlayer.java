package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.MutableCardCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DraftPlayer {
    protected final TableDraft table;
    private final String name;
    private MutableCardCollection cardCollection = new DefaultCardCollection();
    private final List<Booster> boosterQueue = new ArrayList<>();
    protected String chosenCard;

    public DraftPlayer(TableDraft table, String name) {
        this.table = table;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCardCollection(MutableCardCollection cardCollection) {
        this.cardCollection = cardCollection;
    }

    public CardCollection getCardCollection() {
        return cardCollection;
    }

    public void receiveBooster(Booster booster) {
        boosterQueue.add(booster);
    }

    public List<String> getCardsToPickFrom() {
        if (boosterQueue.isEmpty()) {
            return null;
        }
        return boosterQueue.getFirst().getCardsInPack();
    }

    public boolean chooseCard(String cardId) {
        if (boosterQueue.isEmpty()) {
            return false;
        }

        // Check if the card is in the active booster
        if (!getCardsToPickFrom().contains(cardId)) {
            return false;
        }
        chosenCard = cardId;
        // Signal that we are ready to pick
        return table.readyToPick(this);
    }

    public boolean pickChosenCardAndPassBooster() {
        // Check if we have card hovered and the card is in the pack
        if (chosenCard == null) {
            return false;
        }
        if (!getCardsToPickFrom().contains(chosenCard)) {
            return false;
        }
        // Remove the card from booster and add the card to collection
        cardCollection.addItem(boosterQueue.getFirst().pickCard(chosenCard), 1);
        // Pass booster to next player
        table.passBooster(this, boosterQueue.removeFirst());

        return true;
    }

    public String getLastPickedCard() {
        return chosenCard;
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
