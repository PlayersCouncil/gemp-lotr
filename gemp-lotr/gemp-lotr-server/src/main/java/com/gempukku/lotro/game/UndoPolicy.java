package com.gempukku.lotro.game;

/**
 * What taking a game back is allowed to do at one table.  Every kind of game has the same machinery; this is the
 * part that differs between a casual game, a tournament game and a game against a bot.
 *
 * @param green Whether a player may take back their own choices when nothing of anybody else's is undone (it is done
 *              at once, with nobody asked).
 * @param yellow Whether a player may ask for the game to be taken back through another player's choices.
 * @param red Whether a player may ask for it after seeing something they had not seen.
 * @param othersAgreeAtOnce Whether the other players are taken to agree to every request (a bot, a scripted opponent).
 * @param horizon Whether a player is held to the current turn, and to the turn before it until the new turn has
 *                reached its maneuver phase.  Without it, anything since the game began can be gone back to.
 * @param timeoutSeconds How long a request waits for an answer before it counts as refused.
 * @param greenPerPhase How many times a player may take back their own choices in one phase; 0 for any number.
 */
public record UndoPolicy(boolean green, boolean yellow, boolean red, boolean othersAgreeAtOnce, boolean horizon,
                         int timeoutSeconds, int greenPerPhase) {
    /** Between two people with nothing at stake but the game.  Also a league game, unless the league says otherwise. */
    public static final UndoPolicy CASUAL = new UndoPolicy(true, true, true, false, true, 60, 3);
    /** Only one's own choices, unless whoever runs the tournament turns requests on. */
    public static final UndoPolicy TOURNAMENT = new UndoPolicy(true, false, false, false, true, 60, 3);
    /** Against a bot or a scenario's scripted opponent, who never refuses and cannot be annoyed. */
    public static final UndoPolicy SOLO = new UndoPolicy(true, true, true, true, false, 60, 0);
    /** No taking back at all. */
    public static final UndoPolicy OFF = new UndoPolicy(false, false, false, false, true, 60, 0);

    public boolean allows(RevertAssessment.Tier tier) {
        return switch (tier) {
            case GREEN -> green;
            case YELLOW -> yellow;
            case RED -> red;
        };
    }
}
