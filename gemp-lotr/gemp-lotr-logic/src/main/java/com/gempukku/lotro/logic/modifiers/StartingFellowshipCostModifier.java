package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

public class StartingFellowshipCostModifier extends AbstractModifier {
    private final Evaluator evaluator;
    private final String playerId;

    public StartingFellowshipCostModifier(PhysicalCard source, Condition condition, Evaluator amount, String playerId) {
        super(source, "Starting fellowship cost modifier " + amount, null, condition, ModifierEffect.STARTING_FELLOWSHIP_COST_MODIFIER);
        evaluator = amount;
        this.playerId = playerId;
    }

    @Override
    public int getStartingFellowshipCostModifier(LotroGame game, String forPlayerId) {
        if (playerId != null && !playerId.equals(forPlayerId))
            return 0;
        return evaluator.evaluateExpression(game, null);
    }
}
