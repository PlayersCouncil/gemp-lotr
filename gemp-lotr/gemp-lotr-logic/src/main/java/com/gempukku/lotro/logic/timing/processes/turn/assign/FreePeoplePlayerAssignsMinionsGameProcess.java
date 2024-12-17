package com.gempukku.lotro.logic.timing.processes.turn.assign;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.PlayerAssignMinionsDecision;
import com.gempukku.lotro.logic.effects.AssignmentPhaseEffect;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.results.FreePlayerStartsAssigningResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FreePeoplePlayerAssignsMinionsGameProcess implements GameProcess {
    private Set<PhysicalCard> _leftoverMinions;
    private final GameProcess _followingAssignments;
    private LotroGame _game;

    public FreePeoplePlayerAssignsMinionsGameProcess(GameProcess followingAssignments) {
        _followingAssignments = followingAssignments;
    }

    @Override
    public void process(LotroGame game) {
        _game = game;
        final SystemQueueAction action = new SystemQueueAction();
        action.appendEffect(
                new TriggeringResultEffect(new FreePlayerStartsAssigningResult(), "Free people player starts assigning"));
        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(final LotroGame game) {
                        final GameState gameState = game.getGameState();

                        final Collection<PhysicalCard> minions = Filters.filterActive(game, CardType.MINION, Filters.assignableToSkirmish(Side.FREE_PEOPLE, false, false, false));
                        if (!minions.isEmpty()) {
                            final Collection<PhysicalCard> freePeopleTargets =
                                    Filters.filterActive(game,
                                            Filters.and(
                                                    Filters.or(
                                                            CardType.COMPANION, CardType.ALLY),
                                                    Filters.assignableToSkirmish(Side.FREE_PEOPLE, false, false, false)));


                            game.getUserFeedback().sendAwaitingDecision(gameState.getCurrentPlayerId(),
                                    new PlayerAssignMinionsDecision(1, "Assign minions to companions or allies at home", freePeopleTargets, minions) {
                                        @Override
                                        public void decisionMade(String result) throws DecisionResultInvalidException {
                                            Map<PhysicalCard, Set<PhysicalCard>> assignments = getAssignmentsBasedOnResponse(result);

                                            //Start with all minions on the field
                                            Set<PhysicalCard> unassignedMinions = new HashSet<>(Filters.filterActive(game, CardType.MINION));

                                            //Then remove all minions who have already gotten themselves assigned (via assignment actions)
                                            var existingAssignments = gameState.getAssignments();
                                            unassignedMinions.removeAll(existingAssignments.stream().flatMap(x -> x.getShadowCharacters().stream()).collect(
                                                    Collectors.toSet()));

                                            //Finally, remove all minions just recently assigned by the Free Peoples player
                                            // (while incidentally double-checking that freeps did not violate assignment boundaries)
                                            for (PhysicalCard freeCard : assignments.keySet()) {
                                                Set<PhysicalCard> minionsAssigned = assignments.get(freeCard);
                                                if (minionsAssigned.size() + getMinionsAssignedBeforeCount(freeCard, gameState) > 1 + game.getModifiersQuerying().getKeywordCount(game, freeCard, Keyword.DEFENDER))
                                                    throw new DecisionResultInvalidException(GameUtils.getFullName(freeCard) + " can't have so many minions assigned");
                                                unassignedMinions.removeAll(minionsAssigned);
                                            }

                                            if (!game.getModifiersQuerying().isValidAssignments(game, Side.FREE_PEOPLE, assignments))
                                                throw new DecisionResultInvalidException("Assignments are not valid for the effects affecting the cards");

                                            _leftoverMinions = unassignedMinions;

                                            action.appendEffect(
                                                    new AssignmentPhaseEffect(gameState.getCurrentPlayerId(), assignments, "Free People player assignments"));
                                        }
                                    });
                        } else {
                            _leftoverMinions = new HashSet<>(Filters.filterActive(game, CardType.MINION));
                        }
                    }
                });
        game.getActionsEnvironment().addActionToStack(action);
    }

    private int getMinionsAssignedBeforeCount(PhysicalCard freeCard, GameState gameState) {
        for (Assignment assignment : gameState.getAssignments()) {
            if (assignment.getFellowshipCharacter() == freeCard)
                return assignment.getShadowCharacters().size();
        }
        return 0;
    }

    @Override
    public GameProcess getNextProcess() {
        return _game.getFormat().getAdventure().getAfterFellowshipAssignmentGameProcess(_leftoverMinions, _followingAssignments);
    }
}
