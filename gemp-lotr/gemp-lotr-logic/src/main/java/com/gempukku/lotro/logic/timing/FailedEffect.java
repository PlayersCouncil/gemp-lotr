package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.game.state.LotroGame;

public class FailedEffect extends AbstractEffect {
    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        return new FullEffectResult(false);
    }
}
