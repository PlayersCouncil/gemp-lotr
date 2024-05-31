package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersFromHandRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public OptionalTriggersFromHandRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        final Side side = GameUtils.getSide(game, playerId);
                        for (PhysicalCard responseEvent : Filters.filter(game, game.getGameState().getHand(playerId), side)) {
                            final List<OptionalTriggerAction> actions = responseEvent.getBlueprint().getOptionalInHandAfterTriggers(playerId, game, effectResult, responseEvent);
                            if (actions != null)
                                result.addAll(actions);
                        }
                        return result;
                    }
                });
    }
}
