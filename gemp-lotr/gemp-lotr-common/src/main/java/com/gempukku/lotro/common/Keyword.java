package com.gempukku.lotro.common;

public enum Keyword implements Filterable {
    // TODO: not a real keyword - count be refactored out to separate systems
    ROAMING("Roaming", true, false, false, false),
    MOUNTED("Mounted", false, false, false, false),

    // ----------------------------------------- Loaded keywords -----------------------------------------
    // Numeric
    DAMAGE("Damage", true, true, false, true),
    DEFENDER("Defender", true, true, false, true),
    AMBUSH("Ambush", true, true, false, true),
    HUNTER("Hunter", true, true, false, true),
    TOIL("Toil", true, true, false, true),

    // Non-numeric
    AID("Aid", false, false, false, true),
    ARCHER("Archer", true, false, false, true),
    ENDURING("Enduring", true, false, false, true),
    FIERCE("Fierce", true, false, false, true),
    LURKER("Lurker", true, false, false, true),
    MUSTER("Muster", true, false, false, true),
    SANCTUARY("Sanctuary", true, false, true, true),
    RING_BEARER("Ring-Bearer", true, false, true, true),
    UNHASTY("Unhasty", true, false, false, true),

    // Item-class
    SUPPORT_AREA("Support Area", true, false, false, false),

    // ----------------------------------------- Unloaded keywords -----------------------------------------
    BATTLEGROUND("Battleground", true, false, true, true),
    BESIEGER("Besieger", true, false, false, true),
    CORSAIR("Corsair", true, false, false, true),
    DWELLING("Dwelling", true, false, true, true),
    EASTERLING("Easterling", true, false, false, true),
    ENGINE("Engine", true, false, false, true),
    FELLOWSHIP("Fellowship", false, false, false, true),
    FOREST("Forest", true, false, true, true),
    FORTIFICATION("Fortification", true, false, false, true),
    KNIGHT("Knight", true, false, false, true),
    MACHINE("Machine", true, false, false, true),
    MARSH("Marsh", true, false, true, true),
    MOUNTAIN("Mountain", true, false, true, true),
    PLAINS("Plains", true, false, true, true),
    PIPEWEED("Pipeweed", true, false, false, true),
    RANGER("Ranger", true, false, false, true),
    RING_BOUND("Ring-Bound", true, false, false, true),
    RIVER("River", true, false, true, true),
    SEARCH("Search", true, false, false, true),
    SOUTHRON("Southron", true, false, false, true),
    SPELL("Spell", true, false, false, true),
    STEALTH("Stealth", true, false, false, true),
    TALE("Tale", true, false, false, true),
    TENTACLE("Tentacle", true, false, false, true),
    TRACKER("Tracker", true, false, false, true),
    TWILIGHT("Twilight", true, false, false, true),
    UNDERGROUND("Underground", true, false, true, true),
    VALIANT("Valiant", true, false, false, true),
    VILLAGER("Villager", true, false, false, true),
    WEATHER("Weather", true, false, false, true),
    WARG_RIDER("Warg-rider", true, false, false, true),

    // Additional Second Edition keywords
    CUNNING("Cunning", true, false, false, true),
    LOTHLORIEN("Lothlorien", true, false, false, true),
    RIVENDELL("Rivendell", true, false, false, true),
    BREE("Bree", true, false, false, true),
    EDORAS("Edoras", true, false, false, true),
    SHIRE("Shire", true, false, false, true),

	//Additional Hobbit Draft keywords
    WISE("Wise", true, false, false, true),
    BURGLAR("Burglar", true, false, false, true),

    //PC Keywords
    CONCEALED("Concealed", true, false, false, true),
    EXPOSED("Exposed", true, false, false, true);

    private final String humanReadable;
    private final boolean infoDisplayable;
    private final boolean multiples;
    private final boolean terrain;
    private final boolean realKeyword;

    Keyword(String humanReadable, boolean infoDisplayable, boolean multiples, boolean terrain, boolean realKeyword) {
        this.humanReadable = humanReadable;
        this.infoDisplayable = infoDisplayable;
        this.multiples = multiples;
        this.terrain = terrain;
        this.realKeyword = realKeyword;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public String getHumanReadableGeneric() {
        if (multiples)
            return humanReadable + " bonus";
        return humanReadable;
    }

    public boolean isInfoDisplayable() {
        return infoDisplayable;
    }

    public boolean isMultiples() {
        return multiples;
    }

    public boolean isTerrain() {
        return terrain;
    }

    public boolean isRealKeyword() {
        return realKeyword;
    }
}
