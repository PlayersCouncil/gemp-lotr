package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.CharacterWonSkirmishResult;
import org.json.simple.JSONObject;

public class DidWinSkirmish implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);

            final Filter aFilter = Filters.changeToFilter(filterable);

            for (EffectResult effectResult : actionContext.getGame().getActionsEnvironment().getTurnEffectResults()) {
                if (effectResult.getType() == EffectResult.Type.CHARACTER_WON_SKIRMISH) {
                    CharacterWonSkirmishResult winResult = (CharacterWonSkirmishResult) effectResult;
                    if (aFilter.accepts(actionContext.getGame(), winResult.getWinner()))
                        return true;
                }
            }

            return false;
        };
    }
}
