package com.gempukku.lotro.cards.set7.dwarven;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 2
 * Type: Event • Fellowship
 * Game Text: Spot a Dwarf and add a threat to draw 3 cards.
 */
public class Card7_004 extends AbstractEvent {
    public Card7_004() {
        super(Side.FREE_PEOPLE, 2, Culture.DWARVEN, "Calculated Risk", Phase.FELLOWSHIP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.DWARF)
                && PlayConditions.canAddThreat(game, self, 1);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new AddThreatsEffect(playerId, self, 1));
        action.appendEffect(
                new DrawCardsEffect(action, playerId, 3));
        return action;
    }
}
