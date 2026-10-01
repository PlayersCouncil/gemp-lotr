package com.gempukku.lotro.scenario;

/**
 * Something is wrong with how a scenario file is written.  The message says where.
 */
public class ScenarioFormatException extends Exception {
    public ScenarioFormatException(String message) {
        super(message);
    }

    public ScenarioFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
