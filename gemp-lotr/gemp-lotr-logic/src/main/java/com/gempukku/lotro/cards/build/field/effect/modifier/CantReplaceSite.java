package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.modifiers.CantReplaceSiteModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import org.json.simple.JSONObject;

public class CantReplaceSite implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "player");

        final String filter = FieldUtils.getString(object.get("filter"), "filter", "any");
        final String player = FieldUtils.getString(object.get("player"), "player");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player) : null;
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return new ModifierSource() {
            @Override
            public Modifier getModifier(ActionContext actionContext) {
                String bannedPlayer = playerSource != null ? playerSource.getPlayer(actionContext) : null;
                return new CantReplaceSiteModifier(actionContext.getSource(),
                        RequirementCondition.createCondition(requirements, actionContext), bannedPlayer,
                        filterableSource.getFilterable(actionContext));
            }
        };
    }
}
