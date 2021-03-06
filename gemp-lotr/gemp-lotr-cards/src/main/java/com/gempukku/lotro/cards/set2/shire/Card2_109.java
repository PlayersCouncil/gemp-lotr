package com.gempukku.lotro.cards.set2.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.effects.PlayoutDecisionEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Mines of Moria
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Maneuver: Spot Sting or Glamdring and exert its bearer X times to wound X Orcs or X Uruk-hai.
 */
public class Card2_109 extends AbstractEvent {
    public Card2_109() {
        super(Side.FREE_PEOPLE, 0, Culture.SHIRE, "Orc-bane", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Filters.hasAttached(Filters.or(Filters.name("Sting"), Filters.name("Glamdring"))));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseActiveCardEffect(self, playerId, "Choose Sting or Glamdring bearer", Filters.hasAttached(Filters.or(Filters.name("Sting"), Filters.name("Glamdring"))), Filters.canExert(self)) {
                    @Override
                    protected void cardSelected(final LotroGame game, final PhysicalCard bearer) {
                        int vitality = game.getModifiersQuerying().getVitality(game, bearer);
                        action.insertCost(
                                new PlayoutDecisionEffect(playerId,
                                        new IntegerAwaitingDecision(1, "Choose how many times to exert", 1, vitality - 1) {
                                            @Override
                                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                                final int exertionCount = getValidatedResult(result);
                                                for (int i = 0; i < exertionCount; i++) {
                                                    action.insertCost(
                                                            new ExertCharactersEffect(action, self, bearer));
                                                }
                                                action.appendEffect(
                                                        new PlayoutDecisionEffect(playerId,
                                                                new MultipleChoiceAwaitingDecision(1, "Choose action to perform", new String[]{"Wound " + exertionCount + " Orcs", "Wound " + exertionCount + " Uruk-hai"}) {
                                                                    @Override
                                                                    protected void validDecisionMade(int index, String result) {
                                                                        Filterable filter = (index == 0) ? Race.ORC : Race.URUK_HAI;
                                                                        action.insertEffect(
                                                                                new ChooseAndWoundCharactersEffect(action, playerId, exertionCount, exertionCount, filter));
                                                                    }
                                                                }));
                                            }
                                        }
                                ));
                    }
                });

        return action;
    }
}
