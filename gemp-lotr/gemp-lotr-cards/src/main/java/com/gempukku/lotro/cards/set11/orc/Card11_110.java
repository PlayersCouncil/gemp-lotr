package com.gempukku.lotro.cards.set11.orc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddBurdenEffect;
import com.gempukku.lotro.logic.effects.ChoiceEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.RevealHandEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: Shadows
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 1
 * Type: Event • Shadow
 * Game Text: Spot an [ORC] minion to reveal the Free Peoples player's hand. The Free Peoples player chooses to either
 * discard a revealed Free Peoples event or add a burden.
 */
public class Card11_110 extends AbstractEvent {
    public Card11_110() {
        super(Side.SHADOW, 1, Culture.ORC, "Bound to its Fate", Phase.SHADOW);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.ORC, CardType.MINION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        final String fpPlayer = game.getGameState().getCurrentPlayerId();
        action.appendEffect(
                new RevealHandEffect(self, playerId, fpPlayer) {
                    @Override
                    protected void cardsRevealed(Collection<? extends PhysicalCard> cards) {
                        List<Effect> possibleEffects = new LinkedList<>();
                        possibleEffects.add(
                                new ChooseAndDiscardCardsFromHandEffect(action, fpPlayer, false, 1, Side.FREE_PEOPLE, CardType.EVENT) {
                                    @Override
                                    public String getText(LotroGame game) {
                                        return "Discard a revealed Free Peoples event";
                                    }
                                });
                        possibleEffects.add(
                                new AddBurdenEffect(fpPlayer, self, 1));
                        action.appendEffect(
                                new ChoiceEffect(action, fpPlayer, possibleEffects));
                    }
                });
        return action;
    }
}
