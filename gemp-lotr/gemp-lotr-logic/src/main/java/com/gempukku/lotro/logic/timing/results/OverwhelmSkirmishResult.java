package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.HashSet;
import java.util.Set;

public class OverwhelmSkirmishResult extends SkirmishResult {
    private final Set<PhysicalCard> _overwhelmed;

    /**
     * @param losers      every character on the losing side
     * @param overwhelmed those of the losers who are overwhelmed.  The rest (a minion protected from being
     *                    overwhelmed, beside minions who are not) have only lost the skirmish.
     */
    public OverwhelmSkirmishResult(Set<PhysicalCard> winners, Set<PhysicalCard> losers, Set<PhysicalCard> overwhelmed, Set<PhysicalCard> removedFromSkirmish) {
        super(EffectResult.Type.SKIRMISH_FINISHED_WITH_OVERWHELM, winners, losers, removedFromSkirmish);
        _overwhelmed = new HashSet<>(overwhelmed);
        _overwhelmed.retainAll(losers);
    }

    public Set<PhysicalCard> getOverwhelmedLosers() {
        return _overwhelmed;
    }

    public Set<PhysicalCard> getSparedLosers() {
        Set<PhysicalCard> spared = new HashSet<>(getInSkirmishLosers());
        spared.removeAll(_overwhelmed);
        return spared;
    }
}
