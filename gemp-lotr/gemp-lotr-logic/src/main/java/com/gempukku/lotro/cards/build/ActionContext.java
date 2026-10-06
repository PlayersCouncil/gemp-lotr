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

    /**
     * The twilight that the action this context belongs to has still to take from the pool as its own cost,
     * before any of its effects is carried out: the twilight cost of a Shadow event that is being asked whether
     * it can be played.  Zero in every other context, the one in which that event's effects are then carried
     * out included, since by then the event has been paid for.  An effect that will itself take twilight
     * (playing a card from the discard pile) counts this as gone when it is asked beforehand whether it can be
     * carried out in full.
     */
    default int getUnpaidTwilightCost() {
        return 0;
    }
}
