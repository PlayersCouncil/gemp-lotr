package com.gempukku.lotro.logic.timing.processes.turn.assign;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.*;

/**
 * Works out the largest number of minions that can legally be assigned in one go to a set of Free Peoples characters,
 * so the client can tell a player who is leaving minions unassigned that they could have assigned more.
 *
 * The legality rules are the ones the assignment game processes apply to the answer: a Free Peoples character takes
 * at most 1 + Defender minions, counting those already assigned to it by assignment actions (the Shadow player has
 * no such cap), and the whole assignment must pass ModifiersQuerying.isValidAssignments for the assigning side.
 *
 * It is a bounded backtracking search over the minions; if it exhausts its node budget the answer is unknown and
 * null is returned.
 */
public class MaximumAssignment {
    static final int DEFAULT_NODE_BUDGET = 20000;

    private final LotroGame _game;
    private final Side _side;
    private final List<PhysicalCard> _minions;
    private final List<PhysicalCard> _characters;
    private final int[] _capacity;
    private final Map<PhysicalCard, Set<PhysicalCard>> _current = new HashMap<>();
    private final int _target;

    private int _nodesLeft;
    private int _best;

    private MaximumAssignment(LotroGame game, Side side, Collection<PhysicalCard> characters, Collection<PhysicalCard> minions, int nodeBudget) {
        _game = game;
        _side = side;
        _minions = new ArrayList<>(minions);
        _characters = new ArrayList<>(characters);
        _nodesLeft = nodeBudget;

        _capacity = new int[_characters.size()];
        long total = 0;
        for (int i = 0; i < _capacity.length; i++) {
            _capacity[i] = getCapacity(game, side, _characters.get(i));
            total += _capacity[i];
        }
        // Nothing can beat this, so reaching it ends the search
        _target = (int) Math.min(_minions.size(), total);
    }

    /**
     * @return the most minions that can be assigned at once, or null if that could not be determined within the node
     * budget.
     */
    public static Integer compute(LotroGame game, Side side, Collection<PhysicalCard> characters, Collection<PhysicalCard> minions) {
        return compute(game, side, characters, minions, DEFAULT_NODE_BUDGET);
    }

    public static Integer compute(LotroGame game, Side side, Collection<PhysicalCard> characters, Collection<PhysicalCard> minions, int nodeBudget) {
        MaximumAssignment search = new MaximumAssignment(game, side, characters, minions, nodeBudget);
        search.search(0, 0);
        if (search._best == search._target || search._nodesLeft >= 0)
            return search._best;
        return null;
    }

    private static int getCapacity(LotroGame game, Side side, PhysicalCard character) {
        if (side != Side.FREE_PEOPLE)
            return Integer.MAX_VALUE;
        int capacity = 1 + game.getModifiersQuerying().getKeywordCount(game, character, Keyword.DEFENDER)
                - getMinionsAssignedBeforeCount(character, game.getGameState());
        return Math.max(0, capacity);
    }

    private static int getMinionsAssignedBeforeCount(PhysicalCard character, GameState gameState) {
        for (Assignment assignment : gameState.getAssignments()) {
            if (assignment.getFellowshipCharacter() == character)
                return assignment.getShadowCharacters().size();
        }
        return 0;
    }

    private void search(int minionIndex, int assignedCount) {
        if (assignedCount > _best)
            _best = assignedCount;
        if (_best >= _target || minionIndex >= _minions.size())
            return;
        // Even assigning every remaining minion would not beat the best found
        if (assignedCount + (_minions.size() - minionIndex) <= _best)
            return;

        PhysicalCard minion = _minions.get(minionIndex);
        for (int i = 0; i < _characters.size(); i++) {
            PhysicalCard character = _characters.get(i);
            Set<PhysicalCard> assigned = _current.get(character);
            int assignedToCharacter = assigned == null ? 0 : assigned.size();
            if (assignedToCharacter >= _capacity[i])
                continue;
            if (--_nodesLeft < 0)
                return;

            if (assigned == null) {
                assigned = new HashSet<>();
                _current.put(character, assigned);
            }
            assigned.add(minion);
            // Restrictions only ever forbid pairings, so a partial assignment that is not valid can't be extended
            if (_game.getModifiersQuerying().isValidAssignments(_game, _side, _current))
                search(minionIndex + 1, assignedCount + 1);
            assigned.remove(minion);
            if (assigned.isEmpty())
                _current.remove(character);

            if (_best >= _target || _nodesLeft < 0)
                return;
        }

        // Leave this minion unassigned
        search(minionIndex + 1, assignedCount);
    }
}
