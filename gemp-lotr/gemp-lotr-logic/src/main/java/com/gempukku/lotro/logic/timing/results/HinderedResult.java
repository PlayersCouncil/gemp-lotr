package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.timing.EffectResult;

public class HinderedResult extends EffectResult {
    private final PhysicalCard _source;
    private final String _performingPlayer;
    private final PhysicalCard _card;

    public HinderedResult(PhysicalCard source, String performingPlayer, PhysicalCard card) {
        super(Type.FOR_EACH_HINDERED);
        _source = source;
        _performingPlayer = performingPlayer;
        _card = card;
    }

    public PhysicalCard getSource() {
        return _source;
    }
    public String getPerformingPlayer() {
        return _performingPlayer;
    }
    public PhysicalCard getHinderedCard() {
        return _card;
    }
}
