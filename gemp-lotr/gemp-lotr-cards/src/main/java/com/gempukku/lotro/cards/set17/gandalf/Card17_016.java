package com.gempukku.lotro.cards.set17.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.DiscardCardFromDeckEffect;
import com.gempukku.lotro.logic.effects.PutCardFromDeckIntoHandOrDiscardEffect;
import com.gempukku.lotro.logic.effects.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.CountActiveEvaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 4
 * Vitality: 3
 * Resistance: 6
 * Game Text: Barliman Butterbur is strength +1 for each attached [GANDALF] follower. Maneuver: Exert Barliman Butterbur
 * to reveal the top 2 cards of your deck. Take any [GANDALF] cards into hand and discard the rest.
 */
public class Card17_016 extends AbstractCompanion {
    public Card17_016() {
        super(2, 4, 3, 6, Culture.GANDALF, Race.MAN, null, "Barliman Butterbur", "Red-faced Landlord", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new StrengthModifier(self, self, null, new CountActiveEvaluator(Zone.ATTACHED, Culture.GANDALF, CardType.FOLLOWER)));
}

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.MANEUVER, self)
                && PlayConditions.canSelfExert(self, game)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new RevealTopCardsOfDrawDeckEffect(self, playerId, 2) {
                        @Override
                        protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                            for (PhysicalCard card : revealedCards) {
                                if (card.getBlueprint().getCulture() == Culture.GANDALF) {
                                    action.appendEffect(
                                            new PutCardFromDeckIntoHandOrDiscardEffect(card, true));
                                } else {
                                    action.appendEffect(
                                            new DiscardCardFromDeckEffect(card));
                                }
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
