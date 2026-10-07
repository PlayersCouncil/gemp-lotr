package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class MemorizeActive implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "hindered", "memory");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");
        // "hindered: true" takes hindered cards in as well, as it does on ChooseActiveCards, CanSpot and
        // ForEachYouCanSpot: for a text that names cards by nothing but "card" or a side ("each Free Peoples card
        // borne by companions"), which a hindered card still answers to (CR 5.0 "hinder"; Ketura on Q-0098).
        final boolean hindered = FieldUtils.getBoolean(effectObject.get("hindered"), "hindered", false);

        if (memory == null)
            throw new InvalidCardDefinitionException("Memory is required for a Memorize effect.");

        final FilterableSource filterSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new BookkeepingAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final Filterable filterable = filterSource.getFilterable(actionContext);
                        final Collection<PhysicalCard> physicalCards = Filters.filterActive(game,
                                hindered ? SpotOverride.INCLUDE_HINDERED : SpotOverride.NONE, filterable);
                        actionContext.setCardMemory(memory, physicalCards);
                    }
                };
            }
        };
    }

}
