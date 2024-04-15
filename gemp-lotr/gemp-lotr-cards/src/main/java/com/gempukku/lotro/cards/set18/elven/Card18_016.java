package com.gempukku.lotro.cards.set18.elven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 0
 * Type: Event • Fellowship
 * Game Text: Add 2 threats to heal an [ELVEN] companion.
 */
public class Card18_016 extends AbstractEvent {
    public Card18_016() {
        super(Side.FREE_PEOPLE, 0, Culture.ELVEN, "Miruvóre", Phase.FELLOWSHIP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canAddThreat(game, self, 2);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new AddThreatsEffect(playerId, self, 2));
        action.appendEffect(
                new ChooseAndHealCharactersEffect(action, playerId, Culture.ELVEN, CardType.COMPANION));
        return action;
    }
}
