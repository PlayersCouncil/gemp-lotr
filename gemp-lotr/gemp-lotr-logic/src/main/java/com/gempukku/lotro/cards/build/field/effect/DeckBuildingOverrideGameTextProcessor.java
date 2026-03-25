package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.game.DeckValidationContext;
import org.json.simple.JSONObject;

public class DeckBuildingOverrideGameTextProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject effectObj, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObj, "maximumSameName", "skipSiteBlockValidation");

        var context = new DeckValidationContext();

        final Object maxSameName = effectObj.get("maximumSameName");
        if (maxSameName != null) {
            context.setMaximumSameNameOverride(FieldUtils.getInteger(maxSameName, "maximumSameName"));
        }

        final Object skipSiteBlock = effectObj.get("skipSiteBlockValidation");
        if (skipSiteBlock != null) {
            context.setSkipSiteBlockValidation((Boolean) skipSiteBlock);
        }

        blueprint.setDeckBuildingOverrides(context);
    }
}
