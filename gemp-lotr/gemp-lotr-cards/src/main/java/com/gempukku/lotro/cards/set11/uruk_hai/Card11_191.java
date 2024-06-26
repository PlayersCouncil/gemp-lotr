package com.gempukku.lotro.cards.set11.uruk_hai;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 1
 * Type: Possession • Ranged Weapon
 * Vitality: +1
 * Game Text: Bearer must be an [URUK-HAI] minion. While bearer is at a battleground site, it is an archer.
 */
public class Card11_191 extends AbstractAttachable {
    public Card11_191() {
        super(Side.SHADOW, CardType.POSSESSION, 1, Culture.URUK_HAI, PossessionClass.RANGED_WEAPON, "Isengard Siege Bow");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.URUK_HAI, CardType.MINION);
    }

    @Override
    public int getVitality() {
        return 1;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self), new LocationCondition(Keyword.BATTLEGROUND), Keyword.ARCHER, 1));
        return modifiers;
    }
}
