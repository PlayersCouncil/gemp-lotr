package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.results.FinishedPlayingFellowshipResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PlayerPlaysStartingFellowshipGameProcess implements GameProcess {
    private final String _playerId;

    private final GameProcess _followingGameProcess;
    private GameProcess _nextProcess;

    public PlayerPlaysStartingFellowshipGameProcess(String playerId, GameProcess followingGameProcess) {
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(LotroGame game) {
        Collection<PhysicalCard> possibleCharacters = getPossibleCharacters(game, _playerId);
        if (possibleCharacters.size() == 0) {
            game.getActionsEnvironment().emitEffectResult(new FinishedPlayingFellowshipResult(_playerId));
            _nextProcess = _followingGameProcess;
        } else
            game.getUserFeedback().sendAwaitingDecision(_playerId, createChooseNextCharacterDecision(game, _playerId, possibleCharacters));
    }

    private Collection<PhysicalCard> getPossibleCharacters(final LotroGame game, final String playerId) {
        return Filters.filter(game, game.getGameState().getDeck(playerId),
                CardType.COMPANION,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        int twilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, physicalCard, null, 0, false);
                        return game.getGameState().getTwilightPool() + twilightCost <= 4
                                && PlayUtils.checkPlayRequirements(game, physicalCard, Filters.any, 0, 0, false, false, true);
                    }
                });
    }

    private AwaitingDecision createChooseNextCharacterDecision(final LotroGame game, final String playerId, final Collection<PhysicalCard> possibleCharacters) {
        return new ArbitraryCardsSelectionDecision(1, "Starting fellowship - Choose next character or press DONE",
                new LinkedList<>(possibleCharacters), 0, 1) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                List<PhysicalCard> selectedCharacters = getSelectedCardsByResponse(result);
                if (selectedCharacters.size() == 0) {
                    game.getActionsEnvironment().emitEffectResult(new FinishedPlayingFellowshipResult(_playerId));
                    _nextProcess = _followingGameProcess;
                } else {
                    PhysicalCard selectedPhysicalCard = selectedCharacters.get(0);
                    Action playCardAction = PlayUtils.getPlayCardAction(game, selectedPhysicalCard, 0, Filters.any, false);
                    game.getActionsEnvironment().addActionToStack(playCardAction);
                    _nextProcess = new PlayerPlaysStartingFellowshipGameProcess(_playerId, _followingGameProcess);
                }
            }
        };
    }


    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
