package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class ChooseAndDiscardCardsFromPlayEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private CostToEffectAction _resultSubAction;

    public ChooseAndDiscardCardsFromPlayEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose cards to discard", minimum, maximum, filters);
        _action = action;
        _playerId = playerId;
    }

    @Override
    protected Filter getExtraFilterForPlaying(LotroGame game) {
        if (_action.getActionSource() == null)
            return Filters.any;
        return Filters.canBeDiscarded(_playerId, _action.getActionSource());
    }

    @Override
    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
        _resultSubAction = new SubAction(_action);
        _resultSubAction.appendEffect(new DiscardCardsFromPlayEffect(_playerId, _action.getActionSource(), Filters.in(cards)));
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
        cardsToBeDiscardedCallback(cards);
    }

    protected void cardsToBeDiscardedCallback(Collection<PhysicalCard> cards) {

    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
