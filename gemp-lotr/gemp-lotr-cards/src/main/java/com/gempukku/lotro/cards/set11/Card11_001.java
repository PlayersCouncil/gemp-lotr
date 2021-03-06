package com.gempukku.lotro.cards.set11;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.NegateWoundEffect;
import com.gempukku.lotro.logic.effects.PutOnTheOneRingEffect;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Type: The One Ring
 * Resistance: +2
 * Game Text: Response: If the Ring-bearer is about to take a wound, he or she wears The One Ring until the regroup
 * phase. While the Ring-bearer is wearing The One Ring, each time he or she is about to take a wound, add a burden
 * instead.
 */
public class Card11_001 extends AbstractAttachable {
    public Card11_001() {
        super(null, CardType.THE_ONE_RING, 0, null, null, "The One Ring", "The Ring of Rings", true);
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.none;
    }

    @Override
    public int getResistance() {
        return 2;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(final String playerId, LotroGame game, Effect effect, final PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, Filters.hasAttached(self))
                && !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.RING_TEXT_INACTIVE)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(new NegateWoundEffect((WoundCharactersEffect) effect, self.getAttachedTo()));
            action.appendEffect(new AddBurdenEffect(playerId, self, 1));
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
}
