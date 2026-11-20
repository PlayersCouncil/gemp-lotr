package com.gempukku.lotro.scenario;

/**
 * The two chairs at a scenario's table.  The Free Peoples seat takes the first turn.
 */
public enum Seat {
    FREEPS, SHADOW;

    public Seat other() {
        return this == FREEPS ? SHADOW : FREEPS;
    }

    public String key() {
        return name().toLowerCase();
    }
}
