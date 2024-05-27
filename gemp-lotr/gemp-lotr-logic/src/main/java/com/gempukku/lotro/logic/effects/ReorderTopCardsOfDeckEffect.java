package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReorderTopCardsOfDeckEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final String _deckId;
    private final int _count;

    public ReorderTopCardsOfDeckEffect(Action action, String playerId, String deckId, int count) {
        _action = action;
        _playerId = playerId;
        _deckId = deckId;
        _count = count;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getDeck(_deckId).size() >= _count;
    }

    @Override
    public void playEffect(LotroGame game) {
        final List<? extends PhysicalCard> deck = game.getGameState().getDeck(_deckId);
        int count = Math.min(deck.size(), _count);
        Set<PhysicalCard> cards = new HashSet<>(deck.subList(0, count));

        game.getGameState().sendMessage(_playerId + " reorders top " + count + " cards of draw deck");

        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(
                new ChooseAndPutNextCardFromDeckOnTopOfDeck(subAction, cards));
        processSubAction(game, subAction);
    }

    private class ChooseAndPutNextCardFromDeckOnTopOfDeck extends ChooseArbitraryCardsEffect {
        private final Collection<PhysicalCard> _remainingCards;
        private final CostToEffectAction _subAction;

        public ChooseAndPutNextCardFromDeckOnTopOfDeck(CostToEffectAction subAction, Collection<PhysicalCard> remainingCards) {
            super(_playerId, "Choose a card to put on top of the deck", remainingCards, 1, 1);
            _subAction = subAction;
            _remainingCards = remainingCards;
        }

        @Override
        protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
            for (PhysicalCard selectedCard : selectedCards) {
                _subAction.appendEffect(
                        new PutCardFromDeckOnTopOfDeckEffect(_playerId, selectedCard, false));
                _remainingCards.remove(selectedCard);
                if (_remainingCards.size() > 0)
                    _subAction.appendEffect(
                            new ChooseAndPutNextCardFromDeckOnTopOfDeck(_subAction, _remainingCards));
            }
        }
    }

}
