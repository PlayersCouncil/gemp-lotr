package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SanctuaryMayRemoveBurdensModifier;
import org.json.simple.JSONObject;

public class SanctuaryMayRemoveBurdens implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "player");

        final String player = FieldUtils.getString(object.get("player"), "player", "owner");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new SanctuaryMayRemoveBurdensModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext),
                        playerSource.getPlayer(actionContext));
            }
        };
    }
}
