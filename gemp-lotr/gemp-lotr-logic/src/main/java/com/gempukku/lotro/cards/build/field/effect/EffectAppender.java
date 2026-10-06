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

    /**
     * The threats this appender will add when it is carried out as a cost, as far as can be told beforehand;
     * zero for everything that adds none, and for anything that only adds them under a condition or by choice.
     */
    default int getThreatsAddedAsCost(ActionContext actionContext) {
        return 0;
    }

    default boolean canPreEvaluate() {
        return true;
    }

    /**
     * What can be told of this effect before the action it belongs to is taken, for judging whether the action
     * will do nothing (the no-effect flag).  Asked while the action is only on offer: no cost has been paid and
     * nothing has been chosen.  It must change nothing, and in particular must never work out an amount, since
     * an amount with a limit spends the limit as it is worked out.  An effect that cannot tell says UNKNOWN.
     */
    default EffectForecast forecast(ActionContext actionContext) {
        return EffectForecast.UNKNOWN;
    }
}
