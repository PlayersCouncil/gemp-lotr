package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collection;

public interface ActionContext {
    void setValueToMemory(String memory, String value);

    String getValueFromMemory(String memory);

    String getValueFromMemory(String memory, String defaultValue);

    void setCardMemory(String memory, PhysicalCard card);

    void setCardMemory(String memory, Collection<? extends PhysicalCard> cards);

    Collection<? extends PhysicalCard> getCardsFromMemory(String memory);

    PhysicalCard getCardFromMemory(String memory);

    String getPerformingPlayer();

    LotroGame getGame();

    PhysicalCard getSource();

    EffectResult getEffectResult();

    Effect getEffect();
}
