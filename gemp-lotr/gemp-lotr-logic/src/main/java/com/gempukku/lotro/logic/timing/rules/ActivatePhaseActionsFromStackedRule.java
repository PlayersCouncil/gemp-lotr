package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.Action;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsFromStackedRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsFromStackedRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
                        List<Action> result = new LinkedList<>();
                        final Side side = GameUtils.getSide(game, playerId);
                        for (PhysicalCard activableCard : Filters.filter(game, game.getGameState().getStacked(playerId), side,
                                Filters.stackedOn(Filters.active))) {
                            List<? extends Action> list = activableCard.getBlueprint().getPhaseActionsFromStacked(playerId, game, activableCard);
                            if (list != null)
                                result.addAll(list);

                            final List<? extends Action> extraActions = game.getModifiersQuerying().getExtraPhaseActionsFromStacked(game, activableCard);
                            if (extraActions != null) {
                                for (Action action : extraActions) {
                                    if (action != null)
                                        result.add(action);
                                }
                            }
                        }
                        return result;
                    }
                });
    }
}
