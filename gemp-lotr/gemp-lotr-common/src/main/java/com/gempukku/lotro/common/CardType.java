package com.gempukku.lotro.common;

public enum CardType implements Filterable {
    THE_ONE_RING,
    SITE,
    //Characters
    COMPANION, ALLY, MINION,
    //Items
    POSSESSION, ARTIFACT,
    EVENT,
    CONDITION,
    FOLLOWER,
    //Unused
    ADVENTURE,

    //Player's Council card types
    MAP
}
