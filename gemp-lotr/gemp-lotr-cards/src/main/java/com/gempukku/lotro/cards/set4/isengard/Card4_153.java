package com.gempukku.lotro.cards.set4.isengard;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 2
 * Type: Minion • Man
 * Strength: 4
 * Vitality: 3
 * Site: 3
 * Game Text: Each unbound companion (or ally) bearing a Shadow condition is strength -1.
 */
public class Card4_153 extends AbstractMinion {
    public Card4_153() {
        super(2, 4, 3, 3, Race.MAN, Culture.ISENGARD, "Gríma", "Son of Galmód", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new StrengthModifier(self,
                        Filters.and(
                                Filters.hasAttached(Filters.and(Side.SHADOW, CardType.CONDITION)),
                                Filters.or(
                                        Filters.unboundCompanion,
                                        CardType.ALLY
                                )), -1));
    }
}
