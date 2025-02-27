package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.MutableCardCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DraftPlayer {
    protected final TableDraft table;
    private final String name;
    private final List<Booster> boosterQueue = new ArrayList<>();
    protected String chosenCard;
    protected String lastPickedCard;

    public DraftPlayer(TableDraft table, String name) {
        this.table = table;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void startCollection(MutableCardCollection cardCollection) {
        // Remove collection content if it exists
        try {
            CardCollection collection = getCollection();
            if (collection != null) {
                for (CardCollection.Item item : collection.getAll()) {
                    for (int i = 0; i < item.getCount(); i++) {
                        table.getCollectionsManager().sellCardInPlayerCollection(name, table.getCollectionType(), item.getBlueprintId(), 0);
                    }
                }
            }
        } catch (SQLException | IOException ignore) {

        }

        // Create new collection for the player
        var startingCollection = new DefaultCardCollection();

        // Initialize with starting cards
        for (CardCollection.Item item : cardCollection.getAll())
            startingCollection.addItem(item.getBlueprintId(), item.getCount());

        // Save
        table.getCollectionsManager().addPlayerCollection(false, "Draft tournament product", name, table.getCollectionType(), startingCollection);
    }

    public void receiveBooster(Booster booster) {
        boosterQueue.add(booster);
    }

    public List<String> getCardsToPickFrom() {
        if (boosterQueue.isEmpty()) {
            return List.of();
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
        boosterQueue.getFirst().pickCard(chosenCard);
        addToCollection(chosenCard);
        lastPickedCard = chosenCard;
        chosenCard = null;
        // Pass booster to next player
        table.passBooster(this, boosterQueue.removeFirst());

        return true;
    }

    private void addToCollection(String chosenCard) {
        if (this instanceof DraftBot) {
            ((MutableCardCollection) getCollection()).addItem(chosenCard, 1);
        } else {
            DefaultCardCollection pickedCardCollection = new DefaultCardCollection();
            pickedCardCollection.addItem(chosenCard, 1);
            table.getCollectionsManager().addItemsToPlayerCollection(false, "Draft pick", name, table.getCollectionType(), pickedCardCollection.getAll());
        }
    }

    public String getLastPickedCard() {
        return lastPickedCard;
    }

    public CardCollection getCollection() {
        return table.getCollectionsManager().getPlayerCollection(name, table.getCollectionType().getCode());
    }

    public String getChosenCard() {
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
