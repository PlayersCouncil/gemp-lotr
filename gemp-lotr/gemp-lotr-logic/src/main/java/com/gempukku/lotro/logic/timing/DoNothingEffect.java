package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.game.state.LotroGame;

public class DoNothingEffect extends AbstractEffect {
    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        return new FullEffectResult(true);
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }
}
