package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.modifiers.CantPlaySiteModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CantPlaySite implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "player");

        final String player = FieldUtils.getString(object.get("player"), "player");
        if (player == null)
            throw new InvalidCardDefinitionException("Player is required for \"can't play site\" modifier");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                String bannedPlayer = playerSource.getPlayer(actionContext);
                return new CantPlaySiteModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext), bannedPlayer);
            }
        };
    }
}
