package com.gempukku.lotro.cards.set6.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotBurdensCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 0
 * Type: Possession • Hand Weapon
 * Strength: +2
 * Game Text: Bearer must be an Easterling. While you can spot 2 burdens, bearer cannot take wounds. While you can spot
 * 5 burdens, bearer is damage +1.
 */
public class Card6_079 extends AbstractAttachable {
    public Card6_079() {
        super(Side.SHADOW, CardType.POSSESSION, 0, Culture.RAIDER, PossessionClass.HAND_WEAPON, "Easterling Polearm");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Keyword.EASTERLING;
    }

    @Override
    public int getStrength() {
        return 2;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new CantTakeWoundsModifier(self, new SpotBurdensCondition(2), Filters.hasAttached(self)));
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self), new SpotBurdensCondition(5), Keyword.DAMAGE, 1));
        return modifiers;
    }
}
