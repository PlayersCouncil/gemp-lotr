package com.gempukku.lotro.common;

public enum CardVariant {
    ORIGINAL,
    ART,
    ERRATA,
    LANGUAGE,
    REPRINT;

    public static CardVariant Parse(String value) {
        if(value == null || value.isEmpty())
            return ORIGINAL;

        value = value
                .toLowerCase()
                .replace(" ", "_")
                .replace("-", "_");

        for (CardVariant type : values()) {
            if (type.name().toLowerCase().equals(value))
                return type;
        }
        return ORIGINAL;
    }
}
