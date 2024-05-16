package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ChooseAndHeal implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "player");

        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);

        DelayedAppender result = new DelayedAppender() {
            @Override
            protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                List<Effect> result = new LinkedList<>();
                final int maxHealCount = count.getMaximum(actionContext);
                final int minHealCount = count.getMinimum(actionContext);
                for (int i = 0; i < maxHealCount; i++) {
                    final int remainingHeals = maxHealCount - i;
                    //If the healing is optional (like sanctuary healing, an implied "up to" the maximum), then the minimum
                    // is set to 0, otherwise 1 (as in, the player /must/ choose a character to heal).
                    int min = (i < minHealCount) ? 1 : 0;
                    ChooseAndHealCharactersEffect healEffect = new ChooseAndHealCharactersEffect(action, playerSource.getPlayer(actionContext),
                            min, 1, filterableSource.getFilterable(actionContext));
                    healEffect.setChoiceText("Choose card to heal - remaining heals: " + remainingHeals);
                    result.add(healEffect);
                }

                return result;
            }
        };

        return result;
    }

}
