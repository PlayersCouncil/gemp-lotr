package com.gempukku.lotro.cards.set18.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.RemoveCardsFromTheGameEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.cost.ExertExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 0
 * Type: Condition • Support Area
 * Game Text: Spell. To play, exert Gandalf. When you play this condition, remove (3). At the start of the maneuver
 * phase, add (4) and remove this condition from the game.
 */
public class Card18_024 extends AbstractPermanent {
    public Card18_024() {
        super(Side.FREE_PEOPLE, 0, CardType.CONDITION, Culture.GANDALF, "Our Time", null, true);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new ExertExtraPlayCostModifier(self, self, null, Filters.gandalf));
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new RemoveTwilightEffect(3));
            return Collections.singletonList(action);
        }
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.MANEUVER)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new AddTwilightEffect(self, 4));
            action.appendEffect(
                    new RemoveCardsFromTheGameEffect(self.getOwner(), self, Collections.singleton(self)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
