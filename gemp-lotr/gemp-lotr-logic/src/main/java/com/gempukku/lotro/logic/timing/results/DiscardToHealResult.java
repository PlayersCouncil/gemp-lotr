package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;

public class DiscardToHealResult extends ActivateCardResult {
    public DiscardToHealResult(PhysicalCard source, String performingPlayer, Timeword actionTimeword) {
        super(source, performingPlayer, actionTimeword);
    }
}
