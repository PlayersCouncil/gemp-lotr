package com.gempukku.lotro.logic.timing.processes.turn.general;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.PlayOrder;
import com.gempukku.lotro.logic.decisions.CardActionSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.loops.LoopNegotiation;
import com.gempukku.lotro.logic.timing.loops.LoopRun;
import com.gempukku.lotro.logic.timing.loops.LoopTracker;
import com.gempukku.lotro.logic.timing.processes.AsksAgain;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.List;

/**
 * The players take actions of the phase in turn (maneuver, archery, assignment and regroup) until each in a row has
 * passed.  Each step is a new instance; what carries over goes through the constructor, the loop step's tracker
 * (CR 4.2 "loops", LoopTracker) included, and with it the run of a loop the players have settled (LoopRun), which
 * is carried out before anybody is asked anything.
 */
public class PlayersPlayPhaseActionsInOrderGameProcess implements GameProcess, AsksAgain {
    private final PlayOrder _playOrder;
    private int _consecutivePasses;
    private final GameProcess _followingGameProcess;
    private final LoopTracker _loopTracker;
    // The player to be asked, where it is already settled (after the loop step's prompts); null to take the next
    // player of the order.
    private final String _playerToAsk;

    private GameProcess _nextProcess;
    // The player this step asked, and whether that choice of actions is still waiting for an answer (AsksAgain)
    private String _asked;
    private boolean _asking;

    public PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess) {
        this(playOrder, consecutivePasses, followingGameProcess, new LoopTracker(playOrder.getPlayerCount()), null);
    }

    private PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess,
                                                      LoopTracker loopTracker, String playerToAsk) {
        _playOrder = playOrder;
        _consecutivePasses = consecutivePasses;
        _followingGameProcess = followingGameProcess;
        _loopTracker = loopTracker;
        _playerToAsk = playerToAsk;
    }

    @Override
    public void process(final LotroGame game) {
        if (skirmishIsOver(game)) {
            // If the skirmish is cancelled or one side of the skirmish is missing, no more phase actions can be played
            if (_loopTracker.getRun() != null)
                _loopTracker.getRun().stop(game, "the skirmish is over");
            _nextProcess = _followingGameProcess;
        } else {
            _loopTracker.offering(game);

            String playerId;
            final LoopRun run = _loopTracker.getRun();
            if (run != null) {
                // CR 4.2 "loops": the loop the players settled is carried out, an action at a time, as if each
                // were chosen; then the player it leaves to act is asked.
                final Action step = run.nextAction(game);
                if (step != null) {
                    _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, 0, _followingGameProcess, _loopTracker, null);
                    game.getActionsEnvironment().addActionToStack(step);
                    return;
                }
                _loopTracker.runOver();
                game.getGameState().setConsecutiveAction(false);
                playerId = takeTurnOf(run.getNextActor());
                // A run that ended on the other player's pass (F-0565) leaves this player to act after that pass.
                if (run.endedOnPass())
                    _consecutivePasses = 1;
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
                _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, _consecutivePasses, _followingGameProcess, _loopTracker, playerId);
                return;
            }

            offer(game, playerId, playableActions);
        }
    }

    // If the skirmish is cancelled or one side of the skirmish is missing, no more phase actions can be played
    private static boolean skirmishIsOver(LotroGame game) {
        Skirmish skirmish = game.getGameState().getSkirmish();
        return game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && (game.getGameState().getSkirmish().isCancelled()
                || skirmish.getFellowshipCharacter() == null || skirmish.getShadowCharacters().size() == 0);
    }

    // The player passes by himself if he has nothing to play in a phase he is passed through; otherwise he is asked.
    private void offer(final LotroGame game, final String playerId, List<Action> playableActions) {
        if (countUsable(game, playableActions) == 0 && game.shouldAutoPass(playerId, game.getGameState().getCurrentPhase())) {
            playerPassed(playerId);
        } else {
            _asked = playerId;
            _asking = true;
            game.getUserFeedback().sendAwaitingDecision(playerId,
                    new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            _asking = false;
                            if (action != null) {
                                _loopTracker.chose(game, playerId, action);
                                _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, 0, _followingGameProcess, _loopTracker, null);
                                game.getActionsEnvironment().addActionToStack(action);
                            } else {
                                playerPassed(playerId);
                            }
                        }
                    });
        }
    }

    @Override
    public boolean isAsking() {
        return _asking;
    }

    @Override
    public String askedPlayer() {
        return _asked;
    }

    /**
     * Asks the same player again, the actions on offer worked out afresh (AsksAgain): the order is not moved on and
     * the count of passes in a row is kept, since nobody chose or passed.  In a skirmish, one that has lost a side
     * meanwhile (a character an edit left with no vitality died before this is called) takes no more actions, as on
     * a first asking.  The loop step (CR 4.2) is told nothing and not looked at, for the reasons the one-player
     * window gives (PlayerPlaysPhaseActionsUntilPassesGameProcess.askAgain).
     */
    @Override
    public void askAgain(LotroGame game) {
        _asking = false;
        if (skirmishIsOver(game)) {
            _nextProcess = _followingGameProcess;
            return;
        }
        offer(game, _asked, game.getActionsEnvironment().getPhaseActions(_asked));
    }

    // The player to act after a loop's run, the order brought up to him: the next player of the order is the one
    // after him.
    private String takeTurnOf(String playerId) {
        for (int i = 0; i < _playOrder.getPlayerCount() && !playerId.equals(_playOrder.getLastPlayer()); i++)
            _playOrder.getNextPlayer();
        return playerId;
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

    private void playerPassed(String playerId) {
        _loopTracker.passed(playerId);
        _consecutivePasses++;
        if (_consecutivePasses >= _playOrder.getPlayerCount())
            _nextProcess = _followingGameProcess;
        else
            _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, _consecutivePasses, _followingGameProcess, _loopTracker, null);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
