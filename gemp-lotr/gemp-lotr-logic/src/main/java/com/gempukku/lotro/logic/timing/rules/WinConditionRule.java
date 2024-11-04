package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.ReconcileResult;

import java.util.List;

public class WinConditionRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public WinConditionRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResults) {
                        if (game.getGameState().getCurrentPhase() == Phase.REGROUP
                                && game.getGameState().getCurrentSiteNumber() == 9) {
                            if (isWinAtReconcile(game)) {
                                if (effectResults.getType() == EffectResult.Type.RECONCILE
                                        && !((ReconcileResult) effectResults).getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                                    game.playerWon(game.getGameState().getCurrentPlayerId(), "Surviving to end of Regroup phase on site 9");
                                }
                            } else if (effectResults.getType() == EffectResult.Type.START_OF_PHASE) {
                                game.playerWon(game.getGameState().getCurrentPlayerId(), "Surviving to Regroup phase on site 9");
                            }
                        } else if (game.getFormat().winOnControlling5Sites()
                                && effectResults.getType() == EffectResult.Type.CONTROL_SITE) {
                            for (String opponent : GameUtils.getShadowPlayers(game)) {
                                if (Filters.countActive(game, CardType.SITE, Filters.siteControlledByPlayer(opponent)) >= 5)
                                    game.playerWon(opponent, "Controls 5 sites");
                            }
                        }
                        return null;
                    }
                });
    }

    private boolean isWinAtReconcile(LotroGame game) {
        return (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.WIN_CHECK_AFTER_SHADOW_RECONCILE)
                || game.getFormat().winWhenShadowReconciles());
    }
}
