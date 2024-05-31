package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class PlayResponseEventRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public PlayResponseEventRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalAfterActions(String playerId, LotroGame game, EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        final Side side = GameUtils.getSide(game, playerId);
                        for (PhysicalCard responseEvent : Filters.filter(game, game.getGameState().getHand(playerId), side, CardType.EVENT, Keyword.RESPONSE)) {
                            if (PlayUtils.checkPlayRequirements(game, responseEvent, Filters.any, 0, 0, false, false, false)) {
                                final List<PlayEventAction> actions = responseEvent.getBlueprint().getPlayResponseEventAfterActions(playerId, game, effectResult, responseEvent);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }
                        return result;
                    }

                    @Override
                    public List<? extends Action> getOptionalBeforeActions(String playerId, LotroGame game, Effect effect) {
                        List<Action> result = new LinkedList<>();
                        final Side side = GameUtils.getSide(game, playerId);
                        for (PhysicalCard responseEvent : Filters.filter(game, game.getGameState().getHand(playerId), side, CardType.EVENT, Keyword.RESPONSE)) {
                            if (PlayUtils.checkPlayRequirements(game, responseEvent, Filters.any, 0, 0, false, false, false)) {
                                final List<PlayEventAction> actions = responseEvent.getBlueprint().getPlayResponseEventBeforeActions(playerId, game, effect, responseEvent);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }
                        return result;
                    }
                }
        );
    }
}
