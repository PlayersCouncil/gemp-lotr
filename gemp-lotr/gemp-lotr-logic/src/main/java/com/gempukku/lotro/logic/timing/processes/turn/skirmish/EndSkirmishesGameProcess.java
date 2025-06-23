package com.gempukku.lotro.logic.timing.processes.turn.skirmish;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.AssignmentGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.RegroupGameProcess;
import com.gempukku.lotro.logic.timing.results.AfterAllSkirmishesResult;

public class EndSkirmishesGameProcess implements GameProcess {
    private final AfterAllSkirmishesResult _afterAllSkirmishesResult = new AfterAllSkirmishesResult();
    private LotroGame _game;

    @Override
    public void process(LotroGame game) {
        SystemQueueAction action = new SystemQueueAction();
        action.setText("After all skirmishes");
        action.appendEffect(
                new TriggeringResultEffect(null, _afterAllSkirmishesResult, "After all skirmishes"));
        game.getActionsEnvironment().addActionToStack(action);
        _game = game;
    }

    @Override
    public GameProcess getNextProcess() {
        if (_afterAllSkirmishesResult.isCreateAnExtraAssignmentAndSkirmishPhases(_game)) {
            return new GameProcess() {
                @Override
                public void process(LotroGame game) {
                    game.getGameState().setExtraSkirmishes(true);
                }

                @Override
                public GameProcess getNextProcess() {
                    return new AssignmentGameProcess();
                }
            };
        } else {
            return new RegroupGameProcess();
        }
    }
}
