package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class SiteControlledResult extends EffectResult {
    private final String _playerId;
    private final PhysicalCard _site;

    public SiteControlledResult(String playerId, PhysicalCard site) {
        super(Type.CONTROL_SITE);
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
