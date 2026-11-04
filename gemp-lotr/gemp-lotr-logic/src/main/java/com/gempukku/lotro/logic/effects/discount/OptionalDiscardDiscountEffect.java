package com.gempukku.lotro.logic.effects.discount;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.effects.OptionalEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collection;

public class OptionalDiscardDiscountEffect extends AbstractSubActionEffect implements DiscountEffect {
    private final int _discount;
    private boolean _paid;
    private int _minimalDiscount;
    private final String _playerId;
    private final int _discardCount;
    private final Filterable[] _discardFilters;
    private final Action _action;

    public OptionalDiscardDiscountEffect(Action action, int discount, String playerId, int discardCount, Filterable... discardFilters) {
        _action = action;
        _discount = discount;
        _playerId = playerId;
        _discardCount = discardCount;
        _discardFilters = discardFilters;
    }

    @Override
    public int getDiscountPaidFor() {
        return _paid ? _discount : 0;
    }

    @Override
    public void setMinimalRequiredDiscount(int minimalDiscount) {
        _minimalDiscount = minimalDiscount;
    }

    @Override
    public String getText(LotroGame game) {
        return "Discard cards to get a twilight discount";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        if (PlayConditions.canDiscardFromPlay(_action.getActionSource(), game, _discardCount, _discardFilters))
            return _minimalDiscount <= _discount;
        return _minimalDiscount == 0;
    }

    @Override
    public int getMaximumPossibleDiscount(LotroGame game) {
        return PlayConditions.canDiscardFromPlay(_action.getActionSource(), game, _discardCount, _discardFilters) ? _discount : 0;
    }

    @Override
    public void playEffect(LotroGame game) {
        if (isPlayableInFull(game)) {
            SubAction subAction = new SubAction(_action);
            if (PlayConditions.canDiscardFromPlay(_action.getActionSource(), game, _discardCount, _discardFilters)) {
                Effect discard = new ChooseAndDiscardCardsFromPlayEffect(subAction, _playerId, _discardCount, _discardCount, _discardFilters) {
                    @Override
                    protected void cardsToBeDiscardedCallback(Collection<PhysicalCard> cards) {
                        if (cards.size() == _discardCount) {
                            _paid = true;
                            discountPaidCallback(_discardCount);
                        }
                    }
                };
                // Where the pool cannot pay without the discount, the discard is the only way to pay: it is made
                // with no question (F-0483), as a number that can only be one number is not asked for (F-0147).
                // Otherwise the player is asked whether to take it.
                if (_minimalDiscount > 0)
                    subAction.appendEffect(discard);
                else
                    subAction.appendEffect(new OptionalEffect(subAction, _playerId, discard));
            }
            processSubAction(game, subAction);
        }
    }

    protected void discountPaidCallback(int paid) {  }
}
