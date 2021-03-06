package com.gempukku.lotro.cards.set15.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Spot your [ROHAN] Man bearing a possession to discard all possessions from one character.
 */
public class Card15_142 extends AbstractEvent {
    public Card15_142() {
        super(Side.FREE_PEOPLE, 1, Culture.ROHAN, "Swift Stroke", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.owner(self.getOwner()), Culture.ROHAN, Race.MAN, Filters.hasAttached(CardType.POSSESSION));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose a character", Filters.character) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        action.appendEffect(
                                new DiscardCardsFromPlayEffect(self.getOwner(), self, CardType.POSSESSION, Filters.attachedTo(card)));
                    }
                });
        return action;
    }
}
