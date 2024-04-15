package com.gempukku.lotro.cards.set11.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.HealCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Shadows
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 3
 * Type: Event • Fellowship
 * Game Text: Spot an Elf to heal each companion who has resistance 5 or more.
 */
public class Card11_019 extends AbstractEvent {
    public Card11_019() {
        super(Side.FREE_PEOPLE, 3, Culture.ELVEN, "Farewell to Lórien", Phase.FELLOWSHIP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.ELF);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action= new PlayEventAction(self);
        action.appendEffect(
                new HealCharactersEffect(self, self.getOwner(), CardType.COMPANION, Filters.minResistance(5)));
        return action;
    }
}
