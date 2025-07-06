package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.DiscardCardsFromPlayResult;

import java.util.Collections;
import java.util.List;

public class DiscardedCardRule {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public DiscardedCardRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
                            var discardResult = (DiscardCardsFromPlayResult) effectResult;
                            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                            RequiredTriggerAction trigger = discardedCard.getBlueprint().getDiscardedFromPlayRequiredTrigger(game, discardResult);
                            if (trigger != null)
                                return Collections.singletonList(trigger);
                        }
                        return null;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY) {
                            var discardResult = (DiscardCardsFromPlayResult) effectResult;
                            final PhysicalCard discardedCard = discardResult.getDiscardedCard();
                            if (discardedCard.getOwner().equals(playerId)) {
                                OptionalTriggerAction trigger = discardedCard.getBlueprint().getDiscardedFromPlayOptionalTrigger(game, discardResult);
                                if (trigger != null) {
                                    trigger.setVirtualCardAction(true);
                                    return Collections.singletonList(trigger);
                                }
                            }
                        }
                        return null;
                    }
                });
    }
}
