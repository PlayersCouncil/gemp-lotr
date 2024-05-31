package com.gempukku.lotro.logic.effects.discount;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class DiscardCardFromHandDiscountEffect extends AbstractSubActionEffect implements DiscountEffect {
    private final Action _action;
    private final String _playerId;
    private int _minimalDiscount;
    private int _discardedCount;
    private final Filterable[] _discardedCardFilter;

    public DiscardCardFromHandDiscountEffect(Action action, String playerId, Filterable... discardedCardFilter) {
        _action = action;
        _playerId = playerId;
        _discardedCardFilter = discardedCardFilter;
    }

    @Override
    public int getDiscountPaidFor() {
        return _discardedCount;
    }

    @Override
    public void setMinimalRequiredDiscount(int minimalDiscount) {
        _minimalDiscount = minimalDiscount;
    }

    @Override
    public String getText(LotroGame game) {
        return "Discard cards to reduce twilight cost";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public int getMaximumPossibleDiscount(LotroGame game) {
        return Filters.filter(game, game.getGameState().getHand(_playerId), _discardedCardFilter).size();
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.filter(game, game.getGameState().getHand(_playerId), _discardedCardFilter).size() >= _minimalDiscount;
    }

    @Override
    public void playEffect(LotroGame game) {
        if (isPlayableInFull(game)) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(
                    new ChooseAndDiscardCardsFromHandEffect(_action, _playerId, false, _minimalDiscount, Integer.MAX_VALUE, _discardedCardFilter) {
                        @Override
                        protected void cardsBeingDiscardedCallback(Collection<PhysicalCard> cardsBeingDiscarded) {
                            _discardedCount = cardsBeingDiscarded.size();
                            discountPaidCallback(_discardedCount);
                        }
                    });
            processSubAction(game, subAction);
        }
    }

    protected void discountPaidCallback(int paid) {  }
}
