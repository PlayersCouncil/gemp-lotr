package com.gempukku.lotro.cards.set7.sauron;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.MinionSiteNumberModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.condition.InitiativeCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 2
 * Type: Minion • Orc
 * Strength: 6
 * Vitality: 2
 * Site: 6
 * Game Text: Tracker. The site number of each [SAURON] Orc is -1. While you have initiative, this minion
 * is strength +6.
 */
public class Card7_298 extends AbstractMinion {
    public Card7_298() {
        super(2, 6, 2, 6, Race.ORC, Culture.SAURON, "Orc Chaser");
        addKeyword(Keyword.TRACKER);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new MinionSiteNumberModifier(self, Filters.and(Culture.SAURON, Race.ORC), null, -1));
        modifiers.add(
                new StrengthModifier(self, self, new InitiativeCondition(Side.SHADOW), 6));
        return modifiers;
    }
}
