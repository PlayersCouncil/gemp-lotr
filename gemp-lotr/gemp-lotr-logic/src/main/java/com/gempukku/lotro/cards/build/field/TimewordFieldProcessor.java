package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Timeword;

import java.util.HashSet;
import java.util.Set;

public class TimewordFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] timewords = FieldUtils.getStringArray(value, key);
        blueprint.setTimewords(convertTimewords(timewords, key));
    }

    private static Set<Timeword> convertTimewords(String[] timewords, String key) throws InvalidCardDefinitionException {
        Set<Timeword> result = new HashSet<>();
        for (String timewordStr : timewords) {
            Timeword timeword = FieldUtils.getEnum(Timeword.class, timewordStr, key);
            result.add(timeword);
        }
        return result;
    }
}
