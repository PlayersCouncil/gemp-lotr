package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.MaxThreatCondition;
import com.gempukku.lotro.logic.modifiers.Modifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 3
 * Type: Artifact • Hand Weapon
 * Strength: +2
 * Game Text: Bearer must be Aragorn. If you cannot spot a threat, Aragorn is defender +1.
 */
public class Card7_080 extends AbstractAttachableFPPossession {
    public Card7_080() {
        super(3, 2, 0, Culture.GONDOR, CardType.ARTIFACT, PossessionClass.HAND_WEAPON, "Andúril", "King's Blade", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.aragorn;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new KeywordModifier(self, Filters.hasAttached(self), new MaxThreatCondition(0), Keyword.DEFENDER, 1));
    }
}
