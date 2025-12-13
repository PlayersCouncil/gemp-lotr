package com.gempukku.lotro.logic.timing.processes;

import com.gempukku.lotro.game.state.LotroGame;

public interface GameProcess {
    void process(LotroGame game);

    GameProcess getNextProcess();
}
