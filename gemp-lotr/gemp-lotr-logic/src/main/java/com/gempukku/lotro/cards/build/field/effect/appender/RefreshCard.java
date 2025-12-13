package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RefreshCard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select");

        final String select = FieldUtils.getString(effectObject.get("select"), "select", "self");

        MultiEffectAppender result = new MultiEffectAppender();

        EffectAppender cardResolver = CardResolver.resolveCards(select,actionContext -> new ConstantEvaluator(1),
                "_temp", "you", "Choose card to refresh", environment);

        //This is intended to be used with cards using CopyCard, such as Hobbit Farmer.
        // This very well might have side-effects including removing modifiers applied this turn.
        result.addEffectAppender(
                cardResolver);
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext ac) {
                        return new UnrespondableEffect() {
                            @Override
                            protected void doPlayEffect(LotroGame game) {
                                final Collection<? extends PhysicalCard> cardsFromMemory = ac.getCardsFromMemory("_temp");
                                if(cardsFromMemory != null && !cardsFromMemory.isEmpty()) {
                                    ac.getGame().getGameState().reapplyAffectingForCard(ac.getGame(), cardsFromMemory.stream().findFirst().get());
                                }

                            }
                        };
                    }
                }
        );

        return result;
    }

}
