package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.RevealHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RevealHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "memorize");

        final String player = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize");

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final LotroGame game = actionContext.getGame();
                final String revealingPlayer = playerSource.getPlayer(actionContext);
                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, revealingPlayer, actionContext.getPerformingPlayer());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String revealingPlayer = playerSource.getPlayer(actionContext);
                return new RevealHandEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), revealingPlayer) {
                    @Override
                    protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {
                        if (memorize != null)
                            actionContext.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}
