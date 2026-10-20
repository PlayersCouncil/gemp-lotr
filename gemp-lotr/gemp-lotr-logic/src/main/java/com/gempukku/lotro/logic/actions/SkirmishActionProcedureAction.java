package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.loops.LoopNegotiation;
import com.gempukku.lotro.logic.timing.loops.LoopRun;
import com.gempukku.lotro.logic.timing.loops.LoopTracker;

import java.util.List;

/**
 * The players take skirmish actions in turn until each in a row has passed, or the skirmish is cancelled or loses
 * a side.  One instance lasts one skirmish, and so does its loop step's tracker (CR 4.2 "loops", LoopTracker), with
 * the run of a loop the players have settled (LoopRun), which is carried out before anybody is asked anything.
 */
public class SkirmishActionProcedureAction extends SystemQueueAction {
    private LoopTracker _loopTracker;

    public SkirmishActionProcedureAction() {
        appendEffect(
                new StartPlayingActionsEffect());
    }

    private class StartPlayingActionsEffect extends UnrespondableEffect {
        @Override
        protected void doPlayEffect(LotroGame game) {
            PlayOrder playOrder = game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(game.getGameState().getCurrentPlayerId(), true);
            _loopTracker = new LoopTracker(playOrder.getPlayerCount());
            appendEffect(
                    new CheckIfSkirmishFinishedEffect(playOrder, 0));
        }
    }

    private class CheckIfSkirmishFinishedEffect extends UnrespondableEffect {
        private final PlayOrder _playOrder;
        private final int _consecutivePasses;

        private CheckIfSkirmishFinishedEffect(PlayOrder playOrder, int consecutivePasses) {
            _playOrder = playOrder;
            _consecutivePasses = consecutivePasses;
        }

        @Override
        protected void doPlayEffect(LotroGame game) {
            Skirmish skirmish = game.getGameState().getSkirmish();
            if (!game.getGameState().getSkirmish().isCancelled()
                    && skirmish.getFellowshipCharacter() != null && skirmish.getShadowCharacters().size() > 0) {
                appendEffect(
                        new PlayerPlaysNextActionEffect(_playOrder, _consecutivePasses));
            } else if (_loopTracker.getRun() != null) {
                _loopTracker.getRun().stop(game, "the skirmish is over");
            }
        }
    }

    // The actions a player can take: those not barred after a loop (they are offered, and refused if taken).
    private static int countUsable(LotroGame game, List<Action> actions) {
        int count = 0;
        for (Action action : actions) {
            if (!game.getActionsEnvironment().isLoopBanned(action))
                count++;
        }
        return count;
    }

    private class PlayerPlaysNextActionEffect extends UnrespondableEffect {
        private final PlayOrder _playOrder;
        private final int _consecutivePasses;
        // The player to be asked, where it is already settled (after the loop step's prompts); null to take the
        // next player of the order.
        private final String _playerToAsk;

        private PlayerPlaysNextActionEffect(PlayOrder playOrder, int consecutivePasses) {
            this(playOrder, consecutivePasses, null);
        }

        private PlayerPlaysNextActionEffect(PlayOrder playOrder, int consecutivePasses, String playerToAsk) {
            _playOrder = playOrder;
            _consecutivePasses = consecutivePasses;
            _playerToAsk = playerToAsk;
        }

        @Override
        protected void doPlayEffect(final LotroGame game) {
            _loopTracker.offering(game);

            String playerId;
            int consecutivePasses = _consecutivePasses;
            final LoopRun run = _loopTracker.getRun();
            if (run != null) {
                // CR 4.2 "loops": the loop the players settled is carried out, an action at a time, as if each
                // were chosen; then the player it leaves to act is asked.
                final Action step = run.nextAction(game);
                if (step != null) {
                    appendEffect(
                            new CheckIfSkirmishFinishedEffect(_playOrder, 0));
                    game.getActionsEnvironment().addActionToStack(step);
                    return;
                }
                _loopTracker.runOver();
                game.getGameState().setConsecutiveAction(false);
                playerId = takeTurnOf(run.getNextActor());
                // A run that ended on the other player's pass (F-0565) leaves this player to act after that pass.
                if (run.endedOnPass())
                    consecutivePasses = 1;
            } else if (_playerToAsk != null) {
                playerId = _playerToAsk;
            } else if (game.getGameState().isConsecutiveAction()) {
                playerId = _playOrder.getLastPlayer();
                game.getGameState().setConsecutiveAction(false);
            } else {
                playerId = _playOrder.getNextPlayer();
            }

            final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(playerId);

            // CR 4.2 "loops": the player would be offered the first action of a loop for the third time.  The
            // players settle how many more times it runs, and the run is carried out before anybody is asked.
            final LoopTracker.Loop loop = (_playerToAsk == null && run == null) ? _loopTracker.findLoop(game, playerId, playableActions) : null;
            if (loop != null) {
                game.getActionsEnvironment().addActionToStack(new LoopNegotiation(game, loop, _loopTracker::agreed));
                appendEffect(
                        new PlayerPlaysNextActionEffect(_playOrder, _consecutivePasses, playerId));
                return;
            }

            final int passesBefore = consecutivePasses;
            if (countUsable(game, playableActions) == 0 && game.shouldAutoPass(playerId, Phase.SKIRMISH)) {
                playerPassed(playerId, passesBefore);
            } else {
                game.getUserFeedback().sendAwaitingDecision(playerId,
                        new CardActionSelectionDecision(game, 1, "Choose action to play or Pass", playableActions) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _loopTracker.chose(game, playerId, action);
                                    appendEffect(
                                            new CheckIfSkirmishFinishedEffect(_playOrder, 0));
                                    game.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    playerPassed(playerId, passesBefore);
                                }
                            }
                        });
            }
        }

        // The player to act after a loop's run, the order brought up to him: the next player of the order is the
        // one after him.
        private String takeTurnOf(String playerId) {
            for (int i = 0; i < _playOrder.getPlayerCount() && !playerId.equals(_playOrder.getLastPlayer()); i++)
                _playOrder.getNextPlayer();
            return playerId;
        }

        private void playerPassed(String playerId, int consecutivePasses) {
            _loopTracker.passed(playerId);
            if (consecutivePasses + 1 < _playOrder.getPlayerCount())
                appendEffect(
                        new PlayerPlaysNextActionEffect(_playOrder, consecutivePasses + 1));
        }
    }
}
