package com.gempukku.lotro.cards.set15.uruk_hai;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import com.gempukku.lotro.logic.modifiers.condition.NotCondition;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 2
 * Type: Possession • Hand Weapon
 * Strength: +1
 * Game Text: Bearer must be an [URUK-HAI] Uruk-hai. While you control 2 or more sites, bearer can not take wounds
 * (except during skirmish phases). If bearer is Ugluk, he is damage +1.
 */
public class Card15_173 extends AbstractAttachable {
    public Card15_173() {
        super(Side.SHADOW, CardType.POSSESSION, 2, Culture.URUK_HAI, PossessionClass.HAND_WEAPON, "Uglúk's Sword", "Weapon of Command", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.URUK_HAI, Race.URUK_HAI);
    }

    @Override
    public int getStrength() {
        return 1;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new CantTakeWoundsModifier(self,
                        new AndCondition(
                                new NotCondition(new PhaseCondition(Phase.SKIRMISH)),
                                new SpotCondition(2, Filters.siteControlled(self.getOwner()))),
                        Filters.hasAttached(self)));
        modifiers.add(
                new KeywordModifier(self, Filters.and(Filters.hasAttached(self), Filters.name("Ugluk")), Keyword.DAMAGE, 1));
        return modifiers;
    }
}
