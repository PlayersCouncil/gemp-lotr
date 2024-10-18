package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.AssignmentEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.timing.Action;

public class ChooseAndAssignCharacterToMinionEffect extends ChooseActiveCardEffect {
    private final Action _action;
    private final String _playerId;
    private final PhysicalCard _minion;
    private final boolean _skipAllyLocationCheck;

    public ChooseAndAssignCharacterToMinionEffect(Action action, String playerId, PhysicalCard minion, Filterable... filters) {
        this(action, playerId, minion, false, filters);
    }

    public ChooseAndAssignCharacterToMinionEffect(Action action, String playerId, PhysicalCard minion, boolean skipAllyLocationCheck, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose character to assign " + GameUtils.getCardLink(minion) + " to", filters);
        _action = action;
        _playerId = playerId;
        _minion = minion;
        _skipAllyLocationCheck = skipAllyLocationCheck;
    }

    @Override
    protected Filter getExtraFilterForPlaying(final LotroGame game) {
        final Side side = game.getGameState().getCurrentPlayerId().equals(_playerId) ? Side.FREE_PEOPLE : Side.SHADOW;
        return Filters.assignableToSkirmishAgainst(side, _minion, false, _skipAllyLocationCheck);
    }

    @Override
    protected void cardSelected(LotroGame game, PhysicalCard card) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(
                new AssignmentEffect(_playerId, card, _minion, _skipAllyLocationCheck));
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}
