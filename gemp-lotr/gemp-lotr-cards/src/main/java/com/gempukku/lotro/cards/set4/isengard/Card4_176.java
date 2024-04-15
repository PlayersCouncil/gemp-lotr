package com.gempukku.lotro.cards.set4.isengard;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 4
 * Type: Minion • Uruk-Hai
 * Strength: 9
 * Vitality: 3
 * Site: 5
 * Game Text: Tracker. Fierce. The roaming penalty for each [ISENGARD] tracker you play is -2. While you can spot
 * 2 [ISENGARD] trackers, Ugluk is strength +3. While you can spot 3 [ISENGARD] trackers, Ugluk is damage +1.
 */
public class Card4_176 extends AbstractMinion {
    public Card4_176() {
        super(4, 9, 3, 5, Race.URUK_HAI, Culture.ISENGARD, "Uglúk", "Servant of Saruman", true);
        addKeyword(Keyword.TRACKER);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new RoamingPenaltyModifier(self, Filters.and(Culture.ISENGARD, Keyword.TRACKER), -2));
        modifiers.add(
                new StrengthModifier(self, self, new SpotCondition(2, Filters.and(Culture.ISENGARD, Keyword.TRACKER)), 3));
        modifiers.add(
                new KeywordModifier(self, self, new SpotCondition(3, Filters.and(Culture.ISENGARD, Keyword.TRACKER)), Keyword.DAMAGE, 1));
        return modifiers;
    }
}
