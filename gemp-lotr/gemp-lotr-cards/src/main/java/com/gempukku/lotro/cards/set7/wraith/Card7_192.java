package com.gempukku.lotro.cards.set7.wraith;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 2
 * Type: Minion • Orc
 * Strength: 5
 * Vitality: 2
 * Site: 4
 * Game Text: While you can spot 2 wounds on the Ring-bearer, this minion is strength +4 and fierce.
 */
public class Card7_192 extends AbstractMinion {
    public Card7_192() {
        super(2, 5, 2, 4, Race.ORC, Culture.WRAITH, "Morgul Hound");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, self, new SpotCondition(Filters.ringBearer, Filters.hasWounds(2)), 4));
        modifiers.add(
                new KeywordModifier(self, self, new SpotCondition(Filters.ringBearer, Filters.hasWounds(2)), Keyword.FIERCE, 1));
        return modifiers;
    }
}
