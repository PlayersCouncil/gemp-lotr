package com.gempukku.lotro.cards.set6.dwarven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.effects.choose.ChooseCardsFromHandEffect;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 1
 * Type: Event
 * Game Text: While this card is stacked on a [DWARVEN] condition, Gimli is strength +1. Fellowship: Place this card
 * or another [DWARVEN] card from hand on top of or beneath your draw deck.
 */
public class Card6_011 extends AbstractEvent {
    public Card6_011() {
        super(Side.FREE_PEOPLE, 1, Culture.DWARVEN, "Toss Me", Phase.FELLOWSHIP);
    }

    @Override
    public List<? extends Modifier> getStackedOnModifiers(LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(
                new StrengthModifier(self, Filters.gimli,
                        new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                return Filters.and(Culture.DWARVEN, CardType.CONDITION).accepts(game, self.getStackedOn());
                            }
                        }, 1));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new PlayoutDecisionEffect(playerId,
                        new MultipleChoiceAwaitingDecision(1, "Where to put \"Toss Me\"?", new String[]{"Top of deck", "Bottom of deck"}) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                if (index == 0)
                                    action.insertEffect(
                                            new PutPlayedEventOnTopOfDeckEffect(self));
                                else
                                    action.insertEffect(
                                            new PutPlayedEventOnBottomOfDeckEffect(self));
                            }
                        }) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Place played \"Toss Me\" on top or bottom of deck";
                    }
                });
        possibleEffects.add(
                new ChooseCardsFromHandEffect(playerId, 1, 1, Culture.DWARVEN) {
                    @Override
                    public String getText(LotroGame game) {
                        return "Place other DWARVEN card from hand on top or bottom of deck";
                    }

                    @Override
                    protected void cardsSelected(LotroGame game, final Collection<PhysicalCard> selectedCards) {
                        action.insertEffect(
                                new PlayoutDecisionEffect(playerId,
                                        new MultipleChoiceAwaitingDecision(1, "Where to put selected card?", new String[]{"Top of deck", "Bottom of deck"}) {
                                            @Override
                                            protected void validDecisionMade(int index, String result) {
                                                if (index == 0)
                                                    for (PhysicalCard selectedCard : selectedCards)
                                                        action.insertEffect(
                                                                new PutCardFromHandOnTopOfDeckEffect(selectedCard, true));
                                                else
                                                    for (PhysicalCard selectedCard : selectedCards)
                                                        action.insertEffect(
                                                                new PutCardFromHandOnBottomOfDeckEffect(true, selectedCard));
                                            }
                                        })
                        );
                    }
                });
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));

        return action;
    }
}
