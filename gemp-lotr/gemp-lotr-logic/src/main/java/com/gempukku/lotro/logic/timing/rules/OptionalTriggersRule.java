package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class OptionalTriggersRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public OptionalTriggersRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalBeforeTriggers(String playerId, LotroGame game, Effect effect) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activableCard : Filters.filter(game, game.getGameState().getInPlay(), getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activableCard)) {
                                final List<? extends OptionalTriggerAction> actions = activableCard.getBlueprint().getOptionalBeforeTriggers(playerId, game, effect, activableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult) {
                        List<OptionalTriggerAction> result = new LinkedList<>();
                        for (PhysicalCard activableCard : Filters.filter(game, game.getGameState().getInPlay(), getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activableCard)) {
                                final List<? extends OptionalTriggerAction> actions = activableCard.getBlueprint().getOptionalAfterTriggers(playerId, game, effectResult, activableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }
                }
        );
    }

    private Filter getActivatableCardsFilter(String playerId) {
        return Filters.or(
                Filters.and(CardType.SITE,
                        new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                if (game.getGameState().getCurrentPhase().isRealPhase())
                                    return Filters.currentSite.accepts(game, physicalCard);
                                return false;
                            }
                        }),
                Filters.and(Filters.not(CardType.SITE), Filters.owner(playerId), Filters.active));
    }
}