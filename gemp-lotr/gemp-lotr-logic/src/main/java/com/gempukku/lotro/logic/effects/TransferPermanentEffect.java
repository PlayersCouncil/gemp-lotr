package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.CardTransferredResult;

public class TransferPermanentEffect extends AbstractEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _targetCard;

    public TransferPermanentEffect(PhysicalCard physicalCard, PhysicalCard targetCard) {
        _physicalCard = physicalCard;
        _targetCard = targetCard;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return _targetCard.getZone().isInPlay()
                && _physicalCard.getZone().isInPlay()
                && game.getModifiersQuerying().canBeTransferred(game, _physicalCard)
                && game.getModifiersQuerying().canHaveTransferredOn(game, _physicalCard, _targetCard);
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return null;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (isPlayableInFull(game)) {
            GameState gameState = game.getGameState();
            gameState.sendMessage(_physicalCard.getOwner() + " transfers " + GameUtils.getCardLink(_physicalCard) + " to " + GameUtils.getCardLink(_targetCard));
            PhysicalCard transferredFrom = _physicalCard.getAttachedTo();
            gameState.transferCard(_physicalCard, _targetCard);

            game.getActionsEnvironment().emitEffectResult(
                    new CardTransferredResult(_physicalCard, transferredFrom, _targetCard));

            afterTransferredCallback();

            return new FullEffectResult(true);
        }
        return new FullEffectResult(false);
    }

    protected void afterTransferredCallback() {

    }
}
