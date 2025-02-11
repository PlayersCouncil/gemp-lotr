package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.StringWhileInZoneData;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class AppendCardIdsToWhileInZone implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "memory");

        String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        if (memory == null)
            throw new InvalidCardDefinitionException("'memory' needs to be defined for AppendCardIdsToWhileInZone effect.");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        PhysicalCard.WhileInZoneData value = actionContext.getSource().getWhileInZoneData();
                        String result = "";
                        if (value != null)
                            result = value.getValue();
                        for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(memory)) {
                            result = "," + physicalCard.getCardId();
                        }
                        actionContext.getSource().setWhileInZoneData(new StringWhileInZoneData(result));
                    }
                };
            }
        };
    }
}
