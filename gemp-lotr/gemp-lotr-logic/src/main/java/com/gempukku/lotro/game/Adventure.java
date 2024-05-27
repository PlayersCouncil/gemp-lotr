package com.gempukku.lotro.game;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.modifiers.ModifiersLogic;
import com.gempukku.lotro.logic.timing.PlayerOrderFeedback;
import com.gempukku.lotro.logic.timing.PregameSetupFeedback;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.Set;

public interface Adventure {
    void applyAdventureRules(LotroGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic);

    GameProcess getStartingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback, PregameSetupFeedback pregameSetupFeedback);

    GameProcess getAfterFellowshipPhaseGameProcess();

    void appendNextSiteAction(SystemQueueAction action);

    GameProcess getAfterFellowshipArcheryGameProcess(int fellowshipArcheryTotal, GameProcess followingProcess);

    GameProcess getAfterFellowshipAssignmentGameProcess(Set<PhysicalCard> leftoverMinions, GameProcess followingProcess);

    GameProcess getBeforeFellowshipChooseToMoveGameProcess(GameProcess followingProcess);

    GameProcess getPlayerStaysGameProcess(LotroGame game, GameProcess followingProcess);

    boolean isSolo();
}
