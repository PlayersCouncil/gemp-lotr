package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.field.effect.EffectForecast;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.CardResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.effects.TransferPermanentEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import org.json.simple.JSONObject;

import java.util.Collection;

public class Transfer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(boolean cost, JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "select", "where", "checkTarget", "fromSupport", "memorizeTransferred", "memorizeTarget");

        final String select = FieldUtils.getString(effectObject.get("select"), "select");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");
        final boolean checkTarget = FieldUtils.getBoolean(effectObject.get("checkTarget"), "checkTarget", false);
        final String memorizeTransferred = FieldUtils.getString(effectObject.get("memorizeTransferred"), "memorizeTransferred", "_temp1");
        final String memorizeTarget = FieldUtils.getString(effectObject.get("memorizeTarget"), "memorizeTarget", "_temp2");
        final boolean fromSupport = FieldUtils.getBoolean(effectObject.get("fromSupport"), "fromSupport", false);

        MultiEffectAppender result = new MultiEffectAppender();

        // A card that something says cannot be transferred (ModifiersQuerying.canBeTransferred: "Possessions cannot
        // be transferred from this companion", Warrior of Rohan 17_104) is never the card taken, whoever it would
        // go to: TransferPermanentEffect would refuse it, after its new bearer had been asked for (F-0340).
        final boolean whereIsFilter = where.startsWith("choose(") || where.startsWith("all(");
        EffectAppender selectResolver = null;
        if (checkTarget && whereIsFilter) {
            // Only offer cards that have at least one eligible bearer to go to; a card with no bearer definition at
            // all (see RuleUtils.getFullValidTargetFilter) is never a legal choice (#1025).
            final FilterableSource whereSource = environment.getFilterFactory().generateFilter(
                    where.substring(where.indexOf("(") + 1, where.lastIndexOf(")")), environment);
            selectResolver = CardResolver.resolveCard(select,
                            actionContext -> Filters.transferableTo(whereSource.getFilterable(actionContext)),
                            memorizeTransferred, "you", "Choose card to transfer", environment);
            result.addEffectAppender(selectResolver);
        } else {
            selectResolver = CardResolver.resolveCard(select,
                            actionContext -> (Filter) (game, candidate) ->
                                    game.getModifiersQuerying().canBeTransferred(game, candidate),
                            memorizeTransferred, "you", "Choose card to transfer", environment);
            result.addEffectAppender(selectResolver);
        }
        result.addEffectAppender(
                CardResolver.resolveCards(where,
                        SpotOverride.NONE,
                        actionContext -> (Filter) (game, physicalCard) -> {
                            final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory(memorizeTransferred);
                            if (transferCard.isEmpty())
                                return false;
                            final PhysicalCard transferredCard = transferCard.iterator().next();
                            // Can't be transferred to card it's already attached to
                            if (transferredCard.getAttachedTo() == physicalCard)
                                return false;
                            // Optionally check target against original target select
                            if (checkTarget && !RuleUtils.getFullValidTargetFilter(transferredCard.getOwner(), game, transferredCard).accepts(game, physicalCard))
                                return false;

                            return actionContext.getGame().getModifiersQuerying().canHaveTransferredOn(game, transferredCard, physicalCard);
                        }, actionContext -> Filters.any,
                        ValueResolver.resolveEvaluator(1, environment), memorizeTarget, "you", "Choose cards to transfer to", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> transferCard = actionContext.getCardsFromMemory(memorizeTransferred);
                        if (transferCard.isEmpty())
                            return null;

                        PhysicalCard transferredCard = transferCard.iterator().next();
                        if (fromSupport && transferredCard.getZone() != Zone.SUPPORT)
                            return null;

                        final PhysicalCard transferredToCard = actionContext.getCardFromMemory(memorizeTarget);
                        if (transferredToCard == null)
                            return null;

                        return new TransferPermanentEffect(transferredCard, transferredToCard);
                    }
                });

        // Q-0224: with no card to transfer, or with nowhere for any such card to go (the bearers the effect itself
        // would offer for it: not the one it is on, an eligible one where "checkTarget" asks for that, and one it
        // may be transferred to), nothing is transferred.
        final EffectAppender cardToTransfer = selectResolver;
        final FilterableSource bearers = !whereIsFilter ? null : environment.getFilterFactory().generateFilter(
                where.substring(where.indexOf("(") + 1, where.lastIndexOf(")")), environment);
        return TaughtForecast.teach(result, actionContext -> {
            final EffectForecast found = cardToTransfer.forecast(actionContext);
            if (found.getReach() == null)
                return EffectForecast.UNKNOWN;
            if (found.getReach().isEmpty())
                return EffectForecast.NOTHING;
            if (bearers == null)
                return EffectForecast.UNKNOWN;
            final LotroGame game = actionContext.getGame();
            for (PhysicalCard card : found.getReach()) {
                final Filter couldBear = (g, bearer) -> bearer != card.getAttachedTo()
                        && (!checkTarget || RuleUtils.getFullValidTargetFilter(card.getOwner(), g, card).accepts(g, bearer))
                        && g.getModifiersQuerying().canHaveTransferredOn(g, card, bearer);
                if (Filters.countActive(game, bearers.getFilterable(actionContext), couldBear) > 0)
                    return EffectForecast.UNKNOWN;
            }
            return EffectForecast.NOTHING;
        });
    }
}
