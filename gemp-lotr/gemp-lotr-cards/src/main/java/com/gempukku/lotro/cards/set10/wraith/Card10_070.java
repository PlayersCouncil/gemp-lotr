package com.gempukku.lotro.cards.set10.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.HealCharactersEffect;
import com.gempukku.lotro.logic.modifiers.CantExertWithCardModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 5
 * Type: Minion • Nazgul
 * Strength: 10
 * Vitality: 3
 * Site: 2
 * Game Text: Enduring. Fierce. Shadow cards cannot exert Úlairë Nelya during a skirmish phase. Skirmish: Heal Úlairë
 * Nelya to add (1) for each burden.
 */
public class Card10_070 extends AbstractMinion {
    public Card10_070() {
        super(5, 10, 3, 2, Race.NAZGUL, Culture.WRAITH, Names.nelya, "Thrall of The One", true);
        addKeyword(Keyword.ENDURING);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new CantExertWithCardModifier(self, self, new PhaseCondition(Phase.SKIRMISH), Side.SHADOW));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 0)
                && PlayConditions.canHeal(self, game, self)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new HealCharactersEffect(self, self.getOwner(), self));
            action.appendEffect(
                    new AddTwilightEffect(self, game.getGameState().getBurdens()));
            return Collections.singletonList(action);
        }
        return null;
    }
}
