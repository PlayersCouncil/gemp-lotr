package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.logic.modifiers.CantBeAssignedAgainstModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CantBeAssignedToSkirmishAgainst implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "fpCharacter", "minion", "side");

        final String filter = FieldUtils.getString(object.get("fpCharacter"), "fpCharacter");
        final String against = FieldUtils.getString(object.get("minion"), "minion");
        final Side side = FieldUtils.getEnum(Side.class, object.get("side"), "side");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final FilterableSource againstSource = environment.getFilterFactory().generateFilter(against, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);
                final Filterable againstFilterable = againstSource.getFilterable(actionContext);
                return new CantBeAssignedAgainstModifier(actionContext.getSource(), side,
                        filterable, RequirementCondition.createCondition(requirements, actionContext), againstFilterable);
            }
        };
    }

}
