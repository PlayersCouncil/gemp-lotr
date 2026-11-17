package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

public class EventEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "cost", "effect", "requires", "noEffectUnless", "requiresRanger");

        final boolean requiresRanger = FieldUtils.getBoolean(value.get("requiresRanger"), "requiresRanger", false);

        DefaultActionSource actionSource = new DefaultActionSource();
        actionSource.setRequiresRanger(requiresRanger);
        actionSource.setEvent(true);

        EffectUtils.processRequirementsCostsAndEffects(value, environment, actionSource);

        blueprint.setPlayEventAction(actionSource);
    }
}
