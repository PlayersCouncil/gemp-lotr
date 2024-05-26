package com.gempukku.lotro.cards.set18.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 1
 * Type: Possession • Armor
 * Strength: +1
 * Game Text: Bearer must be a [GONDOR] Man. Bearer is a knight.
 */
public class Card18_039 extends AbstractAttachableFPPossession {
    public Card18_039() {
        super(1, 1, 0, Culture.GONDOR, PossessionClass.ARMOR, "Armor of the White City");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.GONDOR, Race.MAN);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new AddKeywordModifier(self, Filters.hasAttached(self), Keyword.KNIGHT));
    }
}
