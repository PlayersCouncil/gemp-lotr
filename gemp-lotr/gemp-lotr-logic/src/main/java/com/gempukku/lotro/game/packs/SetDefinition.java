package com.gempukku.lotro.game.packs;

import java.util.List;
import java.util.Set;

public interface SetDefinition {
    String getSetName();

    String getSetId();

    boolean IsDecipherSet();
    boolean Merchantable();
    boolean NeedsLoading();
    List<String> getCardsOfRarity(String rarity);

    List<String> getTengwarCards();

    String getCardRarity(String cardId);

    Set<String> getAllCards();
}
