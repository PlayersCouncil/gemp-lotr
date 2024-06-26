package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayPermanentAction;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collection;
import java.util.LinkedList;

public class ChooseAndPlayCardFromHandEffect implements Effect {
    private final String _playerId;
    private final boolean _ignoreRoamingPenalty;
    private final boolean _ignoreCheckingDeadPile;
    private final Filter _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;

    public ChooseAndPlayCardFromHandEffect(String playerId, LotroGame game, Filterable... filters) {
        this(playerId, game, 0, false, filters);
    }

    public ChooseAndPlayCardFromHandEffect(String playerId, LotroGame game, int twilightModifier, Filterable... filters) {
        this(playerId, game, twilightModifier, false, filters);
    }

    public ChooseAndPlayCardFromHandEffect(String playerId, LotroGame game, int twilightModifier, boolean ignoreRoamingPenalty, Filterable... filters) {
        this(playerId, game, twilightModifier, ignoreRoamingPenalty, false, filters);
    }

    public ChooseAndPlayCardFromHandEffect(String playerId, LotroGame game, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile, Filterable... filters) {
        _playerId = playerId;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;
        _ignoreCheckingDeadPile = ignoreCheckingDeadPile;
        // Card has to be in hand when you start playing the card (we need to copy the collection)
        _filter = Filters.and(filters, Filters.in(new LinkedList<PhysicalCard>(game.getGameState().getHand(playerId))));
        _twilightModifier = twilightModifier;
    }

    @Override
    public String getText(LotroGame game) {
        return "Play card from hand";
    }

    private Collection<PhysicalCard> getPlayableInHandCards(LotroGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filter, Filters.playable(game, _twilightModifier, _ignoreRoamingPenalty, _ignoreCheckingDeadPile));
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getPlayableInHandCards(game).size() > 0;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public void playEffect(final LotroGame game) {
        Collection<PhysicalCard> playableInHand = getPlayableInHandCards(game);
        if (playableInHand.size() == 1)
            playCard(game, playableInHand.iterator().next());
        else if (playableInHand.size() > 1) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, "Choose a card to play", playableInHand, 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final PhysicalCard selectedCard = getSelectedCardsByResponse(result).iterator().next();
                            playCard(game, selectedCard);
                        }
                    });
        }
    }

    private void playCard(LotroGame game, final PhysicalCard selectedCard) {
        _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, _ignoreRoamingPenalty);
        _playCardAction.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        afterCardPlayed(selectedCard);
                    }
                });
        game.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    protected void afterCardPlayed(PhysicalCard cardPlayed) {
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayPermanentAction)
            return ((PlayPermanentAction) _playCardAction).wasCarriedOut();
        return true;
    }
}
