package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.MutableCardCollection;

public interface StartingCollectionProducer {
    MutableCardCollection getStartingCardCollection(String uniqueEventName, String playerName);
}
