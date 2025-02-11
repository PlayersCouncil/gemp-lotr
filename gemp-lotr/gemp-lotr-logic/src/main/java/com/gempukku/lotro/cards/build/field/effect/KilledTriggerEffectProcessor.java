package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

public class KilledTriggerEffectProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "optional", "player", "requires", "cost", "effect");

        final boolean optional = FieldUtils.getBoolean(value.get("optional"), "optional", false);

        final String player = FieldUtils.getString(value.get("player"), "player");
        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        DefaultActionSource triggerActionSource = new DefaultActionSource();
        if (playerSource != null) {
            triggerActionSource.setPlayingPlayer(playerSource);
        }

        EffectUtils.processRequirementsCostsAndEffects(value, environment, triggerActionSource);
        if (optional)
            blueprint.setKilledOptionalTriggerAction(triggerActionSource);
        else
            blueprint.setKilledRequiredTriggerAction(triggerActionSource);
    }
}
