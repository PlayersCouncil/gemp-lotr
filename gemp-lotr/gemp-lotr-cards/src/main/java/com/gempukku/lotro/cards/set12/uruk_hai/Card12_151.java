package com.gempukku.lotro.cards.set12.uruk_hai;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 4
 * Type: Minion • Uruk-Hai
 * Strength: 11
 * Vitality: 2
 * Site: 5
 * Game Text: Damage +1. Each unbound companion is resistance -2 for each wound he or she has.
 */
public class Card12_151 extends AbstractMinion {
    public Card12_151() {
        super(4, 11, 2, 5, Race.URUK_HAI, Culture.URUK_HAI, "Uruk Desecrator");
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public java.util.List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return java.util.Collections.singletonList(new ResistanceModifier(self, Filters.unboundCompanion, null,
                new Evaluator() {
                    @Override
                    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                        return -2 * game.getGameState().getWounds(cardAffected);
                    }
                }));
    }
}
