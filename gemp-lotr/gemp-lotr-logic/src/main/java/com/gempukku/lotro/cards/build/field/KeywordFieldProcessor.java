package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Names;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class KeywordFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] keywords = FieldUtils.getStringArray(value, key);
        blueprint.setKeywords(convertKeywords(keywords, key));
    }

    private static final Pattern numericKeywordPattern = Pattern.compile("([a-zA-Z-_ ]+?)([ +]*)(\\d+)");

    private static Map<Keyword, Integer> convertKeywords(String[] keywords, String key) throws InvalidCardDefinitionException {
        Map<Keyword, Integer> result = new HashMap<>();
        for (String keywordStr : keywords) {
            var match = numericKeywordPattern.matcher(keywordStr);

            if(match.matches()) {
                Keyword keyword = FieldUtils.getEnum(Keyword.class, match.group(1), key);
                int value = Integer.parseInt(match.group(3));

                result.put(keyword, value);
            }
            else {
                Keyword keyword = FieldUtils.getEnum(Keyword.class, keywordStr, key);
                result.put(keyword, 1);
            }
        }
        return result;
    }

}
