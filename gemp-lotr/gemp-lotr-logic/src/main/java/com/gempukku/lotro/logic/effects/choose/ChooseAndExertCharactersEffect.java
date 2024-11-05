package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class ChooseAndExertCharactersEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final int _times;
    private final Filterable[] _filters;
    private CostToEffectAction _resultSubAction;
    private boolean _forToil;

    public ChooseAndExertCharactersEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        this(action, playerId, minimum, maximum, 1, filters);
    }

    public ChooseAndExertCharactersEffect(Action action, String playerId, int minimum, int maximum, int times, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose characters to exert", minimum, maximum, filters);
        _action = action;
        _times = times;
        _filters = filters;
    }

    public void setForToil(boolean forToil) {
        _forToil = forToil;
    }

    @Override
    protected Filter getExtraFilterForPlaying(LotroGame game) {
        int times = _times;
        do {
            final int exertTimes = times;
            Filter filter = new Filter() {
                @Override
                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                    return game.getModifiersQuerying().canBeExerted(game, _action.getActionSource(), physicalCard)
                            && game.getModifiersQuerying().getVitality(game, physicalCard) > exertTimes;
                }
            };
            if (Filters.countActive(game, Filters.and(_filters, filter))>0)
                return filter;
            times--;
        } while (times > 0);
        return Filters.none;
    }

    @Override
    protected Filter getExtraFilterForPlayabilityCheck(LotroGame game) {
        return new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return game.getModifiersQuerying().canBeExerted(game, _action.getActionSource(), physicalCard)
                        && game.getModifiersQuerying().getVitality(game, physicalCard) > _times;
            }
        };
    }

    @Override
    protected final void cardsSelected(LotroGame game, Collection<PhysicalCard> characters) {
        _resultSubAction = new SubAction(_action);
        for (int i = 0; i < _times; i++) {
            final boolean first = (i==0);
            final ExertCharactersEffect effect = new ExertCharactersEffect(_action, _action.getActionSource(), characters.toArray(new PhysicalCard[characters.size()])) {
                @Override
                protected void forEachExertedByEffect(PhysicalCard physicalCard) {
                    if (first)
                        ChooseAndExertCharactersEffect.this.forEachCardExertedCallback(physicalCard);
                }
            };
            if (_forToil)
                effect.setForToil(true);
            _resultSubAction.appendEffect(effect);
        }
        game.getActionsEnvironment().addActionToStack(_resultSubAction);
        cardsToBeExertedCallback(characters);
    }

    protected void cardsToBeExertedCallback(Collection<PhysicalCard> characters) {

    }

    protected void forEachCardExertedCallback(PhysicalCard character) {

    }

    @Override
    public boolean wasCarriedOut() {
        return super.wasCarriedOut() && _resultSubAction != null && _resultSubAction.wasCarriedOut();
    }
}
