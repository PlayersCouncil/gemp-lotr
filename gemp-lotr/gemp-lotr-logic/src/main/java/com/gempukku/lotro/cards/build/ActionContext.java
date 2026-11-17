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

    /**
     * Whether this context was made to ask an effect, at the moment it is about to be carried out, what it will
     * do (EffectForecast.isCertainToDoNothing).  Everything before the effect in its action has then happened:
     * the costs are paid and what the effect reads from memory has been chosen.  A forecast made in such a
     * context may therefore read memory and look through any zone, which a forecast made while the action is only
     * on offer may not.  False in every other context.
     */
    default boolean isAtResolution() {
        return false;
    }

    /**
     * Whether what this context carries out is a branch of a choice between things to do ("wound 2 Orcs or
     * wound 1 Orc twice") that was open to the player because it can be carried out in full (CR 4.2 "effect":
     * "you must choose an action that you are fully capable of performing (if possible)").  What the player then
     * picks within the branch is an extension of that choice (Ketura on Q-0209), so an effect that asks whom it
     * is to be done to more than once ("wound X twice") offers only those it can be done to in full.  False where
     * no branch of the choice could be carried out in full and they were offered all the same (do what you can and
     * ignore the rest), and in every context that is not a branch of a choice (Choice sets it for its branches).
     */
    default boolean isBranchChosenAsDoableInFull() {
        return false;
    }
}
