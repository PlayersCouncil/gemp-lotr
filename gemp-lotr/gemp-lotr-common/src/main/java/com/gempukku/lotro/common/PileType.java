package com.gempukku.lotro.common;


/**
 * Represents the different kinds of deck piles that the player comes to
 * the table with.
 */
public enum PileType implements Filterable {
    FREE_PEOPLES, SHADOW, ADVENTURE, RING_BEARER, RING, MAP;

    public static PileType Parse(String value) {
        value = value
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        if(value.contains("FREEPS") || value.contains("FREE") || value.contains("PEOPLE"))
            return FREE_PEOPLES;

        if(value.contains("SITE") || value.contains("ADVENTURE"))
            return ADVENTURE;

        for (PileType type : values()) {
            if (type.toString().equalsIgnoreCase(value))
                return type;
        }

        return null;
    }
}
