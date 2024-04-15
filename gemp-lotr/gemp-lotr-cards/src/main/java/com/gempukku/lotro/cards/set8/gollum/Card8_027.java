package com.gempukku.lotro.cards.set8.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.cost.AddBurdenExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Siege of Gondor
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Companion
 * Strength: 3
 * Vitality: 4
 * Resistance: 6
 * Signet: Frodo
 * Game Text: Ring-bound. To play, add a burden. Skirmish: Add threats equal to the total vitality of the minions
 * Smeagol is skirmishing to discard Smeagol. Regroup: Discard Smeagol to take a [GOLLUM] minion from your discard pile
 * into hand.
 */
public class Card8_027 extends AbstractCompanion {
    public Card8_027() {
        super(0, 3, 4, 6, Culture.GOLLUM, null, Signet.FRODO, "Sméagol", "Slippery Sneak", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AddBurdenExtraPlayCostModifier(self, 1, null, self));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && Filters.inSkirmish.accepts(game, self)) {
            int totalVitality = 0;
            for (PhysicalCard minion : Filters.filterActive(game, CardType.MINION, Filters.inSkirmishAgainst(self))) {
                totalVitality += game.getModifiersQuerying().getVitality(game, minion);
            }
            if (PlayConditions.canAddThreat(game, self, totalVitality)) {
                ActivateCardAction action = new ActivateCardAction(self);
                action.appendCost(
                        new AddThreatsEffect(playerId, self, totalVitality));
                action.appendEffect(
                        new SelfDiscardEffect(self));
                return Collections.singletonList(action);
            }
        }
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndPutCardFromDiscardIntoHandEffect(action, playerId, 1, 1, CardType.MINION, Culture.GOLLUM));
            return Collections.singletonList(action);
        }
        return null;
    }
}
