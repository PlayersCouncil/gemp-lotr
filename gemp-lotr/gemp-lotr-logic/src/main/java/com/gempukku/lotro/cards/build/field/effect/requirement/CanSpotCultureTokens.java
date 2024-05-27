package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import org.json.simple.JSONObject;

public class CanSpotCultureTokens implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "culture", "filter", "amount");

        final int count = FieldUtils.getInteger(object.get("amount"), "amount", 1);
        final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
        final Culture culture = FieldUtils.getEnum(Culture.class, object.get("culture"), "culture");
        final Token tokenForCulture = Token.findTokenForCulture(culture);

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        if (tokenForCulture == null) {
            return (actionContext) -> {
                LotroGame game = actionContext.getGame();
                Filterable[] filters = new Filterable[]{filterableSource.getFilterable(actionContext)};
                return GameUtils.getAllSpottableCultureTokens(game, filters) >= count;
            };
        } else {
            return (actionContext) -> {
                LotroGame game = actionContext.getGame();
                Filterable[] filters = new Filterable[]{filterableSource.getFilterable(actionContext)};
                return GameUtils.getSpottableCultureTokensOfType(game, tokenForCulture, filters) >= count;
            };
        }
    }
}
