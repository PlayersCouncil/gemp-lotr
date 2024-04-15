package com.gempukku.lotro.cards.set4.isengard;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.effects.PutCardFromDeckOnBottomOfDeckEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 0
 * Type: Artifact
 * Game Text: To play, spot an [ISENGARD] minion. Plays to your support area. Shadow: Spot an [ISENGARD] minion
 * and remove (2) to reveal the top card of any draw deck. You may place that card beneath that draw deck.
 */
public class Card4_166 extends AbstractPermanent {
    public Card4_166() {
        super(Side.SHADOW, 0, CardType.ARTIFACT, Culture.ISENGARD, "The Palantír of Orthanc", "Seventh Seeing-stone", true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.ISENGARD, CardType.MINION);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, final LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 2)
                && PlayConditions.canSpot(game, Culture.ISENGARD, CardType.MINION)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(2));
            action.appendEffect(
                    new PlayoutDecisionEffect(playerId,
                            new MultipleChoiceAwaitingDecision(1, "Choose player to reveal top card", GameUtils.getAllPlayers(game)) {
                                @Override
                                protected void validDecisionMade(int index, String deckId) {
                                    action.insertEffect(
                                            new RevealTopCardsOfDrawDeckEffect(self, deckId, 1) {
                                                @Override
                                                protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                                                    if (revealedCards.size() > 0) {
                                                        final PhysicalCard card = revealedCards.get(0);
                                                        action.appendEffect(
                                                                new PlayoutDecisionEffect(playerId,
                                                                        new MultipleChoiceAwaitingDecision(1, "Do you want to put " + GameUtils.getFullName(card) + " on bottom of deck?", new String[]{"Yes", "No"}) {
                                                                            @Override
                                                                            protected void validDecisionMade(int index, String result) {
                                                                                if (result.equals("Yes"))
                                                                                    action.insertEffect(
                                                                                            new PutCardFromDeckOnBottomOfDeckEffect(self, card, true));
                                                                            }
                                                                        })
                                                        );
                                                    }
                                                }
                                            });
                                }
                            }));
            return Collections.singletonList(action);
        }
        return null;
    }
}
