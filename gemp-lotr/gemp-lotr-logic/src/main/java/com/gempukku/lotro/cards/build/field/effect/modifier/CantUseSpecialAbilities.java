package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.modifiers.PlayerCantUseCardSpecialAbilitiesModifier;
import com.gempukku.lotro.logic.modifiers.PlayersCantUseCardSpecialAbilitiesModifier;
import org.json.simple.JSONObject;

public class CantUseSpecialAbilities implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "player");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
        final String player = FieldUtils.getString(object.get("player"), "player");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player, environment) : null;

        return actionContext -> {
            if (playerSource != null) {
                return new PlayerCantUseCardSpecialAbilitiesModifier(actionContext.getSource(),
                        playerSource.getPlayer(actionContext),
                        RequirementCondition.createCondition(requirements, actionContext), filterableSource.getFilterable(actionContext));
            } else {
                return new PlayersCantUseCardSpecialAbilitiesModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext), filterableSource.getFilterable(actionContext));
            }
        };
    }
}
