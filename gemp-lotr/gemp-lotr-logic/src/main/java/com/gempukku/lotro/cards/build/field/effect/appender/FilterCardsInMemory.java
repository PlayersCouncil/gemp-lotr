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

public class FilterCardsInMemory implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memory", "memorizeMatching", "memorizeNotMatching");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");
        final String memorizeMatching = FieldUtils.getString(effectObject.get("memorizeMatching"), "memorizeMatching");
        final String memorizeNotMatching = FieldUtils.getString(effectObject.get("memorizeNotMatching"), "memorizeNotMatching");

        if (filter == null)
            throw new InvalidCardDefinitionException("Filter is required for a FilterCardsInMemory effect.");

        if (memory == null)
            throw new InvalidCardDefinitionException("Memory is required for a FilterCardsInMemory effect.");

        if (memorizeMatching == null && memorizeNotMatching == null)
            throw new InvalidCardDefinitionException("One of memorizeMatching or memorizeNotMatching is required for a FilterCardsInMemory effect.");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final Filterable filterable = filterableSource.getFilterable(actionContext);
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        List<PhysicalCard> matchingCards = new LinkedList<>();
                        List<PhysicalCard> notMatchingCards = new LinkedList<>();
                        for (PhysicalCard physicalCard : cardsFromMemory) {
                            if (Filters.accepts(game, filterable, physicalCard))
                                matchingCards.add(physicalCard);
                            else
                                notMatchingCards.add(physicalCard);
                        }

                        if (memorizeMatching != null)
                            actionContext.setCardMemory(memorizeMatching, matchingCards);
                        if (memorizeNotMatching != null)
                            actionContext.setCardMemory(memorizeNotMatching, notMatchingCards);
                    }
                };
            }
        };
    }
}
