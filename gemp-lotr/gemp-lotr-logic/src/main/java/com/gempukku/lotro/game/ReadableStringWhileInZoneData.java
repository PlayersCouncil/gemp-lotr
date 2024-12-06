package com.gempukku.lotro.game;

public class ReadableStringWhileInZoneData implements PhysicalCard.WhileInZoneData {
    private String value;
    private String humanReadable;

    public ReadableStringWhileInZoneData(String value, String humanReadable) {
        this.value = value;
        this.humanReadable = humanReadable;
    }

    @Override
    public String getHumanReadable() {
        return humanReadable;
    }

    @Override
    public String getValue() {
        return value;
    }
}
