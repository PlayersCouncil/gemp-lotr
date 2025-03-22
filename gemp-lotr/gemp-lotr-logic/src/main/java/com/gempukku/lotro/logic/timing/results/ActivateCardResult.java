package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class ActivateCardResult extends EffectResult {
    private final PhysicalCard _source;
    private final Timeword _actionTimeword;
    private final String _player;
    private boolean _effectCancelled;

    public ActivateCardResult(PhysicalCard source, String performingPlayer, Timeword actionTimeword) {
        super(Type.ACTIVATE);
        _source = source;
        _player = performingPlayer;
        _actionTimeword = actionTimeword;
    }

    public Timeword getActionTimeword() {
        return _actionTimeword;
    }

    public PhysicalCard getSource() {
        return _source;
    }
    public String getPerformingPlayer() { return _player; }

    public void cancelEffect() {
        _effectCancelled = true;
    }

    public boolean isEffectCancelled() {
        return _effectCancelled;
    }
}
