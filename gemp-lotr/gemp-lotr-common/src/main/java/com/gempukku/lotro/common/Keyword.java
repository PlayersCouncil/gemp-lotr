package com.gempukku.lotro.common;

public enum Keyword implements Filterable {
    // TODO: not a real keyword - count be refactored out to separate systems
    ROAMING("Roaming", true, false, false, false, true),
    MOUNTED("Mounted", false, false, false, false, false),
    HINDERED("Hindered", true, false, false, false, true),

    // ----------------------------------------- Loaded keywords -----------------------------------------
    // Numeric
    DAMAGE("Damage", true, true, false, true, true),
    DEFENDER("Defender", true, true, false, true, true),
    AMBUSH("Ambush", true, true, false, true, true),
    HUNTER("Hunter", true, true, false, true, true),
    TOIL("Toil", true, true, false, true, true),

    // Non-numeric
    AID("Aid", false, false, false, true, true),
    ARCHER("Archer", true, false, false, true, true),
    ENDURING("Enduring", true, false, false, true, true),
    FIERCE("Fierce", true, false, false, true, true),
    LURKER("Lurker", true, false, false, true, true),
    MUSTER("Muster", true, false, false, true, true),
    /**
     * As per Modern Ruling #1, Sanctuary is not considered a terrain keyword.
     * https://wiki.lotrtcgpc.net/wiki/Modern_Ruling_1
      */
    SANCTUARY("Sanctuary", true, false, false, true, true),
    RING_BEARER("Ring-Bearer", true, false, true, true, true),
    UNHASTY("Unhasty", true, false, false, true, true),

    // Item-class
    SUPPORT_AREA("Support Area", true, false, false, false, true),

    // ----------------------------------------- Unloaded keywords -----------------------------------------
    BATTLEGROUND("Battleground", true, false, true, true, false),
    BESIEGER("Besieger", true, false, false, true, false),
    BEACON("Beacon", true, false, false, true, false),
    CORSAIR("Corsair", true, false, false, true, false),
    DWELLING("Dwelling", true, false, true, true, false),
    EASTERLING("Easterling", true, false, false, true, false),
    ENGINE("Engine", true, false, false, true, false),
    FELLOWSHIP("Fellowship", false, false, false, true, false),
    FOREST("Forest", true, false, true, true, false),
    FORTIFICATION("Fortification", true, false, false, true, false),
    KNIGHT("Knight", true, false, false, true, false),
    MACHINE("Machine", true, false, false, true, false),
    MARSH("Marsh", true, false, true, true, false),
    MOUNTAIN("Mountain", true, false, true, true, false),
    PLAINS("Plains", true, false, true, true, false),
    PIPEWEED("Pipeweed", true, false, false, true, false),
    RANGER("Ranger", true, false, false, true, false),
    RING_BOUND("Ring-Bound", true, false, false, true, false),
    RIVER("River", true, false, true, true, false),
    SEARCH("Search", true, false, false, true, false),
    SOUTHRON("Southron", true, false, false, true, false),
    SPELL("Spell", true, false, false, true, false),
    STEALTH("Stealth", true, false, false, true, false),
    TALE("Tale", true, false, false, true, false),
    TENTACLE("Tentacle", true, false, false, true, false),
    TRACKER("Tracker", true, false, false, true, false),
    TWILIGHT("Twilight", true, false, false, true, false),
    UNDERGROUND("Underground", true, false, true, true, false),
    VALIANT("Valiant", true, false, false, true, false),
    VILLAGER("Villager", true, false, false, true, false),
    WEATHER("Weather", true, false, false, true, false),
    WARG_RIDER("Warg-rider", true, false, false, true, false),

    // Additional Second Edition keywords
    CUNNING("Cunning", true, false, false, true, true),
//    LOTHLORIEN("Lothlorien", true, false, false, true),
//    RIVENDELL("Rivendell", true, false, false, true),
//    BREE("Bree", true, false, false, true),
//    EDORAS("Edoras", true, false, false, true),
//    SHIRE("Shire", true, false, false, true),

	//Additional Hobbit Draft keywords
    WISE("Wise", true, false, false, true, true),
    BURGLAR("Burglar", true, false, false, true, true),

    //PC Keywords
    CONCEALED("Concealed", true, false, false, true, true),
    EXPOSED("Exposed", true, false, false, true, true),
    RELENTLESS("Relentless", true, false, false, true, true);

    private final String humanReadable;
    private final boolean infoDisplayable;
    private final boolean multiples;
    private final boolean terrain;
    private final boolean realKeyword;
    private final boolean loaded;

    Keyword(String humanReadable, boolean infoDisplayable, boolean multiples, boolean terrain, boolean realKeyword, boolean loaded) {
        this.humanReadable = humanReadable;
        this.infoDisplayable = infoDisplayable;
        this.multiples = multiples;
        this.terrain = terrain;
        this.realKeyword = realKeyword;
        this.loaded = loaded;
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

    public boolean isLoaded() {
        return loaded;
    }
    public boolean isUnloaded() {
        return !loaded;
    }


    public static Keyword parse(String name) {
        String nameCaps = name.toUpperCase().trim().replace(' ', '_').replace('-', '_');
        String nameLower = name.toLowerCase();

        for (Keyword keyword : values()) {
            if (keyword.getHumanReadable().toLowerCase().equals(nameLower) || keyword.toString().equals(nameCaps))
                return keyword;
        }
        return null;
    }
}
