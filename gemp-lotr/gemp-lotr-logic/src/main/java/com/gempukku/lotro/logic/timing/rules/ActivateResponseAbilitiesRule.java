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
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.LinkedList;
import java.util.List;

public class ActivateResponseAbilitiesRule {
    private final DefaultActionsEnvironment actionsEnvironment;

    public ActivateResponseAbilitiesRule(DefaultActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getOptionalBeforeActions(String playerId, LotroGame game, Effect effect) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activableCard : Filters.filter(game, game.getGameState().getInPlay(), getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activableCard)) {
                                final List<? extends ActivateCardAction> actions = activableCard.getBlueprint().getOptionalInPlayBeforeActions(playerId, game, effect, activableCard);
                                if (actions != null)
                                    result.addAll(actions);
                            }
                        }

                        return result;
                    }

                    @Override
                    public List<? extends Action> getOptionalAfterActions(String playerId, LotroGame game, EffectResult effectResult) {
                        List<Action> result = new LinkedList<>();
                        for (PhysicalCard activableCard : Filters.filter(game, game.getGameState().getInPlay(), getActivatableCardsFilter(playerId))) {
                            if (!game.getModifiersQuerying().hasTextRemoved(game, activableCard)) {
                                final List<? extends ActivateCardAction> actions = activableCard.getBlueprint().getOptionalInPlayAfterActions(playerId, game, effectResult, activableCard);
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