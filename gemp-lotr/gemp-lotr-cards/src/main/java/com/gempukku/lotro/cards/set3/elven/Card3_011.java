package com.gempukku.lotro.cards.set3.elven;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Set: Realms of Elf-lords
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 1
 * Type: Event
 * Game Text: Maneuver: Spot an Elf to make each opponent discard a card from his or her hand. Draw a card for each
 * card discarded in this way.
 */
public class Card3_011 extends AbstractEvent {
    public Card3_011() {
        super(Side.FREE_PEOPLE, 1, Culture.ELVEN, "Cast It Into the Fire!", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.ELF);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        String[] opponents = GameUtils.getShadowPlayers(game);
        final AtomicInteger integer = new AtomicInteger(0);
        for (String opponent : opponents) {
            action.appendEffect(
                    new ChooseAndDiscardCardsFromHandEffect(action, opponent, true, 1) {
                        @Override
                        protected void cardsBeingDiscardedCallback(Collection<PhysicalCard> cardsBeingDiscarded) {
                            integer.addAndGet(cardsBeingDiscarded.size());
                        }
                    });
        }
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        action.insertEffect(
                                new DrawCardsEffect(action, playerId, integer.get()));
                    }
                });
        return action;
    }
}
