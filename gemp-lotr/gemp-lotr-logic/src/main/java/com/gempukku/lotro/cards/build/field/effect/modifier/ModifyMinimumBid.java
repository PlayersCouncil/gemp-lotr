package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.modifiers.MinimumBidModifier;
import org.json.simple.JSONObject;

public class ModifyMinimumBid implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "amount", "player");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);
        final String player = FieldUtils.getString(object.get("player"), "player", "owner");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> new MinimumBidModifier(actionContext.getSource(),
                RequirementCondition.createCondition(requirements, actionContext),
                amountSource.getEvaluator(actionContext),
                playerSource.getPlayer(actionContext));
    }
}
