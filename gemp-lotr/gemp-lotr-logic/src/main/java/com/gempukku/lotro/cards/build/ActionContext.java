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

    /**
     * The threats that the action this context belongs to has still to add as its own cost, before any of its
     * effects is carried out: "add X threats to play a Wraith from your dead pile", while the action is being
     * asked whether it can be used.  Zero in every other context, the one in which the action's effects are then
     * carried out included, since by then the threats are on the table.  An effect that will play a card counts
     * these as added when it is asked beforehand whether a card can be played, so that a card which must itself
     * add a threat to be played is not counted as playable when the action's own threats leave no room for it.
     */
    default int getUnpaidThreatCost() {
        return 0;
    }
}
