package com.gempukku.lotro.cards.set10.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.CountActiveEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.MultiplyEvaluator;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 1
 * Type: Possession • Mount
 * Strength: +1
 * Vitality: +1
 * Game Text: Bearer must be a [SAURON] Orc. For each site you control, bearer is strength +1 (or +2 if bearer
 * is Gothmog).
 */
public class Card10_089 extends AbstractAttachable {
    public Card10_089() {
        super(Side.SHADOW, CardType.POSSESSION, 1, Culture.SAURON, PossessionClass.MOUNT, "Gothmog's Warg", null, true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.SAURON, Race.ORC);
    }

    @Override
    public int getStrength() {
        return 1;
    }

    @Override
    public int getVitality() {
        return 1;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, Filters.and(Filters.name("Gothmog"), Filters.hasAttached(self)), null,
                        new MultiplyEvaluator(2, new CountActiveEvaluator(Filters.siteControlled(self.getOwner())))));
        modifiers.add(
                new StrengthModifier(self, Filters.and(Filters.not(Filters.name("Gothmog")), Filters.hasAttached(self)), null,
                        new CountActiveEvaluator(Filters.siteControlled(self.getOwner()))));
        return modifiers;
    }
}
