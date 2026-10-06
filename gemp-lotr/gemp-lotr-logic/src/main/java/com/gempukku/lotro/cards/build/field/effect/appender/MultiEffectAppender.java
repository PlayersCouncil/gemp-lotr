package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.logic.actions.CostToEffectAction;

import java.util.LinkedList;
import java.util.List;

public class MultiEffectAppender implements EffectAppender {
    private boolean playabilityCheckedForEffect;

    private final List<EffectAppender> effectAppenders = new LinkedList<>();

    public void setPlayabilityCheckedForEffect(boolean playabilityCheckedForEffect) {
        this.playabilityCheckedForEffect = playabilityCheckedForEffect;
    }

    public void addEffectAppender(EffectAppender effectAppender) {
        effectAppenders.add(effectAppender);
    }

    @Override
    public void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        for (EffectAppender effectAppender : effectAppenders)
            effectAppender.appendEffect(cost, action, actionContext);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        for (EffectAppender effectAppender : effectAppenders) {
            if (!effectAppender.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }

    @Override
    public int getThreatsAddedAsCost(ActionContext actionContext) {
        int result = 0;
        for (EffectAppender effectAppender : effectAppenders)
            result += effectAppender.getThreatsAddedAsCost(actionContext);
        return result;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return playabilityCheckedForEffect;
    }

    // All of the parts happen, one after another.  An effect that is built of a card resolver and what is then
    // done with the cards says something better of itself (see EffectForecast.givenTo); left to this, it says
    // UNKNOWN, since its second part does.
    @Override
    public EffectForecast forecast(ActionContext actionContext) {
        return EffectForecast.together(actionContext, effectAppenders.toArray(new EffectAppender[0]));
    }
}
