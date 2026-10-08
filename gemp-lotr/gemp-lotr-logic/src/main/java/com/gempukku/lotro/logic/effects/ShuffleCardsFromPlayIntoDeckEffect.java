package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ShuffleCardsFromPlayIntoDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerDeck;
    private final Collection<? extends PhysicalCard> _cards;

    public ShuffleCardsFromPlayIntoDeckEffect(PhysicalCard source, String playerDeck, Collection<? extends PhysicalCard> cards) {
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
            if (!card.getZone().isInPlay())
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        Set<PhysicalCard> goingToDiscard = new HashSet<>();
        Set<PhysicalCard> discardedFromPlay = new HashSet<>();
        Set<PhysicalCard> toShuffleIn = new HashSet<>();

        for (PhysicalCard card : _cards) {
            if (card.getZone().isInPlay()) {
                toShuffleIn.add(card);
            }
        }

        if (toShuffleIn.size() > 0) {
            DiscardUtils.cardsToChangeZones(game, toShuffleIn, discardedFromPlay, goingToDiscard);

            Set<PhysicalCard> removeFromPlay = new HashSet<>(goingToDiscard);
            removeFromPlay.addAll(toShuffleIn);

            game.getGameState().removeCardsFromZone(_source.getOwner(), removeFromPlay);

            game.getGameState().shuffleCardsIntoDeck(toShuffleIn, _playerDeck);

            for (PhysicalCard physicalCard : goingToDiscard)
                game.getGameState().addCardToZone(game, physicalCard, Zone.DISCARD);

            for (PhysicalCard physicalCard : discardedFromPlay)
                game.getActionsEnvironment().emitEffectResult(new DiscardCardsFromPlayResult(null, null, physicalCard));

            game.getGameState().sendMessage(getAppendedNames(toShuffleIn) + " " + GameUtils.be(toShuffleIn) + " shuffled into " + _playerDeck + " deck");

            cardsShuffledCallback(toShuffleIn);
        } else {
            // The deck is shuffled though nothing is shuffled into it, as from a discard pile (Q-0102): "on an
            // engine level, the shuffle can target 0 cards" (Ketura on Q-0112; F-0196).  A card that should not
            // be used with nothing to shuffle in says so with a requirement of its own.
            game.getGameState().shuffleDeck(_playerDeck);

            game.getGameState().sendMessage(_playerDeck + " deck is shuffled with no card shuffled into it");
        }

        return new FullEffectResult(toShuffleIn.size() == _cards.size());
    }

    protected void cardsShuffledCallback(Set<PhysicalCard> cardsShuffled) {

    }
}
