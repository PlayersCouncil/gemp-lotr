package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 0
 * Type: Event • Skirmish
 * Game Text: If you have initiative, discard 2 cards from hand to wound a roaming minion skirmishing a [GONDOR] Man
 * twice.
 */
public class Card7_120 extends AbstractEvent {
    public Card7_120() {
        super(Side.FREE_PEOPLE, 0, Culture.GONDOR, "Stand to Arms", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.hasInitiative(game, Side.FREE_PEOPLE)
                && PlayConditions.canDiscardFromHand(game, self.getOwner(), 2, Filters.any);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 2));
        action.appendEffect(
                new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, 2, CardType.MINION, Keyword.ROAMING, Filters.inSkirmishAgainst(Culture.GONDOR, Race.MAN)));
        return action;
    }
}
