package com.gempukku.lotro.cards.set5.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.PutCardsFromDeckBeneathDrawDeckEffect;
import com.gempukku.lotro.logic.effects.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.cost.AddBurdenExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Battle of Helm's Deep
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Companion
 * Strength: 3
 * Vitality: 4
 * Resistance: 6
 * Signet: Frodo
 * Game Text: Ring-bound. To play add a burden. Regroup: Exert Smeagol (or Gollum) twice to reveal the top 4 cards
 * of your draw deck. Wound a minion for each Shadow card revealed. Place those 4 cards beneath your draw deck in any order.
 */
public class Card5_028 extends AbstractCompanion {
    public Card5_028() {
        super(0, 3, 4, 6, Culture.GOLLUM, null, Signet.FRODO, "Sméagol", "Old Noser", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AddBurdenExtraPlayCostModifier(self, 1, null, self));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, final LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)
                && PlayConditions.canExert(self, game, 2, Filters.gollumOrSmeagol)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, 2, Filters.gollumOrSmeagol));
            action.appendEffect(
                    new RevealTopCardsOfDrawDeckEffect(self, playerId, 4) {
                        @Override
                        protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                            int shadowCards = Filters.filter(revealedCards, game, Side.SHADOW).size();
                            action.appendEffect(
                                    new PutCardsFromDeckBeneathDrawDeckEffect(action, self, playerId, revealedCards, true));
                            for (int i = 0; i < shadowCards; i++)
                                action.appendEffect(
                                        new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.MINION));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
