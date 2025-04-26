package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.RestoreCardsInPlayEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ReconcileResult;

import java.util.Collections;
import java.util.List;

public class RestoreRule {
    private final DefaultActionsEnvironment defaultActionsEnvironment;

    public RestoreRule(DefaultActionsEnvironment defaultActionsEnvironment) {
        this.defaultActionsEnvironment = defaultActionsEnvironment;
    }

    public void applyRule() {
        defaultActionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {

                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (TriggerConditions.reconciles(game, effectResult)) {
                            var result = (ReconcileResult) effectResult;

                            RequiredTriggerAction action = new RequiredTriggerAction(null);
                            action.setText("Reconcile restores hindered cards.");
                            action.appendEffect(
                                    new RestoreCardsInPlayEffect((PhysicalCard) null,
                                            //For now, we are only restoring cards owned by the reconciling player; this may
                                            // be re-evaluated in the future depending.
                                            Filters.owner(result.getPlayerId()),
                                            Filters.hindered
                                    ));

                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });
    }
}
