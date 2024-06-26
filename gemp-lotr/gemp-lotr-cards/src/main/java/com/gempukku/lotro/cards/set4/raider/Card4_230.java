package com.gempukku.lotro.cards.set4.raider;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotBurdensCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 4
 * Type: Minion • Man
 * Strength: 9
 * Vitality: 2
 * Site: 4
 * Game Text: Easterling. While you can spot 3 burdens, this minion is fierce and damage +1.
 */
public class Card4_230 extends AbstractMinion {
    public Card4_230() {
        super(4, 9, 2, 4, Race.MAN, Culture.RAIDER, "Easterling Trooper");
        addKeyword(Keyword.EASTERLING);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, self, new SpotBurdensCondition(3), Keyword.FIERCE, 1));
        modifiers.add(
                new KeywordModifier(self, self, new SpotBurdensCondition(3), Keyword.DAMAGE, 1));
        return modifiers;
    }
}
