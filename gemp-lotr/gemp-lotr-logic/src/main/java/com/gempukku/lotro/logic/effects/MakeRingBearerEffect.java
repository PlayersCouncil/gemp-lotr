package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.results.NewRingBearerResult;

import java.util.Collections;

public class MakeRingBearerEffect extends UnrespondableEffect {
    private final PhysicalCard _newRingBearer;

    public MakeRingBearerEffect(PhysicalCard newRingBearer) {
        _newRingBearer = newRingBearer;
    }

    @Override
    public void doPlayEffect(LotroGame game) {
        if (game.getGameState().isWearingRing()) {
            game.getGameState().setWearingRing(false);
        }

        var oldRB = game.getGameState().getRingBearer(game.getGameState().getCurrentPlayerId());
        var theOneRing = game.getGameState().getRing(game.getGameState().getCurrentPlayerId());

        game.getGameState().sendMessage(_newRingBearer.getOwner() + " makes " + GameUtils.getCardLink(_newRingBearer) + " a new Ring-bearer");
        game.getGameState().removeCardsFromZone(_newRingBearer.getOwner(), Collections.singleton(theOneRing));
        game.getGameState().attachCard(game, theOneRing, _newRingBearer);
        game.getGameState().setRingBearer(_newRingBearer);

        game.getActionsEnvironment().emitEffectResult(new NewRingBearerResult(oldRB, _newRingBearer));
    }
}
