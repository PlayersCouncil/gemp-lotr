package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.decisions.CardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.timing.AbstractSubActionEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ChooseAndDiscardCardsFromHandEffect extends AbstractSubActionEffect {
    private final Action _action;
    private final String _playerId;
    private final boolean _forced;
    private final Evaluator _minimum;
    private final Evaluator _maximum;
    private final Filterable[] _filter;
    private String _text = "Choose cards to discard";

    public ChooseAndDiscardCardsFromHandEffect(Action action, String playerId, boolean forced, Evaluator minimum, Evaluator maximum, Filterable... filters) {
        _action = action;
        _playerId = playerId;
        _forced = forced;
        _minimum = minimum;
        _maximum = maximum;
        _filter = filters;
    }

    public ChooseAndDiscardCardsFromHandEffect(Action action, String playerId, boolean forced, int minimum, int maximum, Filterable... filters) {
        this(action, playerId, forced, new ConstantEvaluator(minimum), new ConstantEvaluator(maximum), filters);
    }

    public void setText(String text) {
        _text = text;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return Filters.filter(game.getGameState().getHand(_playerId), game, _filter).size()
                >= _minimum.evaluateExpression(game, null);
    }

    @Override
    public void playEffect(final LotroGame game) {
        if (_forced && !game.getModifiersQuerying().canDiscardCardsFromHand(game, _playerId, _action.getActionSource()))
            return;

        Collection<PhysicalCard> hand = Filters.filter(game.getGameState().getHand(_playerId), game, _filter);
        int maximum = Math.min(_maximum.evaluateExpression(game, null), hand.size());

        int minimum = _minimum.evaluateExpression(game, null);
        if (maximum == 0) {
            cardsBeingDiscardedCallback(Collections.emptySet());
        } else if (hand.size() <= minimum) {
            SubAction subAction = new SubAction(_action);
            subAction.appendEffect(new DiscardCardsFromHandEffect(_action.getActionSource(), _playerId, hand, _forced));
            processSubAction(game, subAction);
            cardsBeingDiscardedCallback(hand);
        } else {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, _text, hand, minimum, maximum) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Set<PhysicalCard> cards = getSelectedCardsByResponse(result);
                            SubAction subAction = new SubAction(_action);
                            subAction.appendEffect(new DiscardCardsFromHandEffect(_action.getActionSource(), _playerId, cards, _forced));
                            processSubAction(game, subAction);
                            cardsBeingDiscardedCallback(cards);
                        }
                    });
        }
    }

    protected void cardsBeingDiscardedCallback(Collection<PhysicalCard> cardsBeingDiscarded) {
    }
}
