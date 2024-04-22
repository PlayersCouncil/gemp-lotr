package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class DoWhileSubAction extends SubAction {
    private final BooleanSupplier condition;
    private final Consumer<CostToEffectAction> effectsAppender;

    private boolean firstExecution = true;

    public DoWhileSubAction(Action action, BooleanSupplier condition, Consumer<CostToEffectAction> effectsAppender) {
        super(action);
        this.condition = condition;
        this.effectsAppender = effectsAppender;
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (firstExecution) {
            effectsAppender.accept(this);
            firstExecution = false;
        }

        if (!hasNextEffect()) {
            if (condition.getAsBoolean()) {
                effectsAppender.accept(this);
            }
        }

        return getNextEffect();
    }
}
