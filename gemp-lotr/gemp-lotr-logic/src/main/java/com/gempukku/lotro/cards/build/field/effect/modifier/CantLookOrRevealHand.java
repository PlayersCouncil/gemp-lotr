package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.AbstractModifier;
import com.gempukku.lotro.logic.modifiers.ModifierEffect;
import org.json.simple.JSONObject;

public class CantLookOrRevealHand implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "hand", "requires");

        final String player = FieldUtils.getString(object.get("player"), "player");
        final String hand = FieldUtils.getString(object.get("hand"), "hand");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new AbstractModifier(actionContext.getSource(), "Player may not look at or reveal cards in another player hand",
                null, RequirementCondition.createCondition(requirements, actionContext), ModifierEffect.LOOK_OR_REVEAL_MODIFIER) {
            @Override
            public boolean canLookOrRevealCardsInHand(LotroGame game, String revealingPlayerId, String actingPlayerId) {
                if (playerSource.getPlayer(actionContext).equals(actingPlayerId)
                        && handSource.getPlayer(actionContext).equals(revealingPlayerId))
                    return false;
                return true;
            }
        };
    }
}
