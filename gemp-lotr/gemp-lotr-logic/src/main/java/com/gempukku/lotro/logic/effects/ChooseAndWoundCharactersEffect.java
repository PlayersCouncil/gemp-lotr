package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.timing.Action;

import java.util.Collection;

public class ChooseAndWoundCharactersEffect extends ChooseActiveCardsEffect {
    private final Action _action;
    private final int _count;

    private String _sourceText;

    public ChooseAndWoundCharactersEffect(Action action, String playerId, int minimum, int maximum, Filterable... filters) {
        this(action, playerId, minimum, maximum, 1, filters);
    }

    public ChooseAndWoundCharactersEffect(Action action, String playerId, int minimum, int maximum, int count, Filterable... filters) {
        super(action.getActionSource(), playerId, "Choose characters to wound", minimum, maximum, filters);
        _action = action;
        _count = count;
    }

    public void setSourceText(String sourceText) {
        _sourceText = sourceText;
    }

    @Override
    protected Filter getExtraFilterForPlaying(LotroGame game) {
        return Filters.canTakeWounds(_action.getActionSource(), 1);
    }

    @Override
    protected Filter getExtraFilterForPlayabilityCheck(LotroGame game) {
        return Filters.canTakeWounds(_action.getActionSource(), _count);
    }

    @Override
    protected void cardsSelected(LotroGame game, Collection<PhysicalCard> cards) {
        SubAction subAction = new SubAction(_action);
        for (int i = 0; i < _count; i++) {
            PhysicalCard source = (_action.getActionSource() != null) ? _action.getActionSource() : null;
            WoundCharactersEffect woundEffect = new WoundCharactersEffect(source, cards.toArray(new PhysicalCard[cards.size()]));
            if (_sourceText != null)
                woundEffect.setSourceText(_sourceText);
            subAction.appendEffect(woundEffect);
        }
        game.getActionsEnvironment().addActionToStack(subAction);
    }
}
