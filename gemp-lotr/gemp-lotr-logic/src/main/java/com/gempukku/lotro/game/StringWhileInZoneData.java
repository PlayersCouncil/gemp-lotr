package com.gempukku.lotro.game;

public class StringWhileInZoneData implements PhysicalCard.WhileInZoneData {
    private String value;

    public StringWhileInZoneData(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getHumanReadable() {
        return value;
    }
}
