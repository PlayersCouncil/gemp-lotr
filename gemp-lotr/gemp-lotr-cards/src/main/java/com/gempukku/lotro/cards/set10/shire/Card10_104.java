package com.gempukku.lotro.cards.set10.shire;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.ChooseArbitraryCardsEffect;
import com.gempukku.lotro.logic.effects.PutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.*;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: Fellowship: Add a burden to choose 2 [SHIRE] events with different card titles from your discard pile.
 * Choose an opponent and make him or her choose 1 of those cards for you to take into hand.
 */
public class Card10_104 extends AbstractPermanent {
    public Card10_104() {
        super(Side.FREE_PEOPLE, 1, CardType.CONDITION, Culture.SHIRE, "Birthday Present");
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.FELLOWSHIP, self)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new AddBurdenEffect(playerId, self, 1));
            Collection<PhysicalCard> shireEvents = Filters.filter(game.getGameState().getDiscard(playerId), game, Culture.SHIRE, CardType.EVENT);
            action.appendEffect(
                    new ChooseArbitraryCardsEffect(playerId, "Choose events", filterUniqueNames(shireEvents), 2, 2) {
                        @Override
                        protected void cardsSelected(LotroGame game, final Collection<PhysicalCard> selectedCards) {
                            if (selectedCards.size() > 0) {
                                action.appendEffect(
                                        new ChooseOpponentEffect(playerId) {
                                            @Override
                                            protected void opponentChosen(String opponentId) {
                                                action.appendEffect(
                                                        new ChooseArbitraryCardsEffect(opponentId, "Choose event Free People player should take into hand", selectedCards, 1, 1) {
                                                            @Override
                                                            protected void cardsSelected(LotroGame game, Collection<PhysicalCard> selectedCards) {
                                                                for (PhysicalCard selectedCard : selectedCards) {
                                                                    action.appendEffect(
                                                                            new PutCardFromDiscardIntoHandEffect(selectedCard));
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    private Collection<PhysicalCard> filterUniqueNames(Collection<PhysicalCard> cards) {
        Map<String, PhysicalCard> nameMap = new HashMap<>();
        for (PhysicalCard card : cards)
            nameMap.put(card.getBlueprint().getTitle(), card);
        return nameMap.values();
    }
}
