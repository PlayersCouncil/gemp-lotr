package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.ValueSource;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collections;
import java.util.Map;

/**
 * "Exert (wound, heal) a <kind> X times" as an EFFECT, where X is a count that has come to 0 or less: the choice
 * of who is not made, since whoever were chosen nothing would happen to him (Ketura on F-0116, "the prompt should
 * be smarter"; Q-0337: B, They Will Never Stop Hunting You 3_84 with a card of cost 0 revealed).  Nothing is
 * asked, the effect's memory is left empty, and the effect's forecast says it will do nothing.
 *
 * Only an effect: as a cost the choice is made as before, so no action becomes playable or unplayable by this.
 * The count is read as the choice comes up, before it is made, so a count that reads the card this very choice
 * picks is read as it stands before the pick (no definition has such a count that can come to 0).
 */
final class NoTimesNoChoice {
    private NoTimesNoChoice() {
    }

    /**
     * @return The card resolver of an exert, wound or heal that CHOOSES its cards, as an effect: carried out only
     * while the count of times is above 0.  As a cost, or where nobody chooses, the resolver as given.
     */
    static EffectAppender resolver(boolean cost, boolean chosen, EffectAppender cardResolver, ValueSource times, String memory) {
        if (cost || !chosen)
            return cardResolver;
        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                if (times.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null) <= 0) {
                    actionContext.setCardMemory(memory, Collections.<PhysicalCard>emptySet());
                    return null;
                }
                final SubAction subAction = new SubAction(action);
                cardResolver.appendEffect(cost, subAction, actionContext);
                return new StackActionEffect(subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return cardResolver.isPlayableInFull(actionContext);
            }

            @Override
            public boolean canPreEvaluate() {
                return cardResolver.canPreEvaluate();
            }

            @Override
            public EffectForecast forecast(ActionContext actionContext) {
                return cardResolver.forecast(actionContext);
            }

            @Override
            public void tallyWoundsPlacedAsCost(ActionContext actionContext, Map<PhysicalCard, Integer> tally) {
                cardResolver.tallyWoundsPlacedAsCost(actionContext, tally);
            }
        };
    }

    /**
     * @return Whether the effect, as an effect, is certain to be carried out no times: its count can be read now
     * and is 0 or less.  A count that reads the action's memory is read only at resolution
     * (ActionContext.isAtResolution), once what it reads has been filled.
     */
    static boolean isCertainlyNoTimes(boolean cost, ActionContext actionContext, ValueSource times, boolean timesReadsMemory) {
        if (cost || (timesReadsMemory && !actionContext.isAtResolution()))
            return false;
        try {
            return times.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null) <= 0;
        } catch (RuntimeException exp) {
            return false;
        }
    }

    /** @return Whether a definition's count, as written, reads the action's memory. */
    static boolean readsMemory(Object timesDefinition) {
        return timesDefinition != null && CardResolver.readsMemory(String.valueOf(timesDefinition));
    }
}
