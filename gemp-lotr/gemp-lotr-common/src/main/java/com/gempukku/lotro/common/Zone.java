package com.gempukku.lotro.common;

public enum Zone implements Filterable {
    // Public knowledge and in play
    FREE_CHARACTERS("play", true, true, true),
    SHADOW_CHARACTERS("play", true, true, true),
    SUPPORT("play", true, true, true),
    ADVENTURE_PATH("play", true, true, true),
    ATTACHED("play", true, true, true),

    // Public knowledge but not in play
    STACKED("stacked", true, true, false),
    DEAD("dead pile", true, true, false),
    REMOVED("removed", true, true, false),

    // Private knowledge
    HAND("hand", false, true, false),
    ADVENTURE_DECK("adventureDeck", false, true, false),
    //In PC formats this is public information, but that is handled as a special case
    DISCARD("discard", false, true, false),

    // Nobody sees
    VOID("void", false, false, false),
    VOID_FROM_HAND("voidFromHand", false, false, false),
    DECK("deck", false, false, false);

    private final String _humanReadable;
    private final boolean _public;
    private final boolean _visibleByOwner;
    private final boolean _inPlay;

    Zone(String humanReadable, boolean isPublic, boolean visibleByOwner, boolean inPlay) {
        _humanReadable = humanReadable;
        _public = isPublic;
        _visibleByOwner = visibleByOwner;
        _inPlay = inPlay;
    }

    public String getHumanReadable() {
        return _humanReadable;
    }

    public boolean isInPlay() {
        return _inPlay;
    }

    public boolean isPublic() {
        return _public;
    }

    public boolean isVisibleByOwner() {
        return _visibleByOwner;
    }
}
