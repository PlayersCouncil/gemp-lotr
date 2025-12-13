package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.HealCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Heal implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "select", "times", "memorize", "player");

        final ValueSource times = ValueResolver.resolveEvaluator(effectObject.get("times"), 1, environment);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(select,
                        SpotOverride.NONE,
                        (actionContext) -> Filters.canHeal,
                        (actionContext) -> (Filter) (game, physicalCard) -> {
                            final int healTimes = times.getEvaluator(actionContext).evaluateExpression(game, physicalCard);
                            return game.getGameState().getWounds(physicalCard) >= healTimes && game.getModifiersQuerying().canBeHealed(game, physicalCard);
                        },
                        count, memory, player, "Choose cards to heal", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final int healTimes = times.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        List<Effect> result = new LinkedList<>();
                        for (int i = 0; i < healTimes; i++) {
                            result.add(new HealCharactersEffect(actionContext.getSource(), playerSource.getPlayer(actionContext), cardsFromMemory.toArray(new PhysicalCard[0])));
                        }
                        return result;
                    }
                });

        return result;
    }

}
