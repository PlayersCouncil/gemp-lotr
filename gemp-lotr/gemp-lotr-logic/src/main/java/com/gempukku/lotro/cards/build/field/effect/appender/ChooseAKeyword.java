package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChooseAKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize", "keywords", "text");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String[] keywordStrings = FieldUtils.getStringArray(effectObject.get("keywords"), "keywords");
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose a keyword");

        for (String key : keywordStrings) {
            var keyword = FieldUtils.getEnum(Keyword.class, key, "keyword");
            if(keyword == null)
                throw new InvalidCardDefinitionException("Invalid keyword defined in 'keywords' field of ChooseAKeyword effect.");
        }

        final String[] keywords = ArrayUtils.isEmpty(keywordStrings) ? getAllKeywords() : keywordStrings;


        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {

                return new PlayoutDecisionEffect(
                        actionContext.getPerformingPlayer(),
                        new MultipleChoiceAwaitingDecision(1, text, keywords) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                actionContext.setValueToMemory(memorize, result.toUpperCase().replace(' ', '_').replace('-', '_'));
                                actionContext.getGame().getGameState().sendMessage(actionContext.getPerformingPlayer() + " has chosen " + result);
                            }
                        });
            }
        };
    }

    private String[] getAllKeywords() {
        List<String> list = new ArrayList<>();
        for (Keyword value : Keyword.values()) {
            if (value.isRealKeyword())
                list.add(value.toString());
        }
        return list.toArray(new String[0]);
    }
}
