package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.logic.modifiers.CantHealModifier;
import org.json.simple.JSONObject;

public class CantHeal implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "filter", "requires", "player");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final String filter = FieldUtils.getString(object.get("filter"), "filter", "character");
        // The player who may not heal the cards; with none named, nobody may.
        final String player = FieldUtils.getString(object.get("player"), "player");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);
        final PlayerSource playerSource = player != null ? PlayerResolver.resolvePlayer(player) : null;

        return (actionContext) -> new CantHealModifier(actionContext.getSource(),
                RequirementCondition.createCondition(requirements, actionContext),
                playerSource != null ? playerSource.getPlayer(actionContext) : null,
                filterableSource.getFilterable(actionContext));
    }
}
