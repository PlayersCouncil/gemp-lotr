package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.DiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "forced", "count", "select", "memorize", "hand", "player");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");
        final String select = FieldUtils.getString(effectObject.get("select"), "select", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(select,
                        actionContext -> Filters.not(actionContext.getSource()),
                        countSource, memorize, player, hand, "Choose cards from hand to discard", true, environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToDiscard = actionContext.getCardsFromMemory(memorize);
                        return new DiscardCardsFromHandEffect(actionContext.getSource(), handSource.getPlayer(actionContext), cardsToDiscard, forced);
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final LotroGame game = actionContext.getGame();

                        final String handPlayer = handSource.getPlayer(actionContext);
                        final String choosingPlayer = playerSource.getPlayer(actionContext);
                        if (!handPlayer.equals(choosingPlayer)
                                && !game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, choosingPlayer))
                            return false;

                        return (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(game, handPlayer, actionContext.getSource()));
                    }
                });

        return result;
    }

}
