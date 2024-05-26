package com.gempukku.lotro.cards.set18.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.CanSpotFPCulturesCondition;
import com.gempukku.lotro.logic.modifiers.condition.CanSpotShadowCulturesCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 4
 * Type: Companion • Man
 * Strength: 8
 * Vitality: 4
 * Resistance: 8
 * Game Text: Hunter 2. While you can spot 3 Free Peoples cultures, Aragorn is defender +1. While you can spot 2 Shadow
 * cultures, Aragorn is defender +1.
 */
public class Card18_038 extends AbstractCompanion {
    public Card18_038() {
        super(4, 8, 4, 8, Culture.GONDOR, Race.MAN, null, "Aragorn", "Heir to the Throne of Gondor", true);
        addKeyword(Keyword.HUNTER, 2);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new AddKeywordModifier(self, self, new CanSpotFPCulturesCondition(self.getOwner(), 3), Keyword.DEFENDER, 1));
        modifiers.add(
                new AddKeywordModifier(self, self, new CanSpotShadowCulturesCondition(self.getOwner(), 2), Keyword.DEFENDER, 1));
        return modifiers;
    }
}
