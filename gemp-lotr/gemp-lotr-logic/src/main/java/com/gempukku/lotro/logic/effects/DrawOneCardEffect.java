package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.Preventable;
import com.gempukku.lotro.logic.timing.results.DrawCardOrPutIntoHandResult;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DrawOneCardEffect extends AbstractEffect implements Preventable {
    private final String _playerId;
    private final boolean _reconcileDraw;
    private boolean _prevented;

    public DrawOneCardEffect(String playerId) {
        this(playerId, false);
    }

    /**
     * @param reconcileDraw true when this draw is part of a player's end-of-regroup reconciliation
     *                      (drawing back up to hand size). Such draws are never subject to
     *                      OPPONENT_FILTERS_YOUR_DRAWS - see the "(except when reconciling)" clause on cards
     *                      that grant that flag (e.g. 94_28). All other draws, including other card-effect
     *                      draws played during the regroup phase, remain subject to it.
     */
    public DrawOneCardEffect(String playerId, boolean reconcileDraw) {
        _playerId = playerId;
        _reconcileDraw = reconcileDraw;
    }

    @Override
    public String getText(LotroGame game) {
        return "Draw a card";
    }

    @Override
    public Effect.Type getType() {
        return Type.BEFORE_DRAW_CARD;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return game.getGameState().getDeck(_playerId).size() >= 1;
    }

    public boolean canDrawCard(LotroGame game) {
        return (!_prevented && game.getGameState().getDeck(_playerId).size() > 0) && game.getModifiersQuerying().canDrawCardNoIncrement(game, _playerId);
    }

    @Override
    public String getPerformingPlayer() {
        return _playerId;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        int drawn = 0;
        if (!_prevented && game.getGameState().getDeck(_playerId).size() > 0 &&
                (!game.getFormat().hasRuleOfFour() || game.getModifiersQuerying().canDrawCardAndIncrementForRuleOfFour(game, _playerId))) {
            if (!_reconcileDraw && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.OPPONENT_FILTERS_YOUR_DRAWS, _playerId))
                opponentFiltersDraw(game);
            else
                game.getGameState().playerDrawsCard(_playerId);
            drawn++;
        }

        if (drawn == 1) {
            game.getActionsEnvironment().emitEffectResult(new DrawCardOrPutIntoHandResult(_playerId, true));
            return new FullEffectResult(true);
        } else
            return new FullEffectResult(false);
    }

    /**
     * Replacement draw used while OPPONENT_FILTERS_YOUR_DRAWS is active for the drawing player (and this is not a
     * reconcile draw): the opponent looks at the top 2 cards of the deck, picks the one the drawing player takes
     * into hand, and the other goes to the bottom of the deck.  With only one card left in the deck there is
     * nothing to choose, so it is simply drawn.
     */
    private void opponentFiltersDraw(LotroGame game) {
        final GameState gameState = game.getGameState();
        final List<? extends PhysicalCard> deck = gameState.getDeck(_playerId);
        if (deck.size() < 2) {
            gameState.playerDrawsCard(_playerId);
            return;
        }

        final List<PhysicalCard> topCards = new LinkedList<>(deck.subList(0, 2));
        final PlayOrder playOrder = gameState.getPlayerOrder().getCounterClockwisePlayOrder(_playerId, false);
        playOrder.getNextPlayer();
        final String opponent = playOrder.getNextPlayer();
        if (opponent == null || opponent.equals(_playerId)) {
            gameState.playerDrawsCard(_playerId);
            return;
        }

        gameState.sendMessage(opponent + " looks at the top 2 cards of " + _playerId + "'s draw deck");

        game.getUserFeedback().sendAwaitingDecision(opponent,
                new ArbitraryCardsSelectionDecision(1, "Choose the card " + _playerId + " draws", topCards, topCards, 1, 1) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selected = getSelectedCardsByResponse(result);
                        PhysicalCard chosen = selected.isEmpty() ? topCards.get(0) : selected.iterator().next();

                        gameState.removeCardsFromZone(null, Collections.singleton(chosen));
                        gameState.addCardToZone(null, chosen, Zone.HAND);

                        for (PhysicalCard other : topCards) {
                            if (other != chosen) {
                                gameState.removeCardsFromZone(null, Collections.singleton(other));
                                gameState.putCardOnBottomOfDeck(other);
                            }
                        }

                        gameState.sendMessage(opponent + " gives " + GameUtils.getCardLink(chosen) + " to " + _playerId
                                + " and places the other card beneath their draw deck");
                    }
                });
    }

    @Override
    public void prevent() {
        _prevented = true;
    }

    @Override
    public boolean isPrevented(LotroGame game) {
        return _prevented;
    }
}
