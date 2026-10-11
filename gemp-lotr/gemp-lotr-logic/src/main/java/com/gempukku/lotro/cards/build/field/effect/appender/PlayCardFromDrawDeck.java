package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayPermanentAction;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.RangeEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.ExtraFilters;
import com.gempukku.lotro.logic.timing.FailedEffect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class PlayCardFromDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "on", "discount", "mustExist", "memorize", "shuffle", "showAll");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String onFilter = FieldUtils.getString(effectObject.get("on"), "on");
        final ValueSource costModifierSource = ValueResolver.resolveEvaluator(effectObject.get("discount"), 0, environment);
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle");
        boolean showAll = FieldUtils.getBoolean(effectObject.get("showAll"), "showAll");

        final boolean mustExist = FieldUtils.getBoolean(effectObject.get("mustExist"), "mustExist", false);

        // If the card is not in the deck, you can still attempt this action to no effect.
        // If the card is in the deck, you can choose to not play it.
        final ValueSource countSource = actionContext -> new RangeEvaluator(0, 1);

        final FilterableSource onFilterableSource = (onFilter != null) ? environment.getFilterFactory().generateFilter(onFilter, environment) : null;

        final FilterableSource playableFilterSource = (actionContext) -> {
            final LotroGame game = actionContext.getGame();
            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(game, actionContext.getSource());
            if (onFilterableSource != null) {
                final Filterable onFilterable = onFilterableSource.getFilterable(actionContext);
                return Filters.and(Filters.playable(costModifier), ExtraFilters.attachableTo(game, costModifier, onFilterable));
            }
            return Filters.playable(costModifier, 0, false, false, true);
        };

        // HOWEVER, if we are in a situation where a card can be played "from draw deck or discard pile", we
        // DO NOT WANT to pretend that there is a card in the draw deck if it's not actually there.  Else the
        // player will be offered a "play from draw deck" button that lies to them and will cause the effect to
        // fizzle.  mustExist therefore asks that the deck hold such a card before the effect is offered.  It
        // does not make the player play one once the deck is being looked through: that choice stays 0 to 1
        // ("There is no penalty if you ... choose not to play a card you are looking for").  This appender is
        // only ever asked whether it could be carried out; it is never added to the result.
        final EffectAppender existenceCheck = !mustExist ? null :
                CardResolver.resolveCardsInDeck(select, playableFilterSource,
                        actionContext -> new ConstantEvaluator(1), memorize, "you", "you", showAll, "Choose card to play", environment);

        // As a branch of a choice: a deck may always be looked through for the card, so the play is certain to do
        // nothing only where the deck is asked to hold such a card (mustExist) and holds none.
        MultiEffectAppender result = new MultiEffectAppender() {
            @Override
            public boolean isCertainToDoNothingIfChosen(ActionContext actionContext) {
                return existenceCheck != null && EffectForecast.findsNoCardNow(actionContext, existenceCheck);
            }
        };
        result.setPlayabilityCheckedForEffect(true);

        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(select, playableFilterSource,
                        countSource, memorize, "you", "you", showAll, "Choose card to play", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToPlay = actionContext.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            final int costModifier = costModifierSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), actionContext.getSource());

                            Filterable onFilterable = (onFilterableSource != null) ? onFilterableSource.getFilterable(actionContext) : Filters.any;

                            final CostToEffectAction playCardAction = PlayUtils.getPlayCardAction(actionContext.getGame(), cardsToPlay.iterator().next(), costModifier, onFilterable, false);
                            // showAll is the player looking through the whole deck for the card.  A card that goes
                            // onto a bearer and an event shuffle the deck they leave in any case; a card played to
                            // the table or the support area does so only when told the deck was looked through.
                            if (showAll && playCardAction instanceof PlayPermanentAction playPermanentAction)
                                playPermanentAction.setDeckLookedThrough();
                            return new StackActionEffect(playCardAction);
                        } else {
                            // This will notify any effect that uses playing from deck as a cost, that the cost was not "paid"
                            return new FailedEffect();
                        }
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        final LotroGame game = actionContext.getGame();
                        if (existenceCheck != null && !existenceCheck.isPlayableInFull(actionContext))
                            return false;
                        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK,
                                actionContext.getPerformingPlayer());
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() {
                        return true;
                    }
                });
        if (shuffle)
            result.addEffectAppender(
                    new DelayedAppender() {
                        @Override
                        protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                            return new ShuffleDeckEffect(actionContext.getPerformingPlayer());
                        }
                    });

        return result;
    }
}
