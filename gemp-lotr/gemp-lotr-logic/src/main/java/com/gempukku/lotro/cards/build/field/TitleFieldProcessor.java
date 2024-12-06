package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;

public class TitleFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        String title = FieldUtils.getString(value, key);
        if (title.startsWith("*"))
            throw new InvalidCardDefinitionException("Do not use '*' to indicate uniqueness.  Use the 'unique' boolean field instead.");

        blueprint.setTitle(title);
    }
}
