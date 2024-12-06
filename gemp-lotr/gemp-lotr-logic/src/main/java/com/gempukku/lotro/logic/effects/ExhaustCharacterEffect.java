package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;

public class ExhaustCharacterEffect extends AbstractSubActionEffect {
    private final PhysicalCard _source;
    private final Action _action;
    private final Filterable[] _filters;

    public ExhaustCharacterEffect(PhysicalCard source, Action action, Filterable... filters) {
        _source = source;
        _action = action;
        _filters = filters;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Exhaust " + GameUtils.getAppendedNames(Filters.filterActive(game, _filters));
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.hasActive(game, _filters);
    }

    @Override
    public void playEffect(LotroGame game) {
        SubAction subAction = new SubAction(_action);
        subAction.appendEffect(new InfiniteExertionEffect(_source, subAction, _filters));
        processSubAction(game, subAction);
        final Collection<PhysicalCard> cards = Filters.filterActive(game, _filters);
        if (cards.size() > 0)
            game.getGameState().sendMessage(GameUtils.getCardLink(_source) + " exhausts " + GameUtils.getAppendedNames(cards));
    }

    private class InfiniteExertionEffect extends ExertCharactersEffect {
        private final CostToEffectAction _subAction;

        private InfiniteExertionEffect(PhysicalCard source, CostToEffectAction subAction, Filterable[] filters) {
            super(_action, source, filters);
            _subAction = subAction;
        }

        @Override
        protected void playoutEffectOn(LotroGame game, Collection<PhysicalCard> cards) {
            super.playoutEffectOn(game, cards);
            if (getAffectedCards(game).size() > 0)
                _subAction.appendEffect(new InfiniteExertionEffect(_source, _subAction, _filters));
        }
    }
}