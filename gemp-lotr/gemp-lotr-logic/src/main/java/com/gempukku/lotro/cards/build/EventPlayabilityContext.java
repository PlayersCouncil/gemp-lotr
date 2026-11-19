package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

/**
 * The context in which an event is asked whether it can be played.  The event has not been paid for at that
 * point, so its own twilight cost is still to come out of the pool before any of its effects is carried out.
 * Memory is the wrapped context's own, so what a trigger memorizes while it is checked is there for the action
 * that follows.
 */
public class EventPlayabilityContext extends DelegateActionContext {
    public EventPlayabilityContext(ActionContext delegate) {
        super(delegate, delegate.getPerformingPlayer());
    }

    @Override
    public int getUnpaidTwilightCost() {
        final LotroGame game = getGame();
        final PhysicalCard event = getSource();
        // The player whose turn it is adds twilight for the cards he plays and takes none from the pool.
        if (event.getOwner().equals(game.getGameState().getCurrentPlayerId()))
            return 0;

        // The least the event can cost, as PlayUtils reckons it when it asks whether the pool can pay for it.
        final int potentialDiscount = game.getModifiersQuerying().getPotentialDiscount(game, event);
        return game.getModifiersQuerying().getTwilightCostToPlay(game, event, null, -potentialDiscount, false);
    }
}
