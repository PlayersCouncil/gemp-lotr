package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class PlayCardResult extends EffectResult {
    private final Zone _playedFrom;
    private final PhysicalCard _playedCard;
    private final PhysicalCard _attachedTo;
    private final PhysicalCard _attachedOrStackedPlayedFrom;
    private final boolean _paidToil;

    public PlayCardResult(Zone playedFrom, PhysicalCard playedCard, PhysicalCard attachedTo, PhysicalCard attachedOrStackedPlayedFrom, boolean paidToil) {
        super(EffectResult.Type.PLAY);
        _playedFrom = playedFrom;
        _playedCard = playedCard;
        _attachedTo = attachedTo;
        _attachedOrStackedPlayedFrom = attachedOrStackedPlayedFrom;
        _paidToil = paidToil;
    }

    public boolean isPaidToil() {
        return _paidToil;
    }

    public PhysicalCard getPlayedCard() {
        return _playedCard;
    }

    public PhysicalCard getAttachedTo() {
        return _attachedTo;
    }

    public PhysicalCard getAttachedOrStackedPlayedFrom() {
        return _attachedOrStackedPlayedFrom;
    }

    public Zone getPlayedFrom() {
        return _playedFrom;
    }
}
