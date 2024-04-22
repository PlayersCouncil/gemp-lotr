package com.gempukku.lotro.cards.set8.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Siege of Gondor
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Make a corsair strength +1 for each [RAIDER] token you spot (limit +6). If you have initiative, you may
 * place this event on top of your draw deck.
 */
public class Card8_066 extends AbstractEvent {
    public Card8_066() {
        super(Side.SHADOW, 1, Culture.RAIDER, "Wind That Sped Ships", Phase.SKIRMISH);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose Corsair", Keyword.CORSAIR) {
                    @Override
                    protected void cardSelected(final LotroGame game, final PhysicalCard card) {
                        int count = Math.min(6, GameUtils.getSpottableTokensTotal(game, Token.RAIDER));

                        IntegerAwaitingDecision spotDecision = new IntegerAwaitingDecision(1, "How many RAIDER tokens you wish to spot?", 0, count) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                int value = getValidatedResult(result);
                                action.appendEffect(
                                        new AddUntilEndOfPhaseModifierEffect(
                                                new StrengthModifier(self, card, value)));
                                if (PlayConditions.hasInitiative(game, Side.SHADOW))
                                    action.appendEffect(
                                            new OptionalEffect(action, playerId,
                                                    new PutPlayedEventOnTopOfDeckEffect(self)));
                            }
                        };
                        spotDecision.setDefaultValue(count);

                        action.appendEffect(
                                new PlayoutDecisionEffect(playerId, spotDecision));
                    }
                });
        return action;
    }
}
