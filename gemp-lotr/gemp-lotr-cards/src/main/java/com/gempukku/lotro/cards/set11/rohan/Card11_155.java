package com.gempukku.lotro.cards.set11.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.modifiers.AbstractExtraPlayCostModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.cost.ExertExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Shadows
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: To play, exert a [ROHAN] Man. While you can spot 2 [ROHAN] mounts, the move limit is +1. Each of your
 * companions that is not mounted is strength -1.
 */
public class Card11_155 extends AbstractPermanent {
    public Card11_155() {
        super(Side.FREE_PEOPLE, 1, CardType.CONDITION, Culture.ROHAN, "Riding Like the Wind");
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(self).getUsedLimit() < 1
                && Filters.canSpot(game, 2, Culture.ROHAN, PossessionClass.MOUNT)) {
            game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                    new MoveLimitModifier(self, 1));
            game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(self).incrementToLimit(1, 1);
        }
        return null;

    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlay(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new ExertExtraPlayCostModifier(self, self, null, Culture.ROHAN, Race.MAN));
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, Filters.and(Filters.owner(self.getOwner()), CardType.COMPANION, Filters.not(Filters.mounted)), -1));
        return modifiers;
    }
}
