package com.gempukku.lotro.cards.set8.gandalf;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDeckIntoHandEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Siege of Gondor
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 3
 * Type: Event • Fellowship
 * Game Text: Spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take 3 cards from that
 * companion’s culture into hand from your draw deck. Shuffle your draw deck.
 */
public class Card8_020 extends AbstractEvent {
    public Card8_020() {
        super(Side.FREE_PEOPLE, 3, Culture.GANDALF, "Saved From the Fire", Phase.FELLOWSHIP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.gandalf);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseActiveCardEffect(self, playerId, "Choose a companion", CardType.COMPANION, Filters.not(Filters.ringBearer)) {
                    @Override
                    protected void cardSelected(LotroGame game, final PhysicalCard card) {
                        action.insertCost(
                                new KillEffect(card, self, KillEffect.Cause.CARD_EFFECT));
                        for (int i = 0; i < 3; i++)
                            action.appendEffect(
                                    new ChooseAndPutCardFromDeckIntoHandEffect(action, playerId, 0, 1, card.getBlueprint().getCulture()));
                        action.appendEffect(
                                new ShuffleDeckEffect(playerId));
                    }
                });
        return action;
    }
}
