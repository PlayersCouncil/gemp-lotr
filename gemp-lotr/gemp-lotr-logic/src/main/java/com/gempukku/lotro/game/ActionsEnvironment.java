package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ActionsEnvironment {
    List<Action> getRequiredBeforeTriggers(Effect effect);

    List<Action> getOptionalBeforeTriggers(String playerId, Effect effect);

    List<Action> getOptionalBeforeActions(String playerId, Effect effect);

    List<Action> getRequiredAfterTriggers(Collection<? extends EffectResult> effectResults);

    Map<OptionalTriggerAction, EffectResult> getOptionalAfterTriggers(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getOptionalAfterActions(String playerId, Collection<? extends EffectResult> effectResults);

    List<Action> getPhaseActions(String playerId);

    void addUntilStartOfPhaseActionProxy(ActionProxy actionProxy, Phase phase);

    void addUntilEndOfPhaseActionProxy(ActionProxy actionProxy, Phase phase);

    void addUntilEndOfTurnActionProxy(ActionProxy actionProxy);
    void addAlwaysOnActionProxy(ActionProxy actionProxy);

    void addActionToStack(Action action);

    void emitEffectResult(EffectResult effectResult);

    /**
     * Takes a result back out of the record of what has happened this phase and this turn (getPhaseEffectResults,
     * getTurnEffectResults).  For a play whose card had left play before the play was complete: such a play did
     * not happen, and is not to be counted as a card "played this phase".
     */
    void retractEffectResult(EffectResult effectResult);

    /**
     * A card that is being played has been placed in play and its play is not announced yet (the entering step of
     * a play: see PlayCardEffect).  Until noteCardEnteredPlay is called for it, a card that came from its owner's
     * hand still counts toward that hand for initiative, so that the loss of initiative is answered together with
     * the play and not a step before it (CR 4.2 "playing a card": "Respond to the playing of The Card (and to
     * losing initiative if necessary)").
     */
    void noteCardEnteringPlay(PhysicalCard card, Zone playedFrom);

    /**
     * The entering step of that card's play is over: its play is about to be announced, or it has left play and
     * will not be.
     */
    void noteCardEnteredPlay(PhysicalCard card);

    /**
     * How many of that player's cards, played from hand, are in play with their play not yet announced.
     */
    int countCardsEnteringPlayFromHand(String playerId);

    <T extends Action> T findTopmostActionOfType(Class<T> clazz);

    List<EffectResult> getTurnEffectResults();

    List<EffectResult> getPhaseEffectResults();
}