package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.PreEvaluateAble;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

public interface EffectAppender extends PreEvaluateAble {
    void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext);

    boolean isPlayableInFull(ActionContext actionContext);

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }

    default boolean canPreEvaluate() {
        return true;
    }
}
