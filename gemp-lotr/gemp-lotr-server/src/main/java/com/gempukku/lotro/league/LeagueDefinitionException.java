package com.gempukku.lotro.league;

/**
 * Thrown when a league definition (a {@link LeagueParams} plus a league type) is not valid.  The parameter name
 * identifies which part of the definition is at fault so callers such as the admin API can report it; the message
 * is already phrased for display to an admin.
 */
public class LeagueDefinitionException extends Exception {
    private final String _parameter;

    public LeagueDefinitionException(String parameter, String message) {
        super(message);
        _parameter = parameter;
    }

    public String getParameter() {
        return _parameter;
    }
}
