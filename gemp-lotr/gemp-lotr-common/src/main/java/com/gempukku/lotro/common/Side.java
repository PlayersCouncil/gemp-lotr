package com.gempukku.lotro.common;

public enum Side implements Filterable {
    FREE_PEOPLE, SHADOW, NONE;

    public static Side Parse(String value) {
        if(value == null)
            return null;

        value = value
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");

        if(value.contains("shadow"))
            return SHADOW;
        if(value.contains("freeps") || value.contains("free") || value.contains("people"))
            return FREE_PEOPLE;
        if(value.contains("none"))
            return NONE;

        return null;
    }
}
