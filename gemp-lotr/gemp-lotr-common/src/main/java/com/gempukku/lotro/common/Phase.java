package com.gempukku.lotro.common;

public enum Phase {
    PUT_RING_BEARER("Put Ring-bearer", false, false),
    PLAY_STARTING_FELLOWSHIP("Play starting fellowship", false, true),
    FELLOWSHIP("Fellowship", true, true),
    SHADOW("Shadow", true, true),
    MANEUVER("Maneuver", true, true),
    ARCHERY("Archery", true, true),
    ASSIGNMENT("Assignment", true, true),
    SKIRMISH("Skirmish", true, true),
    REGROUP("Regroup", true, true),
    BETWEEN_TURNS("Between turns", false, false);

    private String humanReadable;
    private boolean realPhase;
    private boolean cardsAffectGame;

    Phase(String humanReadable, boolean realPhase, boolean cardsAffectGame) {
        this.humanReadable = humanReadable;
        this.realPhase = realPhase;
        this.cardsAffectGame = cardsAffectGame;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public boolean isRealPhase() {
        return realPhase;
    }

    public boolean isCardsAffectGame() {
        return cardsAffectGame;
    }
}
