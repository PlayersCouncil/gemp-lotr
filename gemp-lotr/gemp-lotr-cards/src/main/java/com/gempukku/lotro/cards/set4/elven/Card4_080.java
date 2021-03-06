package com.gempukku.lotro.cards.set4.elven;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.AbstractModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ModifierEffect;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 1
 * Type: Companion • Elf
 * Strength: 5
 * Vitality: 2
 * Resistance: 6
 * Game Text: To play, spot an Elf. The twilight cost of each ranged weapon played on Ordulus is -1.
 */
public class Card4_080 extends AbstractCompanion {
    public Card4_080() {
        super(1, 5, 2, 6, Culture.ELVEN, Race.ELF, null, "Ordulus", "Young Warrior", true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.ELF);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        return Collections.singletonList(
                new AbstractModifier(self, "The twilight cost of each ranged weapon played on Ordulus is -1.", PossessionClass.RANGED_WEAPON, ModifierEffect.TWILIGHT_COST_MODIFIER) {
                    @Override
                    public int getTwilightCostModifier(LotroGame game, PhysicalCard physicalCard, PhysicalCard target, boolean ignoreRoamingPenalty) {
                        if (target == self)
                            return -1;
                        return 0;
                    }
                });
    }
}
