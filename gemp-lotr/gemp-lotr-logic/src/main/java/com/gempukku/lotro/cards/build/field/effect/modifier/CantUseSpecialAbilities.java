package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.modifiers.CantUseSpecialAbilitiesModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CantUseSpecialAbilities implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "phase", "requires", "filter");

        String player = FieldUtils.getString(object.get("player"), "player");
        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        String filter = FieldUtils.getString(object.get("filter"), "filter", "any");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player) : null;
        FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                String bannedPlayer = (playerSource != null) ? playerSource.getPlayer(actionContext) : null;

                return new CantUseSpecialAbilitiesModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext), phase, bannedPlayer, filterableSource.getFilterable(actionContext));
            }
        };
    }
}
