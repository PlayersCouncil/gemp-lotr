package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.Set;

/**
 * A character assigned to skirmish another: a Free Peoples character and a minion, from either side. The engine emits
 * one for each pairing, both ways round, so a defender assigned to two minions makes two results with him as the
 * assigned card, one against each minion (CR 4.2 "defender +X"; Q-0323).
 */
public class AssignAgainstResult extends EffectResult {
    private final String _playerId;
    private final PhysicalCard _assignedCard;
    private final Set<PhysicalCard> _against;

    public AssignAgainstResult(String playerId, PhysicalCard assignedCard, PhysicalCard against) {
        this(playerId, assignedCard, Collections.singleton(against));
    }

    public AssignAgainstResult(String playerId, PhysicalCard assignedCard, Set<PhysicalCard> against) {
        super(Type.ASSIGNED_AGAINST);
        _playerId = playerId;
        _assignedCard = assignedCard;
        _against = against;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public Set<PhysicalCard> getAgainst() {
        return _against;
    }

    public PhysicalCard getAssignedCard() {
        return _assignedCard;
    }
}

