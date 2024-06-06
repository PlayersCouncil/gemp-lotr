package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.logic.GameUtils;

public class OpponentPlayerSource implements PlayerSource {
    private PlayerSource playerSource;

    public OpponentPlayerSource(PlayerSource playerSource) {
        this.playerSource = playerSource;
    }

    @Override
    public String getPlayer(ActionContext actionContext) {
        return GameUtils.getFirstOpponent(actionContext.getGame(), playerSource.getPlayer(actionContext));
    }
}
