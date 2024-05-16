package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ResolveSkirmishDamageAction;
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
                if (resolveSkirmishDamageAction != null
                        && resolveSkirmishDamageAction.getRemainingDamage() > 0) {
                    if (memory != null) {
                        actionContext.setValueToMemory(memory, String.valueOf(resolveSkirmishDamageAction.getRemainingDamage()));
                    }

                    return true;
                }
                return false;
            }
        };
    }
}
