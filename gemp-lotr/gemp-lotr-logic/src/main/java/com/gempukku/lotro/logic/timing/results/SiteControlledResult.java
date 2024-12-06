package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.logic.timing.EffectResult;

public class SiteControlledResult extends EffectResult {
    private final String _playerId;

    public SiteControlledResult(String playerId) {
        super(Type.CONTROL_SITE);
        _playerId = playerId;
    }

    public String getPlayerId() {
        return _playerId;
    }
}
