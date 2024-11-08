package com.gempukku.lotro.common;

public enum OverlayType {
    NONE,
    LEGACY_FOIL;

    public static OverlayType Parse(String value) {
        if(value == null || value.isEmpty())
            return NONE;

        if(value.contains("*"))
            return LEGACY_FOIL;

        return NONE;
    }
}
