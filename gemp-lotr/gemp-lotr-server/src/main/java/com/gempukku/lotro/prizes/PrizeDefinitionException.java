package com.gempukku.lotro.prizes;

/**
 * Thrown when a prize definition (a list of {@link PrizeTier}s, or a request to resolve or create a placeholder) is
 * not valid.  The field names the offending part of the definition ({@code prizeTiers[1].items[0].blueprintId}) so
 * callers such as the admin API can point at it; the message is already phrased for display to an admin.
 */
public class PrizeDefinitionException extends Exception {
    private final String _field;

    public PrizeDefinitionException(String field, String message) {
        super(message);
        _field = field;
    }

    public String getField() {
        return _field;
    }
}
