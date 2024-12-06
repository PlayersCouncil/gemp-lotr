package com.gempukku.lotro.logic.timing.processes.turn.regroup;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.EndOfTurnGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.ShadowPhasesGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.general.EndOfPhaseGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.move.MovementGameProcess;
import com.gempukku.lotro.logic.timing.results.FreePlayerMoveDecisionResult;

public class FellowshipPlayerChoosesToMoveOrStayGameProcess implements GameProcess {
    private GameProcess _nextProcess;

    @Override
    public void process(final LotroGame game) {
        final GameState gameState = game.getGameState();
        final String currentPlayerId = gameState.getCurrentPlayerId();
        if (!game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_MOVE)
                && gameState.getMoveCount() < RuleUtils.calculateMoveLimit(game)) {
            if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.HAS_TO_MOVE_IF_POSSIBLE)) {
                playerMoves(game);
            } else {
                game.getUserFeedback().sendAwaitingDecision(currentPlayerId,
                        new MultipleChoiceAwaitingDecision(1, "Do you want to make another move?", new String[]{"Yes", "No"}) {
                            @Override
                            protected void validDecisionMade(int index, String result) {
                                if (result.equals("Yes"))
                                    playerMoves(game);
                                else {
                                    playerStays(game);
                                }
                            }
                        });
            }
        } else {
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
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
