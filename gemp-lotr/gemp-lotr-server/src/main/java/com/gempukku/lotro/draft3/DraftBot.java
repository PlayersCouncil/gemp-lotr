package com.gempukku.lotro.draft3;

import java.util.List;
import java.util.Random;

public class DraftBot extends DraftPlayer {

    public DraftBot(TableDraft table, String name) {
        super(table, name);
    }

    @Override
    public boolean isBot() {
        return true;
    }

    public void chooseCard(List<String> cardsToPickFrom) {
        table.chooseCard(this, chooseRandom(cardsToPickFrom));
    }

    private String chooseRandom(List<String> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        int randomIndex = rand.nextInt(source.size());
        return source.get(randomIndex);
    }
}
