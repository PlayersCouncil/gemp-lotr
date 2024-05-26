package com.gempukku.lotro.cards.set18.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.RemoveBurdenEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAssignCharacterToMinionEffect;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.RemoveKeywordModifier;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 0
 * Type: Event • Assignment
 * Game Text: Remove a burden to choose one: assign a [WRAITH] minion to a companion who has resistance 0;
 * or have a [WRAITH] minion lose fierce and gain hunter 2 until the regroup phase.
 */
public class Card18_133 extends AbstractEvent {
    public Card18_133() {
        super(Side.SHADOW, 0, Culture.WRAITH, "Pull of the Ring", Phase.ASSIGNMENT);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canRemoveBurdens(game, self, 1);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new RemoveBurdenEffect(playerId, self, 1));
        List<Effect> possibleEffects = new LinkedList<>();
        possibleEffects.add(
                new ChooseActiveCardEffect(self, playerId, "Choose a WRAITH minion", Culture.WRAITH, CardType.MINION, Filters.assignableToSkirmishAgainst(Side.SHADOW, Filters.and(CardType.COMPANION, Filters.maxResistance(0)))) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard minion) {
                        action.appendEffect(
                                new ChooseAndAssignCharacterToMinionEffect(action, playerId, minion, CardType.COMPANION, Filters.maxResistance(0)));
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return "Assign a WRAITH minion to a companion who has resistance 0";
                    }
                });
        possibleEffects.add(
                new ChooseActiveCardEffect(self, playerId, "Choose a WRAITH minion", Culture.WRAITH, CardType.MINION) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard minion) {
                        action.appendEffect(
                                new AddUntilStartOfPhaseModifierEffect(
                                        new RemoveKeywordModifier(self, minion, Keyword.FIERCE), Phase.REGROUP));
                        action.appendEffect(
                                new AddUntilStartOfPhaseModifierEffect(
                                        new AddKeywordModifier(self, minion, Keyword.HUNTER, 2), Phase.REGROUP));
                    }

                    @Override
                    public String getText(LotroGame game) {
                        return "Make a WRAITH minion lose fierce and gain hunter 2 until the regroup phase";
                    }
                });
        action.appendEffect(
                new ChoiceEffect(action, playerId, possibleEffects));
        return action;
    }
}
