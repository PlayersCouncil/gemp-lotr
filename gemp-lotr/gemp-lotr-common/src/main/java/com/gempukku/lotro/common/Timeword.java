package com.gempukku.lotro.common;

public enum Timeword implements Filterable {
    FELLOWSHIP("Fellowship", Phase.FELLOWSHIP),
    SHADOW("Shadow", Phase.SHADOW),
    MANEUVER("Maneuver", Phase.MANEUVER),
    ARCHERY("Archery", Phase.ARCHERY),
    ASSIGNMENT("Assignment", Phase.ASSIGNMENT),
    SKIRMISH("Skirmish", Phase.SKIRMISH),
    REGROUP("Regroup", Phase.REGROUP),
    RESPONSE("Response", null);

    private final String humanReadable;
    private final Phase phase;

    Timeword(String humanReadable, Phase phase) {
        this.humanReadable = humanReadable;
        this.phase = phase;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public Phase getPhase() {
        return phase;
    }

    public static Timeword findByPhase(Phase phase) {
        if (phase == null)
            return null;
        for (Timeword value : values()) {
            if (value.getPhase() == phase)
                return value;
        }
        return null;
    }

    public static Timeword findTimeword(String name) {
        String nameCaps = name.toUpperCase().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase();
        for (Timeword timeword : values()) {
            if (timeword.getHumanReadable().toLowerCase().equals(nameLower) || timeword.toString().equals(nameCaps))
                return timeword;
        }
        return null;
    }
}
