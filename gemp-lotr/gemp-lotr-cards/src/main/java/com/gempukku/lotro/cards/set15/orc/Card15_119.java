package com.gempukku.lotro.cards.set15.orc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: The Hunters
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 1
 * Type: Event • Shadow
 * Game Text: Spot 2 [ORC] minions to discard a Free Peoples condition. The Free Peoples player may add (2) for each
 * Free Peoples condition you can spot to prevent that.
 */
public class Card15_119 extends AbstractEvent {
    public Card15_119() {
        super(Side.SHADOW, 1, Culture.ORC, "Unreasonable Choice", Phase.SHADOW);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Culture.ORC, CardType.MINION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose a card to discard", Side.FREE_PEOPLE, CardType.CONDITION, Filters.canBeDiscarded(self)) {
                    @Override
                    protected void cardSelected(final LotroGame game, PhysicalCard card) {
                        action.appendEffect(
                                new PreventableEffect(action,
                                        new DiscardCardsFromPlayEffect(playerId, self, card),
                                        game.getGameState().getCurrentPlayerId(),
                                        new PreventableEffect.PreventionCost() {
                                            @Override
                                            public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                                return new AddTwilightEffect(self, 2 * Filters.countActive(game, Side.FREE_PEOPLE, CardType.CONDITION));
                                            }
                                        }
                                ));
                    }
                });
        return action;
    }
}
