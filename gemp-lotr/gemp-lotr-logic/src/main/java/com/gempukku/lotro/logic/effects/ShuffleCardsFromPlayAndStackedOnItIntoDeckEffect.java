package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Shuffles cards in play, and every card stacked on them, into draw decks: each card into its owner's, whoever
 * owns the card it was stacked on (a Free Peoples possession stacked on the Shadow player's Twisted Tales goes
 * back to the Free Peoples player's deck).  Each deck that receives a card is shuffled once.
 */
public class ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final Collection<? extends PhysicalCard> _cards;

    public ShuffleCardsFromPlayAndStackedOnItIntoDeckEffect(PhysicalCard source, Collection<? extends PhysicalCard> cards) {
        _source = source;
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

            // A card never goes into a deck that is not its owner's.  Sorted by owner so that the order in
            // which the decks are shuffled does not depend on the set's iteration order.
            Map<String, Set<PhysicalCard>> byOwner = new TreeMap<>();
            for (PhysicalCard card : toShuffleIn)
                byOwner.computeIfAbsent(card.getOwner(), owner -> new HashSet<>()).add(card);

            for (Map.Entry<String, Set<PhysicalCard>> entry : byOwner.entrySet()) {
                game.getGameState().shuffleCardsIntoDeck(entry.getValue(), entry.getKey());

                game.getGameState().sendMessage(getAppendedNames(entry.getValue()) + " " + GameUtils.be(entry.getValue()) + " shuffled into " + entry.getKey() + " deck");
            }

            cardsShuffledCallback(toShuffleIn);
        }

        return new FullEffectResult(inPlay.size() == _cards.size());
    }

    protected void cardsShuffledCallback(Set<PhysicalCard> cardsShuffled) {

    }
}
