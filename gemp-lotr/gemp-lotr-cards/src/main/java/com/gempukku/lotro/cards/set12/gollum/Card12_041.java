package com.gempukku.lotro.cards.set12.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.CantHealModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: To play, spot a [GOLLUM] card. Bearer must be a wounded companion. While you can spot a [GOLLUM] character
 * or a [GOLLUM] card in your support area, bearer cannot heal.
 */
public class Card12_041 extends AbstractAttachable {
    public Card12_041() {
        super(Side.SHADOW, CardType.CONDITION, 0, Culture.GOLLUM, null, "Treacherous Little Toad");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.GOLLUM);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(CardType.COMPANION, Filters.wounded);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new CantHealModifier(self, new SpotCondition(Culture.GOLLUM, Filters.or(Filters.character, Filters.and(Zone.SUPPORT, Filters.owner(self.getOwner())))), Filters.hasAttached(self)));
}
}
