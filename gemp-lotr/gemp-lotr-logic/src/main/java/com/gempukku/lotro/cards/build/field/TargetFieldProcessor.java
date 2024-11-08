package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.*;

public class TargetFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String target = FieldUtils.getString(value, key);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(target, environment);
        blueprint.setTargetFilter(filterableSource);
    }
}
