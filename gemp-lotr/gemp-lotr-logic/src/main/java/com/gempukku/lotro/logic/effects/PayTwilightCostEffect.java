package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;

public class PayTwilightCostEffect extends AbstractEffect {
    private final PhysicalCard _physicalCard;
    private final int _twilightModifier;
    private final boolean _ignoreRoamingPenalty;
    private final boolean _transfer;

    public PayTwilightCostEffect(PhysicalCard physicalCard) {
        this(physicalCard, 0);
    }

    /**
     * The twilight cost of a card paid again to transfer it.  Transferring is not playing: the card is not
     * coming into play, so what other cards in play do to its twilight cost does not reach the payment.
     */
    public static PayTwilightCostEffect forTransfer(PhysicalCard physicalCard) {
        return new PayTwilightCostEffect(physicalCard, 0, false, true);
    }

    public PayTwilightCostEffect(PhysicalCard physicalCard, int twilightModifier) {
        this(physicalCard, twilightModifier, false);
    }

    public PayTwilightCostEffect(PhysicalCard physicalCard, int twilightModifier, boolean ignoreRoamingPenalty) {
        this(physicalCard, twilightModifier, ignoreRoamingPenalty, false);
    }

    private PayTwilightCostEffect(PhysicalCard physicalCard, int twilightModifier, boolean ignoreRoamingPenalty, boolean transfer) {
        _physicalCard = physicalCard;
        _twilightModifier = twilightModifier;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;
        _transfer = transfer;
    }

    private int getTwilightCost(LotroGame game) {
        if (_transfer)
            return game.getModifiersQuerying().getTwilightCostToTransfer(game, _physicalCard);
        return game.getModifiersQuerying().getTwilightCostToPlay(game, _physicalCard, null, _twilightModifier, _ignoreRoamingPenalty);
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
        int twilightCost = getTwilightCost(game);

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
        int twilightCost = getTwilightCost(game);

        String currentPlayerId = game.getGameState().getCurrentPlayerId();
        if (currentPlayerId.equals(_physicalCard.getOwner())) {
            game.getGameState().addTwilight(twilightCost);
            if (twilightCost > 0)
                game.getGameState().sendMessage(_physicalCard.getOwner() + " adds " + twilightCost + " to twilight pool");
            return new FullEffectResult(true);
        } else {
            boolean success = game.getGameState().getTwilightPool() >= twilightCost;
            twilightCost = Math.min(twilightCost, game.getGameState().getTwilightPool());
            if (twilightCost > 0) {
                game.getGameState().removeTwilight(twilightCost);
                game.getGameState().sendMessage(_physicalCard.getOwner() + " removes " + twilightCost + " from twilight pool");
            }
            return new FullEffectResult(success);
        }
    }
}
