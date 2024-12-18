package com.gempukku.lotro.logic.timing.results;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;

public class DiscardToHealResult extends ActivateCardResult {
    public DiscardToHealResult(PhysicalCard source, Timeword actionTimeword) {
        super(source, actionTimeword);
    }
}
