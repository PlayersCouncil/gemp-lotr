package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.HealCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Event • Fellowship
 * Game Text: Add 3 threats to heal all [GONDOR] companions.
 */
public class Card7_089 extends AbstractEvent {
    public Card7_089() {
        super(Side.FREE_PEOPLE, 1, Culture.GONDOR, "Duty of Two", Phase.FELLOWSHIP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canAddThreat(game, self, 3);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new AddThreatsEffect(playerId, self, 3));
        action.appendEffect(
                new HealCharactersEffect(self, self.getOwner(), Culture.GONDOR, CardType.COMPANION));
        return action;
    }
}
