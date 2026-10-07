package com.gempukku.lotro.game.state.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.ActionProxy;
import com.gempukku.lotro.game.ActionsEnvironment;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.ActionStack;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DefaultActionsEnvironment implements ActionsEnvironment {
    private static final Logger LOG = LogManager.getLogger(DefaultActionsEnvironment.class);
    private final LotroGame _lotroGame;
    private final ActionStack _actionStack;
    private final List<ActionProxy> _actionProxies = new LinkedList<>();
    private final Map<Phase, List<ActionProxy>> _untilStartOfPhaseActionProxies = new HashMap<>();
    private final Map<Phase, List<ActionProxy>> _untilEndOfPhaseActionProxies = new HashMap<>();
    private final List<ActionProxy> _untilEndOfTurnActionProxies = new LinkedList<>();

    private Set<EffectResult> _effectResults = new HashSet<>();

    private final List<EffectResult> turnEffectResults = new LinkedList<>();
    private final List<EffectResult> phaseEffectResults = new LinkedList<>();

    // Cards played from hand that are in play with their play not yet announced.
    private final Set<PhysicalCard> _enteringPlayFromHand = new HashSet<>();

    public DefaultActionsEnvironment(LotroGame lotroGame, ActionStack actionStack) {
        _lotroGame = lotroGame;
        _actionStack = actionStack;
    }

    public List<ActionProxy> getUntilStartOfPhaseActionProxies(Phase phase) {
        return _untilStartOfPhaseActionProxies.get(phase);
    }

    @Override
    public void emitEffectResult(EffectResult effectResult) {
        _effectResults.add(effectResult);
        turnEffectResults.add(effectResult);
        phaseEffectResults.add(effectResult);
    }

    @Override
    public void retractEffectResult(EffectResult effectResult) {
        turnEffectResults.remove(effectResult);
        phaseEffectResults.remove(effectResult);
    }

    @Override
    public void noteCardEnteringPlay(PhysicalCard card, Zone playedFrom) {
        if (playedFrom == Zone.HAND)
            _enteringPlayFromHand.add(card);
    }

    @Override
    public void noteCardEnteredPlay(PhysicalCard card) {
        _enteringPlayFromHand.remove(card);
    }

    @Override
    public int countCardsEnteringPlayFromHand(String playerId) {
        int count = 0;
        for (PhysicalCard card : _enteringPlayFromHand) {
            if (card.getOwner().equals(playerId))
                count++;
        }
        return count;
    }

    public Set<EffectResult> consumeEffectResults() {
        Set<EffectResult> result = _effectResults;
        _effectResults = new HashSet<>();
        return result;
    }

    @Override
    public void addAlwaysOnActionProxy(ActionProxy actionProxy) {
        _actionProxies.add(actionProxy);
    }

    public void signalStartOfPhase(Phase phase) {
        List<ActionProxy> list = _untilStartOfPhaseActionProxies.get(phase);
        if (list != null) {
            _actionProxies.removeAll(list);
            list.clear();
        }
    }

    public void signalEndOfPhase(Phase phase) {
        List<ActionProxy> list = _untilEndOfPhaseActionProxies.get(phase);
        if (list != null) {
            _actionProxies.removeAll(list);
            list.clear();
        }
        phaseEffectResults.clear();
    }

    public void signalEndOfTurn() {
        _actionProxies.removeAll(_untilEndOfTurnActionProxies);
        _untilEndOfTurnActionProxies.clear();
        turnEffectResults.clear();
        _enteringPlayFromHand.clear();
    }

    @Override
    public List<EffectResult> getTurnEffectResults() {
        return Collections.unmodifiableList(turnEffectResults);
    }

    @Override
    public List<EffectResult> getPhaseEffectResults() {
        return Collections.unmodifiableList(phaseEffectResults);
    }

    @Override
    public void addUntilStartOfPhaseActionProxy(ActionProxy actionProxy, Phase phase) {
        _actionProxies.add(actionProxy);
        List<ActionProxy> list = _untilStartOfPhaseActionProxies.get(phase);
        if (list == null) {
            list = new LinkedList<>();
            _untilStartOfPhaseActionProxies.put(phase, list);
        }
        list.add(actionProxy);
    }

    @Override
    public void addUntilEndOfPhaseActionProxy(ActionProxy actionProxy, Phase phase) {
        _actionProxies.add(actionProxy);
        List<ActionProxy> list = _untilEndOfPhaseActionProxies.get(phase);
        if (list == null) {
            list = new LinkedList<>();
            _untilEndOfPhaseActionProxies.put(phase, list);
        }
        list.add(actionProxy);
    }

    @Override
    public void addUntilEndOfTurnActionProxy(ActionProxy actionProxy) {
        _actionProxies.add(actionProxy);
        _untilEndOfTurnActionProxies.add(actionProxy);
    }

    @Override
    public List<Action> getRequiredBeforeTriggers(Effect effect) {
        List<Action> gatheredActions = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends RequiredTriggerAction> actions = actionProxy.getRequiredBeforeTriggers(_lotroGame, effect);
            if (actions != null) {
                gatheredActions.addAll(actions);
            }
        }

        return gatheredActions;
    }

    @Override
    public List<Action> getOptionalBeforeTriggers(String playerId, Effect effect) {
        List<Action> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends OptionalTriggerAction> actions = actionProxy.getOptionalBeforeTriggers(playerId, _lotroGame, effect);
            if (actions != null) {
                for (OptionalTriggerAction action : actions) {
                    action.setPerformingPlayer(playerId);
                    result.add(action);
                }
            }
        }

        return result;
    }

    @Override
    public List<Action> getOptionalBeforeActions(String playerId, Effect effect) {
        List<Action> result = new LinkedList<>();

        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getOptionalBeforeActions(playerId, _lotroGame, effect);
            if (actions != null) {
                for (Action action : actions) {
                    action.setPerformingPlayer(playerId);
                    if (_lotroGame.getModifiersQuerying().canPlayAction(_lotroGame, playerId, action))
                        result.add(action);
                }
            }
        }

        return result;
    }

    @Override
    public List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults) {
        List<Action> gatheredActions = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (EffectResult effectResult : effectResults) {
                    List<? extends RequiredTriggerAction> actions = actionProxy.getRequiredAfterTriggers(_lotroGame, effectResult);
                    if (actions != null)
                        gatheredActions.addAll(actions);
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public Map<OptionalTriggerAction, EffectResult> getOptionalAfterTriggers(String playerId, Collection<? extends EffectResult> effectResults) {
        final Map<OptionalTriggerAction, EffectResult> gatheredActions = new HashMap<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (EffectResult effectResult : effectResults) {
                    List<? extends OptionalTriggerAction> actions = actionProxy.getOptionalAfterTriggers(playerId, _lotroGame, effectResult);
                    if (actions != null) {
                        for (OptionalTriggerAction action : actions) {
                            if (!effectResult.wasOptionalTriggerUsed(action)) {
                                action.setPerformingPlayer(playerId);
                                gatheredActions.put(action, effectResult);
                            }
                        }
                    }
                }
            }
        }

        return gatheredActions;
    }

    @Override
    public List<Action> getOptionalAfterActions(String playerId, Collection<? extends EffectResult> effectResults) {
        List<Action> result = new LinkedList<>();

        if (effectResults != null) {
            for (ActionProxy actionProxy : _actionProxies) {
                for (EffectResult effectResult : effectResults) {
                    // A response is not offered to something that has already been cancelled.
                    if (effectResult.isCancelled())
                        continue;
                    List<? extends Action> actions = actionProxy.getOptionalAfterActions(playerId, _lotroGame, effectResult);
                    if (actions != null) {
                        for (Action action : actions) {
                            action.setPerformingPlayer(playerId);
                            if (_lotroGame.getModifiersQuerying().canPlayAction(_lotroGame, playerId, action))
                                result.add(action);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<Action> getPhaseActions(String playerId) {
        List<Action> result = new LinkedList<>();

        final Timeword timeword = Timeword.findByPhase(_lotroGame.getGameState().getCurrentPhase());
        for (ActionProxy actionProxy : _actionProxies) {
            List<? extends Action> actions = actionProxy.getPhaseActions(playerId, _lotroGame);
            if (actions != null) {
                for (Action action : actions) {
                    action.setPerformingPlayer(playerId);
                    action.setActionTimeword(timeword);
                    if (_lotroGame.getModifiersQuerying().canPlayAction(_lotroGame, playerId, action))
                        result.add(action);
                }
            }
        }

        return result;
    }

    @Override
    public void addActionToStack(Action action) {
        _actionStack.stackAction(action);
    }

    @Override
    public <T extends Action> T findTopmostActionOfType(Class<T> clazz) {
        return _actionStack.findTopmostActionOfType(clazz);
    }
}
