package com.gempukku.lotro.logic.timing;

/**
 * Why an action a player is being offered has been judged, before it is taken, to do nothing.  The action is
 * legal and is offered all the same; the client shows it differently so that it is not taken by mistake.
 */
public enum NoEffect {
    /** Its effect will do nothing. */
    EFFECT("effect"),
    /** The ability, or the bonus it gives, has reached its limit. */
    LIMIT("limit"),
    /** A skirmish action that reaches nobody in the skirmish being fought. */
    SKIRMISH("skirmish");

    /** What a decision says of an action that is not flagged. */
    public static final String NOT_FLAGGED = "false";

    private final String word;

    NoEffect(String word) {
        this.word = word;
    }

    /**
     * @return The word a decision sends to the client for this reason.
     */
    public String getWord() {
        return word;
    }

    /**
     * @return The word a decision sends for an action: the reason's own, or "false" for no reason at all.
     */
    public static String wordOf(NoEffect noEffect) {
        return noEffect == null ? NOT_FLAGGED : noEffect.getWord();
    }
}
