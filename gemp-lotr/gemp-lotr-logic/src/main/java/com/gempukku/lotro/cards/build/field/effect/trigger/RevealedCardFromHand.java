package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.RevealCardFromHandResult;
import org.json.simple.JSONObject;

public class RevealedCardFromHand implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "player");

        String player = FieldUtils.getString(value.get("player"), "player");
        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");

        final PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                if (TriggerConditions.forEachCardRevealedFromHand(actionContext.getEffectResult())) {
                    RevealCardFromHandResult revealCardFromHandResult = (RevealCardFromHandResult) actionContext.getEffectResult();
                    if (playerSource != null && !playerSource.getPlayer(actionContext).equals(revealCardFromHandResult.getSource().getOwner()))
                        return false;
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    final PhysicalCard revealedCard = revealCardFromHandResult.getRevealedCard();
                    return Filters.accepts(actionContext.getGame(), revealedCard, filterable);
                }
                return false;
            }
        };
    }
}
