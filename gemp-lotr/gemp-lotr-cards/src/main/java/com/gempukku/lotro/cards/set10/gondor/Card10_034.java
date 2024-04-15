package com.gempukku.lotro.cards.set10.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfTurnModifierEffect;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 5
 * Type: Event • Regroup
 * Game Text: If no opponent controls a site, spot 2 [GONDOR] Men to make the move limit +1 for this turn.
 */
public class Card10_034 extends AbstractEvent {
    public Card10_034() {
        super(Side.FREE_PEOPLE, 5, Culture.GONDOR, "Last Throw", Phase.REGROUP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return !PlayConditions.canSpot(game, Filters.siteControlledByOtherPlayer(self.getOwner()))
                && PlayConditions.canSpot(game, 2, Culture.GONDOR, Race.MAN);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new AddUntilEndOfTurnModifierEffect(
                        new MoveLimitModifier(self, 1)));
        return action;
    }
}
