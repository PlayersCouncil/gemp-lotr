package com.gempukku.lotro.game;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;

/**
 * Brings a fresh game to where an earlier game was, by giving it the earlier game's journal: each note acted on and
 * each answer given, in the order they were written, every answer to the decision it was first given to.
 *
 * The new game has to be the same game as the earlier one in everything a journal does not hold: the same format,
 * the same decks, the same players in the same seats, and its random set to repeat the earlier journal's outcomes
 * (GameJournal.replayRandomFrom) before it started.  Whoever builds the game sees to that; this only does the
 * feeding, and stops at the first answer the new game will not take in the way the earlier one did.
 *
 * A follower that stopped with a problem leaves its game somewhere between the start and where it was meant to get
 * to.  That game is best thrown away.
 *
 * This is the one place a journal is followed: a scenario picking a player up at a checkpoint, a scenario's author
 * moving about in a draft, and a game being taken back to an earlier decision all come through here.
 */
public class JournalFollower {
    /** The game being brought along, as much of it as following a journal needs. */
    public interface Table {
        /**
         * @return The name, in this game, of the player a journal calls by this key (see GameJournal.PlayerKeys).
         */
        String playerOf(String key);

        /**
         * @return The decision the game is waiting on that player for; null if it is not waiting on them.
         */
        AwaitingDecision awaiting(String player);

        /**
         * Gives the game an answer to the decision it is waiting on that player for, and has it carry on until it
         * next comes to rest.
         * @throws RuntimeException if the game does not accept the answer.
         */
        void answer(String player, AwaitingDecision decision, String answer);

        /**
         * A note of the journal has been reached (something that steered whatever runs the game, to be done again at
         * this point).
         * @return Whether anything was done about it.
         * @throws RuntimeException if it could not be done.
         */
        default boolean note(GameJournal.Entry note) {
            return false;
        }

        /**
         * @return What to call the player of this key in the words that say why following stopped.
         */
        default String describe(String key) {
            return key;
        }
    }

    private int _answersFollowed;
    private int _notesFollowed;
    private String _problem;

    /**
     * Gives the game every note and answer of the earlier journal, in order, stopping at the first that does not fit.
     * @return This, for what it has to say afterwards.
     */
    public JournalFollower follow(GameJournal earlier, Table table) {
        for (GameJournal.Entry entry : earlier.entries()) {
            if (entry.kind() == GameJournal.Kind.NOTE) {
                try {
                    if (table.note(entry))
                        _notesFollowed++;
                } catch (RuntimeException exp) {
                    _problem = "the note \"" + entry.detail() + " " + entry.value() + "\" could not be followed: " + exp;
                    return this;
                }
            }
            if (entry.kind() != GameJournal.Kind.ANSWER)
                continue;
            String who = table.describe(entry.player());
            String player = table.playerOf(entry.player());
            AwaitingDecision decision = table.awaiting(player);
            if (!GameJournal.matches(entry, decision)) {
                _problem = "answer " + (_answersFollowed + 1) + " was given by " + who + " to a "
                        + entry.detail() + ", but the game is "
                        + (decision == null ? "not asking " + who + " anything" : "asking " + who + " a " + GameJournal.fingerprint(decision)
                                + " (\"" + decision.getText() + "\")");
                return this;
            }
            String concrete = GameJournal.concrete(decision, entry.value());
            if (concrete == null) {
                _problem = "answer " + (_answersFollowed + 1) + " (\"" + entry.value() + "\") is not among what the game is offering "
                        + who + " now";
                return this;
            }
            try {
                table.answer(player, decision, concrete);
            } catch (RuntimeException exp) {
                _problem = "answer " + (_answersFollowed + 1) + " (\"" + entry.value() + "\") was not accepted: " + exp;
                return this;
            }
            _answersFollowed++;
        }
        return this;
    }

    /** How many of the earlier game's answers the game has been given so far. */
    public int answersFollowed() {
        return _answersFollowed;
    }

    /** How many of the earlier game's notes something was done about. */
    public int notesFollowed() {
        return _notesFollowed;
    }

    /**
     * @return Why the game could not be given the whole journal; null if it was.
     */
    public String problem() {
        return _problem;
    }
}
