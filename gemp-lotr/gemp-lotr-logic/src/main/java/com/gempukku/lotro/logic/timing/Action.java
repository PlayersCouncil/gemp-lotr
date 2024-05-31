package com.gempukku.lotro.logic.timing;

import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public interface Action {
    public enum Type {
        PLAY_CARD, SPECIAL_ABILITY, TRIGGER, TRANSFER, RECONCILE, RESOLVE_DAMAGE, OTHER
    }

    public Type getType();

    public PhysicalCard getActionSource();

    public void setActionTimeword(Timeword timeword);

    public PhysicalCard getActionAttachedToCard();

    public void setVirtualCardAction(boolean virtualCardAction);

    public boolean isVirtualCardAction();

    public void setPerformingPlayer(String playerId);

    public String getPerformingPlayer();

    public Timeword getActionTimeword();

    public String getText(LotroGame game);

    public Effect nextEffect(LotroGame game);
}
