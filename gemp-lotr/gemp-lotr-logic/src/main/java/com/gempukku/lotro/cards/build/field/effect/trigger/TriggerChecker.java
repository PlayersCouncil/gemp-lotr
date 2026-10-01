package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.Requirement;

public interface TriggerChecker extends Requirement {
    boolean isBefore();

    /**
     * True for "constantly check" triggers, which model a continuous condition ("while you can spot X, ...") rather
     * than a reaction to one particular event. They are re-checked against every batch of effect results, so the same
     * trigger can be collected again before an earlier copy of it has resolved.
     */
    default boolean isConstantCheck() {
        return false;
    }
}
