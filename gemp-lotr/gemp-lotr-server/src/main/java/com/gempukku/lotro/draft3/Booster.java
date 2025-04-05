package com.gempukku.lotro.draft3;

import java.util.ArrayList;
import java.util.List;

public class Booster {
    private List<String> cardsInPack;

    public Booster(List<String> cards) {
        this.cardsInPack = cards;
    }

    public List<String> getCardsInPack() {
        return new ArrayList<>(cardsInPack);
    }

    public boolean isEmpty() {
        return cardsInPack.isEmpty();
    }

    public String pickCard(String toPick) {
        return cardsInPack.remove(toPick) ? toPick : null;
    }
}
