package com.gempukku.lotro.cards.set10.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.OptionalEffect;
import com.gempukku.lotro.logic.effects.PutPlayedEventOnTopOfDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;

/**
 * Set: Mount Doom
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Make a minion skirmishing an Elf strength -2. Spot a site in a support area to place this event on top
 * of your draw deck.
 */
public class Card10_010 extends AbstractEvent {
    public Card10_010() {
        super(Side.FREE_PEOPLE, 1, Culture.ELVEN, "Fleet-footed", Phase.SKIRMISH);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, -2, CardType.MINION, Filters.inSkirmishAgainst(Race.ELF)));
        if (Filters.canSpot(game, CardType.SITE, Zone.SUPPORT))
            action.appendEffect(
                    new OptionalEffect(action, playerId,
                            new PutPlayedEventOnTopOfDeckEffect(self)));
        return action;
    }
}
