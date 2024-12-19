package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

public class MemorizeTitle implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "from", "memory");

        final String from = FieldUtils.getString(effectObject.get("from"), "from");
        final String memory = FieldUtils.getString(effectObject.get("memory"), "memory");

        if (from == null)
            throw new InvalidCardDefinitionException("From is required for a MemorizeTitle effect");
        if (memory == null)
            throw new InvalidCardDefinitionException("Memory is required for a MemorizeTitle effect");

        return new DelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        PhysicalCard card = actionContext.getCardFromMemory(from);
                        String title = card != null ? card.getBlueprint().getTitle() : "";
                        actionContext.setValueToMemory(memory, title);
                    }
                };
            }
        };
    }

}
