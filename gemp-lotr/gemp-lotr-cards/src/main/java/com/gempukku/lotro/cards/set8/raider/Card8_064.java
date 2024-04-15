package com.gempukku.lotro.cards.set8.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.condition.InitiativeCondition;
import com.gempukku.lotro.logic.modifiers.condition.MinThreatCondition;
import com.gempukku.lotro.logic.modifiers.condition.OrCondition;

import java.util.LinkedList;
import java.util.List;

/**
 * Set: Siege of Gondor
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 3
 * Type: Possession • Mount
 * Strength: +3
 * Game Text: Bearer must be a Southron. Bearer is fierce. While you have initiative or can spot 4 threats, bearer
 * is damage +1.
 */
public class Card8_064 extends AbstractAttachable {
    public Card8_064() {
        super(Side.SHADOW, CardType.POSSESSION, 3, Culture.RAIDER, PossessionClass.MOUNT, "Mûmakil");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Keyword.SOUTHRON;
    }

    @Override
    public int getStrength() {
        return 3;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<>();
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self), Keyword.FIERCE));
        modifiers.add(
                new KeywordModifier(self, Filters.hasAttached(self),
                        new OrCondition(
                                new InitiativeCondition(Side.SHADOW),
                                new MinThreatCondition(4)
                        ), Keyword.DAMAGE, 1));
        return modifiers;
    }
}
