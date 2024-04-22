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
import com.gempukku.lotro.logic.timing.results.AssignedToSkirmishResult;
import org.json.simple.JSONObject;

public class WasAssignedToSkirmish implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return actionContext -> {
            final Filterable filterable = filterableSource.getFilterable(actionContext);

            final Filter aFilter = Filters.changeToFilter(filterable);

            for (EffectResult effectResult : actionContext.getGame().getActionsEnvironment().getTurnEffectResults()) {
                if (effectResult.getType() == EffectResult.Type.ASSIGNED_TO_SKIRMISH) {
                    AssignedToSkirmishResult assignResult = (AssignedToSkirmishResult) effectResult;
                    if (aFilter.accepts(actionContext.getGame(), assignResult.getAssignedCard()))
                        return true;
                }
            }

            return false;
        };
    }
}
