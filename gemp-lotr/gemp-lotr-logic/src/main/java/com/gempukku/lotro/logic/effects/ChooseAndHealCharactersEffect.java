package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class ChooseAndHealCharactersEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final String _playerId;
    private final int _count;

    public ChooseAndHealCharactersEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        this(action, playerId, minimum, maximum, 1, filters);
    }

    public ChooseAndHealCharactersEffect(Action action, String playerId, int minimum, int maximum, int count, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose characters to heal", minimum, maximum, SpotOverride.NONE, filters);
        _action = action;
        _playerId = playerId;
        _count = count;
    }

    @Override
    protected Filter getExtraFilterForPlayabilityCheck(LotroGame game) {
        return Filters.changeToFilter(
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getGameState().getWounds(physicalCard) >= _count && game.getModifiersQuerying().canBeHealed(game, physicalCard);
                    }
                });
    }

    @Override
    protected Filter getExtraFilterForPlaying(LotroGame game) {
        return Filters.and(
                Filters.wounded,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        return game.getModifiersQuerying().canBeHealed(game, physicalCard);
                    }
                });
    }

    @Override
    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
        SubAction subAction = new SubAction(_action);
        for (int i = 0; i < _count; i++)
            subAction.appendEffect(new HealCharactersEffect(_action.getActionSource(), _playerId, Filters.in(cards)));
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}
