package com.gempukku.lotro.cards.set4.elven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.Condition;
import com.gempukku.lotro.logic.modifiers.DoesNotAddToArcheryTotalModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Companion • Elf
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Game Text: To play, spot an Elf.
 * While Thonnas bears a ranged weapon, each minion skirmishing him is strength -2 and Thonnas does not add to
 * the fellowship archery total.
 */
public class Card4_086 extends AbstractCompanion {
    public Card4_086() {
        super(2, 6, 3, 6, Culture.ELVEN, Race.ELF, null, "Thónnas", "Naith Captain", true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.ELF);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new StrengthModifier(self, Filters.and(CardType.MINION, Filters.inSkirmishAgainst(self)),
                        new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                return Filters.countActive(game, self, Filters.hasAttached(PossessionClass.RANGED_WEAPON)) > 0;
                            }
                        }, -2));
        modifiers.add(
                new DoesNotAddToArcheryTotalModifier(self, self,
                        new Condition() {
                            @Override
                            public boolean isFullfilled(LotroGame game) {
                                return Filters.countActive(game, self, Filters.hasAttached(PossessionClass.RANGED_WEAPON)) > 0;
                            }
                        }));
        return modifiers;
    }
}
