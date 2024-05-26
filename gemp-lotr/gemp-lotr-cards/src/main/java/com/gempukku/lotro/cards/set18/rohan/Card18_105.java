package com.gempukku.lotro.cards.set18.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 3
 * Type: Companion • Man
 * Strength: 7
 * Vitality: 3
 * Resistance: 6
 * Game Text: While you can spot Gandalf, Theoden is resistance +2. While you can spot Eomer, Theoden is damage +1.
 * While you can spot 3 mounted [ROHAN] companions, Theoden is strength +2.
 */
public class Card18_105 extends AbstractCompanion {
    public Card18_105() {
        super(3, 7, 3, 6, Culture.ROHAN, Race.MAN, null, Names.theoden, "Ednew", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new ResistanceModifier(self, self, new SpotCondition(Filters.gandalf), 2));
        modifiers.add(
                new AddKeywordModifier(self, self, new SpotCondition(Filters.name(Names.eomer)), Keyword.DAMAGE, 1));
        modifiers.add(
                new StrengthModifier(self, self, new SpotCondition(3, Culture.ROHAN, CardType.COMPANION, Filters.mounted), 2));
        return modifiers;
    }
}
