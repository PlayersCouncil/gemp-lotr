package com.gempukku.lotro.logic.modifiers.evaluator;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public interface Evaluator {
    int evaluateExpression(LotroGame game, PhysicalCard cardAffected);

    default int getMinimum(LotroGame game, PhysicalCard cardAffected) {
        return evaluateExpression(game, cardAffected);
    }

    default int getMaximum(LotroGame game, PhysicalCard cardAffected) {
        return evaluateExpression(game, cardAffected);
    }

    /**
     * Whether this amount is capped and the cap is spent for the given card, so that it would come to nothing.
     * Unlike evaluateExpression it changes nothing: a capped amount spends its cap as it is worked out, so
     * whoever only wants to know beforehand whether anything is left (the no-effect flag) asks this instead.
     * @return True only if the amount is known to be spent; false for every amount that has no cap.
     */
    default boolean isSpent(LotroGame game, PhysicalCard cardAffected) {
        return false;
    }
}
