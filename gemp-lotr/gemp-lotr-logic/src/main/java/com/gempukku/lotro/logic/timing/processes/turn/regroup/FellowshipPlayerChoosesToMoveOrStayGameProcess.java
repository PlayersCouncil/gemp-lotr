package com.gempukku.lotro.logic.timing.processes.turn.regroup;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.processes.AsksAgain;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.EndOfTurnGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ShadowPhasesGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.general.EndOfPhaseGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.move.MovementGameProcess;
import com.gempukku.lotro.logic.timing.results.FreePlayerMoveDecisionResult;

public class FellowshipPlayerChoosesToMoveOrStayGameProcess implements GameProcess, AsksAgain {
    private GameProcess _nextProcess;
    // The player asked whether to move again, while the question waits for an answer (AsksAgain); null otherwise
    private String _asking;

    @Override
    public void process(final LotroGame game) {
        final GameState gameState = game.getGameState();
        final String currentPlayerId = gameState.getCurrentPlayerId();
        if (RuleUtils.canChooseToMoveAgain(game)) {
            if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.HAS_TO_MOVE_IF_POSSIBLE)) {
                playerMoves(game);
            } else {
                _asking = currentPlayerId;
                game.getUserFeedback().sendAwaitingDecision(currentPlayerId,
                        new MultipleChoiceAwaitingDecision(1, "Do you want to make another move?", new String[]{"Yes", "No"}) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                _asking = null;
                                if (result.equals("Yes"))
                                    playerMoves(game);
                                else {
                                    playerStays(game);
                                }
                            }
                        });
            }
        } else {
            // CR 5.0 "hinder": with no unhindered companion the Free Peoples player cannot choose to move.  The
            // player is not asked, and is told why, since the move limit alone would not explain it.
            if (!RuleUtils.fellowshipHasUnhinderedCompanion(game))
                gameState.sendMessage(currentPlayerId + " has no unhindered companion and cannot choose to move again.");
            playerStays(game);
        }
    }

    private void playerMoves(LotroGame game) {

        game.getGameState().sendMessage(game.getGameState().getCurrentPlayerId() + " decides to move again.");

        _nextProcess = new MovementGameProcess(
                new EndOfPhaseGameProcess(Phase.REGROUP,
                        new ShadowPhasesGameProcess()));

        final SystemQueueAction action = new SystemQueueAction();
        action.appendEffect(
                new TriggeringResultEffect(new FreePlayerMoveDecisionResult(true), "Free Peoples player decides to move again."));
        game.getActionsEnvironment().addActionToStack(action);
    }

    private void playerStays(LotroGame game) {
        game.getGameState().sendMessage(game.getGameState().getCurrentPlayerId() + " decides to stay and reconcile.");

        _nextProcess = game.getFormat().getAdventure().getPlayerStaysGameProcess(game,
                new EndOfPhaseGameProcess(Phase.REGROUP,
                        new EndOfTurnGameProcess()));

        final SystemQueueAction action = new SystemQueueAction();
        action.appendEffect(
                new TriggeringResultEffect(new FreePlayerMoveDecisionResult(false), "Free Peoples player decides to stay."));
        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public boolean isAsking() {
        return _asking != null;
    }

    @Override
    public String askedPlayer() {
        return _asking;
    }

    /**
     * Asked again, whether the fellowship can move again is worked out afresh, as on the first asking: if it can no
     * longer (an edit hindered every companion, say), it stays without being asked.
     */
    @Override
    public void askAgain(LotroGame game) {
        _asking = null;
        process(game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
