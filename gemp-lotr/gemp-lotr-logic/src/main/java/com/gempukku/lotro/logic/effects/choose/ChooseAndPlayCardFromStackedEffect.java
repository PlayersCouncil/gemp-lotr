package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayPermanentAction;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.*;

public class ChooseAndPlayCardFromStackedEffect implements Effect {
    private final String _playerId;
    private final Filterable _stackedOn;
    private final Filter _filter;
    private final int _twilightModifier;
    private CostToEffectAction _playCardAction;

    public ChooseAndPlayCardFromStackedEffect(String playerId, Filterable stackedOn, Filterable... filter) {
        this(playerId, stackedOn, 0, filter);
    }

    public ChooseAndPlayCardFromStackedEffect(String playerId, Filterable stackedOn, int twilightModifier, Filterable... filter) {
        _playerId = playerId;
        _stackedOn = stackedOn;
        _filter = Filters.and(filter);
        _twilightModifier = twilightModifier;
    }

    @Override
    public String getText(LotroGame game) {
        return "Play card from stacked";
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getPlayableFromStacked(game).size() > 0;
    }

    @Override
    public Type getType() {
        return null;
    }

    private Collection<PhysicalCard> getPlayableFromStacked(LotroGame game) {
        Set<PhysicalCard> possibleCards = new HashSet<>();
        for (PhysicalCard stackedOnCard : Filters.filterActive(game, _stackedOn))
            possibleCards.addAll(Filters.filter(game.getGameState().getStackedCards(stackedOnCard), game, _filter, Filters.playable(game, _twilightModifier)));

        return possibleCards;
    }

    @Override
    public void playEffect(final LotroGame game) {
        Collection<PhysicalCard> playableFromStacked = getPlayableFromStacked(game);
        if (playableFromStacked.size() > 0) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new ArbitraryCardsSelectionDecision(1, "Choose a card to play", new LinkedList<>(playableFromStacked), 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            List<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (selectedCards.size() > 0) {
                                final PhysicalCard selectedCard = selectedCards.get(0);
                                _playCardAction = PlayUtils.getPlayCardAction(game, selectedCard, _twilightModifier, Filters.any, false);
                                _playCardAction.appendEffect(
                                        new UnrespondableEffect() {
                                            @Override
                                            protected void doPlayEffect(LotroGame game) {
                                                afterCardPlayed(selectedCard);
                                            }
                                        });
                                game.getActionsEnvironment().addActionToStack(_playCardAction);
                            }
                        }
                    });
        }
    }

    protected void afterCardPlayed(PhysicalCard cardPlayed) {
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayPermanentAction)
            return ((PlayPermanentAction) _playCardAction).wasCarriedOut();
        return true;
    }
}