package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.DefaultCardCollection;
import com.gempukku.lotro.game.MutableCardCollection;

import java.util.List;
import java.util.Random;

public class DraftBot extends DraftPlayer {
    MutableCardCollection cardCollection = new DefaultCardCollection();

    public DraftBot(TableDraft table, String name) {
        super(table, name);
    }

    @Override
    public void startCollection(MutableCardCollection cardCollection) {
        this.cardCollection = cardCollection;
    }

    @Override
    public CardCollection getCollection() {
        return cardCollection;
    }

    @Override
    public void receiveBooster(Booster booster) {
        super.receiveBooster(booster);
        // Signal that we are always ready to pick
        chooseCard(null);
    }

    @Override
    public boolean chooseCard(String dummy) {
        // Signal that we are ready to pick
        return table.readyToPick(this);
    }

    @Override
    public boolean pickChosenCardAndPassBooster() {
        // Time to pick, let's choose a card
        chosenCard = pickRandom(getCardsToPickFrom());
        return super.pickChosenCardAndPassBooster();
    }

    private String pickRandom(List<String> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(source.size());
        return source.get(randomIndex);
    }
}
