package com.gempukku.lotro.cards.set4.gandalf;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.MayNotBearModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 2
 * Type: Possession • Mount
 * Strength: +2
 * Game Text: Bearer must be Gandalf. Discard any hand weapon he bears. Gandalf may not bear a hand weapon.
 * At the start of each skirmish involving Gandalf, each minion skirmishing him must exert.
 */
public class Card4_100 extends AbstractAttachableFPPossession {
    public Card4_100() {
        super(2, 2, 0, Culture.GANDALF, PossessionClass.MOUNT, "Shadowfax", null, true);
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.gandalf;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new MayNotBearModifier(self, Filters.hasAttached(self), PossessionClass.HAND_WEAPON));
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new DiscardCardsFromPlayEffect(self.getOwner(), self, Filters.and(Filters.attachedTo(Filters.hasAttached(self)), PossessionClass.HAND_WEAPON)));
            return Collections.singletonList(action);
        }
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.SKIRMISH)) {
            final Skirmish skirmish = game.getGameState().getSkirmish();
            if (skirmish != null && skirmish.getFellowshipCharacter() != null && skirmish.getFellowshipCharacter().getBlueprint().getTitle().equals("Gandalf")) {
                RequiredTriggerAction action = new RequiredTriggerAction(self);
                action.appendEffect(
                        new ExertCharactersEffect(action, self, Filters.inSkirmishAgainst(Filters.gandalf)));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
