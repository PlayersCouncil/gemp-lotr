package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.logic.modifiers.RaceSpotModifier;
import org.json.simple.JSONObject;

public class ModifyRaceSpotCount implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "race");

        Race race = FieldUtils.getEnum(Race.class, object.get("race"), "race");
        if (race == null)
            throw new InvalidCardDefinitionException("Race is required for \"modify race spot count\"");

        return (actionContext) -> new RaceSpotModifier(actionContext.getSource(), race);
    }
}
