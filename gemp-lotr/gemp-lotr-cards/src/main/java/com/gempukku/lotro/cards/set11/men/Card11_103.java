package com.gempukku.lotro.cards.set11.men;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Men
 * Twilight Cost: 2
 * Type: Minion • Man
 * Strength: 8
 * Vitality: 1
 * Site: 4
 * Game Text: Ambush (1). While this minion is at a plains site, it gains an additional ambush (1).
 */
public class Card11_103 extends AbstractMinion {
    public Card11_103() {
        super(2, 8, 1, 4, Race.MAN, Culture.MEN, "Warrior of Dunland");
        addKeyword(Keyword.AMBUSH, 1);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new KeywordModifier(self, self, new LocationCondition(Keyword.PLAINS), Keyword.AMBUSH, 1));
}
}
