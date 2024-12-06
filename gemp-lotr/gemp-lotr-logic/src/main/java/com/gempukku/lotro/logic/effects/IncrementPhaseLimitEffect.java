package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

public class IncrementPhaseLimitEffect extends UnrespondableEffect {
    private final PhysicalCard card;
    private final int limit;
    private Phase phase;
    private final String prefix;

    public IncrementPhaseLimitEffect(PhysicalCard card, Phase phase, int limit) {
        this(card, phase, "", limit);
    }

    public IncrementPhaseLimitEffect(PhysicalCard card, Phase phase, String prefix, int limit) {
        this.card = card;
        this.phase = phase;
        this.prefix = prefix;
        this.limit = limit;
    }

    @Override
    protected void doPlayEffect(LotroGame game) {
        if (phase == null)
            phase = game.getGameState().getCurrentPhase();
        game.getModifiersQuerying().getUntilEndOfPhaseLimitCounter(card, prefix, phase).incrementToLimit(limit, 1);
    }
}
