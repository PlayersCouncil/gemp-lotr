package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CardInfoFieldProcessor implements FieldProcessor {

    private final Map<String, FieldProcessor> fieldProcessors = new HashMap<>();

    public CardInfoFieldProcessor() {
        fieldProcessors.put("language", new LanguageFieldProcessor());
        fieldProcessors.put("style", new StyleFieldProcessor());
        fieldProcessors.put("revision", new RevisionFieldProcessor());
        fieldProcessors.put("image", new ImageFieldProcessor());
        fieldProcessors.put("collinfo", new CollInfoFieldProcessor());
        fieldProcessors.put("rarity", new RarityFieldProcessor());
        fieldProcessors.put("variant", new VariantFieldProcessor());
        fieldProcessors.put("parent", new ParentFieldProcessor());
        fieldProcessors.put("variantpath", new VariantPathFieldProcessor());
    }

    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {

        if (!(value instanceof JSONObject info))
            throw new InvalidCardDefinitionException("Card Info is not a valid object.");

        Set<Map.Entry<String, Object>> infoFields = info.entrySet();
        for (Map.Entry<String, Object> pair : infoFields) {
            final String field = pair.getKey().toLowerCase();
            final Object fieldValue = pair.getValue();
            final FieldProcessor fieldProcessor = fieldProcessors.get(field);
            if (fieldProcessor == null)
                throw new InvalidCardDefinitionException("Unrecognized card info field: " + field);

            fieldProcessor.processField(field, fieldValue, blueprint, environment);
        }
    }
}
