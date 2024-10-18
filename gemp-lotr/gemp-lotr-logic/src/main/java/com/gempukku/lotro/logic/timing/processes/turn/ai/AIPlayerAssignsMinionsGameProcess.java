package com.gempukku.lotro.logic.timing.processes.turn.ai;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.effects.AssignmentPhaseEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AIPlayerAssignsMinionsGameProcess implements GameProcess {
    private final Set<PhysicalCard> _leftoverMinions;
    private final GameProcess _followingGameProcess;

    public AIPlayerAssignsMinionsGameProcess(Set<PhysicalCard> leftoverMinions, GameProcess followingGameProcess) {
        _leftoverMinions = leftoverMinions;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(LotroGame game) {
        GameState gameState = game.getGameState();
        Filter minionFilter = Filters.and(CardType.MINION, Filters.owner("AI"), Filters.in(_leftoverMinions));

        final Set<PhysicalCard> minions = new HashSet<>(Filters.filterActive(game, minionFilter, Filters.assignableToSkirmish(Side.SHADOW, true, false)));
        if (minions.size() > 0) {
            final PhysicalCard ringBearer = game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId());
            Map<PhysicalCard, Set<PhysicalCard>> assignments = Collections.singletonMap(ringBearer, minions);

            if (game.getModifiersQuerying().isValidAssignments(game, Side.SHADOW, assignments)) {
                SystemQueueAction action = new SystemQueueAction();
                action.appendEffect(
                        new AssignmentPhaseEffect("AI", assignments, "Shadow player assignments"));

                game.getActionsEnvironment().addActionToStack(action);
            }
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
