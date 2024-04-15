package com.gempukku.lotro.cards.set12.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ForEachYouSpotEffect;
import com.gempukku.lotro.logic.effects.ReorderTopCardsOfDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 7
 * Type: Event • Fellowship
 * Game Text: Toil 3. (For each [GANDALF] character you exert when playing this, its twilight cost is -3)
 * Spot a [GANDALF] wizard and X other companions to examine the top X cards of your draw deck. Replace those cards
 * in any order.
 */
public class Card12_026 extends AbstractEvent {
    public Card12_026() {
        super(Side.FREE_PEOPLE, 7, Culture.GANDALF, "Discoveries", Phase.FELLOWSHIP);
        addKeyword(Keyword.TOIL, 3);
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.GANDALF, Race.WIZARD);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ForEachYouSpotEffect(playerId, CardType.COMPANION, Filters.not(Culture.GANDALF, Race.WIZARD)) {
                    @Override
                    protected void spottedCards(int spotCount) {
                        action.appendEffect(
                                new ReorderTopCardsOfDeckEffect(action, playerId, spotCount));
                    }
                });
        return action;
    }
}
