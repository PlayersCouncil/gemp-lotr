package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;

public class UniqueFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        int uniqueness = FieldUtils.getUniqueness(value, key);

        if(uniqueness < 1 || uniqueness > 4)
            throw new InvalidCardDefinitionException("Uniqueness must be either true/false or a number from 1-4");

        blueprint.setUniqueness(uniqueness);
    }
}
