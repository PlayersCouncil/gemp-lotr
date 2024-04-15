package com.gempukku.lotro.cards.set7.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddThreatsEffect;
import com.gempukku.lotro.logic.effects.AssignmentEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ExhaustCharacterEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.cost.AddBurdenExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Companion
 * Strength: 3
 * Vitality: 4
 * Resistance: 6
 * Signet: Frodo
 * Game Text: Ring-bound. To play, add a burden. Assignment: Assign a minion to Smeagol and add 2 threats
 * to exhaust that minion.
 */
public class Card7_071 extends AbstractCompanion {
    public Card7_071() {
        super(0, 3, 4, 6, Culture.GOLLUM, null, Signet.FRODO, "Sméagol", "Always Helps", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AddBurdenExtraPlayCostModifier(self, 1, null, self));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, final LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.ASSIGNMENT, self)
                && Filters.assignableToSkirmishAgainst(Side.FREE_PEOPLE, CardType.MINION).accepts(game, self)
                && PlayConditions.canAddThreat(game, self, 2)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseActiveCardEffect(self, playerId, "Choose minion to assign to Smeagol", CardType.MINION, Filters.assignableToSkirmishAgainst(Side.FREE_PEOPLE, self)) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.insertCost(
                                    new AssignmentEffect(playerId, self, card));
                            action.appendEffect(
                                    new ExhaustCharacterEffect(self, action, card));
                        }
                    });
            action.appendCost(
                    new AddThreatsEffect(playerId, self, 2));
            return Collections.singletonList(action);
        }
        return null;
    }
}
