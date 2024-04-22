package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class GetCardsFromTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memorize");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        if (filter == null)
            throw new InvalidCardDefinitionException("Filter is required for a GetCardsFromTopOfDeck effect.");

        if (memorize == null)
            throw new InvalidCardDefinitionException("Memorize is required for a GetCardsFromTopOfDeck effect.");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final Filter acceptFilter = Filters.changeToFilter(filterable);
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final List<? extends PhysicalCard> deck = game.getGameState().getDeck(actionContext.getPerformingPlayer());
                        List<PhysicalCard> result = new LinkedList<>();
                        for (PhysicalCard physicalCard : deck) {
                            if (acceptFilter.accepts(game, physicalCard))
                                result.add(physicalCard);
                            else
                                break;
                        }

                        actionContext.setCardMemory(memorize, result);
                    }
                };
            }
        };
    }
}
