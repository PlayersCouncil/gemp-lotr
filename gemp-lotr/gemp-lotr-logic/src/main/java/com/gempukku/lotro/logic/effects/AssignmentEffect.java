package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.AssignAgainstResult;
import com.gempukku.lotro.logic.timing.results.AssignedToSkirmishResult;

import java.util.Collections;

public class AssignmentEffect extends AbstractEffect {
    private final String _playerId;
    private boolean _ignoreExistingAssignments;
    private boolean _ignoreDefender;
    private final boolean _skipAllyLocationCheck;
    private final PhysicalCard _fpChar;
    private final PhysicalCard _minion;

    public AssignmentEffect(String playerId, PhysicalCard fpChar, PhysicalCard minion) {
        this(playerId, fpChar, minion, false, false, false);
    }

    public AssignmentEffect(String playerId, PhysicalCard fpChar, PhysicalCard minion, boolean ignoreExistingAssignments,
            boolean ignoreDefender, boolean skipAllyLocationCheck) {
        _playerId = playerId;
        _fpChar = fpChar;
        _minion = minion;
        _skipAllyLocationCheck = skipAllyLocationCheck;
        _ignoreExistingAssignments = ignoreExistingAssignments;
        _ignoreDefender = ignoreDefender;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Assign " + GameUtils.getCardLink(_minion) + " to skirmish " + GameUtils.getCardLink(_fpChar);
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        Side side = _playerId.equals(game.getGameState().getCurrentPlayerId()) ? Side.FREE_PEOPLE : Side.SHADOW;
        return Filters.assignableToSkirmishAgainst(side, _fpChar, _ignoreExistingAssignments, _ignoreDefender, _skipAllyLocationCheck).accepts(game, _minion);
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            if (Filters.notAssignedToSkirmish.accepts(game, _fpChar))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(_fpChar, _playerId));
            if (Filters.notAssignedToSkirmish.accepts(game, _minion))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(_minion, _playerId));

            game.getGameState().sendMessage(_playerId + " assigns " + GameUtils.getCardLink(_minion) + " to skirmish " + GameUtils.getCardLink(_fpChar));
            game.getGameState().assignToSkirmishes(_fpChar, Collections.singleton(_minion));

            game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_playerId, _fpChar, _minion));
            game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_playerId, _minion, _fpChar));

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }
}
