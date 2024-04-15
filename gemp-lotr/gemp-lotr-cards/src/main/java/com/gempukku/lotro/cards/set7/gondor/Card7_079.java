package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.PlaySiteEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.MayNotBearModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 3
 * Type: Artifact • Hand Weapon
 * Strength: +2
 * Vitality: +1
 * Game Text: Bearer must be Aragorn. Discard any other weapon he bears. He is damage +1 and cannot bear other weapons.
 * Fellowship or Regroup: If the fellowship is at any site 2 or any site 5, play the fellowship's next site (replacing
 * opponent's site if necessary).
 */
public class Card7_079 extends AbstractAttachableFPPossession {
    public Card7_079() {
        super(3, 2, 1, Culture.GONDOR, CardType.ARTIFACT, PossessionClass.HAND_WEAPON, "Andúril", "Flame of the West", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.aragorn;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self), Keyword.DAMAGE, 1));
        modifiers.add(
                new MayNotBearModifier(self, Filters.hasAttached(self), Filters.and(Filters.weapon, Filters.not(self))));
        return modifiers;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if ((PlayConditions.canUseFPCardDuringPhase(game, Phase.FELLOWSHIP, self) || PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self))
                && (game.getGameState().getCurrentSiteNumber() == 2 || game.getGameState().getCurrentSiteNumber() == 5)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new PlaySiteEffect(action, playerId, null, game.getGameState().getCurrentSiteNumber() + 1));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new DiscardCardsFromPlayEffect(self.getOwner(), self, Filters.weapon, Filters.attachedTo(Filters.hasAttached(self)), Filters.not(self)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
