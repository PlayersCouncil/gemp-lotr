package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectUtils;
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
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
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

        // A forced discard from a hand its owner is protected from ("Shadow cards may not discard cards from your
        // hand", Frodo, Courteous Halfling 4_301 and Frodo, Little Master 19_28) has no card to be chosen: none of
        // them can be discarded, so nobody is asked for one (F-0514, F-0116), as ChooseAndDiscardCardsFromHandEffect
        // already asks nothing.  The bar is read as the choice comes up, which is where DiscardCardsFromHandEffect
        // would read it.
        EffectAppender cardResolver = CardResolver.resolveCardsInHand(select,
                actionContext -> isBarred(actionContext, forced, handSource) ? Filters.none : Filters.not(actionContext.getSource()),
                countSource, memorize, player, hand, "Choose cards from hand to discard", true, environment);

        EffectUtils.validatePreEvaluate(cost, effectObject, handSource, playerSource, countSource, cardResolver);

        result.addEffectAppender(
                cardResolver);
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

                        return !isBarred(actionContext, forced, handSource);
                    }
                });

        // Taught for Q-0224 (IdleOffers): with no card in that hand for the choice to be made from, nothing is
        // discarded ("you may discard up to 2 cards from hand", the hand empty), nor from a hand this card may not
        // discard from (Q-0231, F-0514): what DiscardCardsFromHandEffect itself asks before it discards, as
        // DiscardCardAtRandomFromHand's forecast knows it.  The bar is no secret of the hand's, so it is said of
        // another player's hand too.  A choice it is a branch of hears that, except of the whole hand: "discard
        // your hand" with none in it is a real choice (Ketura on Q-0255, Pelennor Flat 7_345).  What the bar adds
        // here is exercised by no printed card (engine pass 46): the two cards that lay it bar Shadow cards, and no
        // Shadow card's forced discard from hand is an optional trigger or ability that pays nothing, or a branch
        // of a choice (1_120, 2_91, 3_87, 3_104, 12_80, 31_36 are events, required triggers or paid abilities).
        final EffectAppender taught = TaughtForecast.actsOnWhatItFindsInHand(result, handSource, cardResolver,
                actionContext -> isBarred(actionContext, forced, handSource));
        return select.toLowerCase().startsWith("all(") ? TaughtForecast.forOffersOnly(taught) : taught;
    }

    // Whether a forced discard from the hand is barred for the card doing it (canDiscardCardsFromHand).  A discard
    // the player chooses to make is never barred (CR 4.2 line 2480: "If a Shadow card gives you a choice, you may
    // choose to discard"; Q-0171, F-0205).
    private static boolean isBarred(ActionContext actionContext, boolean forced, PlayerSource handSource) {
        if (!forced)
            return false;
        final LotroGame game = actionContext.getGame();
        return !game.getModifiersQuerying().canDiscardCardsFromHand(game, handSource.getPlayer(actionContext), actionContext.getSource());
    }

}
