package com.gempukku.lotro.cards.set3.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;

/**
 * Set: Realms of Elf-lords
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 4
 * Type: Event
 * Game Text: Spell. Maneuver: Spot Gandalf and 4 twilight tokens to discard all Shadow conditions.
 */
public class Card3_030 extends AbstractEvent {
    public Card3_030() {
        super(Side.FREE_PEOPLE, 4, Culture.GANDALF, "Deep in Thought", Phase.MANEUVER);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Filters.gandalf)
                && game.getGameState().getTwilightPool() >= 4;
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new DiscardCardsFromPlayEffect(self.getOwner(), self, Filters.and(Side.SHADOW, CardType.CONDITION)));
        return action;
    }
}
