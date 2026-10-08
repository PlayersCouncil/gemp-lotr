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

    /**
     * What evaluateExpression would answer now, worked out without changing anything.  A capped amount spends its
     * cap as it is worked out, so whoever has to know the amount before it is given (a play's discount, which
     * decides what can be played before anything is) asks this, and asks evaluateExpression once, when it is
     * given.  CardPhaseLimitEvaluator is the one amount that answers this for itself.  Every other amount works
     * itself out as evaluateExpression does, a value built round a capped amount (a sum, a multiplication)
     * included: such a value does not pass the question on, so it must not be asked this, and
     * ValueResolver.resolveDiscount refuses a definition that would need it to be.
     */
    default int evaluateWithoutSpending(LotroGame game, PhysicalCard cardAffected) {
        return evaluateExpression(game, cardAffected);
    }
}
