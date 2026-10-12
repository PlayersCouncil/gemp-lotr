package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CharacterWonSkirmishResult extends EffectResult {
    private final PhysicalCard _winner;
    private final Set<PhysicalCard> _involving;
    private final SkirmishType _type;
    private final Map<PhysicalCard, Integer> _loserStrengths;

    /**
     * @param loserStrengths The losing characters, each with the strength it had as the skirmish resolved: the
     *                       characters of the losing side, or Legacy Ruling 2's final removed character where that
     *                       side was emptied (with the strength it had as it left the skirmish).  F-0480.
     */
    public CharacterWonSkirmishResult(SkirmishType type, PhysicalCard winner, Set<PhysicalCard> involving,
                                      Map<PhysicalCard, Integer> loserStrengths) {
        super(EffectResult.Type.CHARACTER_WON_SKIRMISH);
        _type = type;
        _winner = winner;
        _involving = new HashSet<>(involving);
        _loserStrengths = Collections.unmodifiableMap(new LinkedHashMap<>(loserStrengths));
    }

    public Set<PhysicalCard> getInvolving() {
        return _involving;
    }

    public SkirmishType getSkirmishType() {
        return _type;
    }

    public PhysicalCard getWinner() {
        return _winner;
    }

    /**
     * @return The losing characters of the skirmish (Legacy Ruling 2's final removed character among them, where the
     * losing side was emptied), whether or not they are still in play.
     */
    public Set<PhysicalCard> getLosers() {
        return _loserStrengths.keySet();
    }

    /**
     * @return The total of the losing characters' strengths as the skirmish resolved, whatever has befallen them
     * since (a wound for losing, a kill, leaving play).
     */
    public int getLosersStrengthAsResolved() {
        int total = 0;
        for (int strength : _loserStrengths.values())
            total += strength;
        return total;
    }
}
