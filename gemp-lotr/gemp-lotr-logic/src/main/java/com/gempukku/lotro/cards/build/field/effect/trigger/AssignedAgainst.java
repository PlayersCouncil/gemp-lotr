package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.AssignAgainstResult;
import org.json.simple.JSONObject;

public class AssignedAgainst implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "side", "against", "memorizeAgainst", "memorizeAssigned");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String against = FieldUtils.getString(value.get("against"), "against", "any");
        final String memorizeAssigned = FieldUtils.getString(value.get("memorizeAssigned"), "memorizeAssigned");
        final String memorizeAgainst = FieldUtils.getString(value.get("memorizeAgainst"), "memorizeAgainst");
        final Side side = FieldUtils.getEnum(Side.class, value.get("side"), "side");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource againstSource = environment.getFilterFactory().generateFilter(against, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable assignedFilterable = filterableSource.getFilterable(actionContext);
                final Filterable againstFilterable = againstSource.getFilterable(actionContext);
                final boolean result = TriggerConditions.assignedAgainst(actionContext.getGame(), actionContext.getEffectResult(), side,
                        againstFilterable, assignedFilterable);

                if(result) {
                    var assignmentResult = (AssignAgainstResult) actionContext.getEffectResult();
                    if (memorizeAgainst != null) {
                        actionContext.setCardMemory(memorizeAgainst, assignmentResult.getAgainst());
                    }
                    if (memorizeAssigned != null) {
                        actionContext.setCardMemory(memorizeAssigned, assignmentResult.getAssignedCard());
                    }
                }

                return result;
            }
        };
    }
}
