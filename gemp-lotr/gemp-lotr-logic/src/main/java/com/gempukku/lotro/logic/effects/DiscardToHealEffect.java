package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.results.DiscardToHealResult;

public class DiscardToHealEffect extends ActivateCardEffect {
    public DiscardToHealEffect(PhysicalCard source, String performingPlayer, Timeword actionTimeword) {
        super(source, performingPlayer, actionTimeword);
        _activateCardResult = new DiscardToHealResult(_source, performingPlayer, _actionTimeword);
    }

    @Override
    public String getText(LotroGame game) {
        return "Discarded " + GameUtils.getCardLink(_source) + " to heal.";
    }
}
