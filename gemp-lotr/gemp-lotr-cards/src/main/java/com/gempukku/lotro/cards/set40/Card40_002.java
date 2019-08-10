package com.gempukku.lotro.cards.set40;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Title: The One Ring, The Ruling Ring
 * Set: Second Edition
 * Side: Free
 * Culture: Ring
 * Twilight Cost: 0
 * Type: The One Ring
 * Strength: +1
 * Card Number: 1C2
 * Game Text: While wearing The One Ring, each time the Ring-bearer is about to take a wound during a skirmish, add a burden instead. Response: If bearer is about to take a wound in a skirmish, he wears The One Ring until the regroup phase.
 */
public class Card40_002 extends AbstractAttachable {
    public Card40_002() {
        super(null, CardType.THE_ONE_RING, 0, null, null, "The One Ring", "The Ruling Ring", true);
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.none;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(new StrengthModifier(self, Filters.hasAttached(self), 1));
        modifiers.add(new KeywordModifier(self, Filters.hasAttached(self), Keyword.RING_BOUND));
        return modifiers;
    }

    @Override
    public List<? extends Action> getOptionalInPlayBeforeActions(final String playerId, LotroGame game, Effect effect, final PhysicalCard self) {
        if (game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && TriggerConditions.isGettingWounded(effect, game, Filters.hasAttached(self))
                && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_TEXT_INACTIVE)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(new NegateWoundEffect((WoundCharactersEffect) effect, self.getAttachedTo()));
            action.appendEffect(new AddBurdenEffect(self.getOwner(), self, 1));
            action.appendEffect(new PutOnTheOneRingEffect());
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, Filters.hasAttached(self))
                && game.getGameState().isWearingRing()
                && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_TEXT_INACTIVE)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(new NegateWoundEffect((WoundCharactersEffect) effect, self.getAttachedTo()));
            action.appendEffect(new AddBurdenEffect(self.getOwner(), self, 1));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if ((TriggerConditions.startOfPhase(game, effectResult, Phase.REGROUP) || TriggerConditions.endOfPhase(game, effectResult, Phase.REGROUP))
                && game.getGameState().isWearingRing()) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(new TakeOffTheOneRingEffect());
            return Collections.singletonList(action);
        }
        return null;
    }
}