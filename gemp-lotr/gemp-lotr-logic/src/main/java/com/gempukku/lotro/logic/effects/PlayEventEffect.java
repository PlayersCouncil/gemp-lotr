package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.timing.results.PlayEventResult;

import java.util.Collections;

public class PlayEventEffect extends PlayCardEffect {
    private final PhysicalCard _cardPlayed;
    private final PlayEventResult _playEventResult;

    public PlayEventEffect(String performingPlayerId, PlayEventAction action, Zone playedFrom, PhysicalCard cardPlayed, boolean requiresRanger, boolean paidToil) {
        super(performingPlayerId, playedFrom, cardPlayed, (Zone) null, null, paidToil);
        _cardPlayed = cardPlayed;
        _playEventResult = new PlayEventResult(performingPlayerId, action, playedFrom, getPlayedCard(), requiresRanger, paidToil);
    }

    public PlayEventResult getPlayEventResult() {
        return _playEventResult;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        if (_cardPlayed.getZone() == Zone.VOID_FROM_HAND) {
            // At this point, card should change initiative if played
            game.getGameState().removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
            game.getGameState().addCardToZone(game, _cardPlayed, Zone.VOID);
        }

        game.getActionsEnvironment().emitEffectResult(_playEventResult);
        return new FullEffectResult(true);
    }
}
