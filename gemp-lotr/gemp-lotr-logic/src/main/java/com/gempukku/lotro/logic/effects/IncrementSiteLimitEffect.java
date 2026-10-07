package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

/**
 * Counts one use of a card against its limit per site ("limit once per site").
 */
public class IncrementSiteLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;

    public IncrementSiteLimitEffect(PhysicalCard card, int limit) {
        this.card = card;
        this.limit = limit;
    }

    @Override
    protected void doPlayEffect(LotroGame game) {
        game.getModifiersQuerying().getSiteLimitCounter(card).incrementToLimit(limit, 1);
    }
}
