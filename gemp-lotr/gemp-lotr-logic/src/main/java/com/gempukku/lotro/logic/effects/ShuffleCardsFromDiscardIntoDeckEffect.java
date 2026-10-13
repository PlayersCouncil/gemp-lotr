package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShuffleCardsFromDiscardIntoDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerDeck;
    private final Collection<? extends PhysicalCard> _cards;

    public ShuffleCardsFromDiscardIntoDeckEffect(PhysicalCard source, String playerDeck, Collection<? extends PhysicalCard> cards) {
        _source = source;
        _playerDeck = playerDeck;
        _cards = cards;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        for (PhysicalCard card : _cards) {
            if (card.getZone() != Zone.DISCARD)
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        Set<PhysicalCard> toShuffleIn = new HashSet<>();
        for (PhysicalCard card : _cards) {
            if (card.getZone() == Zone.DISCARD)
                toShuffleIn.add(card);
        }

        if (toShuffleIn.size() > 0) {
            game.getGameState().removeCardsFromZone(_source.getOwner(), toShuffleIn);

            game.getGameState().shuffleCardsIntoDeck(toShuffleIn, _playerDeck);

            game.getGameState().sendMessage(getAppendedNames(toShuffleIn) + " " + GameUtils.be(toShuffleIn) + " shuffled into " + _playerDeck + " deck");
        } else {
            // The deck is shuffled though nothing is shuffled into it: "it's more consistent for the shuffle to
            // always happen even if you're shuffling 'in' nothing" (Ketura on Q-0102; F-0178).  So "you may
            // shuffle up to 2 ... from your discard pile into your draw deck" always does something.
            game.getGameState().shuffleDeck(_playerDeck);

            game.getGameState().sendMessage(_playerDeck + " deck is shuffled with no card shuffled into it");
        }

        return new FullEffectResult(toShuffleIn.size() == _cards.size());
    }
}
