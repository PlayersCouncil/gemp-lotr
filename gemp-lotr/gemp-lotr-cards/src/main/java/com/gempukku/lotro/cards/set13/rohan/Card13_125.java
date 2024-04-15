package com.gempukku.lotro.cards.set13.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 1
 * Type: Event • Maneuver
 * Game Text: To play, spot 2 [ROHAN] Men. Unless you can spot a companion who has resistance 3 or less, discard
 * a possession from play.
 */
public class Card13_125 extends AbstractEvent {
    public Card13_125() {
        super(Side.FREE_PEOPLE, 1, Culture.ROHAN, "Ferthu Théoden Hal", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Culture.ROHAN, Race.MAN);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        if (!PlayConditions.canSpot(game, CardType.COMPANION, Filters.maxResistance(3)))
            action.appendEffect(
                    new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, CardType.POSSESSION));
        return action;
    }
}
