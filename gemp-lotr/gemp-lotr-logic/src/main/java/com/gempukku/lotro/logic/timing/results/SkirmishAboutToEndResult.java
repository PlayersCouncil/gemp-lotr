package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SkirmishAboutToEndResult extends EffectResult {
    private final Set<PhysicalCard> _minionsInvolved;
    private final PhysicalCard _freepsInvolved;

    public SkirmishAboutToEndResult(PhysicalCard freepsInvolved, Set<PhysicalCard> minionsInvolved) {
        super(EffectResult.Type.SKIRMISH_ABOUT_TO_END);
        _freepsInvolved = freepsInvolved;
        _minionsInvolved = Objects.requireNonNullElseGet(minionsInvolved, HashSet::new);
    }

    public Set<PhysicalCard> getMinionsInvolved() {
        return _minionsInvolved;
    }

    public PhysicalCard getFreepsInvolved() {
        return _freepsInvolved;
    }

    public Set<PhysicalCard> getAllInvolved() {
        var everyone = new HashSet<>(_minionsInvolved);
        everyone.add(_freepsInvolved);
        return everyone;
    }
}
