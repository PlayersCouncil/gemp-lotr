package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public abstract class ChooseCardsFromDeckEffect extends AbstractEffect {
    private final String _playerId;
    private final String _deckId;
    private final boolean showAll;
    private final int _minimum;
    private final int _maximum;
    private final Filter _filter;

    public ChooseCardsFromDeckEffect(String playerId, String deckId, boolean showAll, int minimum, int maximum, Filterable... filters) {
        _playerId = playerId;
        _deckId = deckId;
        this.showAll = showAll;
        _minimum = minimum;
        _maximum = maximum;
        _filter = Filters.and(filters);
    }

    @Override
    public String getText(LotroGame game) {
        return "Choose card from deck";
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game, game.getGameState().getDeck(_deckId), _filter);
        return cards.size() >= _minimum;
    }

    /**
     * Decision parameter marking the "look through the deck" step of a search, so the client can tell it apart from
     * a reveal that is the point of an effect.  Value "own" when the player searches their own deck, "opponent" for
     * another player's.  The client may auto-dismiss the "own" case when the player asks for that.
     */
    public static final String DECK_INSPECTION_PARAM = "deckInspection";

    private interface Dismissed {
        void run() throws DecisionResultInvalidException;
    }

    /**
     * The decision that shows the whole deck being searched (rules-wise, searching a deck lets you see it).
     */
    private ArbitraryCardsSelectionDecision inspectDeckDecision(LotroGame game, int id, String text, Dismissed dismissed) {
        final boolean ownDeck = _playerId.equals(_deckId);
        return new ArbitraryCardsSelectionDecision(id, text, game.getGameState().getDeck(_deckId), new LinkedList<>(), 0, 0) {
            {
                setParam(DECK_INSPECTION_PARAM, ownDeck ? "own" : "opponent");
            }

            @Override
            public void decisionMade(String ignored) throws DecisionResultInvalidException {
                dismissed.run();
            }
        };
    }

    private String inspectionText(boolean before) {
        String whose = _playerId.equals(_deckId) ? "your deck" : _deckId + "'s deck";
        return "You may inspect the contents of " + whose + (before ? " before retrieving cards" : " while retrieving cards");
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final LotroGame game) {
        Collection<PhysicalCard> cards = Filters.filter(game, game.getGameState().getDeck(_deckId), _filter);

        boolean success = cards.size() >= _minimum;

        int minimum = Math.min(_minimum, cards.size());

        if (_maximum == 0) {
            if (showAll) {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        inspectDeckDecision(game, 2, inspectionText(false), () -> cardsSelected(game, Collections.emptySet())));
            }
            else {
                cardsSelected(game, Collections.emptySet());
            }
        } else if (cards.size() == minimum) {
            if (showAll) {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        inspectDeckDecision(game, 2, inspectionText(false), () -> cardsSelected(game, cards)));
            }
            else {
                cardsSelected(game, cards);
            }
        } else {

            if (showAll) {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        inspectDeckDecision(game, 1, inspectionText(true), () ->
                                game.getUserFeedback().sendAwaitingDecision(_playerId,
                                        new ArbitraryCardsSelectionDecision(2, "Choose cards from deck", new LinkedList<>(cards), minimum, _maximum) {
                                            @Override
                                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                                cardsSelected(game, getSelectedCardsByResponse(result));
                                            }
                                        })));
            }
            else {
                game.getUserFeedback().sendAwaitingDecision(_playerId,
                        new ArbitraryCardsSelectionDecision(1, "Choose cards from deck", new LinkedList<>(cards), minimum, _maximum) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                cardsSelected(game, getSelectedCardsByResponse(result));
                            }
                        });
            }
        }

        return new FullEffectResult(success);
    }

    protected abstract void cardsSelected(LotroGame game, Collection<PhysicalCard> cards);
}
