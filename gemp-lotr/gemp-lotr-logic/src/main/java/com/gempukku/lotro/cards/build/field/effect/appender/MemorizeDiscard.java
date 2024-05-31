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
import java.util.HashMap;
import java.util.Map;

public class MemorizeDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "memory", "uniqueTitles");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "any");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");
        final boolean uniqueTitles = FieldUtils.getBoolean(effectObject.get("uniqueTitles"), "uniqueTitles", false);

        if (memory == null)
            throw new InvalidCardDefinitionException("Memory is required for a MemorizeDiscard effect.");

        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final Filterable filterable = filterSource.getFilterable(actionContext);
                        Collection<? extends PhysicalCard> discard = game.getGameState().getDiscard(actionContext.getPerformingPlayer());
                        if (uniqueTitles) {
                            discard = filterUniqueTitles(discard);
                        }
                        final Collection<PhysicalCard> physicalCards = Filters.filter(game, discard, filterable);
                        actionContext.setCardMemory(memory, physicalCards);
                    }
                };
            }
        };
    }

    private Collection<PhysicalCard> filterUniqueTitles(Collection<? extends PhysicalCard> cards) {
        Map<String, PhysicalCard> nameMap = new HashMap<>();
        for (PhysicalCard card : cards)
            nameMap.put(card.getBlueprint().getTitle(), card);
        return nameMap.values();
    }
}
