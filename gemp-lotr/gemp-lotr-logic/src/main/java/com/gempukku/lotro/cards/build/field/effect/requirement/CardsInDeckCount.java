package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class CardsInDeckCount implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "deck", "count");

        final String deck = FieldUtils.getString(object.get("deck"), "deck", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("count"), environment);

        return actionContext -> {
            final String player = playerSource.getPlayer(actionContext);
            final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

            return actionContext.getGame().getGameState().getDeck(player).size() == count;
        };
    }
}
