package com.gempukku.lotro.cards.set12.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 7
 * Vitality: 3
 * Resistance: 7
 * Game Text: While you can spot a [ROHAN] Man, Eomer's twilight cost is -1. Maneuver: If you can spot more minions than
 * companions, exert Eomer to play a [ROHAN] companion from your draw deck.
 */
public class Card12_112 extends AbstractCompanion {
    public Card12_112() {
        super(3, 7, 3, 7, Culture.ROHAN, Race.MAN, null, Names.eomer, "Éored Leader", true);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        if (Filters.canSpot(game, Culture.ROHAN, Race.MAN))
            return -1;
        return 0;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && PlayConditions.canSelfExert(self, game)
                && Filters.countActive(game, CardType.MINION) > Filters.countActive(game, CardType.COMPANION)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndPlayCardFromDeckEffect(playerId, Culture.ROHAN, CardType.COMPANION));
            return Collections.singletonList(action);
        }
        return null;
    }
}
