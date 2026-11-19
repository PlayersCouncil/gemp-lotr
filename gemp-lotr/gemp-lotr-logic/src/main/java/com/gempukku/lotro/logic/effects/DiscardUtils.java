package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DiscardUtils {
    public static void cardsToChangeZones(LotroGame game, Collection<PhysicalCard> movingCards, Collection<PhysicalCard> discardedCards, Collection<PhysicalCard> toMoveToDiscard) {
        cardsToChangeZones(game, movingCards, Collections.emptySet(), discardedCards, toMoveToDiscard);
    }

    /**
     * @param stayingCards Cards borne by the moving cards that are not discarded with them, and with which
     *                     whatever they bear in turn is left.  The caller sees to where they lie afterwards.
     */
    public static void cardsToChangeZones(LotroGame game, Collection<PhysicalCard> movingCards, Collection<PhysicalCard> stayingCards, Collection<PhysicalCard> discardedCards, Collection<PhysicalCard> toMoveToDiscard) {
        for (PhysicalCard card : movingCards) {
            cardsToChangeZones(game, movingCards, stayingCards, card, discardedCards, toMoveToDiscard);
        }
    }

    private static void cardsToChangeZones(LotroGame game, Collection<PhysicalCard> movingCards, Collection<PhysicalCard> stayingCards, PhysicalCard card, Collection<PhysicalCard> discardedCards, Collection<PhysicalCard> toMoveToDiscard) {
        List<PhysicalCard> attachedCards = game.getGameState().getAttachedCards(card);
        for (PhysicalCard attachedCard : attachedCards) {
            if (!movingCards.contains(attachedCard) && !stayingCards.contains(attachedCard)) {
                discardedCards.add(attachedCard);
                toMoveToDiscard.add(attachedCard);
                cardsToChangeZones(game, movingCards, stayingCards, attachedCard, discardedCards, toMoveToDiscard);
            }
        }

        List<PhysicalCard> stackedCards = game.getGameState().getStackedCards(card);
        toMoveToDiscard.addAll(stackedCards);
    }
}
