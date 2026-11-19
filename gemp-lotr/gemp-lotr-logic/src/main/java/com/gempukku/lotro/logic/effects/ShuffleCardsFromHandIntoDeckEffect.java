package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShuffleCardsFromHandIntoDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerDeck;
    private final Collection<? extends PhysicalCard> _cards;

    public ShuffleCardsFromHandIntoDeckEffect(PhysicalCard source, String playerDeck, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _playerDeck = playerDeck;
        _cards = cards;
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
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.HAND)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        Set<PhysicalCard> toShuffleIn = new HashSet<>();
        for (PhysicalCard card : _cards) {
            if (card.getZone() == Zone.HAND)
                toShuffleIn.add(card);
        }

        if (toShuffleIn.size() > 0) {
            game.getGameState().removeCardsFromZone(_source.getOwner(), toShuffleIn);

            game.getGameState().shuffleCardsIntoDeck(toShuffleIn, _playerDeck);

            game.getGameState().sendMessage(getAppendedNames(toShuffleIn) + " shuffled cards from hand into " + _playerDeck + "'s deck");
        } else {
            // As from the discard pile (ShuffleCardsFromDiscardIntoDeckEffect; Ketura on Q-0102), and as a whole
            // hand, which is shuffled in though it is empty: the deck is shuffled with nothing shuffled into it.
            game.getGameState().shuffleDeck(_playerDeck);

            game.getGameState().sendMessage(_playerDeck + " deck is shuffled with no card shuffled into it");
        }

        return new FullEffectResult(toShuffleIn.size() == _cards.size());
    }
}
