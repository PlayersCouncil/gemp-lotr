package com.gempukku.lotro.draft3;

import java.util.List;
import java.util.Random;

public class DraftBot extends DraftPlayer {
    public DraftBot(TableDraft table, String name) {
        super(table, name);
    }

    @Override
    public void receiveBooster(Booster booster) {
        super.receiveBooster(booster);
        // Signal that we are always ready to pick
        chooseCard(pickRandom(getCardsToPickFrom()));
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
