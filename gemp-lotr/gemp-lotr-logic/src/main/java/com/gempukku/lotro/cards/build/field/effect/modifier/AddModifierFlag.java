package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.SpecialFlagModifier;
import org.json.simple.JSONObject;

public class AddModifierFlag implements ModifierSourceProducer {
    private ModifierFlag modifierFlag;

    public AddModifierFlag(ModifierFlag modifierFlag) {
        this.modifierFlag = modifierFlag;
    }

    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object,"requires", "player");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        // Without "player" the flag is global, as it has always been.
        final String player = FieldUtils.getString(object.get("player"), "player");
        final PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                return new SpecialFlagModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext), modifierFlag,
                        playerSource != null ? playerSource.getPlayer(actionContext) : null);
            }
        };
    }
}
