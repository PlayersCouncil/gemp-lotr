package com.gempukku.lotro.logic.timing.processes.pregame;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ModifierFlag;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.results.FinishedPlayingFellowshipResult;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PlayerPlaysStartingFellowshipGameProcess implements GameProcess {
    /**
     * The card types a starting fellowship may contain beyond companions when
     * {@link ModifierFlag#EXTRA_CARD_TYPES_IN_STARTING_FELLOWSHIP} is active for the player.
     */
    private static final Set<CardType> EXTRA_STARTING_TYPES = EnumSet.of(
            CardType.POSSESSION, CardType.ARTIFACT, CardType.CONDITION, CardType.ALLY, CardType.FOLLOWER);

    private final String _playerId;

    private final GameProcess _followingGameProcess;
    private GameProcess _nextProcess;

    /** Extra card types of which a zero-cost card has already gone down this way. */
    private final Set<CardType> _zeroCostTypesPlayed;

    public PlayerPlaysStartingFellowshipGameProcess(String playerId, GameProcess followingGameProcess) {
        this(playerId, followingGameProcess, EnumSet.noneOf(CardType.class));
    }

    private PlayerPlaysStartingFellowshipGameProcess(String playerId, GameProcess followingGameProcess,
                                                     Set<CardType> zeroCostTypesPlayed) {
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
        _zeroCostTypesPlayed = zeroCostTypesPlayed;
    }

    @Override
    public void process(LotroGame game) {
        Collection<PhysicalCard> possibleCharacters = getPossibleCharacters(game, _playerId);
        if (possibleCharacters.isEmpty()) {
            game.getActionsEnvironment().emitEffectResult(new FinishedPlayingFellowshipResult(_playerId));
            _nextProcess = _followingGameProcess;
        } else
            game.getUserFeedback().sendAwaitingDecision(_playerId, createChooseNextCharacterDecision(game, _playerId, possibleCharacters));
    }

    private boolean allowsExtraCardTypes(LotroGame game, String playerId) {
        return game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.EXTRA_CARD_TYPES_IN_STARTING_FELLOWSHIP, playerId);
    }

    private Collection<PhysicalCard> getPossibleCharacters(final LotroGame game, final String playerId) {
        final boolean extraTypes = allowsExtraCardTypes(game, playerId);
        Filterable typeFilter = CardType.COMPANION;
        if (extraTypes)
            typeFilter = Filters.or(CardType.COMPANION, CardType.POSSESSION, CardType.ARTIFACT,
                    CardType.CONDITION, CardType.ALLY, CardType.FOLLOWER);

        return Filters.filter(game, game.getGameState().getDeck(playerId),
                typeFilter,
                new Filter() {
                    @Override
                    public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                        int twilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, physicalCard, null, 0, false);
                        if (extraTypes && twilightCost == 0
                                && _zeroCostTypesPlayed.contains(physicalCard.getBlueprint().getCardType()))
                            return false;
                        int startingFellowshipLimit = 4 + game.getModifiersQuerying().getStartingFellowshipCostModifier(game, playerId);
                        return game.getGameState().getTwilightPool() + twilightCost <= startingFellowshipLimit
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
                    Set<CardType> zeroCostTypesPlayed = EnumSet.copyOf(_zeroCostTypesPlayed);
                    final CardType selectedType = selectedPhysicalCard.getBlueprint().getCardType();
                    if (EXTRA_STARTING_TYPES.contains(selectedType)
                            && game.getModifiersQuerying().getTwilightCostToPlay(game, selectedPhysicalCard, null, 0, false) == 0)
                        zeroCostTypesPlayed.add(selectedType);
                    Action playCardAction = PlayUtils.getPlayCardAction(game, selectedPhysicalCard, 0, Filters.any, false);
                    game.getActionsEnvironment().addActionToStack(playCardAction);
                    _nextProcess = new PlayerPlaysStartingFellowshipGameProcess(_playerId, _followingGameProcess, zeroCostTypesPlayed);
                }
            }
        };
    }


    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
