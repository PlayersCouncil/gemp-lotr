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

public class ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final String _playerDeck;
    private final Collection<? extends PhysicalCard> _cards;

    public ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect(PhysicalCard source, String playerDeck, Collection<? extends PhysicalCard> cards) {
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
            if (card.getZone() != Zone.STACKED && !card.getZone().isInPlay())
                return false;
        }

        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        Set<PhysicalCard> toShuffleIn = new HashSet<>();
        Set<PhysicalCard> inPlay = new HashSet<>();

        for (PhysicalCard card : _cards) {
            if (card.getZone().isInPlay()) {
                inPlay.add(card);
                toShuffleIn.add(card);
                toShuffleIn.addAll(game.getGameState().getStackedCards(card));
            }
        }

        if (toShuffleIn.size() > 0) {
            game.getGameState().removeCardsFromZone(_source.getOwner(), toShuffleIn);

            game.getGameState().shuffleCardsIntoDeck(toShuffleIn, _playerDeck);

            game.getGameState().sendMessage(getAppendedNames(toShuffleIn) + " " + GameUtils.be(toShuffleIn) + " shuffled into " + _playerDeck + " deck");

            cardsShuffledCallback(toShuffleIn);
        }

        return new FullEffectResult(inPlay.size() == _cards.size());
    }

    protected void cardsShuffledCallback(Set<PhysicalCard> cardsShuffled) {

    }
}
