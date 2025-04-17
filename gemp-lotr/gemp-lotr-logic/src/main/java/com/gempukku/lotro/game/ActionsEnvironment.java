package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActionsEnvironment {
    List<Action> getRequiredBeforeTriggers(Effect effect);

    List<Action> getOptionalBeforeTriggers(String playerId, Effect effect);

    List<Action> getOptionalBeforeActions(String playerId, Effect effect);

    List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults);

    Map<OptionalTriggerAction, EffectResult> getOptionalAfterTriggers(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getOptionalAfterActions(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getPhaseActions(String playerId);

    void addUntilStartOfPhaseActionProxy(ActionProxy actionProxy, Phase phase);

    void addUntilEndOfPhaseActionProxy(ActionProxy actionProxy, Phase phase);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);
    void addAlwaysOnActionProxy(ActionProxy actionProxy);

    void addActionToStack(Action action);

    void emitEffectResult(EffectResult effectResult);

    <T extends Action> T findTopmostActionOfType(Class<T> clazz);

    List<EffectResult> getTurnEffectResults();

    List<EffectResult> getPhaseEffectResults();
}