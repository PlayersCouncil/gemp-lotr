package com.gempukku.lotro.scenario;

/**
 * The scenario's script and the game disagree: an answer does not fit the decision it was written for, a decision
 * turned up that no step expects, and so on.  Nothing is wrong with the file's syntax; it is wrong about the game.
 */
public class ScenarioScriptException extends Exception {
    public ScenarioScriptException(String message) {
        super(message);
    }
}
