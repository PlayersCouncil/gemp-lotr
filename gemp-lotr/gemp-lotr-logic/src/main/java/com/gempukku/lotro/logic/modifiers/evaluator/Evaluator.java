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
}
