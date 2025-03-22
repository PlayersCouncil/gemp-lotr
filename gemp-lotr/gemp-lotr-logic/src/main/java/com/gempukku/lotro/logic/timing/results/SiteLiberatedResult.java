package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class SiteLiberatedResult extends EffectResult {
    private final String _playerId;
    private final PhysicalCard _site;

    public SiteLiberatedResult(String playerId, PhysicalCard site) {
        super(Type.LIBERATE_SITE);
        _playerId = playerId;
        _site = site;
    }

    public String getPlayerId() {
        return _playerId;
    }
    public PhysicalCard getSite() {
        return _site;
    }
}
