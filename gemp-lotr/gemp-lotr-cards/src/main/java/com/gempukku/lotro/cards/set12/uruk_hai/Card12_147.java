package com.gempukku.lotro.cards.set12.uruk_hai;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 4
 * Type: Minion • Uruk-Hai
 * Strength: 11
 * Vitality: 2
 * Site: 5
 * Game Text: Damage +1. While there is a companion in the dead pile, each unbound companion is resistance -4.
 */
public class Card12_147 extends AbstractMinion {
    public Card12_147() {
        super(4, 11, 2, 5, Race.URUK_HAI, Culture.URUK_HAI, "Suppressing Uruk", null, true);
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public java.util.List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return java.util.Collections.singletonList(new ResistanceModifier(self, Filters.unboundCompanion,
                new Condition() {
                    @Override
                    public boolean isFullfilled(LotroGame game) {
                        return Filters.filter(game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId()), game, CardType.COMPANION).size() > 0;
                    }
                }, -4));
    }
}
