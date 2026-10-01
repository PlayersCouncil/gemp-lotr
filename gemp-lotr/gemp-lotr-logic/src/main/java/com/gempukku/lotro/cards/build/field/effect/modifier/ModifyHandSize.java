package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.logic.modifiers.HandSizeModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import org.json.simple.JSONObject;

/**
 * { type: modifyHandSize, amount: -4 }
 *
 * amount: how much to add to the format's hand size (8 in every format so far).
 * player: whose hand size changes.  Defaults to "you"; "everyone" changes it for all players.
 * requires: the usual conditions.
 */
public class ModifyHandSize implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount", "player", "requires");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(object.get("amount"), environment);
        final String player = FieldUtils.getString(object.get("player"), "player", "you");
        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final PlayerSource playerSource = player.equalsIgnoreCase("everyone") ? null : PlayerResolver.resolvePlayer(player);
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return (actionContext) -> {
            final Evaluator evaluator = valueSource.getEvaluator(actionContext);
            return new HandSizeModifier(actionContext.getSource(),
                    RequirementCondition.createCondition(requirements, actionContext),
                    playerSource == null ? null : () -> playerSource.getPlayer(actionContext),
                    evaluator);
        };
    }
}
