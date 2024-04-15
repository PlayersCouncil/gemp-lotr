package com.gempukku.lotro.cards.set13.elven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.PreventCardEffect;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Companion • Elf
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Game Text: While another companion is assigned to a skirmish, during each skirmish, prevent all wounds to this
 * companion after the first wound.
 */
public class Card13_020 extends AbstractCompanion {
    public Card13_020() {
        super(2, 6, 3, 6, Culture.ELVEN, Race.ELF, null, "Lórien Protector");
    }

    @Override
    public List<RequiredTriggerAction> getRequiredBeforeTriggers(LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, self)
                && PlayConditions.canSpot(game, Filters.not(self), CardType.COMPANION, Filters.assignedToSkirmish)
                && PlayConditions.isPhase(game, Phase.SKIRMISH)
                && game.getModifiersEnvironment().getWoundsTakenInCurrentPhase(self) >= 1) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new PreventCardEffect((PreventableCardEffect) effect, self));
            return Collections.singletonList(action);
        }
        return null;
    }
}
