package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.logic.timing.EffectResult;

public class SiteLiberatedResult extends EffectResult {
    private final String _playerId;

    public SiteLiberatedResult(String playerId) {
        super(Type.LIBERATE_SITE);
        _playerId = playerId;
    }

    public String getPlayerId() {
        return _playerId;
    }
}
