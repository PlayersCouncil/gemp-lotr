package com.gempukku.lotro.cards.set15.rohan;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.condition.NotCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 5
 * Vitality: 3
 * Resistance: 6
 * Game Text: While you can spot a valiant Man, this companion’s twilight cost is -1. While no opponent controls a site,
 * this companion is strength +2.
 */
public class Card15_130 extends AbstractCompanion {
    public Card15_130() {
        super(3, 5, 3, 6, Culture.ROHAN, Race.MAN, null, "Horseman of the North");
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        if (Filters.canSpot(game, Race.MAN, Keyword.VALIANT))
            return -1;
        return 0;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(new StrengthModifier(self, self, new NotCondition(new SpotCondition(Filters.siteControlledByOtherPlayer(self.getOwner()))), 2));
}
}
