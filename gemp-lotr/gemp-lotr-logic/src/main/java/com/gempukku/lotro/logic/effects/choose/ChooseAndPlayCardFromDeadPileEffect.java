package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.PlayPermanentAction;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChooseAndPlayCardFromDeadPileEffect implements Effect {
    private final String _playerId;
    private final Filter _filter;
    private final int _twilightModifier;
    private Action _playCardAction;

    public ChooseAndPlayCardFromDeadPileEffect(String playerId, LotroGame game, Filterable... filter) {
        this(playerId, game, 0, filter);
    }

    public ChooseAndPlayCardFromDeadPileEffect(String playerId, LotroGame game, int twilightModifier, Filterable... filter) {
        _playerId = playerId;
        // Card has to be in dead pile when you start playing the card (we need to copy the collection)
        _filter = Filters.and(filter, Filters.in(new LinkedList<PhysicalCard>(game.getGameState().getDeadPile(playerId))));
        _twilightModifier = twilightModifier;
    }

    @Override
    public String getText(LotroGame game) {
        return "Play card from dead pile";
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getPlayableInDeadPile(game).size() > 0;
    }

    @Override
    public Type getType() {
        return null;
    }

    private Collection<PhysicalCard> getPlayableInDeadPile(LotroGame game) {
        return Filters.filter(game.getGameState().getDeadPile(_playerId), game, _filter, Filters.playable(game, _twilightModifier, false, true));
    }

    @Override
    public void playEffect(final LotroGame game) {
        Collection<PhysicalCard> deadPile = getPlayableInDeadPile(game);
        if (deadPile.size() > 0) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose a card to play", new LinkedList<>(deadPile), 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (selectedCards.size() > 0) {
                                PhysicalCard selectedCard = selectedCards.get(0);
                                _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, false);
                                game.getActionsEnvironment().addActionToStack(_playCardAction);
                            }
                        }
                    });
        }
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