package com.gempukku.lotro.logic.timing.processes.turn.assign;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.SystemQueueAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.decisions.PlayerAssignMinionsDecision;
import com.gempukku.lotro.logic.effects.AssignmentPhaseEffect;
import com.gempukku.lotro.logic.effects.StackActionEffect;
import com.gempukku.lotro.logic.effects.TriggeringResultEffect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.processes.GameProcess;
import com.gempukku.lotro.logic.timing.results.FreePlayerStartsAssigningResult;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
                                                if (!RuleUtils.hasRoomForAssignments(game, freeCard, minionsAssigned.size()))
                                                    throw new DecisionResultInvalidException(GameUtils.getFullName(freeCard) + " can't have so many minions assigned");
                                                unassignedMinions.removeAll(minionsAssigned);
                                            }

                                            if (!game.getModifiersQuerying().isValidAssignments(game, Side.FREE_PEOPLE, assignments))
                                                throw new DecisionResultInvalidException("Assignments are not valid for the effects affecting the cards");

                                            _leftoverMinions = unassignedMinions;

                                            payForAndMakeAssignments(game, action, assignments);
                                        }
                                    });
                        } else {
                            _leftoverMinions = new HashSet<>(Filters.filterActive(game, CardType.MINION));
                        }
                    }
                });
        game.getActionsEnvironment().addActionToStack(action);
    }

    /**
     * What follows the Free Peoples player's answer.  A cost of assigning ("the Free Peoples player must exert a
     * companion to assign this minion to a skirmish", ExtraCostToAssign) is paid for each assignment that has
     * one, one after another in the order of the minions' card ids, before any assignment is made.  Then every
     * assignment that was paid for or cost nothing is made at once (CR 4.2 "assignment phase").  An assignment
     * whose cost was not paid in full (a burden prevented, nobody left to exert) is not made, and its minion,
     * never having been assigned, is a leftover minion for the Shadow player; the Free Peoples player is not
     * asked to assign again (Q-0221).
     */
    private void payForAndMakeAssignments(final LotroGame game, final SystemQueueAction action, final Map<PhysicalCard, Set<PhysicalCard>> assignments) {
        final String assigningPlayer = game.getGameState().getCurrentPlayerId();

        final Map<PhysicalCard, PhysicalCard> characterOfMinion = new TreeMap<>(Comparator.comparingInt(PhysicalCard::getCardId));
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> assignment : assignments.entrySet()) {
            for (PhysicalCard minion : assignment.getValue())
                characterOfMinion.put(minion, assignment.getKey());
        }

        // Each cost is asked for when its turn comes, so that it is the cost as the game then stands
        final Map<PhysicalCard, CostToEffectAction> payments = new HashMap<>();
        for (final Map.Entry<PhysicalCard, PhysicalCard> pair : characterOfMinion.entrySet()) {
            action.appendEffect(
                    new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            final CostToEffectAction payment = game.getModifiersQuerying()
                                    .getAssignmentCosts(game, assigningPlayer, pair.getValue(), pair.getKey());
                            if (payment != null) {
                                payments.put(pair.getKey(), payment);
                                action.insertEffect(new StackActionEffect(payment));
                            }
                        }
                    });
        }

        action.appendEffect(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(LotroGame game) {
                        final Map<PhysicalCard, Set<PhysicalCard>> madeAssignments = new HashMap<>();
                        for (Map.Entry<PhysicalCard, PhysicalCard> pair : characterOfMinion.entrySet()) {
                            final PhysicalCard minion = pair.getKey();
                            final PhysicalCard character = pair.getValue();
                            // A payment is answered like any other cost, so a card may have left play or been
                            // hindered since the answer: it takes no part
                            if (!canStillSkirmish(game, minion))
                                continue;

                            final CostToEffectAction payment = payments.get(minion);
                            if (payment != null && !payment.wasCarriedOut()) {
                                game.getGameState().sendMessage(assigningPlayer + " did not pay to assign " + GameUtils.getCardLink(minion));
                                _leftoverMinions.add(minion);
                            } else if (!canStillSkirmish(game, character)) {
                                _leftoverMinions.add(minion);
                            } else {
                                madeAssignments.computeIfAbsent(character, x -> new HashSet<>()).add(minion);
                            }
                        }

                        action.appendEffect(
                                new AssignmentPhaseEffect(assigningPlayer, madeAssignments, "Free People player assignments"));
                    }
                });
    }

    private static boolean canStillSkirmish(LotroGame game, PhysicalCard card) {
        return card.getZone() != null && card.getZone().isInPlay() && !game.getGameState().isHindered(card);
    }

    @Override
    public GameProcess getNextProcess() {
        return _game.getFormat().getAdventure().getAfterFellowshipAssignmentGameProcess(_leftoverMinions, _followingAssignments);
    }
}
