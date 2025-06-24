package com.gempukku.lotro.logic.timing.processes.turn.skirmish;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.AssignmentGameProcess;
import com.gempukku.lotro.logic.timing.processes.turn.RegroupGameProcess;

public class AfterSkirmishesGameProcess implements GameProcess {
    private GameProcess _followingGameProcess;

    @Override
    public void process(LotroGame game) {
        GameState gameState = game.getGameState();
        // Extra skirmishes are the final round of skirmishes; we are done with skirmishing at this point.
        // This flag will be set for us during the EndSkirmishesGameProcess if an effect like The Witch-king's Beast
        // is triggered (or if Relentless minions are on the board).
        if (gameState.isExtraSkirmishes()) {
            gameState.setExtraSkirmishes(false);
            _followingGameProcess = new RegroupGameProcess();
        }
        // If we haven't yet processed fierce skirmishes, we will do so now
        else if (!gameState.isFierceSkirmishes() && Filters.countActive(game, CardType.MINION, Keyword.FIERCE) > 0) {
            gameState.setFierceSkirmishes(true);
            gameState.sendMessage("Fierce skirmishes.");
            _followingGameProcess = new AssignmentGameProcess();
        }
        else {
            if (gameState.isFierceSkirmishes()) {
                gameState.setFierceSkirmishes(false);
            }
            _followingGameProcess = new EndSkirmishesGameProcess();
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
