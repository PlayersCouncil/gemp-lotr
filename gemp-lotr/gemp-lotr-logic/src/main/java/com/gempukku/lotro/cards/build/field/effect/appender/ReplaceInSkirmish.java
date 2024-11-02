package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ReplaceInSkirmishEffect;
import com.gempukku.lotro.logic.timing.DoNothingEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ReplaceInSkirmish implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "with");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String with = FieldUtils.getString(effectObject.get("with"), "with");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(with, "_temp", "you", "Choose character to replace with in skirmish", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory("_temp");
                        if (card != null)
                            return new ReplaceInSkirmishEffect(card, filterableSource.getFilterable(actionContext));
                        else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }

}
