package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import org.json.simple.JSONObject;

public class CanSpotCultureTokens implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "culture", "filter", "amount", "memorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), 1, environment);
        final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
        final Culture culture = FieldUtils.getEnum(Culture.class, object.get("culture"), "culture");
        final Token tokenForCulture = culture != null ? Token.findTokenForCulture(culture) : null;
        final String memory = FieldUtils.getString(object.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        if (tokenForCulture == null) {
            return (actionContext) -> {
                LotroGame game = actionContext.getGame();
                Filterable[] filters = new Filterable[]{filterableSource.getFilterable(actionContext)};
                int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return GameUtils.getAllSpottableCultureTokens(game, filters) >= count;
            };
        } else {
            return (actionContext) -> {
                LotroGame game = actionContext.getGame();
                Filterable[] filters = new Filterable[]{filterableSource.getFilterable(actionContext)};
                int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                int tokens = GameUtils.getSpottableCultureTokensOfType(game, tokenForCulture, filters);

                if (memory != null) {
                    actionContext.setValueToMemory(memory, String.valueOf(tokens));
                }

                return tokens >= count;
            };
        }
    }
}
