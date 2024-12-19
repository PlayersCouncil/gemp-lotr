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
import com.gempukku.lotro.logic.effects.TransferToSupportEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TransferToSupport implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "memorizeBearer");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        String bearerMemory = FieldUtils.getString(effectObject.get("memorizeBearer"), "memorizeBearer");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(select, "_temp1", "you", "Choose card to transfer", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory("_temp1");
                        if (transferCard.isEmpty())
                            return null;

                        PhysicalCard toTransferCard = transferCard.iterator().next();
                        if (bearerMemory != null && toTransferCard.getAttachedTo() != null) {
                            actionContext.setCardMemory(bearerMemory, toTransferCard.getAttachedTo());
                        }

                        return Collections.singletonList(new TransferToSupportEffect(toTransferCard));
                    }
                });

        return result;
    }
}
