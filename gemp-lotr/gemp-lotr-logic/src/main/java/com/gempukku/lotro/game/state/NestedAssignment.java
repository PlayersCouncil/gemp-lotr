package com.gempukku.lotro.game.state;

import com.gempukku.lotro.game.PhysicalCard;

import java.util.*;

public class NestedAssignment extends Assignment {
    private final Map<PhysicalCard, Assignment> _totalAssignments;
    private final Map<PhysicalCard, Assignment> _pendingAssignments;

    public NestedAssignment(PhysicalCard fellowshipCharacter, Set<PhysicalCard> shadowCharacters) {
        super(fellowshipCharacter, shadowCharacters);
        _pendingAssignments = new HashMap<>();
        _totalAssignments = new HashMap<>();

        for(var minion : shadowCharacters) {
            _pendingAssignments.put(minion, new Assignment(fellowshipCharacter, Collections.singleton(minion)));
            _totalAssignments.put(minion, new Assignment(fellowshipCharacter, Collections.singleton(minion)));
        }
    }

    public Assignment resolveSubSkirmish(PhysicalCard minion) {
        var assignment = _pendingAssignments.get(minion);

        _pendingAssignments.remove(minion);

        return assignment;
    }

    public Collection<Assignment> getPendingSubskirmishes() {
        return _pendingAssignments.values();
    }
}
