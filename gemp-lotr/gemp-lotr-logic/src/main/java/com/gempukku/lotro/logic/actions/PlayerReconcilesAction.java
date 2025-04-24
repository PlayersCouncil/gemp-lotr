package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.*;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.ReconcileResult;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class PlayerReconcilesAction implements Action {
    private final LotroGame _game;
    private final String _playerId;

    private Queue<Effect> _effectQueue;

    public PlayerReconcilesAction(LotroGame game, String playerId) {
        _game = game;
        _playerId = playerId;
    }

    @Override
    public Type getType() {
        return Type.RECONCILE;
    }

    @Override
    public void setVirtualCardAction(boolean virtualCardAction) {
    }

    @Override
    public boolean isVirtualCardAction() {
        return false;
    }

    @Override
    public Timeword getActionTimeword() {
        return null;
    }

    @Override
    public void setActionTimeword(Timeword timeword) {
    }

    @Override
    public String getPerformingPlayer() {
        return null;
    }

    @Override
    public void setPerformingPlayer(String playerId) {
    }

    @Override
    public PhysicalCard getActionSource() {
        return null;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Player reconciles";
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (_effectQueue == null) {

            _effectQueue = new LinkedList<>();

            final int handSize = game.getFormat().getHandSize();

            GameState gameState = _game.getGameState();
            final Set<? extends PhysicalCard> cardsInHand = new HashSet<PhysicalCard>(gameState.getHand(_playerId));

            // Formats which are set to end the game at the end of the regroup phase instead of at the start of the regroup phase
            // should prematurely end the game instead of doing a true reconcile.
            if(game.getFormat().winWhenShadowReconciles() && game.getGameState().getCurrentPhase() == Phase.REGROUP
                    && game.getGameState().getCurrentSiteNumber() == 9 && !_playerId.equals(game.getGameState().getCurrentPlayerId())) {
                game.getGameState().sendMessage("End of regroup phase reached.");
                _effectQueue.add(new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
            }
            else {
                game.getGameState().sendMessage(_playerId + " reconciles");

                if (cardsInHand.size() > handSize) {
                    _effectQueue.add(new PlayoutDecisionEffect(_playerId,
                            new CardsSelectionDecision(1, "Choose cards to discard down to "+handSize, cardsInHand, cardsInHand.size() - handSize, cardsInHand.size() - handSize) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                    _effectQueue.add(new DiscardCardsFromHandEffect(null, _playerId, cards, false));
                                    _effectQueue.add(
                                            new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                                }
                            }));
                } else if (!cardsInHand.isEmpty()) {
                    _effectQueue.add(new PlayoutDecisionEffect(_playerId,
                            new CardsSelectionDecision(1, "Reconcile - choose card to discard or press DONE", cardsInHand, 0, 1) {
                                @Override
                                public void decisionMade(String result) throws DecisionResultInvalidException {
                                    Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                                    if (!selectedCards.isEmpty()) {
                                        _effectQueue.add(new DiscardCardsFromHandEffect(null, _playerId, selectedCards, false));
                                    }
                                    //We refresh the hand size here because effects could have reacted to the discard, causing
                                    // the previous hand size calculation to be stale.
                                    //See https://github.com/PlayersCouncil/gemp-lotr/issues/89
                                    //As the discard selection is only queued, we need to subtract it from the current hand size.
                                    int cardsInHandAfterDiscard = gameState.getHand(_playerId).size() - selectedCards.size();
                                    if (cardsInHandAfterDiscard < handSize) {
                                        _effectQueue.add(new DrawCardsEffect(PlayerReconcilesAction.this, _playerId, handSize - cardsInHandAfterDiscard));
                                    }
                                    _effectQueue.add(
                                            new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                                }
                            }));
                } else {
                    _effectQueue.add(new DrawCardsEffect(PlayerReconcilesAction.this, _playerId, handSize));
                    _effectQueue.add(
                            new TriggeringResultEffect(new ReconcileResult(_playerId), "Player reconciled"));
                }
            }
        }

        return _effectQueue.poll();
    }
}
