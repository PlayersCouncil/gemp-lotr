package com.gempukku.lotro.cards.set3.gondor;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.PutCardFromStackedIntoHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndStackCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Realms of Elf-lords
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Artifact
 * Game Text: Plays to your support area. Fellowship: Stack a [GONDOR] card from hand here. Fellowship: Add (1) to take
 * a card stacked here into hand.
 */
public class Card3_044 extends AbstractPermanent {
    public Card3_044() {
        super(Side.FREE_PEOPLE, 1, CardType.ARTIFACT, Culture.GONDOR, "The Shards of Narsil", null, true);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        List<ActivateCardAction> actions = new LinkedList<>();

        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.FELLOWSHIP, self)) {
            if (PlayConditions.canStackCardFromHand(self, game, playerId, 1, self, Culture.GONDOR)) {
                final ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Stack a GONDOR card from hand here");
                action.appendEffect(
                        new ChooseAndStackCardsFromHandEffect(action, playerId, 1, 1, self, Culture.GONDOR));
                actions.add(action);
            }

            List<PhysicalCard> stackedCards = game.getGameState().getStackedCards(self);
            if (stackedCards.size() > 0) {
                final ActivateCardAction action = new ActivateCardAction(self);
                action.setText("Add (1) to take card stacked here into hand");
                action.appendCost(
                        new AddTwilightEffect(self, 1));
                action.appendEffect(
                        new ChooseArbitraryCardsEffect(playerId, "Choose card", stackedCards, 1, 1) {
                            @Override
                            protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                for (PhysicalCard selectedCard : selectedCards) {
                                    action.appendEffect(
                                            new PutCardFromStackedIntoHandEffect(selectedCard));
                                }
                            }
                        });
                actions.add(action);
            }

        }

        return actions;
    }
}
