package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ResolveSkirmishDamageAction;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.ForEachKilledResult;
import com.gempukku.lotro.logic.timing.results.WoundResult;
import org.json.simple.JSONObject;

public class KilledWithSurplusDamage implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "memorize");

        final String memory = FieldUtils.getString(object.get("memorize"), "memorize");

        return new Requirement() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                LotroGame game = actionContext.getGame();
                final ResolveSkirmishDamageAction resolveSkirmishDamageAction = game.getActionsEnvironment().findTopmostActionOfType(ResolveSkirmishDamageAction.class);
                if (resolveSkirmishDamageAction == null)
                    return false;
                // The wounds that the character whose kill is being answered did not take, and no other loser's
                final int remainingDamage = resolveSkirmishDamageAction.getRemainingDamage(getKilledCard(actionContext));
                if (remainingDamage > 0) {
                    if (memory != null) {
                        actionContext.setValueToMemory(memory, String.valueOf(remainingDamage));
                    }

                    return true;
                }
                return false;
            }
        };
    }

    /**
     * @return The character whose kill the action is answering, or null if it answers none.  A kill by wounds is
     * answered at the wound that killed (see TriggerConditions.forEachKilledBy), any other at the kill itself.
     */
    public static PhysicalCard getKilledCard(ActionContext actionContext) {
        final EffectResult effectResult = actionContext.getEffectResult();
        if (effectResult instanceof ForEachKilledResult killedResult)
            return killedResult.getKilledCard();
        if (effectResult instanceof WoundResult woundResult)
            return woundResult.getWoundedCard();
        return null;
    }
}
