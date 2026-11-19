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
    private final String _doerId;
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
        this(playerId, playerId, fpChar, minion, ignoreExistingAssignments, ignoreDefender, skipAllyLocationCheck);
    }

    /**
     * @param playerId The player who assigns: whose side the assignment is judged for (what may be assigned, and
     *                 by whom), as he is asked it.
     * @param doerId The player the assignment is credited to: the one named as assigning in the game log and in
     *               the results that "each time the Free Peoples player assigns ..." reads (AssignedToSkirmishResult,
     *               AssignAgainstResult).  The same player, except where a card made him assign and left him no
     *               choice in it: then it is that card's player (Q-0251 with Q-0171; F-0513).
     */
    public AssignmentEffect(String playerId, String doerId, PhysicalCard fpChar, PhysicalCard minion, boolean ignoreExistingAssignments,
            boolean ignoreDefender, boolean skipAllyLocationCheck) {
        _playerId = playerId;
        _doerId = doerId;
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
        return Filters.assignableToSkirmishAgainst(side, _fpChar, _ignoreExistingAssignments, _ignoreDefender, _skipAllyLocationCheck)
                .accepts(game, _minion);
    }

    /**
     * Makes the assignment if it can still be made.  A cost of assigning the minion paid for this assignment
     * (GameState.isAssignmentPaidFor; Q-0283) is used by it, made or not.
     */
    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        final boolean made = assign(game);
        game.getGameState().useAssignmentPaidFor(_minion);
        return new FullEffectResult(made);
    }

    private boolean assign(LotroGame game) {
        if (isPlayableInFull(game)) {
            if (Filters.notAssignedToSkirmish.accepts(game, _fpChar))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(_fpChar, _doerId));
            if (Filters.notAssignedToSkirmish.accepts(game, _minion))
                game.getActionsEnvironment().emitEffectResult(new AssignedToSkirmishResult(_minion, _doerId));

            game.getGameState().sendMessage(_doerId + " assigns " + GameUtils.getCardLink(_minion) + " to skirmish " + GameUtils.getCardLink(_fpChar));
            game.getGameState().assignToSkirmishes(_fpChar, Collections.singleton(_minion));

            game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_doerId, _fpChar, _minion));
            game.getActionsEnvironment().emitEffectResult(new AssignAgainstResult(_doerId, _minion, _fpChar));

            return true;
        }
        return false;
    }
}
