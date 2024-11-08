package com.gempukku.lotro.common;

public enum CardStyle {
    STANDARD("_", "Standard"),
    FULL_ART("U", "Full Art"),
    MASTERWORK("O", "Masterwork"),
    HOLIDAY("H", "Holiday"),
    CHAMPIONSHIP("C", "Championship"),
    ALTERNATE_IMAGE("A", "Alternate Image");

    private final String code;
    private final String humanReadable;

    CardStyle(String code, String humanReadable) {
        this.code = code;
        this.humanReadable = humanReadable;
    }

    public String getCode() { return code; }
    public String getHumanReadable() { return humanReadable; }

    public static CardStyle Parse(String value) {
        if(value == null || value.isEmpty())
            return STANDARD;

        value = value
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        for (CardStyle style : values()) {
            if (style.name().equals(value) || style.code.equals(value))
                return style;
        }
        return STANDARD;
    }
}
