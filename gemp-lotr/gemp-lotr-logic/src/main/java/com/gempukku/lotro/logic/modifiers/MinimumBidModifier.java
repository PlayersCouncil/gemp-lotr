package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class MinimumBidModifier extends AbstractModifier {
    private final Evaluator evaluator;
    private final String playerId;

    public MinimumBidModifier(PhysicalCard source, Condition condition, Evaluator amount, String playerId) {
        super(source, "Minimum bid modifier " + amount, null, condition, ModifierEffect.MINIMUM_BID_MODIFIER);
        evaluator = amount;
        this.playerId = playerId;
    }

    @Override
    public int getMinimumBidModifier(LotroGame game, String forPlayerId) {
        if (playerId != null && !playerId.equals(forPlayerId))
            return 0;
        return evaluator.evaluateExpression(game, null);
    }
}
