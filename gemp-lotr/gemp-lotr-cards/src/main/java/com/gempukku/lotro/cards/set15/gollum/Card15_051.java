package com.gempukku.lotro.cards.set15.gollum;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ShuffleDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDeckEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Hunters
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 2
 * Type: Event • Shadow
 * Game Text: Spot Gollum or Smeagol to play a minion from your draw deck. If that minion is a [GOLLUM] minion, you may
 * also play a Shadow possession or Shadow condition from your draw deck. Shuffle your draw deck.
 */
public class Card15_051 extends AbstractEvent {
    public Card15_051() {
        super(Side.SHADOW, 2, Culture.GOLLUM, "Sudden Strike", Phase.SHADOW);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.gollumOrSmeagol);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndPlayCardFromDeckEffect(playerId, CardType.MINION) {
                    @Override
                    protected void afterCardPlayed(PhysicalCard cardPlayed) {
                        if (Filters.and(Culture.GOLLUM, CardType.MINION).accepts(game, cardPlayed)) {
                            action.insertEffect(
                                    new ChooseAndPlayCardFromDeckEffect(playerId, Side.SHADOW, Filters.or(CardType.POSSESSION, CardType.CONDITION)));
                        }
                    }
                });
        action.appendEffect(
                new ShuffleDeckEffect(playerId));
        return action;
    }
}
