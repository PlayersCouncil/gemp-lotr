package com.gempukku.lotro.logic.modifiers.condition;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.Condition;

public class AndCondition implements Condition {
    private final Condition[] _conditions;

    public AndCondition(Condition... conditions) {
        _conditions = conditions;
    }

    @Override
    public boolean isFullfilled(LotroGame game) {
        for (Condition condition : _conditions) {
            if (condition != null && !condition.isFullfilled(game))
                return false;
        }

        return true;
    }
}
