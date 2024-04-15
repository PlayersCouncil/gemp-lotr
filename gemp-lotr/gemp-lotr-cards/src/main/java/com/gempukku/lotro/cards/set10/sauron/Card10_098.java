package com.gempukku.lotro.cards.set10.sauron;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractResponseEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.modifiers.PlayerCantPlayCardsModifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 0
 * Type: Event • Response
 * Game Text: If the Free Peoples player plays a possession, discard a [SAURON] minion from hand to prevent him or her
 * from playing any more cards until the end of this phase.
 */
public class Card10_098 extends AbstractResponseEvent {
    public Card10_098() {
        super(Side.SHADOW, 0, Culture.SAURON, "Ruinous Hall");
    }

    @Override
    public List<PlayEventAction> getPlayResponseEventAfterActions(String playerId, final LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, Filters.owner(game.getGameState().getCurrentPlayerId()), CardType.POSSESSION)
                && PlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false)
                && PlayConditions.canDiscardCardsFromHandToPlay(self, game, playerId, 1, Culture.SAURON, CardType.MINION)) {
            PlayEventAction action = new PlayEventAction(self);
            action.appendCost(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 1, Culture.SAURON, CardType.MINION));
            action.appendEffect(
                    new AddUntilEndOfPhaseModifierEffect(
                            new PlayerCantPlayCardsModifier(self, game.getGameState().getCurrentPlayerId())));
            return Collections.singletonList(action);
        }
        return null;
    }
}
