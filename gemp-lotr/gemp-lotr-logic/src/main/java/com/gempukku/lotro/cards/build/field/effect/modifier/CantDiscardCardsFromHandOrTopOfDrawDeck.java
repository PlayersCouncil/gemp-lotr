package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.logic.modifiers.CantDiscardCardsFromHandOrTopOfDeckModifier;
import org.json.simple.JSONObject;

public class CantDiscardCardsFromHandOrTopOfDrawDeck implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "filter");

        final String filter = FieldUtils.getString(object.get("filter"), "filter");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext ->
                new CantDiscardCardsFromHandOrTopOfDeckModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext),
                        actionContext.getPerformingPlayer(),
                        filterableSource.getFilterable(actionContext));
    }
}
