package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.AssignAgainstResult;
import com.gempukku.lotro.logic.timing.results.AssignedToSkirmishResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AssignmentPhaseEffect extends AbstractEffect {
    private final Map<PhysicalCard, Set<PhysicalCard>> _assignments;
    private final String _text;
    private final String _playerId;

    public AssignmentPhaseEffect(String playerId, Map<PhysicalCard, Set<PhysicalCard>> assignments, String text) {
        _playerId = playerId;
        // Sanitize the assignments
        _assignments = new HashMap<>();
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> physicalCardListEntry : assignments.entrySet()) {
            PhysicalCard fpChar = physicalCardListEntry.getKey();
            Set<PhysicalCard> minions = physicalCardListEntry.getValue();
            if (minions != null && minions.size() > 0)
                _assignments.put(fpChar, minions);
        }
        _text = text;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return _text;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (_assignments.size() > 0) {
            game.getGameState().sendMessage(_playerId + " assigns characters to skirmish");
        }
        else {
            game.getGameState().sendMessage(_playerId + " skips assigning any characters");
        }
        for (Map.Entry<PhysicalCard, Set<PhysicalCard>> physicalCardListEntry : _assignments.entrySet()) {
            PhysicalCard fpChar = physicalCardListEntry.getKey();
            Set<PhysicalCard> minions = physicalCardListEntry.getValue();

            if (Filters.notAssignedToSkirmish.accepts(game, fpChar))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(fpChar, _playerId));
            for (PhysicalCard notAssignedMinion : Filters.filter(game, minions, Filters.notAssignedToSkirmish))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(notAssignedMinion, _playerId));

            game.getGameState().assignToSkirmishes(fpChar, minions);


            game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_playerId, fpChar, minions));
            for (PhysicalCard minion : minions)
                game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_playerId, minion, fpChar));
        }
        return new FullEffectResult(true);
    }
}
