package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Set;
import java.util.TreeSet;

public class ChooseACulture implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memorize");

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final Set<String> possibleCultures = new TreeSet<>();
                for (Culture culture : Culture.values()) {
                    possibleCultures.add(culture.getHumanReadable());
                }

                return new PlayoutDecisionEffect(
                        actionContext.getPerformingPlayer(),
                        new MultipleChoiceAwaitingDecision(1, "Choose a culture",
                                possibleCultures.toArray(new String[0])) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                actionContext.setValueToMemory(memorize, Culture.findCultureByHumanReadable(result).toString());
                                actionContext.getGame().getGameState().sendMessage(actionContext.getPerformingPlayer() + " has chosen " + result);
                            }
                        });
            }
        };
    }
}
