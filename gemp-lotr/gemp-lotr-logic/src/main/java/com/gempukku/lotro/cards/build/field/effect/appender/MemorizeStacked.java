package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MemorizeStacked implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "on", "memory");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String on = FieldUtils.getString(effectObject.get("on"), "on");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        if (memory == null)
            throw new InvalidCardDefinitionException("Memory is required for a MemorizeStacked effect.");

        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource onFilterSource = environment.getFilterFactory().generateFilter(on, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final Filterable filterable = filterSource.getFilterable(actionContext);
                        final Filterable onFilterable = onFilterSource.getFilterable(actionContext);
                        final Collection<PhysicalCard> cardsWithStack = Filters.filterActive(game, onFilterable);

                        List<PhysicalCard> cardsToMemorize = new LinkedList<>();
                        for (PhysicalCard cardWithStack : cardsWithStack)
                            cardsToMemorize.addAll(Filters.filter(game.getGameState().getStackedCards(cardWithStack), game, filterable));
                        actionContext.setCardMemory(memory, cardsToMemorize);
                    }
                };
            }
        };
    }

}
