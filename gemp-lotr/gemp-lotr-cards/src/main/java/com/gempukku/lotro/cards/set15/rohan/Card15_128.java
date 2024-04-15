package com.gempukku.lotro.cards.set15.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Hunters
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 5
 * Vitality: 3
 * Resistance: 6
 * Game Text: While you can spot 2 mounts, Haleth is strength +2. While you can spot 3 mounts, Haleth is damage +1.
 * While you can spot 4 Free Peoples mounts, those mounts can not be discarded by a Shadow player.
 */
public class Card15_128 extends AbstractCompanion {
    public Card15_128() {
        super(2, 5, 3, 6, Culture.ROHAN, Race.MAN, null, "Haleth", "Son of Háma", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, self, new SpotCondition(2, PossessionClass.MOUNT), 2));
        modifiers.add(
                new KeywordModifier(self, self, new SpotCondition(3, PossessionClass.MOUNT), Keyword.DAMAGE, 1));
        modifiers.add(
                new CantDiscardFromPlayByPlayerModifier(self, "Can't be discarded by a Shadow player", new SpotCondition(4, Side.FREE_PEOPLE, PossessionClass.MOUNT),
                        Filters.and(Side.FREE_PEOPLE, PossessionClass.MOUNT), self.getOwner()));
        return modifiers;
    }
}
