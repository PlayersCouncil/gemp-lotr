package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.timing.Action;

import java.util.LinkedList;
import java.util.List;

public class ActivatePhaseActionsRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivatePhaseActionsRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activableCard : Filters.filter(game, game.getGameState().getInPlay(), getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activableCard)) {
                                final List<? extends ActivateCardAction> actions = activableCard.getBlueprint().getPhaseActionsInPlay(playerId, game, activableCard);
                                if (actions != null)
                                    result.addAll(actions);

                                final List<? extends Action> extraActions = game.getModifiersQuerying().getExtraPhaseActions(game, activableCard);
                                if (extraActions != null) {
                                    for (Action action : extraActions) {
                                        if (action != null)
                                            result.add(action);
                                    }
                                }
                            }
                        }

                        return result;
                    }
                });
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
