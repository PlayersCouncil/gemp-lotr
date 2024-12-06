package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Action;

public class PlayNextSiteEffect extends PlaySiteEffect {
    public PlayNextSiteEffect(Action action, String playerId, Filterable... extraSiteFilters) {
        super(action, playerId, null, 0, extraSiteFilters);
    }

    @Override
    protected int getSiteNumberToPlay(LotroGame game) {
        return game.getGameState().getCurrentSiteNumber() + 1;
    }
}
