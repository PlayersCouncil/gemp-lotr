package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;

import java.util.Collections;

public class PlayCardEffect extends AbstractEffect {
    private final String performingPlayerId;
    private final Zone _playedFrom;
    private final PhysicalCard _cardPlayed;
    private PhysicalCard _attachedToCard;
    private final Zone _zone;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    private final boolean _paidToil;

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed, Zone playedTo, PhysicalCard attachedOrStackedPlayedFrom, boolean paidToil) {
        this.performingPlayerId = performingPlayerId;
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _zone = playedTo;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _paidToil = paidToil;
    }

    public PlayCardEffect(String performingPlayerId, Zone playedFrom, PhysicalCard cardPlayed, PhysicalCard attachedToCard, PhysicalCard attachedOrStackedPlayedFrom, boolean paidToil) {
        this.performingPlayerId = performingPlayerId;
        _playedFrom = playedFrom;
        _cardPlayed = cardPlayed;
        _attachedToCard = attachedToCard;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _paidToil = paidToil;
        _zone = Zone.ATTACHED;
    }

    public PhysicalCard getPlayedCard() {
        return _cardPlayed;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedToCard;
    }

    @Override
    public Effect.Type getType() {
        return null;
    }

    @Override
    public String getText(LotroGame game) {
        return "Play " + GameUtils.getFullName(_cardPlayed);
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return true;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        game.getGameState().removeCardsFromZone(_cardPlayed.getOwner(), Collections.singleton(_cardPlayed));
        if (_attachedToCard != null) {
            game.getGameState().attachCard(game, _cardPlayed, _attachedToCard);
        } else {
            game.getGameState().addCardToZone(game, _cardPlayed, _zone);
        }

        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(performingPlayerId, _playedFrom, _cardPlayed, _attachedToCard, _attachedOrStackedPlayedFrom, _paidToil));

        return new FullEffectResult(true);
    }
}
