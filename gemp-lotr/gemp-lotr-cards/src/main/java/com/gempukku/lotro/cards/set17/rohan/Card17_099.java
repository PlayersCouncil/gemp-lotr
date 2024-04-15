package com.gempukku.lotro.cards.set17.rohan;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.modifiers.CancelKeywordBonusTargetModifier;
import com.gempukku.lotro.logic.modifiers.CancelStrengthBonusTargetModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 2
 * Type: Companion • Man
 * Strength: 6
 * Vitality: 3
 * Resistance: 6
 * Game Text: While the Ringbearer is assigned to a skirmish, each minion skirmishing Hama loses strength bonuses
 * and damage bonuses from possessions.
 */
public class Card17_099 extends AbstractCompanion {
    public Card17_099() {
        super(2, 6, 3, 6, Culture.ROHAN, Race.MAN, null, "Háma", "Northman", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new CancelStrengthBonusTargetModifier(self,
                        new SpotCondition(Filters.ringBearer, Filters.assignedToSkirmish),
                        Filters.and(CardType.MINION, Filters.inSkirmishAgainst(self)), CardType.POSSESSION));
        modifiers.add(
                new CancelKeywordBonusTargetModifier(self, Keyword.DAMAGE,
                        new SpotCondition(Filters.ringBearer, Filters.assignedToSkirmish),
                        Filters.and(CardType.MINION, Filters.inSkirmishAgainst(self)), CardType.POSSESSION));
        return modifiers;
    }
}
