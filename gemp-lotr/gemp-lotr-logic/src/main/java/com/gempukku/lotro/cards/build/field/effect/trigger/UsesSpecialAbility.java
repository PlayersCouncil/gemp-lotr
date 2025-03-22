package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ActivateCardResult;
import com.gempukku.lotro.logic.timing.results.DiscardToHealResult;
import org.json.simple.JSONObject;

public class UsesSpecialAbility implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "player", "memorize", "phase");

        String player = FieldUtils.getString(value.get("player"), "player");
        String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Timeword timeword = FieldUtils.getEnum(Timeword.class, value.get("phase"), "phase");

        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");
        final PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player) : null;
        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                String player = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                boolean activated = TriggerConditions.activated(actionContext.getGame(), player, actionContext.getEffectResult(), filterableSource.getFilterable(actionContext));

                if (activated) {
                    ActivateCardResult activateCardResult = (ActivateCardResult) actionContext.getEffectResult();
                    if(activateCardResult instanceof DiscardToHealResult)
                        return false;

                    if (timeword != null) {
                        if (activateCardResult.getActionTimeword() != timeword)
                            return false;
                    }

                    if (memorize != null) {
                        PhysicalCard playedCard = activateCardResult.getSource();
                        actionContext.setCardMemory(memorize, playedCard);
                    }
                }
                return activated;
            }
        };
    }
}
