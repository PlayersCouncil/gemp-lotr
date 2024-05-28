package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.ReplaceFpCharacterInAssignmentEffect;
import com.gempukku.lotro.logic.timing.DoNothingEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

public class ReplaceInAssignment implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "with");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String with = FieldUtils.getString(effectObject.get("with"), "with");

        MultiEffectAppender result = new MultiEffectAppender();
        result.addEffectAppender(
                CardResolver.resolveCard(select, "_oldAssignee", "you", "Choose assigned character to replace", environment));
        result.addEffectAppender(
                CardResolver.resolveCard(with, "_newAssignee", "you", "Choose character to replace with in assignment", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard oldCard = actionContext.getCardFromMemory("_oldAssignee");
                        final PhysicalCard newCard = actionContext.getCardFromMemory("_newAssignee");
                        if (oldCard != null && newCard != null)
                            return new ReplaceFpCharacterInAssignmentEffect(newCard, oldCard);
                        else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }

}
