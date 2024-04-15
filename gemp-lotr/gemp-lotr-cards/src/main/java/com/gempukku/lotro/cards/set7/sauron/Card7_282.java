package com.gempukku.lotro.cards.set7.sauron;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.discount.RemoveThreatsToDiscountEffect;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 10
 * Type: Minion • Orc
 * Strength: 21
 * Vitality: 5
 * Site: 6
 * Game Text: When you play Host of Udun, you may remove X threats to make its twilight cost -X.
 */
public class Card7_282 extends AbstractMinion {
    public Card7_282() {
        super(10, 21, 5, 6, Race.ORC, Culture.SAURON, "Host of Udûn", null, true);
    }

    @Override
    public int getPotentialDiscount(LotroGame game, String playerId, PhysicalCard self) {
        return game.getGameState().getThreats();
    }

    @Override
    public void appendPotentialDiscountEffects(LotroGame game, CostToEffectAction action, String playerId, PhysicalCard self) {
        action.appendPotentialDiscount(new RemoveThreatsToDiscountEffect(action));
    }
}
