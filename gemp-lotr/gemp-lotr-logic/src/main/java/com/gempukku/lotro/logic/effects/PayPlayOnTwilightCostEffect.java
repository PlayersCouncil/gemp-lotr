package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

public class PayPlayOnTwilightCostEffect extends AbstractEffect {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _target;
    private final int _twilightModifier;

    public PayPlayOnTwilightCostEffect(PhysicalCard physicalCard, PhysicalCard target, int twilightModifier) {
        _physicalCard = physicalCard;
        _target = target;
        _twilightModifier = twilightModifier;
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
    public boolean isPlayableInFull(LotroGame game) {
        int twilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, _physicalCard, _target, _twilightModifier, false);

        String currentPlayerId = game.getGameState().getCurrentPlayerId();
        if (!currentPlayerId.equals(_physicalCard.getOwner())) {
            int twilightPool = game.getGameState().getTwilightPool();
            if (twilightPool < twilightCost)
                return false;
        }
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        int twilightCost = game.getModifiersQuerying().getTwilightCostToPlay(game, _physicalCard, _target, _twilightModifier, false);

        String currentPlayerId = game.getGameState().getCurrentPlayerId();
        if (currentPlayerId.equals(_physicalCard.getOwner())) {
            game.getGameState().addTwilight(twilightCost);
            if (twilightCost > 0)
                game.getGameState().sendMessage(_physicalCard.getOwner() + " adds " + twilightCost + " to twilight pool");
            return new FullEffectResult(true);
        } else {
            int twilightPool = game.getGameState().getTwilightPool();
            boolean success = twilightPool >= twilightCost;
            twilightCost = Math.min(twilightPool, twilightCost);
            game.getGameState().removeTwilight(twilightCost);
            if (twilightCost > 0)
                game.getGameState().sendMessage(_physicalCard.getOwner() + " removes " + twilightCost + " from twilight pool");
            return new FullEffectResult(success);
        }
    }
}
