package com.gempukku.lotro.cards.set4.isengard;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 7
 * Type: Minion • Uruk-Hai
 * Strength: 14
 * Vitality: 3
 * Site: 5
 * Game Text: Damage +1. To play, spot an Uruk-hai. While at a battleground, this minion is fierce. While you control
 * a battleground, this minion is strength +6. While you control 2 battlegrounds, this minion may not take wounds.
 */
public class Card4_179 extends AbstractMinion {
    public Card4_179() {
        super(7, 14, 3, 5, Race.URUK_HAI, Culture.ISENGARD, "Uruk Assault Band");
        addKeyword(Keyword.DAMAGE);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.URUK_HAI);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, self, new LocationCondition(Keyword.BATTLEGROUND), Keyword.FIERCE, 1));
        modifiers.add(
                new StrengthModifier(self, self, new SpotCondition(Filters.siteControlled(self.getOwner()), Keyword.BATTLEGROUND), 6));
        modifiers.add(
                new CantTakeWoundsModifier(self, new SpotCondition(2, Filters.and(Filters.siteControlled(self.getOwner()), Keyword.BATTLEGROUND)), self));
        return modifiers;
    }
}
