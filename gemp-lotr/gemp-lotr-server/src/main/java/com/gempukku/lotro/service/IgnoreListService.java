package com.gempukku.lotro.service;

import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A player's ignore list, as the hall's user-list menu and the My Account "Ignored players" list manage it.  Wraps the
 * same {@link IgnoreDAO} the /ignore, /unignore and /listIgnores chat commands use (the cached one, so the hall's
 * table filtering sees a change straight away), with the /ignore command's validation: a name of 2 to 30 characters
 * that is not your own.  Unlike the /ignore chat command it also checks that the player exists and stores their name as
 * registered, so a typo or a different capitalisation cannot put a dead entry on the list.
 */
public class IgnoreListService {
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 30;

    public enum Outcome {
        ADDED, ALREADY_IGNORED, REMOVED, NOT_IGNORED, INVALID_NAME, SELF, UNKNOWN_PLAYER;

        public boolean isError() {
            return this == INVALID_NAME || this == SELF || this == UNKNOWN_PLAYER;
        }
    }

    /** @param name the name as stored on the list (the registered spelling), or the trimmed input when rejected */
    public record Result(Outcome outcome, String name) {
        public String message() {
            return switch (outcome) {
                case ADDED -> name + " added to your ignore list.";
                case ALREADY_IGNORED -> name + " is already on your ignore list.";
                case REMOVED -> name + " removed from your ignore list.";
                case NOT_IGNORED -> name + " is not on your ignore list.";
                case INVALID_NAME -> "'" + name + "' is not a valid username.";
                case SELF -> "You cannot ignore yourself.";
                case UNKNOWN_PLAYER -> "There is no player called '" + name + "'.";
            };
        }
    }

    private final IgnoreDAO _ignoreDAO;
    private final PlayerDAO _playerDAO;

    public IgnoreListService(IgnoreDAO ignoreDAO, PlayerDAO playerDAO) {
        _ignoreDAO = ignoreDAO;
        _playerDAO = playerDAO;
    }

    /** For callers that only list and remove (the /unignore chat command); {@link #ignore} needs the players table. */
    public IgnoreListService(IgnoreDAO ignoreDAO) {
        this(ignoreDAO, null);
    }

    /** The players this player ignores, sorted case-insensitively. */
    public List<String> getIgnoredPlayers(String playerName) {
        List<String> result = snapshot(playerName);
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    public Result ignore(String playerName, String target) {
        String name = target == null ? "" : target.trim();
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH)
            return new Result(Outcome.INVALID_NAME, name);
        if (name.equalsIgnoreCase(playerName))
            return new Result(Outcome.SELF, name);
        if (_playerDAO == null)
            throw new IllegalStateException("IgnoreListService was created without a PlayerDAO; it cannot add names");

        Player player = _playerDAO.getPlayer(name);
        if (player == null)
            return new Result(Outcome.UNKNOWN_PLAYER, name);
        String registered = player.getName();
        if (registered.equalsIgnoreCase(playerName))
            return new Result(Outcome.SELF, registered);

        String listed = findListed(playerName, registered);
        if (listed != null)
            return new Result(Outcome.ALREADY_IGNORED, listed);
        if (!_ignoreDAO.addIgnoredUser(playerName, registered))
            return new Result(Outcome.ALREADY_IGNORED, registered);
        return new Result(Outcome.ADDED, registered);
    }

    /**
     * Takes a name off the list (for the REST endpoint and the /unignore chat command alike).  Any entry on the list can be removed (matched exactly first, then ignoring case), so
     * old entries saved by the chat command with another spelling can still be cleared.
     */
    public Result unignore(String playerName, String target) {
        String name = target == null ? "" : target.trim();
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH)
            return new Result(Outcome.INVALID_NAME, name);

        String listed = findListed(playerName, name);
        if (listed == null)
            return new Result(Outcome.NOT_IGNORED, name);
        if (!_ignoreDAO.removeIgnoredUser(playerName, listed))
            return new Result(Outcome.NOT_IGNORED, listed);
        return new Result(Outcome.REMOVED, listed);
    }

    /** The list's own spelling of name: an exact match if there is one, else a case-insensitive one, else null. */
    private String findListed(String playerName, String name) {
        List<String> listed = snapshot(playerName);
        if (listed.contains(name))
            return name;
        for (String entry : listed)
            if (entry.equalsIgnoreCase(name))
                return entry;
        return null;
    }

    private List<String> snapshot(String playerName) {
        Set<String> ignored = _ignoreDAO.getIgnoredUsers(playerName);
        if (ignored == null)
            return new ArrayList<>();
        // the cached DAO hands out a synchronized set; iterating it must hold its lock
        synchronized (ignored) {
            return new ArrayList<>(ignored);
        }
    }
}
