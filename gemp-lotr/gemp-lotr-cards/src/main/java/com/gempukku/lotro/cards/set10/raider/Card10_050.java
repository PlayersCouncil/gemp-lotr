package com.gempukku.lotro.cards.set10.raider;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 3
 * Type: Minion • Man
 * Strength: 8
 * Vitality: 2
 * Site: 4
 * Game Text: Southron. While the total number of other minions and twilight tokens is 3 or fewer, this minion
 * is strength +3 and fierce.
 */
public class Card10_050 extends AbstractMinion {
    public Card10_050() {
        super(3, 8, 2, 4, Race.MAN, Culture.RAIDER, "Southron Savage");
        addKeyword(Keyword.SOUTHRON);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(final LotroGame game, final PhysicalCard self) {
        Condition con = new Condition() {
            @Override
            public boolean isFullfilled(LotroGame game) {
                return Filters.countActive(game, Filters.not(self), CardType.MINION) + game.getGameState().getTwilightPool() <= 3;
            }
        };

        final List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, self, con, 3));
        modifiers.add(
                new KeywordModifier(self, self, con, Keyword.FIERCE, 1));
        return modifiers;
    }
}
