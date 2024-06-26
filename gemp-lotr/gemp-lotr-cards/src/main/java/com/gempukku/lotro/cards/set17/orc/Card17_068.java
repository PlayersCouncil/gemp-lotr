package com.gempukku.lotro.cards.set17.orc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 0
 * Type: Condition • Support Area
 * Game Text: To play, spot no minions. Each time you play a minion, it is twilight cost +1. While you can spot
 * an [ORC] minion, each companion is strength -1.
 */
public class Card17_068 extends AbstractPermanent {
    public Card17_068() {
        super(Side.SHADOW, 0, CardType.CONDITION, Culture.ORC, "Chaotic Clash");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return !PlayConditions.canSpot(game, CardType.MINION);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new TwilightCostModifier(self, Filters.and(Filters.owner(self.getOwner()), CardType.MINION), 1));
        modifiers.add(
                new StrengthModifier(self, CardType.COMPANION, new SpotCondition(Culture.ORC, CardType.MINION), -1));
        return modifiers;
    }
}
