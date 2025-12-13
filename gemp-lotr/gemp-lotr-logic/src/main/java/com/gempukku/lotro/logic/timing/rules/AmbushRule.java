package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.effects.AddTwilightEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.AssignAgainstResult;

import java.util.Collections;
import java.util.List;

public class AmbushRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public AmbushRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.ASSIGNED_AGAINST) {
                            AssignAgainstResult assignmentResult = (AssignAgainstResult) effectResult;
                            if (assignmentResult.getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                                PhysicalCard assignedCard = assignmentResult.getAssignedCard();
                                if (Filters.and(CardType.MINION, Keyword.AMBUSH, Filters.owner(playerId)).accepts(game, assignedCard)) {
                                    final int count = game.getModifiersQuerying().getKeywordCount(game, assignedCard, Keyword.AMBUSH);
                                    OptionalTriggerAction action = new OptionalTriggerAction("ambush" + assignedCard.getCardId(), assignedCard);
                                    action.setMessage(playerId + " uses Ambush (" + count + ") from " + GameUtils.getCardLink(assignedCard));
                                    action.setText("Ambush - add " + count);
                                    action.appendEffect(
                                            new AddTwilightEffect(assignedCard, AddTwilightEffect.Cause.AMBUSH, count));
                                    return Collections.singletonList(action);
                                }
                            }
                        }
                        return null;
                    }
                });
    }
}
