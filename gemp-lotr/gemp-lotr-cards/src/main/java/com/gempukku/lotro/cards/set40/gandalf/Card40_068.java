package com.gempukku.lotro.cards.set40.gandalf;

import com.gempukku.lotro.cards.AbstractEvent;
import com.gempukku.lotro.cards.PlayConditions;
import com.gempukku.lotro.cards.actions.PlayEventAction;
import com.gempukku.lotro.cards.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.cards.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;

import java.util.Collection;

/**
 * Title: Discernment
 * Set: Second Edition
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 0
 * Type: Event - Fellowship
 * Card Number: 1C68
 * Game Text: Spell. Exert Gandalf and add (X) to discard up to two Shadow conditions with a combined twilight cost of X or less.
 */
public class Card40_068 extends AbstractEvent {
    public Card40_068() {
        super(Side.FREE_PEOPLE, 0, Culture.GANDALF, "Discernment", Phase.FELLOWSHIP);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile)
                && PlayConditions.canExert(self, game, Filters.gandalf);
    }

    @Override
    public PlayEventAction getPlayCardAction(final String playerId, LotroGame game, final PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.gandalf));
        action.appendCost(
                new PlayoutDecisionEffect(playerId,
                        new IntegerAwaitingDecision(1, "Choose number of twilight to add", 0) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                final int twilightCount = getValidatedResult(result);
                                if (twilightCount > 0)
                                    action.appendCost(
                                            new AddTwilightEffect(self, twilightCount));
                                action.appendEffect(
                                        new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, Side.SHADOW, CardType.CONDITION, Filters.maxPrintedTwilightCost(twilightCount)) {
                                            @Override
                                            protected void cardsToBeDiscardedCallback(Collection<PhysicalCard> cards) {
                                                int remainingTwilight = twilightCount;
                                                for (PhysicalCard card : cards)
                                                    remainingTwilight -= card.getBlueprint().getTwilightCost();

                                                action.appendEffect(
                                                        new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, Side.SHADOW, CardType.CONDITION, Filters.maxPrintedTwilightCost(remainingTwilight)));
                                            }
                                        });
                            }
                        }));
        return action;
    }
}