package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;

public class GameTextFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] textLines = FieldUtils.getStringArray(value, key);
        String gametext = String.join("<br>", textLines);
        blueprint.setGameText(gametext);
    }
}
