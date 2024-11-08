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
    MAP;


    public static CardType Parse(String value) {
        value = value
                .toLowerCase()
                .replace(" ", "_")
                .replace("-", "_");

        for (CardType type : values()) {
            if (type.name().toLowerCase().equals(value))
                return type;
        }
        return null;
    }
}
