package com.gempukku.lotro.cards.set8.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDiscardOnTopOfDeckEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Siege of Gondor
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 2
 * Type: Possession • Support Area
 * Game Text: Shadow: Remove a threat or discard a [RAIDER] card from hand to add a [RAIDER] token here.
 * Regroup: Remove 2 [RAIDER] tokens here to place a [RAIDER] card from your discard pile on top of your draw deck.
 */
public class Card8_065 extends AbstractPermanent {
    public Card8_065() {
        super(Side.SHADOW, 2, CardType.POSSESSION, Culture.RAIDER, "Ships of Great Draught");
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && (PlayConditions.canRemoveThreat(game, self, 1)
                || PlayConditions.canDiscardFromHand(game, playerId, 1, Culture.RAIDER))) {
            ActivateCardAction action = new ActivateCardAction(self);
            List<Effect> possibleCosts = new LinkedList<>();
            possibleCosts.add(
                    new RemoveThreatsEffect(self, 1) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Remove a threat";
                        }
                    });
            possibleCosts.add(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 1, Culture.RAIDER) {
                        @Override
                        public String getText(LotroGame game) {
                            return "Discard a RAIDER card from hand";
                        }
                    });
            action.appendCost(
                    new ChoiceEffect(action, playerId, possibleCosts));
            action.appendEffect(
                    new AddTokenEffect(self, self, Token.RAIDER));
            return Collections.singletonList(action);
        }
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)
                && PlayConditions.canRemoveTokens(game, self, Token.RAIDER, 2)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTokenEffect(self, self, Token.RAIDER, 2));
            action.appendEffect(
                    new ChooseAndPutCardFromDiscardOnTopOfDeckEffect(action, playerId, 1, 1, Culture.RAIDER));
            return Collections.singletonList(action);
        }
        return null;
    }
}
