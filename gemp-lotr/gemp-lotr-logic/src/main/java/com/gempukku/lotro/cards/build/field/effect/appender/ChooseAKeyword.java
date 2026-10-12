package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
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
        FieldUtils.validateAllowedFields(effectObject, "memorize", "keywords", "text", "fromCard", "noneOption");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");
        final String[] keywordStrings = FieldUtils.getStringArray(effectObject.get("keywords"), "keywords");
        final String text = FieldUtils.getString(effectObject.get("text"), "text", "Choose a keyword");
        // "fromCard": the game text keywords that card has now, in place of a list (Into the Caves 17_100; Q-0282).
        // "noneOption": an answer of that text that names no keyword, memorized as "". With "fromCard" and no
        // keyword on the card, nothing is asked and "" is memorized.
        final String fromCard = FieldUtils.getString(effectObject.get("fromCard"), "fromCard");
        final String noneOption = FieldUtils.getString(effectObject.get("noneOption"), "noneOption");
        if (fromCard != null && !ArrayUtils.isEmpty(keywordStrings))
            throw new InvalidCardDefinitionException("ChooseAKeyword takes either 'keywords' or 'fromCard', not both");
        final FilterableSource fromCardSource = fromCard == null ? null
                : environment.getFilterFactory().generateFilter(fromCard, environment);

        for (String key : keywordStrings) {
            var keyword = FieldUtils.getEnum(Keyword.class, key, "keyword");
            if(keyword == null)
                throw new InvalidCardDefinitionException("Invalid keyword defined in 'keywords' field of ChooseAKeyword effect.");
        }

        final String[] keywords = ArrayUtils.isEmpty(keywordStrings) ? getAllKeywords() : keywordStrings;


        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {

                final List<String> options = new ArrayList<>(List.of(
                        fromCardSource == null ? keywords : getKeywordsOfCards(actionContext, fromCardSource)));
                if (options.isEmpty()) {
                    actionContext.setValueToMemory(memorize, "");
                    return null;
                }
                if (noneOption != null)
                    options.add(noneOption);

                return new PlayoutDecisionEffect(
                        actionContext.getPerformingPlayer(),
                        new MultipleChoiceAwaitingDecision(1, text, options.toArray(new String[0])) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                if (noneOption != null && index == options.size() - 1)
                                    actionContext.setValueToMemory(memorize, "");
                                else
                                    actionContext.setValueToMemory(memorize, result.toUpperCase().replace(' ', '_').replace('-', '_'));
                                actionContext.getGame().getGameState().sendMessage(actionContext.getPerformingPlayer() + " has chosen " + result);
                            }
                        });
            }
        };
    }

    private String[] getKeywordsOfCards(ActionContext actionContext, FilterableSource fromCardSource) {
        final var game = actionContext.getGame();
        List<String> list = new ArrayList<>();
        final var cards = Filters.filterActive(game, fromCardSource.getFilterable(actionContext));
        for (Keyword value : Keyword.values()) {
            if (!value.isRealKeyword())
                continue;
            for (PhysicalCard card : cards) {
                if (game.getModifiersQuerying().hasKeyword(game, card, value)) {
                    list.add(value.toString());
                    break;
                }
            }
        }
        return list.toArray(new String[0]);
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
