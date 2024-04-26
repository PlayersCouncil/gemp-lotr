package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.IntegerAwaitingDecision;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.PregameSetupFeedback;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MappingPreBidGameProcess implements GameProcess {
    private final Set<String> _players;
    private final PlayerOrderFeedback _playerOrderFeedback;
    private final PregameSetupFeedback _pregameSetupFeedback;

    public MappingPreBidGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback,
            PregameSetupFeedback pregameSetupFeedback) {
        _players = players;
        _playerOrderFeedback = playerOrderFeedback;
        _pregameSetupFeedback = pregameSetupFeedback;
    }

    @Override
    public void process(LotroGame game) {
        _pregameSetupFeedback.populatePregameInfo();
    }


    @Override
    public GameProcess getNextProcess() {
        return new BiddingGameProcess(_players, _playerOrderFeedback);
    }
}
