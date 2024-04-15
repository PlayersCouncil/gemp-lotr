package com.gempukku.lotro.cards.set15.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.modifiers.cost.AddBurdenExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Companion
 * Strength: 3
 * Vitality: 4
 * Resistance: 5
 * Game Text: Ring-bound. To play, add a burden. Each Ring-bound companion is resistance +1.
 * Skirmish: Exert 2 Ring-bound Hobbits to make Smeagol strength +1 and prevent him from being overwhelmed unless his
 * strength is tripled.
 */
public class Card15_049 extends AbstractCompanion {
    public Card15_049() {
        super(0, 3, 4, 5, Culture.GOLLUM, null, null, "Sméagol", "Wretched and Hungry", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AddBurdenExtraPlayCostModifier(self, 1, null, self));
    }

        @Override
        public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return List.of(
                new ResistanceModifier(self, Filters.and(CardType.COMPANION, Keyword.RING_BOUND), 1));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canExert(self, game, 1, 2, Race.HOBBIT, Keyword.RING_BOUND)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 2, 2, 1, Race.HOBBIT, Keyword.RING_BOUND));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new StrengthModifier(self, self, 1)));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new OverwhelmedByMultiplierModifier(self, self, 3)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
