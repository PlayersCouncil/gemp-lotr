package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.CardStyle;

public class StyleFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        blueprint.getCardInfo().style = CardStyle.Parse(FieldUtils.getString(value, key));
    }
}
