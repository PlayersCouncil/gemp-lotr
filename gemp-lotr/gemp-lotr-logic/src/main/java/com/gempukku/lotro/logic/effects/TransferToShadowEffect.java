package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

public class TransferToShadowEffect extends AbstractEffect {
    private final PhysicalCard _card;

    public TransferToShadowEffect(PhysicalCard card) {
        _card = card;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _card.getZone().isInPlay();
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            game.getGameState().transferCardAsMinion(_card);
            cardTransferredCallback();
            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    protected void cardTransferredCallback() {
    }
}
