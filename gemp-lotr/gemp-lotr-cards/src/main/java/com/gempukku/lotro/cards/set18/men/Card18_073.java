package com.gempukku.lotro.cards.set18.men;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 5
 * Type: Minion • Man
 * Strength: 10
 * Vitality: 3
 * Site: 2
 * Game Text: While the fellowship is in region 1, this minion is strength +4. While the fellowship is in region 2,
 * this minion is strength +2.
 */
public class Card18_073 extends AbstractMinion {
    public Card18_073() {
        super(5, 10, 3, 2, Race.MAN, Culture.MEN, "Rough Man of the South");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, self, new LocationCondition(Filters.region(1)), 4));
        modifiers.add(
                new StrengthModifier(self, self, new LocationCondition(Filters.region(2)), 2));
        return modifiers;
    }
}
