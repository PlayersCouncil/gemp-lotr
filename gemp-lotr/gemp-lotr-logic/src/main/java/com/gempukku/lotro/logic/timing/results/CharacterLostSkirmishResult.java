package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.HashSet;
import java.util.Set;

public class CharacterLostSkirmishResult extends EffectResult {
    private final PhysicalCard _loser;
    private final Set<PhysicalCard> _involving;
    private final SkirmishType _type;

    public CharacterLostSkirmishResult(SkirmishType type, PhysicalCard loser, Set<PhysicalCard> involving) {
        super(Type.CHARACTER_LOST_SKIRMISH);
        _type = type;
        _loser = loser;
        _involving = new HashSet<>(involving);
    }

    public Set<PhysicalCard> getInvolving() {
        return _involving;
    }

    public SkirmishType getSkirmishType() {
        return _type;
    }

    public PhysicalCard getLoser() {
        return _loser;
    }
}